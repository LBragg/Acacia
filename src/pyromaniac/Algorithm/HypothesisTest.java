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

import pyromaniac.IO.AcaciaLogger;

// TODO: Auto-generated Javadoc
/**
 * The Class HypothesisTest.
 */
public abstract class HypothesisTest 
{
	
	/** The observations above mode. */
	protected int observationsAboveMode;
	
	/** The observations below mode. */
	protected int observationsBelowMode;
	
	/** The observations at mode. */
	protected int observationsAtMode;
	
	/** The mode length. */
	protected int modeLength;
	
	/** The avg flow pos. */
	private double avgFlowPos;

	/** The N. */
	protected int N;
	
	/** The P. */
	protected double [] P;
	
	/** The p. */
	protected double p;

	/** The logger. */
	protected AcaciaLogger logger;
	
	/** The alpha. */
	protected double alpha;
	
	/** The verbose. */
	boolean verbose;

	/** The significant above. */
	protected boolean significantAbove;
	
	/** The significant below. */
	protected boolean significantBelow;

	/**
	 * Instantiates a new hypothesis test.
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
	public HypothesisTest(int obsAbove, int obsBelow, int obsMode,int modeLength, double [] P,  double alpha, double avgFlowPos, AcaciaLogger logger, boolean verbose)
	{
		this.observationsAboveMode = obsAbove;
		this.observationsBelowMode = obsBelow;
		this.observationsAtMode = obsMode;
		this.modeLength = modeLength;
		this.P = P;
		this.p = -1;
		this.logger = logger;
		this.alpha = alpha;
		this.N = this.observationsAboveMode + this.observationsBelowMode + this.observationsAtMode;	
		this.verbose = verbose;
		this.significantAbove = false;
		this.significantBelow = false;
		this.setAvgFlowPos(avgFlowPos);
	}

	/**
	 * Checks if is significant above.
	 *
	 * @return true, if is significant above
	 */
	public boolean isSignificantAbove() {
		return significantAbove;
	}

	/**
	 * Sets the significant above.
	 *
	 * @param significantAbove the new significant above
	 */
	public void setSignificantAbove(boolean significantAbove) {
		this.significantAbove = significantAbove;
	}

	/**
	 * Checks if is significant below.
	 *
	 * @return true, if is significant below
	 */
	public boolean isSignificantBelow() {
		return significantBelow;
	}

	/**
	 * Sets the significant below.
	 *
	 * @param significantBelow the new significant below
	 */
	public void setSignificantBelow(boolean significantBelow) {
		this.significantBelow = significantBelow;
	}

	/**
	 * Run test.
	 *
	 * @throws Exception the exception
	 */
	public abstract void runTest() throws Exception;

	/**
	 * Sets the avg flow pos.
	 *
	 * @param avgFlowPos the new avg flow pos
	 */
	public void setAvgFlowPos(double avgFlowPos) {
		this.avgFlowPos = avgFlowPos;
	}

	/**
	 * Gets the avg flow pos.
	 *
	 * @return the avg flow pos
	 */
	public double getAvgFlowPos() {
		return avgFlowPos;
	}
}
