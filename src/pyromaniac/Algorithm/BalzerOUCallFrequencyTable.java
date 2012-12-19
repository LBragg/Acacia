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
import java.math.BigDecimal;
import java.net.URL;
import java.util.HashMap;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistributionImpl;

import pyromaniac.AcaciaConstants;
import pyromaniac.DataStructures.FlowCycler;
import pyromaniac.DataStructures.Pair;
import pyromaniac.IO.AcaciaLogger;


/**
 * The Class MaldeOUCallFrequencyTable.
 */
public class BalzerOUCallFrequencyTable implements OUFrequencyTable
{
	
	/** The Constant SEGMENT_SIZE. */
	public static final int SEGMENT_SIZE = 40;
	private static final int SCALE = 9;
	
	/** The probabilities. */
	HashMap <Integer, HashMap <Integer, BigDecimal []>> probabilities;
	
	//hash of SEGMENT: REFLEN
	HashMap <Integer, Pair <Double, Double>> normalDistParams;
	
	
	/**
	 * Instantiates a new malde ou call frequency table.
	 *
	 * @param probFile the prob file
	 */
	public BalzerOUCallFrequencyTable(String probFile)
	{
		probabilities = new HashMap<Integer, HashMap <Integer, BigDecimal []>>(20);

		this.normalDistParams = new HashMap <Integer, Pair <Double, Double>>();
		normalDistParams.put(0, new Pair <Double, Double>(0.1230, 0.737));
		normalDistParams.put(1, new Pair <Double, Double>(1.0193, 0.1227));
		normalDistParams.put(2, new Pair <Double, Double> (2.0006, 0.1585));
		normalDistParams.put(3, new Pair <Double, Double> (2.9934, 0.2188));
		normalDistParams.put(4, new Pair <Double, Double> (3.9962, 0.3168));
		normalDistParams.put(5, new Pair <Double, Double> (4.9550, 0.3863));
		
		if(probFile != null)
		{
			loadProbabilities(probFile);
		}
		else
		{
			bulkCalculateProbabilities();
		}
	}
	
