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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import pyromaniac.AcaciaConstants;
import pyromaniac.AcaciaEngine;
import pyromaniac.DataStructures.MIDPrimerCombo;
import pyromaniac.DataStructures.Pair;
import pyromaniac.DataStructures.PatriciaTrie;
import pyromaniac.DataStructures.Pyrotag;
import pyromaniac.DataStructures.Triplet;
import pyromaniac.IO.AcaciaLogger;

// TODO: Auto-generated Javadoc
/**
 * The Class SimpleClusterAligner.
 */
public class SimpleClusterAligner implements ClusterAligner 
{
	
	/**
	 * Instantiates a new simple cluster aligner.
	 */
	private SimpleClusterAligner()
	{
	}
	
	//singleton pattern
	/**
	 * Gets the single instance of SimpleClusterAligner.
	 *
	 * @return single instance of SimpleClusterAligner
	 */
	public static SimpleClusterAligner getInstance()
	{
		return SimpleClusterAlignerHolder.getInstance();
	}
	
	/**
	 * The Class SimpleClusterAlignerHolder.
	 */
	public static class SimpleClusterAlignerHolder 
	{ 
		
		/** The Constant INSTANCE. */
		private static final SimpleClusterAligner INSTANCE = new SimpleClusterAligner();

		/**
		 * Gets the single instance of SimpleClusterAlignerHolder.
		 *
		 * @return single instance of SimpleClusterAlignerHolder
		 */
		public static SimpleClusterAligner getInstance() 
		{
			return SimpleClusterAlignerHolder.INSTANCE;
		}
	}
	
	/* (non-Javadoc)
	 * @see pyromaniac.Algorithm.ClusterAligner#generateAlignments(pyromaniac.IO.AcaciaLogger, java.util.HashMap, java.util.LinkedList, java.lang.String, java.util.HashMap, java.util.HashMap, java.util.LinkedList, java.util.LinkedList)
	 */
	public void generateAlignments(
			AcaciaLogger logger,
			HashMap <String, String> settings, 
			LinkedList <Pyrotag> cluster, 
			String consensus, //consensus from hash-map
			HashMap <String, BufferedWriter> outputHandles,
			HashMap <Pyrotag,Integer> representativeSeqs, 
			ArrayDeque <Pair<RLEAlignmentIndelsOnly, HashMap<Pyrotag, Pair<Integer, Character>>>> allResults,
			LinkedList <Pyrotag> singletons
	) throws Exception 
	{
		boolean verbose = false;
		
		//current result
		Triplet <RLEAlignmentIndelsOnly, HashMap <Pyrotag, Pair<Integer,Character>>, LinkedList <Pyrotag>> result = null;

		int ctr = 0;

		String currConsensus = consensus;
		LinkedList <Pyrotag> currCluster = cluster;

		/* keep trying to align the cluster
		 * remove sequences that did align
		 * try aligning the remaining sequences, too few form a consensus
		 * use perfect matching alignment (ie. reads align for the length of the smaller read/s).
		 * if the consensus consists of one sequence, treat everyone as a singleton. 
		 */

		do
		{
			
			result = _alignSequences(logger, settings, currCluster, currConsensus, verbose);
			
			
			allResults.add(new Pair <RLEAlignmentIndelsOnly, HashMap <Pyrotag, Pair <Integer, Character>>>(result.first, result.second));

			//all aligned
			if(result.third.size() == 0) 
			{
				break;
			}
			//only one sequence did not fit the alignment, mark it as a signleton
			if(result.third.size() == 1)
			{
				
				singletons.add(result.third.getFirst());
				break;
			}


			//'checking' to see if there is a decent consensus...
			Pair <String, Integer> consensusRes = this._alignSequencesHelper(logger, result.third); 

			//there doesn't seem to be any consensus in the RLE, and not much power with the number of sequences to align.
			//make every sequence a singleton (ie. if there is only 1 sequence as a consensus...)
			if(consensusRes.getSecond() == 1 && result.third.size() < 5)
			{

				
				for(Pyrotag p: result.third)
				{
					singletons.add(p);
				}
				break; //we are done here too...
			}

			/* if there aren't vast improvements in the number of things are being added, we will rely on perfect identity
			 * the condition to use the trie is that we've tried doing it the normal way once, and the previous cluster is only
			 * two larger than the curr cluster (ie. it's not going anywhere)
			 */

			//may or may not make a difference....
			if(ctr > 1 && currCluster.size() - result.third.size() <= 2 && currCluster.size() < 10)
			{

				PatriciaTrie trie = new PatriciaTrie();

				for(Pyrotag p: result.third)
				{	
					//insert all the non-conforming sequences intro the trie.
					trie.insertString(new String(p.getCollapsedRead()), p);
				}


				//it is possible for one sequence to belong to one or more pSets

				//okay so we have inserted all the RLE reads that are not aligning.
				LinkedList <Pair <HashSet<Pyrotag>, String>> prefixSets = trie.getPrefixSets();

				
				//formally align only those sequences which share identical prefixes.
				//dont allow a sequence to be added to multiple locations
				
				HashSet<Pyrotag> processed = new HashSet <Pyrotag>();
				
				for(Pair <HashSet <Pyrotag>, String> pSet : prefixSets)
				{
					LinkedList <Pyrotag> prefixes = new LinkedList <Pyrotag> ();
					
					//stops reads belonging to multiple alignments.
					for(Pyrotag p: pSet.getFirst())
					{
						if(!processed.contains(p))
						{
							prefixes.add(p);
							processed.add(p);
						}
					}
					result = _alignSequences(logger, settings, prefixes, pSet.getSecond(), verbose);
					//result contains the tags to their initial flow positions, straight after the MID.

					allResults.add(new Pair <RLEAlignmentIndelsOnly, HashMap <Pyrotag, Pair <Integer, Character>>>(result.first, result.second));

					if(result.third.size() > 0)
					{
						
						throw new Exception("Is assumed that result returned will have zero outliers in alignment");
					}

				}
				break; //this will be the end	
			}
			else
			{
				currConsensus = consensusRes.getFirst();
				currCluster = result.third;
			}

			ctr++;

		}while(result.third.size() > 0);
	}
	

