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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistributionImpl;

import pyromaniac.DataStructures.FlowCycler;

// TODO: Auto-generated Javadoc
/**
 * The Class MaldeOUCallFrequencyTable.
 */
public class MaldeOUCallFrequencyTable implements OUFrequencyTable
{
	
	/** The Constant SEGMENT_SIZE. */
	public static final int SEGMENT_SIZE = 40;
	
	/** The probabilities. */
	HashMap <Integer, HashMap <Integer, double []>> probabilities;
	
	/**
	 * Instantiates a new malde ou call frequency table.
	 *
	 * @param probFile the prob file
	 */
	public MaldeOUCallFrequencyTable(String probFile)
	{
		probabilities = new HashMap<Integer, HashMap <Integer, double []>>(20);
		
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
			
	//		System.out.println("Path; " + url.getPath());
			
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));	
			
			String line = in.readLine();
			
			String header = null;
			int segment = -1;
			HashMap <Integer, double []> probsForCurrSegment = null;
			
			while(line != null)
			{
				if(header == null || ! line.contains(","))
				{
					if(header != null)
					{
						this.probabilities.put(segment, probsForCurrSegment);
					}
					else
					{
						//System.out.println("header was null");
					}
					probsForCurrSegment = new HashMap <Integer, double [] >();
					header = line;
					segment = Integer.parseInt(header);
				}
				else
				{
					String [] probabilities = line.split(",");
					
					if(probabilities.length != 4)
						throw new Exception ("Probability file has incorrect format");
					
					
					double [] probs = new double [probabilities.length - 1];
					
					int mode = Integer.parseInt(probabilities[0]);
					
					for(int i = 1; i < probabilities.length; i++)
					{
						probs[i-1] = Double.parseDouble(probabilities[i]);	
					}
					probsForCurrSegment.put(mode, probs);
				}
				
				line = in.readLine();
			}

			//add the last cycle
			this.probabilities.put(segment, probsForCurrSegment);			
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
	public double[] getProbabilities(HashMap <String, Object> attributes, FlowCycler cycler) 
	{
		int flowNumber = (Integer) attributes.get(OUFrequencyTable.FLOW_POSITION);
		int obsMode = (Integer) attributes.get(OUFrequencyTable.RLE_LENGTH);
		
		if(flowNumber < 1)
			return null;
		
		int flowToSegment =  _getSegmentForFlow(flowNumber);

		
		
		if(! this.probabilities.containsKey(flowToSegment))
		{
			this.probabilities.put(flowToSegment, new HashMap <Integer, double []>());
		}
		
		if(! this.probabilities.get(flowToSegment).containsKey(obsMode))
		{
			double [] probs = _calculateProbability(obsMode, flowNumber);
			
			this.probabilities.get(flowToSegment).put(obsMode, probs);
		}
		
		return this.probabilities.get(flowToSegment).get(obsMode); 
	}
	
	
	//flow numbers outside this method are generally starting from zero
	/**
	 * _get segment for flow.
	 *
	 * @param flowNumber the flow number
	 * @return the int
	 */
	public int _getSegmentForFlow(int flowNumber)
	{
		if(flowNumber < 0)
			return NO_FLOW;
		
		return (int) Math.floor(flowNumber / SEGMENT_SIZE) + 1;
	}
	
	/**
	 * _calculate probability.
	 *
	 * @param mode the mode
	 * @param segmentNumber the segment number
	 * @return the double[]
	 */
	private double []  _calculateProbability(int mode, int segmentNumber)
	{	
		//what does malde suggest this is?	
		double sd = 0.03494  + mode * 0.06856;
		double flowEffect = 0.003 * segmentNumber;
		
		//double check that the flow effect is accurate TODO
		
		sd = sd + flowEffect;
		
		NormalDistributionImpl norm = new NormalDistributionImpl(mode, sd);
	
		try
		{
				double probLessThan = norm.cumulativeProbability((double)mode - (double)1/2);
				double probMoreThan = 1 - norm.cumulativeProbability((double)mode + (double)1/2);
				double probEqualTo = 1 - probLessThan - probMoreThan;
					
				double [] probs = {probLessThan, probEqualTo, probMoreThan};
				return probs;
		}
		catch(MathException me)
		{
			me.getStackTrace();
		}
		return null;
	}
	
	//initialises all the probabilities
	/* (non-Javadoc)
	 * @see pyromaniac.Algorithm.OUFrequencyTable#calculateProbabilities(int, int)
	 */
	public void calculateProbabilities(int mode, int segmentNumber)
	{
		if(this.probabilities.containsKey(segmentNumber))
		{
			return;
		}
		
		if(! probabilities.containsKey(segmentNumber))
		{
			probabilities.put(segmentNumber, new HashMap<Integer, double []>());
		}
		
		probabilities.get(segmentNumber).put(mode, _calculateProbability(mode, segmentNumber));
	}
}
