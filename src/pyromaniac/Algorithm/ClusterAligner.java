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

import java.io.BufferedWriter;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.LinkedList;

import pyromaniac.DataStructures.Pair;
import pyromaniac.DataStructures.Pyrotag;
import pyromaniac.IO.AcaciaLogger;

// TODO: Auto-generated Javadoc
/**
 * The Interface ClusterAligner.
 */
public interface ClusterAligner 
{
	
	/**
	 * Generate alignments.
	 *
	 * @param logger the logger
	 * @param settings the settings
	 * @param cluster the cluster
	 * @param consensus the consensus
	 * @param outputHandles the output handles
	 * @param representativeSeqs the representative seqs
	 * @param allResults the all results
	 * @param singletons the singletons
	 * @throws Exception the exception
	 */
	public void generateAlignments(
			AcaciaLogger logger,
			HashMap <String, String> settings, 
			LinkedList <Pyrotag> cluster, 
			String consensus, //consensus from hashmapping
			HashMap <String, BufferedWriter> outputHandles,
			HashMap <Pyrotag,Integer> representativeSeqs, 
			ArrayDeque<Pair<RLEAlignmentIndelsOnly, HashMap<Pyrotag, Pair<Integer, Character>>>> allResults,
			LinkedList <Pyrotag> singletons
			) throws Exception;
}
