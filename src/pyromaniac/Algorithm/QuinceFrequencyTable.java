/*
 * Acacia - GS-FLX & Titanium read error-correction and de-replication software.
 * Copyright (C) <2011>  <Lauren Bragg and Glenn Stone - CSIRO CMIS & University of Queensland>
 * 
 * 	This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package pyromaniac.Algorithm;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistributionImpl;

import pyromaniac.Algorithm.OUFrequencyTable;
import pyromaniac.DataStructures.FlowCycler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;

// TODO: Auto-generated Javadoc
/**
 * The Class QuinceFrequencyTable.
 */
public class QuinceFrequencyTable implements OUFrequencyTable
{	
	
	/** The probabilities. */
	HashMap <Integer, double []> probabilities;
	
	/**
	 * Instantiates a new quince frequency table.
	 *
	 * @param probFile the prob file
	 */
	public QuinceFrequencyTable(String probFile)
	{		
		probabilities = new HashMap<Integer, double []>(20);
		
		if(probFile == null)
		{
			for(int i = 1; i <= MAX_PRELOAD;i++)
			{
				calculateProbabilities(i, NO_FLOW);
			}
		}
		else
		{
			loadProbabilities(probFile);
		}
	}
	
	/* (non-Javadoc)
	 * @see pyromaniac.Algorithm.OUFrequencyTable#loadProbabilities(java.lang.String)
	 */
	public void loadProbabilities(String probFile)
	{
		try
		{
			URL url = getClass().getResource(probFile);
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			
			String header = in.readLine();
			
			if(header.split(",").length != 4)
			{
				throw new Exception("Call probabilities does not have four columns as expected");
			}
			
			String line = in.readLine();
			
			while(line != null)
			{				
				String [] fields = line.split(",");
				
				int mode = Integer.parseInt(fields[0]);
				double lessThan = Double.parseDouble(fields[1]);
				double equalTo = Double.parseDouble(fields[2]);
				double moreThan = Double.parseDouble(fields[3]);
				
				double [] probs = {lessThan, equalTo, moreThan};
				this.probabilities.put(mode, probs);
				line = in.readLine();
			}
		}
		catch(NumberFormatException nfe)
		{
			nfe.printStackTrace();
		}
		catch(IOException ie)
		{
			ie.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	
	/* (non-Javadoc)
	 * @see pyromaniac.Algorithm.OUFrequencyTable#getProbabilities(int, int)
	 */
	public double [] getProbabilities(HashMap <String, Object> factors, FlowCycler cycler)
	{
		int mode = (Integer)factors.get(OUFrequencyTable.RLE_LENGTH);
		int flowNumber = (Integer)factors.get(OUFrequencyTable.FLOW_POSITION);
		
		if(this.probabilities.containsKey(mode))
		{
			return this.probabilities.get(mode);
		}
		else
		{
			calculateProbabilities(mode, flowNumber);
			return this.probabilities.get(mode);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see pyromaniac.Algorithm.OUFrequencyTable#calculateProbabilities(int, int)
	 */
	public void calculateProbabilities(int mode, int flowNumber)
	{

		if(this.probabilities.containsKey(mode))
		{
			return;
		}
		
		double sd = 0.04  + mode * 0.03;
		
		NormalDistributionImpl norm = new NormalDistributionImpl(mode, sd);
	
		try
		{
				double probLessThan = norm.cumulativeProbability((double)mode - (double)1/2);
				double probMoreThan = 1 - norm.cumulativeProbability((double)mode + (double)1/2);
				double probEqualTo = 1 - probLessThan - probMoreThan;
				
				System.out.println("Mode is " + mode + " sd is " + sd + "( " + probLessThan + ", " + probEqualTo + " " + probMoreThan);
				
				
				double [] probs = {probLessThan, probEqualTo, probMoreThan};
				probabilities.put(mode, probs);
		}
		catch(MathException me)
		{
			me.getStackTrace();
		}
	}
	
}
