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

import java.util.HashSet;

import org.apache.commons.math.distribution.NormalDistributionImpl;

import pyromaniac.IO.AcaciaLogger;

// TODO: Auto-generated Javadoc
/**
 * The Class MultinomialOneSidedTest.
 */
public class MultinomialOneSidedTest extends HypothesisTest
{
	
	/**
	 * Instantiates a new multinomial one sided test.
	 *
	 * @param obsAbove the obs above
	 * @param obsBelow the obs below
	 * @param obsMode the obs mode
	 * @param modeLength the mode length
	 * @param P the p
	 * @param alpha the alpha
	 * @param avgFlowPos the avg flow pos
	 * @param logger the logger
	 * @param verbose the verbose
	 */
	public MultinomialOneSidedTest(int obsAbove, int obsBelow, int obsMode,int modeLength, double [] P,  double alpha, double avgFlowPos, AcaciaLogger logger, boolean verbose) 
	{
		super(obsAbove, obsBelow, obsMode, modeLength, P, alpha, avgFlowPos, logger,verbose);
	}

	/* (non-Javadoc)
	 * @see pyromaniac.Algorithm.HypothesisTest#runTest()
	 */
	public void runTest() throws Exception 
	{
		//not significant if no observations below and no observations above
		if (observationsBelowMode == 0 && observationsAboveMode == 0) 
		{
			this.p = 1;
		}//significant if the number of observations above mode is equal to that at the mode. 
		/*			else if(N <= 5 && observationsAtMode != N)
		{
			this.significant = true;
			this.p = 0;
		}*/
		else if ( alpha > 0 && (observationsAboveMode == observationsAtMode
				|| observationsBelowMode == observationsAtMode)) 
		{
			this.significantAbove = true;
			this.significantBelow = true;
			//this.significantCombined = true;
			this.p = 0;
		}
		else
		{

			int indexBelowMode = 1;
			int indexAboveMode = 2;

			double [] X = new double [] {this.observationsAtMode, this.observationsBelowMode, this.observationsAboveMode};
			double [] xDivN =  new double [] {(double)this.observationsAtMode / (double)this.N, 
					(double)this.observationsBelowMode / (double)this.N, (double)this.observationsAboveMode / (double)this.N};
			HashSet <Integer> gamma = new HashSet <Integer> ();

			if(verbose)
			{
				for(int i = 0; i < X.length; i++)
				{
					logger.writeLog("X[" + i + "] = " + X[i], AcaciaLogger.LOG_DEBUG);
					logger.writeLog("XDivN[" + i + "] = " + xDivN[i], AcaciaLogger.LOG_DEBUG);
					logger.writeLog("P[" + i + "] = " + P[i], AcaciaLogger.LOG_DEBUG);
				}
			}

			for(int i = 1; i < xDivN.length; i++)
			{
				if(xDivN[i] >= P[i])
				{
					gamma.add(i);
				}
			}

			double sumX  = 0;
			double sumP = 0;

			for(int index: gamma)
			{
				sumX += X[index];
				sumP += P[index];
			}

			while(gamma.size() < 2)
			{
				boolean added = false;

				for(int i = 1; i < xDivN.length; i++)
				{
					double adjP  = X[i] * (1 - sumP) /(this.N - sumX);

					if(adjP > P[i] & ! gamma.contains(i))
					{
						gamma.add(i);
						added = true;
					}
				}

				if(! added)
				{
					break;
				}

				sumP = 0;
				sumX = 0;

				for(int index: gamma)
				{
					sumX += X[index];
					sumP += P[index];
				}
			}

			NormalDistributionImpl norm = new NormalDistributionImpl();

			double w2Num  = Math.pow(((N - sumX) - N * (1 - sumP)),2) ; 
			double w2Denom =(N * (1 - sumP)); 
			double resSum = 0;
			for(int index: gamma)
			{
				resSum += Math.pow(X[index] - N * P[index], 2) / (N * P[index]);
			}

			double w2 =  (w2Num/w2Denom) + resSum;	
			double m = Math.sqrt(P[indexBelowMode] * P[indexAboveMode] / (1 - P[indexBelowMode] - P[indexAboveMode]));
			double calcP = (0.25 + (Math.atan(m) / (2 * Math.PI))) * Math.exp(-w2 / 2) + (1 - norm.cumulativeProbability(Math.sqrt(w2)));

			this.p = calcP;

			if (this.alpha > 0 ) 
			{
				boolean sig = (this.p <= this.alpha);
				this.significantAbove = sig;
				this.significantBelow = sig;
				//	this.significantCombined = sig;
			}
			else //alpha == zero
			{
			}
		}
	}
}
