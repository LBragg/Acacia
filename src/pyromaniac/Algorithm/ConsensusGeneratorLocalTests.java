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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import pyromaniac.AcaciaConstants;
import pyromaniac.AcaciaEngine;
import pyromaniac.Algorithm.RLEAlignmentIndelsOnly.AlignmentColumn;
import pyromaniac.DataStructures.FlowCycler;
import pyromaniac.DataStructures.Pair;
import pyromaniac.DataStructures.PatriciaTrie;
import pyromaniac.DataStructures.Pyrotag;
import pyromaniac.IO.AcaciaLogger;

// TODO: Auto-generated Javadoc
/**
 * The Class ConsensusGeneratorLocalTests.
 */
public class ConsensusGeneratorLocalTests implements ConsensusGenerator
{

	/* (non-Javadoc)
	 * @see pyromaniac.Algorithm.ConsensusGenerator#generateConsensus(pyromaniac.IO.AcaciaLogger, java.util.HashMap, pyromaniac.Algorithm.ThreadedAlignment, java.util.LinkedList, java.util.HashMap, boolean)
	 */
	
	//at return returns unprocessed tags, 
	public UnprocessedPyrotagResult generateConsensus
	( 
			AcaciaLogger logger,
			HashMap<String, String> settings, 
			RLEAlignmentIndelsOnly ta,
			HashSet <Pyrotag> tagsToProcess,
			LinkedList <HashSet <Pyrotag>> consensusClusters,
			HashMap<Pyrotag, Pair<Integer, Character>> tagToCurrPosInFlow,
			FlowCycler cycler,
			boolean varyIdentically) throws Exception 
	{
		if(tagsToProcess.size() == 1)
		{	
			consensusClusters.add(tagsToProcess);
			
			
			return null;
		}

		
		
		//go through corrected branches here!!!
		double thresholdPValue = AcaciaEngine.getEngine().parseSignificanceThreshold(settings.get(AcaciaConstants.OPT_SIGNIFICANCE_LEVEL));
		OUFrequencyTable table = null;
		
		if(settings.get(AcaciaConstants.OPT_ERROR_MODEL).equals(AcaciaConstants.OPT_FLOWSIM_ERROR_MODEL))
		{ 
			table = new MaldeOUCallFrequencyTable(AcaciaConstants.FLOWSIM_PROBS_LOCATION);
		}
		else if (settings.get(AcaciaConstants.OPT_ERROR_MODEL).equals(AcaciaConstants.OPT_ACACIA_TITANIUM_ERROR_MODEL))
		{
			//this model is still be evaluated
			table = new MaldeOUCallFrequencyTable(AcaciaConstants.ACACIA_EMP_MODEL_TITANIUM_LOCATION);
		}
		else if(settings.get(AcaciaConstants.OPT_ERROR_MODEL).equals(AcaciaConstants.OPT_ACACIA_IONTORRENT316_MODEL))
		{
			table = new IonTorrentOUCallFrequency(AcaciaConstants.IONTORRENT_316_PROBS_LOCATION);
		}
		else if(settings.get(AcaciaConstants.OPT_ERROR_MODEL).equals(AcaciaConstants.OPT_ACACIA_IONTORRENT314_MODEL))
		{
			table = new IonTorrentOUCallFrequency(AcaciaConstants.IONTORRENT_314_PROBS_LOCATION);
		}
		else //quince
		{
			table = new pyromaniac.Algorithm.QuinceFrequencyTable(AcaciaConstants.PYRONOISE_PROBS_LOCATION);
		}

		int alignmentCounter =  0;
		Iterator <AlignmentColumn> it = ta.iterator();	
		int numSignificant = 0;

		int columnIndex = 0;

		//option is to record the number of transgressions of a particular pyrotag? And also co-varying things?
		HashMap <Pyrotag, HashMap <Pyrotag, Integer>> varyingTogether = new HashMap <Pyrotag, HashMap <Pyrotag, Integer>> ();
		HashMap <Pyrotag, Integer> numDifferences = new HashMap <Pyrotag, Integer>();

		//iterate through all the alignment positions, identify those that differ from the mode at each point, and record that they 'differ'
		//ignore sequences that appear to have disagreed already ??

		int numTests = 0;
		int initialSize = -1;
		AlignmentColumn premBreak = null;

		Integer minFlowTrunc = this.getMinFlowTrunc(settings);
		Double minReadRepTruncation = AcaciaEngine.getEngine().getMinReadRepTruncation(settings);


		while(it.hasNext())
		{
			AlignmentColumn ac = it.next();
			alignmentCounter++;

			char lastChar;

			if(columnIndex != 0)
			{
				if(initialSize == -1)
				{
					initialSize = ac.getTags().size();
				}

				double prop = (double)ac.getTags().size() / (double)initialSize; 

				HypothesisTest test = processAlignmentColumn(logger, settings, tagToCurrPosInFlow, ac, 
						tagsToProcess, table, thresholdPValue, varyingTogether, numDifferences, varyIdentically, false, ta, cycler);

			
				if(test != null && (minReadRepTruncation != null && minFlowTrunc != null && prop < minReadRepTruncation && minFlowTrunc <= test.getAvgFlowPos()))
				{
					premBreak = ac;
					break;
				}

				if(test != null)
					numTests++;

				if(test != null && (test.isSignificantAbove() || test.isSignificantBelow()))
				{
					numSignificant++;
				}

				lastChar = ac.getValue();
			}
			else
			{
				lastChar = Pyrotag.CYCLE_START; //this is the easy way to do it, assuming the last char for any of the sequences
				//is unlikely to be consistent.
			}

			columnIndex++;

			char [] validBases = {'A', 'T', 'G', 'C'};
				
			for(char base: validBases)
			{
				AlignmentColumn insert = ac.getInsertionCorrespondingTo(base);
				
				if(insert != null)
				{
					HypothesisTest testInner = processAlignmentColumn(logger, settings, tagToCurrPosInFlow, insert, 
							tagsToProcess, table, thresholdPValue, varyingTogether, numDifferences, varyIdentically, false, ta, cycler);

					//at the end of the alignment
					if(testInner == null)
						continue;
					
					numTests++;
					
					if(testInner.isSignificantAbove() || testInner.isSignificantBelow())
					{
						numSignificant++;
					}
				}
				
			}
		}
			
		//caveat is that if I change the alignment algorithm, such that it tolerates double insertions, this will break.
			/*
			//iterate through the insertion columns
			AlignmentColumn firstInsert = ac.nextInsertionGivenLastFlow(lastChar);
			AlignmentColumn lastInsert = firstInsert;

			do
			{
				if(lastInsert == null)
					break;


				HypothesisTest testInner = processAlignmentColumn(logger, settings, tagToCurrPosInFlow, lastInsert, 
						tagsToProcess, table, thresholdPValue, varyingTogether, numDifferences, varyIdentically, false, ta);

				//at the end of the alignment
				if(testInner == null)
					continue;
				
				numTests++;

				lastInsert = ac.nextInsertionGivenLastFlow(lastInsert.getValue());

				if(testInner.isSignificantAbove() || testInner.isSignificantBelow())
				{
					numSignificant++;
				}
			}
			while(lastInsert != firstInsert);
		}
		*/

		if(numSignificant == 0)
		{
			consensusClusters.add(tagsToProcess);
			return null;
		}

		HashSet <Pyrotag> tagsToRemove = new HashSet <Pyrotag>();
		
		if(numSignificant > 0)
		{
			for(Pyrotag p: numDifferences.keySet()) //there is no consensus? In this case? Seems really grossly inefficient...
			{			
					//bugfix: changed numDifferences.contains to tagsToProcess.contains
					if(tagsToProcess.contains(p) && numDifferences.get(p) >= 1)
					{
						tagsToRemove.add(p); //tagsToRemove contains non-consensus tags, puts everything in there for vary identically.
					}
			}					
		}

		//consensus for this cluster. But if in vary identically, this should be empty.
		HashSet <Pyrotag> tagsToKeep = new HashSet <Pyrotag>();

		for(Pyrotag p: tagsToProcess)
		{		
			if(! tagsToRemove.contains(p))
			{	
				tagsToKeep.add(p);
			}
		}
		
		//this is the stuff up here... won't be created here using varyIdentically...
		if(tagsToKeep.size() > 0)
		{
			consensusClusters.add(tagsToKeep);
		}

		UnprocessedPyrotagResult utr = new UnprocessedPyrotagResult(tagsToRemove, varyingTogether, numDifferences);
		return utr;
	}
	
