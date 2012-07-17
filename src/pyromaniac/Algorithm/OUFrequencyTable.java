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

import java.util.HashMap;

import pyromaniac.DataStructures.FlowCycler;

// TODO: Auto-generated Javadoc
/**
 * The Interface OUFrequencyTable.
 */
public interface OUFrequencyTable 
{
	
	/** The Constant LESS_THAN. */
	public static final int LESS_THAN = 0;
	
	/** The Constant EQUAL_TO. */
	public static final int EQUAL_TO = 1;
	
	/** The Constant GREATER_THAN. */
	public static final int GREATER_THAN = 2;
	
	/** The Constant MAX_PRELOAD. */
	public static final int MAX_PRELOAD = 10;
	
	/** The Constant NO_FLOW. */
	public static final int NO_FLOW = -1;
	
	public static final String RLE_LENGTH = "RLE_LENGTH";
	public static final String FLOW_POSITION = "FLOW_POSITION";
	

	/**
	 * Load probabilities.
	 *
	 * @param probFile the prob file
	 */
	public void loadProbabilities(String probFile);
	
	/**
	 * Calculate probabilities.
	 *
	 * @param obsMode the obs mode
	 * @param flowNumber the flow number
	 */
	public void calculateProbabilities(int obsMode, int flowNumber);
	
	/**
	 * Gets the probabilities.
	 *
	 * @param obsMode the obs mode
	 * @param flowNumber the flow number
	 * @return the probabilities
	 */
	public double [] getProbabilities(HashMap <String, Object> factors, FlowCycler cycler);
}