	public int getScale()
	{
		return this.SCALE;
	}
	
	
	//do I want to, and how.
	private void bulkCalculateProbabilities()
	{
		//note that the hash GOES SEGMENT: REFLEN
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
			HashMap <Integer, BigDecimal []> probsForCurrSegment = null;
			
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
					probsForCurrSegment = new HashMap <Integer, BigDecimal [] >();
					header = line;
					segment = Integer.parseInt(header);
				}
				else
				{
					String [] probabilities = line.split(",");
					
					if(probabilities.length != 4)
						throw new Exception ("Probability file has incorrect format");
					
					
					BigDecimal [] probs = new BigDecimal [probabilities.length - 1];
					
					int mode = Integer.parseInt(probabilities[0]);
	
					BigDecimal sumProbs = new BigDecimal("0").setScale(SCALE, BigDecimal.ROUND_HALF_UP);
					
					for(int i = 1; i < probabilities.length; i++)
					{
						probs[i-1] = new BigDecimal(probabilities[i]).setScale(SCALE, BigDecimal.ROUND_HALF_UP);
						
						sumProbs = sumProbs.add(probs[i-1]);
					}
					
					if(! sumProbs.equals(new BigDecimal("1").setScale(SCALE, BigDecimal.ROUND_HALF_UP)))
					{
						probs[OUFrequencyTable.GREATER_THAN] = probs[OUFrequencyTable.GREATER_THAN].divide(sumProbs, SCALE, BigDecimal.ROUND_HALF_UP) ;
						probs[OUFrequencyTable.EQUAL_TO] = probs[OUFrequencyTable.EQUAL_TO].divide(sumProbs, SCALE, BigDecimal.ROUND_HALF_UP) ;						
						probs[OUFrequencyTable.LESS_THAN] = probs[OUFrequencyTable.LESS_THAN].divide(sumProbs, SCALE, BigDecimal.ROUND_HALF_UP) ;
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
	public BigDecimal [] getProbabilities(AcaciaLogger logger, HashMap <String, Object> factors, FlowCycler cycler) throws Exception
	{
		int flowNumber = (Integer) factors.get(OUFrequencyTable.FLOW_POSITION);
		int obsMode = (Integer) factors.get(OUFrequencyTable.RLE_LENGTH);
		
		boolean verbose = false;
		
		if(flowNumber < 1)
			return null;
		
		int flowToSegment =  _getSegmentForFlow(flowNumber);

		if(verbose)
			logger.writeLog("Flow to segment returns: " + flowToSegment, AcaciaLogger.LOG_DEBUG);
		
		if(! this.probabilities.containsKey(flowToSegment))
		{
			this.probabilities.put(flowToSegment, new HashMap <Integer, BigDecimal []>());
		}
		
		if(! this.probabilities.get(flowToSegment).containsKey(obsMode))
		{
			BigDecimal [] probs = _calculateProbabilitiesHelper(flowToSegment, obsMode);
			
			this.probabilities.get(flowToSegment).put(obsMode, probs);
		}
		
		if(verbose)
		{
			logger.writeLog("Attempt to get flow to segment for observed mode: " + obsMode + " in segment: " + flowToSegment + " prob: " + this.probabilities.get(flowToSegment).get(obsMode)[1], AcaciaLogger.LOG_DEBUG);
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
			return INVALID_FLOW;
		
		return (int) Math.floor(flowNumber / SEGMENT_SIZE) + 1;
	}
	
	/**
	 * _calculate probability.
	 *
	 * @param mode the mode
	 * @param segmentNumber the segment number
	 * @return the BigDecimal [] of probabilities, all with scale of 10.
	 */
	private BigDecimal []  _calculateProbabilitiesHelper(int segmentNumber, int mode)
	{	
		BigDecimal sd, mean, modeBD;
		
		//this multiplicative factor was taken from elsewhere...
		BigDecimal flowEffect = new BigDecimal("0.003").multiply(new BigDecimal(segmentNumber)).setScale(SCALE,  BigDecimal.ROUND_HALF_UP);
		
		modeBD = new BigDecimal(mode);
		
		if(mode >= 6)
		{	
			mean = new BigDecimal(mode).setScale(SCALE,  BigDecimal.ROUND_HALF_UP); 
			//standard deviation is 0.03 + effect of RefLen + effect of flow position 
			sd = new BigDecimal("0.03494").add(mean.multiply(new BigDecimal("0.06856"))).add(flowEffect);
		}
		else
		{
			mean = new BigDecimal(this.normalDistParams.get(mode).getFirst()).setScale(SCALE,  BigDecimal.ROUND_HALF_UP);
			sd = new BigDecimal(this.normalDistParams.get(mode).getSecond()).add(flowEffect).setScale(SCALE,  BigDecimal.ROUND_HALF_UP);
		}

		NormalDistributionImpl norm = new NormalDistributionImpl(mean.doubleValue(), sd.doubleValue());
	
		try
		{
				//due to rounding...
				//cumulative probability [X <= x]
			    //so prob under is [X <= MODE - 0.51], and prob over is 1 - prob [X <= MODE + 0.49] (i.e. prob X > MODE + 0.49)
				BigDecimal lowerBound = modeBD.subtract(new BigDecimal(SUBTRACT_FOR_LB)).setScale(SCALE, BigDecimal.ROUND_HALF_UP);
				BigDecimal upperBound = modeBD.add(new BigDecimal(ADD_FOR_UB)).setScale(SCALE, BigDecimal.ROUND_HALF_UP);

				BigDecimal probLessThan = new BigDecimal(norm.cumulativeProbability(lowerBound.doubleValue())).setScale(SCALE,  BigDecimal.ROUND_HALF_UP);
				BigDecimal probMoreThan = new BigDecimal("1").subtract(new BigDecimal(norm.cumulativeProbability(upperBound.doubleValue())).setScale(SCALE,  BigDecimal.ROUND_HALF_UP));
				BigDecimal probEqualTo = new BigDecimal("1").subtract(probLessThan).subtract(probMoreThan).setScale(SCALE,  BigDecimal.ROUND_HALF_UP);

				BigDecimal summed = probLessThan.add(probEqualTo).add(probMoreThan).setScale(SCALE,  BigDecimal.ROUND_HALF_UP);
				if(!summed.equals(new BigDecimal("1").setScale(SCALE, BigDecimal.ROUND_HALF_UP)))
				{
					probLessThan = probLessThan.divide(summed, SCALE, BigDecimal.ROUND_HALF_UP);
					probMoreThan = probMoreThan.divide(summed, SCALE, BigDecimal.ROUND_HALF_UP);
					probEqualTo = probEqualTo.divide(summed, SCALE, BigDecimal.ROUND_HALF_UP);
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
		int obsMode = (Integer) factors.get(OUFrequencyTable.RLE_LENGTH);
		int flowNumber = (Integer) factors.get(OUFrequencyTable.FLOW_POSITION);
		int segmentNumber = this._getSegmentForFlow(flowNumber);
		
		return this._calculateProbabilitiesHelper(obsMode, segmentNumber);
	}

	@Override
	public BigDecimal [] getEmpiricalProbability(AcaciaLogger logger, HashMap<String, Object> factors, FlowCycler cycler) throws Exception 
	{
		int flowNumber = (Integer) factors.get(OUFrequencyTable.FLOW_POSITION);
		int obsMode = (Integer) factors.get(OUFrequencyTable.RLE_LENGTH);
		int segmentNumber = this._getSegmentForFlow(flowNumber);
		
		if(this.probabilities.containsKey(segmentNumber) && this.probabilities.get(segmentNumber).containsKey(obsMode))
		{
			return this.probabilities.get(segmentNumber).get(obsMode);
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
			BalzerOUCallFrequencyTable it = new BalzerOUCallFrequencyTable(AcaciaConstants.FLOWSIM_PROBS_LOCATION);


			//probs zero works
			BigDecimal [] res = it._calculateProbabilitiesHelper(0,1);

			for(int i = 0; i < res.length; i++)
				System.out.println(res[i]);

			BigDecimal [] res2 = it._calculateProbabilitiesHelper(2,5);

			for(int i = 0; i < res2.length; i++)
				System.out.println(res2[i]);
			
			//okay all the get probabilities are tested. Interestingly should cache them so not calculating again.
			HashMap <String, Object> factors = new HashMap <String, Object>();
			factors.put(OUFrequencyTable.FLOW_POSITION, 33);
			factors.put(OUFrequencyTable.RLE_LENGTH, 2);

			//what I was actually looking up is PIC 1 in cycle 1 for reflen 2.
			BigDecimal [] res4 = it.getProbabilities(logger, factors, cycler);

			System.out.println("Res4: ");
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