	/**
	 * The Class UnprocessedPyrotagResult.
	 */
	public class UnprocessedPyrotagResult implements DivergentTagResult
	{
		
		/** The tags. */
		HashSet <Pyrotag> tags;
		
		/** The varying together. */
		HashMap <Pyrotag, HashMap <Pyrotag, Integer>> varyingTogether;
		
		/** The num disagreements. */
		HashMap <Pyrotag, Integer> numDisagreements;
		
		/**
		 * Instantiates a new unprocessed pyrotag result.
		 *
		 * @param tags the tags
		 * @param varyingTogether the varying together
		 * @param numDisagreements the num disagreements
		 */
		public UnprocessedPyrotagResult(HashSet <Pyrotag> tags, HashMap <Pyrotag, HashMap <Pyrotag, Integer>> varyingTogether, 
				HashMap <Pyrotag, Integer> numDisagreements)
		{
			this.tags = tags;
			this.varyingTogether = varyingTogether;
			this.numDisagreements = numDisagreements;
		}
		
		/* (non-Javadoc)
		 * @see pyromaniac.Algorithm.DivergentTagResult#getTags()
		 */
		public HashSet <Pyrotag> getTags()
		{
			return this.tags;
		}
		
		/**
		 * Gets the varying together.
		 *
		 * @return the varying together
		 */
		public HashMap <Pyrotag, HashMap <Pyrotag, Integer>> getVaryingTogether()
		{
			return this.varyingTogether;
		}
		
