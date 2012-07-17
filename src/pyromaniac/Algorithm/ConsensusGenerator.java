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
import java.util.HashSet;
import java.util.LinkedList;

import pyromaniac.DataStructures.FlowCycler;
import pyromaniac.DataStructures.Pair;
import pyromaniac.DataStructures.PatriciaTrie;
import pyromaniac.DataStructures.Pyrotag;
import pyromaniac.IO.AcaciaLogger;

// TODO: Auto-generated Javadoc
/**
 * The Interface ConsensusGenerator.
 */
public interface ConsensusGenerator 
{
	
	/**
	 * Generate consensus.
	 *
	 * @param logger the logger
	 * @param settings the settings
	 * @param ta the ta
	 * @param tagsToProcess the tags to process
	 * @param consensusClusters the consensus clusters
	 * @param tagToCurrPosInFlow the tag to curr pos in flow
	 * @param varyIdentically the vary identically
	 * @return the divergent tag result
	 * @throws Exception the exception
	 */
	public DivergentTagResult generateConsensus
	( 
			AcaciaLogger logger,
			HashMap<String, String> settings, 
			RLEAlignmentIndelsOnly ta,
			HashSet <Pyrotag> tagsToProcess,
			LinkedList <HashSet <Pyrotag>> consensusClusters,
			HashMap<Pyrotag, Pair<Integer, Character>> tagToCurrPosInFlow,
			FlowCycler cycler,
			boolean varyIdentically
	) throws Exception;
}