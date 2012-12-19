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

import pyromaniac.AcaciaConstants;
import pyromaniac.Algorithm.OUFrequencyTable;
import pyromaniac.DataStructures.FlowCycler;
import pyromaniac.IO.AcaciaLogger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.HashMap;

// TODO: Auto-generated Javadoc
/**
 * The Class QuinceFrequencyTable.
 */
public class QuinceOUFrequencyTable implements OUFrequencyTable
{	
	
	/** The probabilities. */
	HashMap <Integer, BigDecimal []> probabilities;
	public static int MAX_PRELOAD = 10;
	public static final int SCALE = 9;
	
	/**
	 * Instantiates a new quince frequency table.
	 *
	 * @param probFile the prob file
	 */
	public QuinceOUFrequencyTable(String probFile)
	{		
		probabilities = new HashMap<Integer, BigDecimal []>(20);
		
		if(probFile == null)
		{
			bulkComputeProbabilities();
		}
		else
		{
			loadProbabilities(probFile);
		}
	}
	
	public int getScale()
	{
		return this.SCALE;
	}
	
	private void bulkComputeProbabilities()
	{
		for(int i = 1; i <= MAX_PRELOAD;i++)
		{
			_calculateProbabilitiesHelper(i);
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
				BigDecimal lessThan = new BigDecimal(fields[1]).setScale(SCALE,  BigDecimal.ROUND_HALF_UP);
				BigDecimal equalTo = new BigDecimal(fields[2]).setScale(SCALE,  BigDecimal.ROUND_HALF_UP);
				BigDecimal moreThan = new BigDecimal(fields[3]).setScale(SCALE,  BigDecimal.ROUND_HALF_UP);
				
				BigDecimal sum = lessThan.add(equalTo).add(moreThan).setScale(SCALE, BigDecimal.ROUND_HALF_UP);
				
				if(! sum.equals(new BigDecimal("1").setScale(SCALE, BigDecimal.ROUND_HALF_UP)))
				{
					lessThan = lessThan.divide(sum, SCALE,BigDecimal.ROUND_HALF_UP);
					equalTo = equalTo.divide(sum, SCALE, BigDecimal.ROUND_HALF_UP);
					moreThan = moreThan.divide(sum, SCALE, BigDecimal.ROUND_HALF_UP);	
				}
				
				BigDecimal [] probs = {lessThan, equalTo, moreThan};
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
	 * If probability does not exist in empirical table, it is estimated, saved in the table, and returned
	 */
	public BigDecimal [] getProbabilities(AcaciaLogger logger, HashMap <String, Object> factors, FlowCycler cycler)
	{
		int mode = (Integer)factors.get(OUFrequencyTable.RLE_LENGTH);
		
		if(this.probabilities.containsKey(mode))
		{
			return this.probabilities.get(mode);
		}
		else
		{
			BigDecimal [] res = _calculateProbabilitiesHelper(mode);
			this.probabilities.put(mode, res);
			return res;
		}
	}
	
	/* (non-Javadoc)
	 * @see pyromaniac.Algorithm.OUFrequencyTable#calculateProbabilities(int, int)
	 */
	
	private BigDecimal []  _calculateProbabilitiesHelper(int mode)
	{
		BigDecimal sd = new BigDecimal("0.04").add(new BigDecimal(mode).multiply(new BigDecimal("0.03")));
		BigDecimal modeBD = new BigDecimal(mode);
	
		BigDecimal lowerBound = modeBD.subtract(new BigDecimal(SUBTRACT_FOR_LB)).setScale(SCALE, BigDecimal.ROUND_HALF_UP);
		BigDecimal upperBound = modeBD.add(new BigDecimal(ADD_FOR_UB)).setScale(SCALE, BigDecimal.ROUND_HALF_UP);
		
		NormalDistributionImpl norm = new NormalDistributionImpl(mode, sd.doubleValue());
	
		try
		{
				BigDecimal probLessThan = new BigDecimal(norm.cumulativeProbability(lowerBound.doubleValue())).setScale(SCALE,  BigDecimal.ROUND_HALF_UP);
				BigDecimal probMoreThan = new BigDecimal("1").subtract(new BigDecimal(norm.cumulativeProbability(upperBound.doubleValue()))).setScale(SCALE,  BigDecimal.ROUND_HALF_UP);
				BigDecimal probEqualTo = new BigDecimal("1").subtract(probLessThan).subtract(probMoreThan).setScale(SCALE,  BigDecimal.ROUND_HALF_UP);
				
				BigDecimal totalProb = probLessThan.add(probEqualTo).add(probMoreThan).setScale(SCALE, BigDecimal.ROUND_HALF_UP);
				
				if(!totalProb.equals(new BigDecimal("1").setScale(SCALE, BigDecimal.ROUND_HALF_UP)))
				{
					probLessThan = probLessThan.divide(totalProb, SCALE, BigDecimal.ROUND_HALF_UP);
					probMoreThan = probMoreThan.divide(totalProb, SCALE, BigDecimal.ROUND_HALF_UP);
					probEqualTo = probEqualTo.divide(totalProb,SCALE, BigDecimal.ROUND_HALF_UP);
				}
				
				BigDecimal [] probs = {probLessThan, probEqualTo, probMoreThan};
				return probs;
		}
		catch(MathException me)
		{
			me.getStackTrace();
		}
		return null;
	}
	

	@Override
	public BigDecimal [] estimateProbability(AcaciaLogger logger, HashMap<String, Object> factors, FlowCycler cycler) throws Exception
	{
		// TODO Auto-generated method stub
		int mode = (Integer)factors.get(OUFrequencyTable.RLE_LENGTH);
		return _calculateProbabilitiesHelper(mode);
	}

	@Override
	public BigDecimal [] getEmpiricalProbability(AcaciaLogger logger, HashMap<String, Object> factors, FlowCycler cycler) throws Exception 
	{
		int mode = (Integer)factors.get(OUFrequencyTable.RLE_LENGTH);
		if (this.probabilities.containsKey(mode))
		{
			return this.probabilities.get(mode);
		}
		return null;
	}

	
	
		//	TESTING
		public static void main(String [] args)
		{
			AcaciaLogger logger = new AcaciaLogger();
			FlowCycler cycler = new FlowCycler(AcaciaConstants.OPT_FLOW_CYCLE_454, logger);

			try
			{
				//TODO: to ensure it found the file, had to change alot of code, which will need to change back.


				QuinceOUFrequencyTable it = new QuinceOUFrequencyTable(AcaciaConstants.PYRONOISE_PROBS_LOCATION);


				//probs zero works
				BigDecimal [] res = it._calculateProbabilitiesHelper(0);

				for(int i = 0; i < res.length; i++)
					System.out.println(res[i]);

				BigDecimal [] res2 = it._calculateProbabilitiesHelper(2);

				for(int i = 0; i < res2.length; i++)
					System.out.println(res2[i]);
				
				//okay all the get probabilities are tested. Interestingly should cache them so not calculating again.
				HashMap <String, Object> factors = new HashMap <String, Object>();
				factors.put(OUFrequencyTable.FLOW_POSITION, 33);
				factors.put(OUFrequencyTable.RLE_LENGTH, 2);

				System.out.println("Res 4 ");
				//what I was actually looking up is PIC 1 in cycle 1 for reflen 2.
				BigDecimal [] res4 = it.getProbabilities(logger, factors, cycler);

				for(int i = 0; i < res4.length; i++)
					System.out.println(res4[i]);


				//pretend there is a cycle 12..
				HashMap <String, Object> factors2 = new HashMap <String, Object>();
				factors2.put(OUFrequencyTable.FLOW_POSITION, 50);
				factors2.put(OUFrequencyTable.RLE_LENGTH, 5);

				BigDecimal [] res5 = it.getProbabilities(logger, factors2, cycler);

				for(int i = 0; i < res5.length; i++)
					System.out.println(res5[i]);


			}
			catch(Exception e)
			{
				System.out.println("An exception ocurred: " + e.getMessage());
				e.printStackTrace();

			}
		}

}