		/**
		 * Gets the num disagreements.
		 *
		 * @return the num disagreements
		 */
		public HashMap <Pyrotag, Integer> getNumDisagreements()
		{
			return this.numDisagreements;
		}
	}
	
	//this will generate the set of sub clusters that need to be processed
	/**
	 * Generate clusters from vary together.
	 *
	 * @param logger the logger
	 * @param settings the settings
	 * @param consensusClusters the consensus clusters
	 * @param upr the upr
	 * @param verbose the verbose
	 * @return the linked list
	 * @throws Exception the exception
	 */
	public LinkedList <HashSet<Pyrotag>> generateClustersFromVaryTogether
	(
			AcaciaLogger logger,
			HashMap<String, String> settings, 
			LinkedList <HashSet <Pyrotag>> consensusClusters,
			UnprocessedPyrotagResult upr,
			boolean verbose
	) throws Exception
	{
		//this function is allowing a read to be both a member of consensus clusters and be processed again 
		
		HashMap <Pyrotag, HashSet <Pyrotag>> otherBranches = new HashMap <Pyrotag, HashSet <Pyrotag>> ();	
		HashSet <Pyrotag> goingSolo = new HashSet <Pyrotag>();
		
		//identify which ones varied in the same direction,they can go into a new set.
		//ideally, if one sequence does not vary with another all the time, or the set is too small.. should be broken up.
		//this should be for the initial split only
		
		for(Pyrotag p: upr.getTags()) //changing from varyingTogether to tags to Remove
		{
			boolean processed = false;
			
			//how can processed evaluate to false, but the data be inside branches?
			for(Pyrotag second : upr.getTags())
			{
				if(second == p)
					continue; //ignore me for now!

				if(goingSolo.contains(second))
				{
					continue;
				}
				
				//this checks whether p or second should be put in the same cluster, if not, moves to the next pyrotag.	
				//if neither varies with the other, that is silly.
				
				int numDiff = 0;
				
				if(upr.getVaryingTogether().containsKey(p) && upr.getVaryingTogether().get(p).containsKey(second))
				{
					numDiff = upr.getVaryingTogether().get(p).get(second);
				}
				else if(upr.getVaryingTogether().containsKey(second) && upr.getVaryingTogether().get(second).containsKey(p))
				{	
					numDiff = upr.getVaryingTogether().get(second).get(p);
				}
				else
				{
					continue; //next inner pyrotag.
				}
				
				
				
				if(
						numDiff != upr.getNumDisagreements().get(p) ||
						numDiff != upr.getNumDisagreements().get(second) 
				)
				{
					continue;//both have to have the same number of differences... so complete linkage.
				}

				
				//they are not already clustered.
				if(!otherBranches.containsKey(p) && !otherBranches.containsKey(second))
				{
					//create a new set of tags containing both p and second...
					HashSet <Pyrotag> newBranch = new HashSet <Pyrotag>();
					newBranch.add(p);
					newBranch.add(second);
					otherBranches.put(p, newBranch);
					otherBranches.put(second, newBranch);

				}
				else if(otherBranches.containsKey(p) && otherBranches.containsKey(second))
				{
					//check they are in the same cluster... otherwise, they possibly should be merged.
					if(otherBranches.get(p) != otherBranches.get(second))
					{
						HashSet <Pyrotag> merged = new HashSet <Pyrotag>();
						merged.addAll(otherBranches.get(p));
						merged.addAll(otherBranches.get(second));

						for(Pyrotag pMerged: merged)
						{
							otherBranches.remove(pMerged);
							otherBranches.put(pMerged, merged);
						}
					}
				}
				else if(otherBranches.containsKey(p))
				{
					otherBranches.get(p).add(second);
					otherBranches.put(second,otherBranches.get(p));
				}
				else if(otherBranches.containsKey(second))
				{
					otherBranches.get(second).add(p);
					otherBranches.put(p, otherBranches.get(second));
				}
	
				//processed = true; still not sure why this didnt work
			}
	
			if(goingSolo.contains(p) || otherBranches.containsKey(p))
			{
				processed = true;
			}
			
			//never varied with anything
			if(!processed)
			{
				HashSet <Pyrotag> soloP = new HashSet<Pyrotag>();
				soloP.add(p);
				goingSolo.add(p);
				consensusClusters.add(soloP);
			}
		}
	
		//detects all the new branches...
		int numNewBranches = 0;
		 
		HashSet <HashSet <Pyrotag>> seenBefore  = new HashSet <HashSet <Pyrotag>>();
		
		for(Pyrotag p: otherBranches.keySet())
		{				
			if(! seenBefore.contains(otherBranches.get(p)))
			{
				numNewBranches++;
				seenBefore.add(otherBranches.get(p));
			}
		}
		
		//does this clean anything up
		otherBranches.clear();
		otherBranches = null;

		LinkedList <HashSet <Pyrotag>> retVal = new LinkedList <HashSet<Pyrotag>>();
		retVal.addAll(seenBefore) ;
		
		return retVal;		
	}