	/**
	 * _align sequences.
	 *
	 * @param logger the logger
	 * @param settings the settings
	 * @param cluster the cluster
	 * @param consensus the consensus
	 * @return the triplet
	 * @throws Exception the exception
	 */
	private Triplet <RLEAlignmentIndelsOnly, HashMap <Pyrotag, Pair<Integer,Character>>, LinkedList <Pyrotag>> _alignSequences(
			AcaciaLogger logger,
			HashMap <String, String> settings,
			LinkedList <Pyrotag> cluster, 
			String consensus,
			boolean verbose
			) throws Exception
	{
		
		Integer maxRecurseDepth = Integer.parseInt(settings.get(AcaciaConstants.OPT_MAX_RECURSE_DEPTH));
		RLEAlignmentIndelsOnly ta = new RLEAlignmentIndelsOnly(consensus, logger, maxRecurseDepth);	

		int count = 0;
		
		HashMap <Pyrotag, Pair <Integer, Character>> tagToCurrPosInFlow = 
			new HashMap <Pyrotag, Pair<Integer, Character>>();


		String key = settings.get(AcaciaConstants.OPT_FLOW_KEY);
		char lastInKey = key.charAt(key.length() - 1);


		//sequences which could not be aligned... should be outputted already... and also the stats of these should be recorded.
		//prepare tag to flow position hash
		LinkedList <Pyrotag> unalignableTags = new LinkedList <Pyrotag>();
		
		for(Pyrotag p: cluster)
		{
			
			boolean alignable = ta.align(p);
			if(alignable)
			{
				count++;
				
				
				int posAfterKeyAndMID = p.getFlowForCollapsedReadPos(settings.get(AcaciaConstants.OPT_FLOW_KEY), 0);	
				MIDPrimerCombo mid = p.getMultiplexTag();							
				char currPos = (mid == AcaciaConstants.NO_MID_GROUP)? lastInKey : mid.getMID().charAt(mid.getMID().length() - 1);
				
				
				tagToCurrPosInFlow.put(p, new Pair <Integer, Character>(posAfterKeyAndMID,currPos));					
			}
			else
			{
				unalignableTags.add(p);				
			}
		}
		
		Triplet <RLEAlignmentIndelsOnly, HashMap <Pyrotag, Pair <Integer, Character>>, LinkedList <Pyrotag>> result = new Triplet 
		<RLEAlignmentIndelsOnly, HashMap <Pyrotag, Pair <Integer, Character>>, LinkedList <Pyrotag>> (ta, tagToCurrPosInFlow, unalignableTags);
		return result;
	}
	
	//this method decides whether we should stop attempting to align these sequences, as they will never agree.
	//there is no consensus if no-one shares a RLE prefix.
	/**
	 * _align sequences helper.
	 *
	 * @param logger the logger
	 * @param unalignable the unalignable
	 * @return the pair
	 * @throws Exception the exception
	 */
	private Pair <String,Integer> _alignSequencesHelper(AcaciaLogger logger, LinkedList<Pyrotag> unalignable) throws Exception
	{
		//prepare sequences maybe? dunno really...
		//sequences that made it into this thingy have a substitution. They should try to be aligned again. They may have an insertion error also.
		int smallestRLELength = -1;
		
		for(Pyrotag p: unalignable)
		{
			char [] rle = p.getCollapsedRead();
			
			if(smallestRLELength == -1 || rle.length < smallestRLELength)
			{
				smallestRLELength = rle.length;
			}
		}
		HashMap <String, Integer> mostCommonPrefix = new HashMap <String, Integer>();
		
		int mostCommon = -1;
		String mostCommonSeq = null;
	
		for(Pyrotag p: unalignable)
		{
			char [] seq = p.getCollapsedRead();
			char [] subseq = Arrays.copyOfRange(seq, 0, smallestRLELength);
			
			if(!mostCommonPrefix.containsKey(new String(subseq)))
			{
				mostCommonPrefix.put(new String(subseq), 0);
			}
			
			int newValue = mostCommonPrefix.get(new String(subseq)) + 1;

			if(mostCommon < newValue)
			{
				mostCommon = newValue;
				mostCommonSeq = new String(subseq);
			}
			
			mostCommonPrefix.put(new String(subseq), newValue);
		}
		
		return new Pair <String, Integer> (mostCommonSeq, mostCommon);
	}
}