	//assumes singletons have been removed.
	/**
	 * Generate trie from unprocessed tags.
	 *
	 * @param logger the logger
	 * @param tagsRemaining the tags remaining
	 * @return the patricia trie
	 * @throws Exception the exception
	 */
	public PatriciaTrie generateTrieFromUnprocessedTags
	(
			AcaciaLogger logger,
			HashSet <Pyrotag> tagsRemaining
	) throws Exception 
	{
		assert(tagsRemaining.size() > 0); 
		
		PatriciaTrie trie = new PatriciaTrie();
		for(Pyrotag p: tagsRemaining)
		{
				trie.insertString(new String(p.getProcessedString()), p);
		}
		return trie;
	}
	
	
	

	//singleton pattern
	/**
	 * Gets the single instance of ConsensusGeneratorLocalTests.
	 *
	 * @return single instance of ConsensusGeneratorLocalTests
	 */
	public static ConsensusGeneratorLocalTests getInstance()
	{
		return ConsensusGeneratorHolder.getInstance();
	}
	
	/**
	 * The Class ConsensusGeneratorHolder.
	 */
	public static class ConsensusGeneratorHolder 
	{ 
		
		/** The Constant INSTANCE. */
		private static final ConsensusGeneratorLocalTests INSTANCE = new ConsensusGeneratorLocalTests();

		/**
		 * Gets the single instance of ConsensusGeneratorHolder.
		 *
		 * @return single instance of ConsensusGeneratorHolder
		 */
		public static ConsensusGeneratorLocalTests getInstance() 
		{
			return ConsensusGeneratorHolder.INSTANCE;
		}
	}
	

	/**
	 * Gets the min flow trunc.
	 *
	 * @param settings the settings
	 * @return the min flow trunc
	 */
	private Integer getMinFlowTrunc(HashMap<String, String> settings) 
	{
		if(settings.get(AcaciaConstants.OPT_MIN_FLOW_TRUNCATION) != null)
		{
			return Integer.parseInt(settings.get(AcaciaConstants.OPT_MIN_FLOW_TRUNCATION));
		}
		return null;
	}
	
	//prepares the variables for hypothesis testing on an individual column
	/**
	 * Process alignment column.
	 *
	 * @param logger the logger
	 * @param settings the settings
	 * @param tagToCurrPosInFlow the tag to curr pos in flow
	 * @param ac the ac
	 * @param tagsInCluster the tags in cluster
	 * @param table the table
	 * @param significanceLevel the significance level
	 * @param varyingTogether the varying together
	 * @param numDifferences the num differences
	 * @param varyIdentically the vary identically
	 * @param verbose the verbose
	 * @param ta the ta
	 * @return the hypothesis test
	 * @throws Exception the exception
	 */
	
	//can guarantee that numDifferences will be populated if varyIdentically, but not varyingTogether.
	
	//fix this
	private HypothesisTest processAlignmentColumn(
			AcaciaLogger logger,
			HashMap <String, String> settings, 
			HashMap <Pyrotag, Pair<Integer, Character>> tagToCurrPosInFlow, 
			AlignmentColumn ac,
			HashSet <Pyrotag> tagsInCluster,
			OUFrequencyTable table, 
			double significanceLevel, 
			HashMap<Pyrotag, HashMap<Pyrotag, Integer>> varyingTogether, 
			HashMap<Pyrotag, Integer> numDifferences, 
			boolean varyIdentically, 
			boolean verbose, 
			RLEAlignmentIndelsOnly ta,
			FlowCycler cycler
	
		) throws Exception
			{
		
		verbose = false;
		
		//
		HashMap <Integer, HashSet <Pyrotag>> observationsAtPosition = ac.getHPLengthToTags(tagsInCluster);

			
		int mode = -1;
		int modeFreq = -1;

		HashMap <Integer, Integer> flowToNumReads = new HashMap <Integer, Integer>();

		char currValue = ac.getValue();

		if(verbose)
		{
			logger.writeLog("Curr value: " + currValue, AcaciaLogger.LOG_DEBUG);
		}

		//all columns should be processed... so the flow should be correct...
		for(Integer obsLength: observationsAtPosition.keySet())
		{

			//get tags that have the observed length.
			HashSet <Pyrotag> tagsWithObsLength = observationsAtPosition.get(obsLength);

			//if the size is greater than the curr freq, than this must be the mode...
			if(tagsWithObsLength.size() > modeFreq)
			{
				modeFreq = tagsWithObsLength.size();
				mode = obsLength;
			}

			//for each of the pyrotags with this length
			for(Pyrotag p : tagsWithObsLength)
			{

				//get previous position in flow
				Pair <Integer, Character> prevPosInFlow = tagToCurrPosInFlow.get(p);				

				int prevPos = prevPosInFlow.getFirst();
				
				if(verbose)
				{
					logger.writeLog("Previous position: " + prevPos + "with char <" + prevPosInFlow.getSecond() + "> for read " + p.getID(), AcaciaLogger.LOG_DEBUG);
					logger.writeLog("Has observed length : " + obsLength + " for read " + p.getID(), AcaciaLogger.LOG_DEBUG);
				}
				
				//calculate the distance between the flows.
				//how is it possible that the previous base is the same as the curr base?

				
				int currPosInFlow = prevPosInFlow.getFirst();
				
				if(obsLength > 0)
				{
					int dist = p.getFlowCycler().minPossibleFlowsBetweenFlowPosXAndCharY(prevPos, currValue);
					

					if(verbose)
					{
						logger.writeLog("Dist is " + dist, AcaciaLogger.LOG_DEBUG);
					}
					
					currPosInFlow += dist;
					tagToCurrPosInFlow.put(p, new Pair <Integer, Character> (currPosInFlow, currValue)); //update
				}
					
				//initialise flowToNumReads if not defined
				if(!flowToNumReads.containsKey(currPosInFlow))
				{
					flowToNumReads.put(currPosInFlow, 0);
				}

				//increment flowToNumReads
				flowToNumReads.put(currPosInFlow, flowToNumReads.get(currPosInFlow) + 1);
			}
		}
		
		if(mode == -1)
		{
			return null;
		}
		
		
		int obsBelow = 0;
		int obsAbove = 0;

		for(Integer obsLength : observationsAtPosition.keySet())
		{
			if(obsLength > mode)
			{
				obsAbove += observationsAtPosition.get(obsLength).size();
			}
			else if(obsLength < mode)
			{
				obsBelow += observationsAtPosition.get(obsLength).size();
			}
		}

		if(verbose)
		{
			logger.writeLog("About to do test: mode is " + mode + ", obs below: " + obsBelow + " obsAbove: " + obsAbove + " obsAt: " + modeFreq, AcaciaLogger.LOG_DEBUG);

		}

		boolean verbose2 = verbose; //too much information from run test for significance
		
		//perform test for significance, get result
		HypothesisTest res = this._runTestForSignificance(logger, mode, obsBelow,obsAbove, modeFreq, 
				flowToNumReads, table, significanceLevel, cycler, verbose2);

		
		
		//to handle introduced deletions for very small clusters.
		//overrides the result of the hypothesis test
		if(modeFreq + obsAbove + obsBelow == 2 && modeFreq != 2)
		{
				boolean splitIfDifferent = Boolean.parseBoolean(settings.get(AcaciaConstants.OPT_SIGNIFICANT_WHEN_TWO));
			if(splitIfDifferent)
			{
					if(obsAbove > 0)
					{
						res.significantAbove = true;
					}
					else
					{
						res.significantBelow = true;
					}
			}
		}
		
		if(verbose)
		{
			logger.writeLog("Sig above: " + res.isSignificantAbove(), AcaciaLogger.LOG_DEBUG);
			logger.writeLog("Sig below: " + res.isSignificantBelow(), AcaciaLogger.LOG_DEBUG);

		}
	
		
		ArrayList <Pyrotag> obsAboveMode = new ArrayList <Pyrotag>();
		ArrayList <Pyrotag> obsBelowMode = new ArrayList <Pyrotag>();
	
		if(verbose)
			logger.writeLog("Iterating through observations to count numDifferences", AcaciaLogger.LOG_DEBUG);

		for(Integer obsLength: observationsAtPosition.keySet())
		{
			if(obsLength != mode)
			{	
				for(Pyrotag p: observationsAtPosition.get(obsLength))
				{
					//three things need to be done in this loop				
					if((obsLength > mode && res.isSignificantAbove()) || (obsLength < mode && res.isSignificantBelow()))
					{
						if(! numDifferences.containsKey(p))
						{
							numDifferences.put(p, 0);	
						}
						numDifferences.put(p, numDifferences.get(p) + 1);			
					}
					
					//for the other bit.
					if(obsLength < mode)
					{
						obsBelowMode.add(p);
					}	
					else if (obsLength > mode)
					{
						obsAboveMode.add(p);
					}						
				}
			}
		}
		
		if(varyIdentically)
			return res;
		
		//else we record whether they varied together above or below the mode.
		
		if(verbose)
			logger.writeLog("Running the code varying together", AcaciaLogger.LOG_DEBUG);
		
		_varyingTogether(varyingTogether, obsBelowMode);
		_varyingTogether(varyingTogether, obsAboveMode);		
		
		if(verbose)
			logger.writeLog("Returning from hypothesis testing", AcaciaLogger.LOG_DEBUG);
		
		return res;
	}
	
	//populates the varying together hash
	/**
	 * _varying together.
	 *
	 * @param varyingTogether the varying together
	 * @param tags the tags
	 */
	private void _varyingTogether(HashMap<Pyrotag, HashMap<Pyrotag, Integer>> varyingTogether, ArrayList <Pyrotag> tags)
	{
		for(int i = 0; i < tags.size(); i++)
		{
			Pyrotag p = tags.get(i);
			
			for(int j = i + 1; j < tags.size(); j++)
			{
				Pyrotag pInner = tags.get(j);
				
				if(varyingTogether.containsKey(p))
				{
					if(varyingTogether.get(p).containsKey(pInner))
					{
						varyingTogether.get(p).put(pInner, varyingTogether.get(p).get(pInner) + 1);
					}
					else
					{
						varyingTogether.get(p).put(pInner, 1);
					}
				}
				else if (varyingTogether.containsKey(pInner))
				{
					if(varyingTogether.get(pInner).containsKey(p))
					{
						varyingTogether.get(pInner).put(p, varyingTogether.get(pInner).get(p) + 1);
					}
					else
					{
						varyingTogether.get(pInner).put(p, 1);
					}
				}
				else //just put it in p.
				{
					varyingTogether.put(p, new HashMap <Pyrotag, Integer>());
					varyingTogether.get(p).put(pInner, 1);
				}
				
				/*
				if(! varyingTogether.containsKey(p))
				{
					varyingTogether.put(p, new HashMap <Pyrotag, Integer>());
				}
				if(! varyingTogether.containsKey(pInner))
				{
					varyingTogether.put(pInner, new HashMap <Pyrotag, Integer>());
				}

				if(! varyingTogether.get(p).containsKey(pInner))
				{
					varyingTogether.get(p).put(pInner, 0);
				}
				if(! varyingTogether.get(pInner).containsKey(p))
				{
					varyingTogether.get(pInner).put(p, 0);
				}
				
				varyingTogether.get(p).put(pInner, varyingTogether.get(p).get(pInner) + 1);
				varyingTogether.get(pInner).put(p, varyingTogether.get(pInner).get(p) + 1);
				*/
			}
			
			/*
			//new code, check first that p is in there.
			if(!varyingTogether.containsKey(p))
			{
				varyingTogether.put(p, new HashMap <Pyrotag, Integer>());
			}
			*/
			
			/*
			//even though this will never happen
			if(! varyingTogether.get(p).containsKey(p))
			{
				varyingTogether.get(p).put(p, 0);
			}
			varyingTogether.get(p).put(p, varyingTogether.get(p).get(p) + 1); //so we now record whether a sequence varies with itself!
			*/
		}
	}
	
	/**
	 * _run test for significance.
	 *
	 * @param logger the logger
	 * @param modeVal the mode val
	 * @param obsBelow the obs below
	 * @param obsAbove the obs above
	 * @param obsAt the obs at
	 * @param flowPosToFreq the flow pos to freq
	 * @param table the table
	 * @param thresholdPValue the threshold p value
	 * @param verbose the verbose
	 * @return the hypothesis test
	 * @throws Exception the exception
	 */	
	private HypothesisTest _runTestForSignificance(
			AcaciaLogger logger, 
			int modeVal, 
			int obsBelow, 
			int obsAbove, 
			int obsAt, 
			HashMap <Integer, Integer> flowPosToFreq, 
			OUFrequencyTable table, 
			double thresholdPValue, 
			FlowCycler cycler, 
			boolean verbose) throws Exception
			{

		double [] weightedP = new double [3];
		double flowSum = 0.0;
		double numSeqs = 0.0;

		//okay things might be relevant
		//flow position, position in cycle, base, rle length, are all things that relate 


		HashMap <String, Object> probFactors = new HashMap <String, Object>();
		probFactors.put(OUFrequencyTable.RLE_LENGTH, modeVal);


		for(Integer flowPos: flowPosToFreq.keySet())
		{
			//replace this every time, to save on hashmap construction
			probFactors.put(OUFrequencyTable.FLOW_POSITION, flowPos); 

			double freq = flowPosToFreq.get(flowPos);
			double seqProp =  freq / (double)(obsBelow + obsAt + obsAbove);

			flowSum+= (freq * flowPos);
			numSeqs += freq;

			double [] oldP = table.getProbabilities(probFactors, cycler);

			//double [] oldP = table.getProbabilities(modeVal, flowPos);		
			double [] rearrangedP = new double [] {oldP[OUFrequencyTable.EQUAL_TO], 
					oldP[OUFrequencyTable.LESS_THAN], oldP[OUFrequencyTable.GREATER_THAN]};		

			// It is weighted because sequences will be at different
			// positions in the flow. Therefore... the probabilities need to be weighted by the proportion of reads which are
			// at that flow segment.
			for(int i= 0; i < weightedP.length; i++)
			{
				weightedP[i] += (seqProp * rearrangedP[i]);
			}
		}

		probFactors = null; //clean up

		double avgFlow = flowSum / numSeqs;

		HypothesisTest ht = null;
		int minSize = 100;

		for(int i = 0; i < weightedP.length; i++)
		{
			int expected = (int) weightedP[i] * (obsAbove + obsBelow + obsAt);

			if(expected < minSize)
			{
				minSize = expected;
			}
		}

		//only way we could be doing 'worse' is if the binomial is less sensitive. 

		if(minSize < 5) //minimum size in any category is less than the golden number
		{					
			ht = new BinomialTest(obsAbove, obsBelow, obsAt, modeVal, weightedP, thresholdPValue, avgFlow, logger, verbose);
		}
		else
		{
			//	One-sided multinomial test of significance
			ht = new MultinomialOneSidedTest(obsAbove, obsBelow, obsAt, modeVal, weightedP, thresholdPValue, avgFlow, logger, verbose);
		}

		ht.runTest();
		return ht;
	}
	
}
