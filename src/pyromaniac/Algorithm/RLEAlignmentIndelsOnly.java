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
import java.util.Set;

import pyromaniac.AcaciaEngine;
import pyromaniac.DataStructures.MutableInteger;
import pyromaniac.DataStructures.Pair;
import pyromaniac.DataStructures.Pyrotag;
import pyromaniac.IO.AcaciaLogger;

public class RLEAlignmentIndelsOnly 
{
	
	/** The head. */
	private AlignmentColumn head;
	
	/** The logger. */
	private AcaciaLogger logger;
	
	/** The max recurse depth. */
	private int maxRecurseDepth;
	
	/** The STAR t_ col. */
	private static AlignmentColumn START_COL = null;
	
	/** The truncate from. */
	private AlignmentColumn truncateFrom; //this column indicates the last column we want to consider in the alignment, due to low coverage.
	
	//how to know when to truncate at??
	
	/**
	 * Gets the truncate from.
	 *
	 * @return the truncate from
	 */
	public AlignmentColumn getTruncateFrom() 
	{
		return truncateFrom;
	}

	/**
	 * Sets the truncate from.
	 *
	 * @param truncateFrom the new truncate from
	 */
	public void setTruncateFrom(AlignmentColumn truncateFrom) 
	{
		this.truncateFrom = truncateFrom;
	}

	/**
	 * Instantiates a new threaded alignment.
	 *
	 * @param consensusSeed the consensus seed
	 * @param logger the logger
	 * @param maxRecurseDepth the max recurse depth
	 */
	public RLEAlignmentIndelsOnly(String consensusSeed, AcaciaLogger logger, int maxRecurseDepth)
	{
		this.head = new AlignmentColumn(' ');
		this.logger = logger;
		this.maxRecurseDepth = maxRecurseDepth; 
		this.truncateFrom = null;
		
		if(START_COL == null)
		{
			START_COL = new RLEAlignmentIndelsOnly.AlignmentColumn('$');
		}
		
		AlignmentColumn curr = head;
		
		for(int i = 0; i < consensusSeed.length(); i++)
		{
			AlignmentColumn newChild = new AlignmentColumn(consensusSeed.charAt(i));
			newChild.setParent(curr);
			curr.setChild(newChild);
			curr = newChild;
		}
	}
	
	/**
	 * Gets the head column.
	 *
	 * @return the head column
	 */
	public AlignmentColumn getHeadColumn()
	{
		return this.head;
	}
	
	
	/**
	 * Instantiates a new threaded alignment.
	 */
	private RLEAlignmentIndelsOnly()
	{
		this.head = null;
	}
	
	
	/**
	 * Clone alignment with tags.
	 *
	 * @param p the p
	 * @return the threaded alignment
	 */
	public RLEAlignmentIndelsOnly cloneAlignmentWithTags(HashSet <Pyrotag> p)
	{
		try
		{
			RLEAlignmentIndelsOnly ta = new RLEAlignmentIndelsOnly();
			ta.head = this.head.cloneRecursivelyWithTags(p,null);
			ta.logger = this.logger;
			ta.maxRecurseDepth = this.maxRecurseDepth;
			
			return ta;
		}
		catch(Exception e)
		{
			System.out.println("logger had an exception");
		}
		return null;
	}
	
	/**
	 * Clone alignment with tags_truncate at.
	 *
	 * @param p the p
	 * @param truncateAt the truncate at
	 * @return the threaded alignment
	 */
	public RLEAlignmentIndelsOnly cloneAlignmentWithTags_truncateAt(HashSet <Pyrotag > p, AlignmentColumn truncateAt)
	{
		try
		{
			RLEAlignmentIndelsOnly ta = new RLEAlignmentIndelsOnly();
			ta.head = this.head.cloneRecursivelyWithTagsAndTruncateAt(p,null, truncateAt);
			ta.logger = this.logger;
			ta.maxRecurseDepth = this.maxRecurseDepth;
			return ta;
		}
		catch(Exception e)
		{
			System.out.println("logger had an exception");
		}
		return null;
	}
	

	/**
	 * Gets the all tags.
	 *
	 * @return the all tags
	 */
	public HashSet<Pyrotag> getAllTags()
	{
		HashSet <Pyrotag> allTags = new HashSet <Pyrotag>();
		if (this.head.child == null)
			return allTags;
		
		
		
		//done!
		allTags.addAll(this.head.child.getTags());
		allTags.addAll(this.head.child.getInsertionTags());
		
		return allTags;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
			
		AlignmentColumn curr = this.head;
		while(curr != null)
		{
			sb.append(curr.toString());
			curr = curr.child;
		}
		return sb.toString();
	}
	
	/**
	 * Align.
	 *
	 * @param p the p
	 * @param verbose 
	 * @return true, if successful
	 * @throws Exception the exception
	 */
	public boolean align(Pyrotag p, boolean verbose) throws Exception
	{		
		try
		{
			AlignmentColumn nodeBeforeLastMismatch = null;		
			AlignmentColumn curr = this.head.child;
			AlignmentColumn lastDefined = this.head;
			
			LinkedList <PendingChange> pendingChanges = new LinkedList <PendingChange>();			
			SearchObject soStart = new SearchObject(curr, lastDefined, nodeBeforeLastMismatch, pendingChanges, p, 0, this, this.logger, 0, verbose);			
			
			boolean successful =  soStart.run();
		
			if(successful)
			{			
				for(PendingChange pc : soStart.pendingChanges)
				{	
					pc.performModification();
				}
				return true;
			}
			else
			{
				return false;
			}
		}
		catch(Exception e)
		{
			logger.writeLog("An exception occurred", AcaciaLogger.LOG_DEBUG);
			System.exit(1);
		}
		return false;
	}

	/**
	 * Gets the longest consensus.
	 *
	 * @param tagsInCluster the tags in cluster
	 * @return the longest consensus
	 * @throws Exception the exception
	 */
	public Pair <String, HashMap <Pyrotag, MutableInteger >> getLongestConsensus(HashSet <Pyrotag> tagsInCluster) throws Exception
	{
		HashMap <Pyrotag, MutableInteger> cleanedTagLength = new HashMap <Pyrotag, MutableInteger>();
		StringBuilder consensus = new StringBuilder();
		AlignmentColumn curr = this.head;
		
		boolean verbose = false;
		
		while(curr != null)
		{
			char value = curr.getValue();
			if(value != ' ')
			{
				//curr = curr char, consensus   is our string builder, and cleaned tag length is our tags + length
				_helperGetLongestConsensus(curr, tagsInCluster, consensus, cleanedTagLength, verbose);
			}
			
			for(int i = 0; i < curr.insertionsAfter.length; i++)
			{
				if(curr.insertionsAfter[i] != null)
				{
					_helperGetLongestConsensus(curr.insertionsAfter[i], tagsInCluster, consensus, cleanedTagLength, verbose);
				}
			}
			curr = curr.child;
		}
		
		return new Pair <String, HashMap <Pyrotag, MutableInteger>>(consensus.toString(), cleanedTagLength);
	}
	
	/**
	 * _helper get longest consensus.
	 *
	 * @param curr the curr
	 * @param tagsInCluster the tags in cluster
	 * @param consensus the consensus
	 * @param cleanedTagLength the cleaned tag length
	 * @param verbose the verbose
	 * @return the int
	 * @throws Exception the exception
	 */
	public int _helperGetLongestConsensus(AlignmentColumn curr, HashSet <Pyrotag> tagsInCluster, StringBuilder consensus, 
			HashMap<Pyrotag, MutableInteger> cleanedTagLength, boolean verbose) throws Exception
	{
		char value = curr.getValue();
		int totalObs =  0;

		if(value != ' ')
		{
			int modeObs = -1 ;
			int modeLength = -1;
			
			HashMap <Integer, HashSet <Pyrotag>> hpToTags = curr.getHPLengthToTags(tagsInCluster);	
			
			for(Integer length: hpToTags.keySet())
			{
				totalObs += hpToTags.get(length).size();	
				if(hpToTags.get(length).size() > modeObs)
				{					
					modeLength = length;
					modeObs = hpToTags.get(length).size();
				}
			}
			
			//make sure we keep a record of where these sequences line up to .
			for(Integer length: hpToTags.keySet())
			{
				for(Pyrotag p: hpToTags.get(length))
				{
					if(!cleanedTagLength.containsKey(p))
					{
						cleanedTagLength.put(p, new MutableInteger(0)); //initialising
					}
					cleanedTagLength.get(p).add(modeLength);
				}
			}
			
			for(int i = 0; i < modeLength; i++)
			{
				consensus.append(value);
			}
		}
		
		return totalObs;
	}
	
	/**
	 * Split non conforming.
	 *
	 * @param nonConforming the non conforming
	 * @return the linked list
	 */
	public LinkedList<RLEAlignmentIndelsOnly> splitNonConforming(HashSet<HashSet<Pyrotag>> nonConforming) 
	{
		LinkedList <RLEAlignmentIndelsOnly> newAligns = new LinkedList <RLEAlignmentIndelsOnly>();
		
		for(HashSet <Pyrotag> tagSet : nonConforming)
		{
				newAligns.add(this.cloneAlignmentWithTags(tagSet));
		}
		
		return newAligns;
	}

	/**
	 * Correct read.
	 *
	 * @param p the p
	 * @param cluster the cluster
	 * @return the pair
	 */
	public Pair <String, Integer> correctRead(Pyrotag p, HashSet <Pyrotag> cluster) 
	{
		StringBuilder construction = new StringBuilder();
		MutableInteger position = new MutableInteger(0);
		MutableInteger numModifications = new MutableInteger(0);	
		AlignmentColumn curr = head; //no recursion
		
		while(curr != null)
		{
			curr.correctRead(p, cluster, construction, position, numModifications);
			curr = curr.child;
		}
		
		return new Pair <String, Integer> (construction.toString(), numModifications.value());
	}
	
	/**
	 * The Class SearchObject.
	 */
	protected class SearchObject
	{
		
		/** The curr. */
		private AlignmentColumn curr;
		
		/** The last defined. */
		private AlignmentColumn lastDefined;
		
		/** The node before last mismatch. */
		private AlignmentColumn nodeBeforeLastMismatch;
		
		/** The pending changes. */
		private LinkedList <PendingChange> pendingChanges;
		
		/** The p. */
		private Pyrotag p;
		
		/** The curr index. */
		private int currIndex;
		
		/** The successful. */
		private boolean successful;
		
		/** The ta. */
		private RLEAlignmentIndelsOnly ta;
		
		/** The logger. */
		private AcaciaLogger logger;
		
		/** The indent. */
		private String indent;
		
		/** The depth. */
		private int depth;
		
		/** The verbose. */
		private boolean verbose;
		

		/**
		 * Instantiates a new search object.
		 *
		 * @param curr the curr
		 * @param lastDefined the last defined
		 * @param nodeBeforeLastMismatch the node before last mismatch
		 * @param pendingChanges the pending changes
		 * @param p the p
		 * @param currIndex the curr index
		 * @param ta the ta
		 * @param logger the logger
		 * @param depth the depth
		 * @param verbose the verbose
		 */
		public SearchObject (AlignmentColumn curr, AlignmentColumn lastDefined, AlignmentColumn nodeBeforeLastMismatch, LinkedList <PendingChange> pendingChanges,
				Pyrotag p, int currIndex, RLEAlignmentIndelsOnly ta, AcaciaLogger logger, int depth, boolean verbose)
		{
			this.curr = curr;
			this.lastDefined = lastDefined;
			this.nodeBeforeLastMismatch = nodeBeforeLastMismatch;
			this.pendingChanges = pendingChanges;
			this.p = p;
			this.currIndex = currIndex;
			this.successful = true; //false when we hit a bad point
			this.ta = ta;
			this.logger = logger;
			this.verbose = verbose;
			this.depth = depth;
			
			StringBuilder indentB = new StringBuilder();
			
			for(int i = 0; i < this.depth; i++)
			{
				indentB.append(">");
			}
			
			this.indent = indentB.toString();

		}
		
		/**
		 * Run.
		 *
		 * @return true, if successful
		 * @throws Exception the exception
		 */
		public boolean run() throws Exception
		{
			char [] value = p.getProcessedString();

			boolean endOfReference = false;
			boolean continueProcessing = false;


			for(int i = this.currIndex; i < value.length; i++)
			{
				char currToken = value[i];

				int startPos = i; //need to re-use this start pos I think, for the deletion case.

				while(i < value.length && currToken == value[i])
				{
					i++;
				}

				int hpLength = i - startPos; 
				i--; //dont want to go too far forward.

				//double check this continue logic, could be whack

				//think of better choice for this.
				if(endOfReference) //this could be troublesome, assuming that this char is the most common.
				{
					AlignmentColumn newChild = new AlignmentColumn(currToken);
					newChild.obs.put(p, hpLength);
					newChild.setParent(lastDefined);
					lastDefined.setChild(newChild);
					lastDefined = newChild;
					continueProcessing = true;
					
					if(verbose)
						logger.writeLog(this.indent + "EOR: " + i + " curr token: " + currToken, AcaciaLogger.LOG_DEBUG);
					
				}
				else
				{

					//first things first, lets check for identity
					if(currToken == curr.getValue())
					{
						pendingChanges.add(new AddObservation(curr, p, hpLength)); //so I can roll back changes if necessary, if a failure occurs later in align.
						lastDefined = curr;
						curr = curr.child; //so this will be null if we are at the end.
						continueProcessing = true;
						
						if(verbose)
							logger.writeLog(this.indent + "IDENTITY: " + i + " curr token: " + currToken, AcaciaLogger.LOG_DEBUG);

					}
					else
					{
						//consecutive mismatches will not be tolerated.
						//well, this keeps throwing an exception, because curr.getParent() is null?
						
						if(verbose)
							logger.writeLog(this.indent + "Before test that throws exception", AcaciaLogger.LOG_DEBUG);
						
						if(nodeBeforeLastMismatch != null && nodeBeforeLastMismatch == curr.getParent())
						{
							if(verbose)
								logger.writeLog(this.indent + "CONSECUTIVE MISMATCH: " + i + " curr token: " + currToken, AcaciaLogger.LOG_DEBUG);
							
							this.successful = false;

							return false; //we will not tolerate TWO consecutive errors, so stick it!
						}
						
						if(verbose)
							logger.writeLog(this.indent + "After test that may throw exception", AcaciaLogger.LOG_DEBUG);

						//this is another good condition, as in, we are at the end of the consensus, and this sequence keeps going.
						if(curr.getChild() == null)
						{
							
							if(verbose)
								logger.writeLog(this.indent + "TERMINAL: " + i + " curr token: " + currToken, AcaciaLogger.LOG_DEBUG);

							
							AlignmentColumn newTerminal = new AlignmentColumn(currToken);
							newTerminal.obs.put(p, hpLength);
							endOfReference = true;
							
							curr.child = newTerminal;
							curr = newTerminal;
							lastDefined = newTerminal;
							newTerminal.parent = curr;
							continueProcessing = true;
							this.successful = true; //it must be true, since we're actually modifying the alignment object!
							
						}
						else
						{
							if(verbose)
								logger.writeLog(this.indent + " About to try insertion deletion", AcaciaLogger.LOG_DEBUG);
							
							//okay, we're not matching up, try looking ahead?
							SearchObject insertion = null;
							SearchObject deletion = null;
							
							if(verbose)
								logger.writeLog("Depth is? " + this.depth, AcaciaLogger.LOG_DEBUG);
							
							if(this.depth + 1 > this.ta.maxRecurseDepth)
							{
								if(verbose)
									logger.writeLog(this.indent + "MAX_DEPTH: " + i + " curr token: " + currToken, AcaciaLogger.LOG_DEBUG);
								
								this.successful = false;
								return false;
							}
	
							//insertion
							if(i + 1 < value.length && value[i + 1] == curr.getValue()) //
							{
								if(verbose)
									logger.writeLog(this.indent + "TRY INS: " + i + " curr token: " + currToken, AcaciaLogger.LOG_DEBUG);
								
								LinkedList <PendingChange> clonedPending = this.clonePendingChanges(); //clones all the pending changes
	
								nodeBeforeLastMismatch = curr.getParent();
								int newDepth = this.depth + 1;
								
								insertion = new SearchObject(curr, lastDefined, nodeBeforeLastMismatch, clonedPending, this.p, i + 1, 
										this.ta, this.logger, newDepth, this.verbose);
	
								boolean insAheadSuccessful = insertion.run();
								
								AlignmentColumn grandParent = curr.getParent();
	
								if(insAheadSuccessful)
								{
									if(verbose)
									{
										logger.writeLog(this.indent + "INS SUCCESSFUL: " + i + " curr token: " + currToken, AcaciaLogger.LOG_DEBUG);
										logger.writeLog(this.indent + "WHAT IS HAPPENING???", AcaciaLogger.LOG_DEBUG);
										if(grandParent != null)
										{
											logger.writeLog(this.indent + "Grandparent have insertion? " + (grandParent.getInsertionCorrespondingTo(currToken) != null), AcaciaLogger.LOG_DEBUG);
										}
										else
										{
											logger.writeLog(this.indent + "Grandparent is null.", AcaciaLogger.LOG_DEBUG);
										}
									}
										
									//copy the pending changes of 'insertion' 
									this.pendingChanges = insertion.pendingChanges; //double check no pending changes are lost!
	
									if(grandParent.getInsertionCorrespondingTo(currToken) != null)
									{
										if(verbose)
											logger.writeLog(this.indent + "INS EXISTED: " + i + " curr token: " + currToken, AcaciaLogger.LOG_DEBUG);
										
										AlignmentColumn insertionNode = grandParent.getInsertionCorrespondingTo(currToken);
										pendingChanges.add(new AddObservation(insertionNode, p, hpLength));
	
										nodeBeforeLastMismatch = grandParent;
									}
									else
									{	
									
										if(verbose)
										{
											logger.writeLog(this.indent + "INS UNIQUE: " + i + " curr token: " + currToken, AcaciaLogger.LOG_DEBUG);
										}
										
										//make an insertion node representing that insertion
										pendingChanges.add(new InsertAChild(this.ta, grandParent, p, currToken, hpLength));
										nodeBeforeLastMismatch = grandParent;
									}
	
									if(verbose)
									{
										logger.writeLog(this.indent + "RETURN TRUE, INS SUCCESS: " + i + " curr token: " + currToken, AcaciaLogger.LOG_DEBUG);
									}
									
									//we be done here.
									//all the results are sitting in pending changes.
									return true;//do we care that insertions are preferentially selected over deletions, no!	
								}
							}
	
							//insertion was not successful
							
							if(verbose)
								logger.writeLog(this.indent + "INS FAILED: " + i + " curr token: " + currToken, AcaciaLogger.LOG_DEBUG);
							
							if(curr.getChild().getValue() == value[i]) //clean deletion in the inserted sequence.
							{
								if(verbose)
									logger.writeLog(this.indent + "TRYING DEL: " + i + " curr token: " + currToken, AcaciaLogger.LOG_DEBUG);
								
								LinkedList <PendingChange> clonedPending = this.clonePendingChanges();
	
								//start pos was i before I started incrementing it to check out the HP length.
								
								int newDepth = this.depth + 1;
								
								deletion = new SearchObject(curr.getChild(), lastDefined, curr, clonedPending, this.p, startPos,
										this.ta, this.logger,newDepth,this.verbose);
								boolean successful = deletion.run();
	
								if(successful)
								{
									if(verbose)
										logger.writeLog(this.indent + "DEL SUCCESS:" + i + " curr token: " + currToken, AcaciaLogger.LOG_DEBUG);
									
									pendingChanges = deletion.pendingChanges; //keep these
									pendingChanges.add(new AddObservation(curr, p, 0));
									return true;
								}
							}
							
							if(verbose)
								logger.writeLog(this.indent + "DISCONTINUE PROCESSING: " + i + " curr token: " + currToken, AcaciaLogger.LOG_DEBUG);
							
							continueProcessing = false; //neither insertion or deletion worked.
						}
					}
					
					//at end of all alternatives...
					if(!continueProcessing) //only reason we keep going is end of reference?
					{
					if(verbose)
							logger.writeLog(this.indent + "FAILURE: " + i + " curr token: " + currToken, AcaciaLogger.LOG_DEBUG);
						
						this.successful = false;
						return false;
					}
				}
				
				if(curr == null)
				{
					endOfReference = true;
				}
			}
			//at end of 
			if(verbose)
				logger.writeLog(this.indent + "COMPLETE CONSUMED", AcaciaLogger.LOG_DEBUG);
			
			return true;
		}

		/**
		 * Clone pending changes.
		 *
		 * @return the linked list
		 */
		private LinkedList <PendingChange> clonePendingChanges() 
		{
				LinkedList <PendingChange> clone = new LinkedList <PendingChange>();
				clone.addAll(this.pendingChanges);
				return clone;
		}
	}
	

	
	
	
	/**
	 * The Class AlignmentColumn.
	 */
	public class AlignmentColumn
	{
		
		/** The obs. */
		HashMap <Pyrotag, Integer> obs;
		
		/** The value. */
		char value;
		
		/** The child. */
		AlignmentColumn child;
		
		/** The parent. */
		AlignmentColumn parent;
		
		/** The flow number. */
		int flowNumber;

		/** The insertions after. */
		AlignmentColumn [] insertionsAfter;

		/**
		 * Instantiates a new alignment column.
		 *
		 * @param value the value
		 */
		public AlignmentColumn(char value)
		{
			this.value = value;
			this.obs = new HashMap <Pyrotag, Integer>();
			this.parent = null;
			this.child = null;
			this.insertionsAfter = new AlignmentColumn []{null,null,null}; //will only accept an insertion of another base
			this.flowNumber = -1;
		}

		
		/**
		 * Clone recursively with tags.
		 *
		 * @param pToKeep the to keep
		 * @param newParent the new parent
		 * @return the alignment column
		 */
		public AlignmentColumn cloneRecursivelyWithTags(HashSet<Pyrotag> pToKeep, AlignmentColumn newParent) 
		{
			AlignmentColumn ac = new AlignmentColumn(this.value);
			ac.parent = newParent;

			if(newParent != null)
				newParent.child = ac;

			for(Pyrotag p : this.obs.keySet())
			{
				if(pToKeep.contains(p))
				{
					ac.obs.put(p, this.obs.get(p));
				}
			}

			ac.flowNumber = this.flowNumber;

			for(int i = 0; i < this.insertionsAfter.length; i++)
			{
				if(this.insertionsAfter[i] != null)
				{
					//f(ofInterest != null)
					//	System.out.print("I:");
					ac.insertionsAfter[i] = this.insertionsAfter[i].cloneWithTags(pToKeep);
				}
			}

			if(child != null)
			{
				this.child.cloneRecursivelyWithTags(pToKeep, ac);
			}
			
			return ac;
		}
		
		/**
		 * Clone recursively with tags and truncate at.
		 *
		 * @param pToKeep the to keep
		 * @param newParent the new parent
		 * @param truncateAt the truncate at
		 * @return the alignment column
		 */
		public AlignmentColumn cloneRecursivelyWithTagsAndTruncateAt(HashSet<Pyrotag> pToKeep, AlignmentColumn newParent, AlignmentColumn truncateAt)
		{
			//initially, this returns the head node. Should never truncate at the head...
			
			AlignmentColumn ac = new AlignmentColumn(this.value);
			ac.parent = newParent;

			if(newParent != null)
				newParent.child = ac;

			for(Pyrotag p : this.obs.keySet())
			{
				if(pToKeep.contains(p))
				{
					ac.obs.put(p, this.obs.get(p));
				}
			}

			ac.flowNumber = this.flowNumber;

			for(int i = 0; i < this.insertionsAfter.length; i++)
			{
				if(this.insertionsAfter[i] != null)
				{
					//f(ofInterest != null)
					//	System.out.print("I:");
					ac.insertionsAfter[i] = this.insertionsAfter[i].cloneWithTags(pToKeep);
				}
			}

			//handles the case of truncate at.
			if(child != null && child != truncateAt)
			{
				this.child.cloneRecursivelyWithTags(pToKeep, ac);
			}
			
			return ac;
		}

		/**
		 * Clone with tags.
		 *
		 * @param pToKeep the to keep
		 * @return the alignment column
		 */
		public AlignmentColumn cloneWithTags(HashSet <Pyrotag> pToKeep)
		{
			AlignmentColumn ac = new AlignmentColumn(this.value);
			for(Pyrotag p : this.obs.keySet())
			{
				if(pToKeep.contains(p))
				{
					ac.obs.put(p, this.obs.get(p));
				}
			}
			return ac;
		}


		/**
		 * Next insertion given last flow.
		 *
		 * @param lastFlow the last flow
		 * @return the alignment column
		 * @throws Exception the exception
		 */
	/*
		public AlignmentColumn nextInsertionGivenLastFlow(char lastFlow) throws Exception
		{
			char curr = lastFlow;
			do
			{
				for(int i = 0; i < insertionsAfter.length; i++)
				{
					if(insertionsAfter[i] != null && insertionsAfter[i].value == curr)
					{
						return insertionsAfter[i];
					}	
				}
				
				if(! Pyrotag.get.containsKey(curr))
				{
					throw new Exception("Curr has invalid char: " + curr);
				}

				curr = Pyrotag.flowCycle.get(curr); //moves forward
			}while(curr != lastFlow);

			return null;
		}
		*/
		

		/**
		 * Sets the flow number.
		 *
		 * @param flowNumber the new flow number
		 */
		public void setFlowNumber(int flowNumber)
		{
			this.flowNumber = flowNumber;
		}

		/**
		 * Gets the tags.
		 *
		 * @return the tags
		 */
		public Set <Pyrotag> getTags() 
		{
			return this.obs.keySet();
		}

		/**
		 * Gets the insertion tags.
		 *
		 * @return the insertion tags
		 */
		public Set <Pyrotag> getInsertionTags()
		{
			HashSet <Pyrotag> insertTags = new HashSet <Pyrotag>();

			for(int i = 0; i < this.insertionsAfter.length; i++)
			{
				if(this.insertionsAfter[i] != null)
				{
					for(Pyrotag p: this.insertionsAfter[i].obs.keySet())
					{
						insertTags.add(p);
					}
				}
			}
			return insertTags;
		}

		/**
		 * Sets the parent.
		 *
		 * @param newParent the new parent
		 */
		public void setParent(AlignmentColumn newParent)
		{
			this.parent = newParent;
		}

		/**
		 * Gets the parent.
		 *
		 * @return the parent
		 */
		public AlignmentColumn getParent()
		{
			return this.parent;
		}

		/**
		 * Sets the child.
		 *
		 * @param newChild the new child
		 */
		public void setChild(AlignmentColumn newChild)
		{
			this.child = newChild;
		}

		/**
		 * Adds the inserted.
		 *
		 * @param insertionAfterThis the insertion after this
		 * @throws Exception the exception
		 */
		public void addInserted(AlignmentColumn insertionAfterThis) throws Exception
		{
			for(int i = 0; i < this.insertionsAfter.length; i++)
			{
				if(this.insertionsAfter[i] == null)
				{
					this.insertionsAfter[i] = insertionAfterThis;
					return;
				}
			}

			throw new Exception("Node could not be added");
		}

		/**
		 * Gets the insertion corresponding to.
		 *
		 * @param c the c
		 * @return the insertion corresponding to
		 */
		public AlignmentColumn getInsertionCorrespondingTo(char c)
		{
			for(int i = 0; i < this.insertionsAfter.length; i++)
			{
				if(this.insertionsAfter[i]!= null && this.insertionsAfter[i].value == c)
					return this.insertionsAfter[i];
			}
			return null;
		}

		/**
		 * Gets the child.
		 *
		 * @return the child
		 */
		public AlignmentColumn getChild()
		{
			return this.child;
		}

		/**
		 * Gets the value.
		 *
		 * @return the value
		 */
		public char getValue()
		{
			return this.value;
		}

		/**
		 * Gets the observations.
		 *
		 * @return the observations
		 */
		public HashMap <Pyrotag, Integer> getObservations()
		{
			return this.obs;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString()
		{
			StringBuilder bd = new StringBuilder();
			bd.append("[");
			bd.append(this.value);
			bd.append(":");

			HashMap <Integer, HashSet <Pyrotag>> lengthToFreq = this.getHPLengthToTags(null);
			for(int length: lengthToFreq.keySet())
			{
				int freq = lengthToFreq.get(length).size();				
				bd.append(freq);
				bd.append("@");
				bd.append(length);
				bd.append(",");		
			}		

			bd.deleteCharAt(bd.length() - 1);


			bd.append("<");
			boolean insertExists =false;
			for(int i = 0; i < this.insertionsAfter.length; i++)
			{
				if(this.insertionsAfter[i] != null)
				{
					bd.append(this.insertionsAfter[i].toString());
					insertExists = true;
				}
			}

			if(!insertExists)
			{
				bd.deleteCharAt(bd.length() - 1);
			}
			else
			{
				bd.append(">");
			}
			bd.append("]");

			return bd.toString();
		}

		/**
		 * Gets the hP length to tags.
		 *
		 * @param ofInterest the of interest
		 * @return the hP length to tags
		 */
		public HashMap <Integer, HashSet <Pyrotag>> getHPLengthToTags(HashSet<Pyrotag> ofInterest)
		{
			HashMap <Integer, HashSet <Pyrotag>> lengthToFreq = new HashMap <Integer, HashSet <Pyrotag>>();

			for(Pyrotag p: this.obs.keySet())
			{
				if(! (ofInterest == null || ofInterest.contains(p)))
				{
					continue;
				}
				
				Integer length = this.obs.get(p);

				if(! lengthToFreq.containsKey(length))
				{
					lengthToFreq.put(length, new HashSet <Pyrotag>());
				}

				lengthToFreq.get(length).add(p);
			}		
			return lengthToFreq;
		}

		/**
		 * Record zero for insertions before.
		 *
		 * @param toAdd the to add
		 */
		public void recordZeroForInsertionsBefore(Pyrotag toAdd) 
		{
			AlignmentColumn parent = this.getParent();

			if(parent != null)
			{
				for(int i = 0; i < parent.insertionsAfter.length; i++)
				{
					if(parent.insertionsAfter[i] != null)
					{
						if(!parent.insertionsAfter[i].obs.containsKey(toAdd)) //dont replace the observation!
							parent.insertionsAfter[i].obs.put(toAdd, 0);
					}
				}
			}
		}

		//try to avoid recursion, could be the reason for the lag.
		//boolean tells you if the job is complete.
		/**
		 * Correct read.
		 *
		 * @param p the p
		 * @param tagsInCluster the tags in cluster
		 * @param construction the construction
		 * @param position the position
		 * @param numModifications the num modifications
		 * @return true, if successful
		 */
		public boolean correctRead(Pyrotag p, HashSet <Pyrotag> tagsInCluster,StringBuilder construction, MutableInteger position, MutableInteger numModifications) 
		{
			char [] pStr = p.getCollapsedRead();

			if(this.value == ' ')
			{	
				return false; 
			}
			else if(position.value() >= pStr.length) //dont want to go longer than the read is
			{
				return true; 
			}
			else
			{
				char curr = pStr[position.value()];

				int modeObs = -1;
				int modeLength = -1;

				HashMap <Integer, HashSet <Pyrotag>> freq = this.getHPLengthToTags(tagsInCluster);

				int tagObsLength = -1;

				for(Integer length: freq.keySet())
				{
					if(freq.get(length).size() > modeObs)
					{
						modeLength = length;
						modeObs = freq.get(length).size();
					}

					if(freq.get(length).contains(p))
					{
						tagObsLength  = length;
					}
				}

				if(tagObsLength != modeLength)
				{
					numModifications.increment();
				}

				for(int i = 0; i < modeLength; i++)
				{	
					construction.append(curr);
				}

				if(modeLength > 0) //takes care of those one-base insertions
					position.increment();

				for(int i = 0; i < this.insertionsAfter.length; i++)
				{
					if(this.insertionsAfter[i] != null)
					{
						this.insertionsAfter[i].correctRead(p, tagsInCluster, construction, position, numModifications); //will return because they have no children.
					}
				}
				return false;
			}
		}
	}
	
	/**
	 * The Interface PendingChange.
	 */
	public interface PendingChange
	{
		
		/**
		 * Perform modification.
		 *
		 * @return true, if successful
		 */
		boolean performModification();
		
		/**
		 * To string.
		 *
		 * @return the string
		 */
		String toString();
	}
	
	/**
	 * The Class InsertAChild.
	 */
	private class InsertAChild implements PendingChange
	{
		
		/** The before insertion. */
		AlignmentColumn beforeInsertion;
		
		/** The p. */
		Pyrotag p;
		
		/** The inserted char. */
		char insertedChar;
		
		/** The obs length. */
		int obsLength;
		
		/** The ta. */
		RLEAlignmentIndelsOnly ta;
		
		/**
		 * Instantiates a new insert a child.
		 *
		 * @param ta the ta
		 * @param beforeInsertion the before insertion
		 * @param p the p
		 * @param insertedChar the inserted char
		 * @param obsLength the obs length
		 */
		public InsertAChild(RLEAlignmentIndelsOnly ta, AlignmentColumn beforeInsertion, Pyrotag p, char insertedChar, int obsLength)
		{
			this.beforeInsertion = beforeInsertion;
			this.p = p;
			this.insertedChar = insertedChar;
			this.obsLength = obsLength;
			this.ta = ta;
		}

		/* (non-Javadoc)
		 * @see pyromaniac.Algorithm.ThreadedAlignment.PendingChange#performModification()
		 */
		public boolean performModification() 
		{
			try
			{
				//perform all the stuff required to do an insertion node.

				AlignmentColumn insertionInQuery = new AlignmentColumn(insertedChar);
				HashSet <Pyrotag> allTags = ta.getAllTags();
			
				for(Pyrotag noInsert: allTags)
				{
					insertionInQuery.obs.put(noInsert, 0);
				}
				
				insertionInQuery.obs.put(p, this.obsLength);
				beforeInsertion.addInserted(insertionInQuery);
				
			}
			catch(Exception e)
			{
				System.out.println("An error occurred!!!");
			}
			return false;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString()
		{
			StringBuilder sb = new StringBuilder();
			sb.append("Insert a child after node: " + beforeInsertion);
			sb.append(", for pyrotag: " + p.getID());
			sb.append("Inserted char: " + insertedChar + " obs length: " + obsLength);
			return sb.toString();
		}
	}
	
	/**
	 * The Class AddObservation.
	 */
	private class AddObservation implements PendingChange
	{
		
		/** The to update. */
		AlignmentColumn toUpdate;
		
		/** The to add. */
		Pyrotag toAdd;
		
		/** The obs length. */
		int obsLength;
		
		/**
		 * Instantiates a new adds the observation.
		 *
		 * @param toUpdate the to update
		 * @param toAdd the to add
		 * @param obsLength the obs length
		 */
		public AddObservation(AlignmentColumn toUpdate, Pyrotag toAdd, int obsLength)
		{
			this.toUpdate = toUpdate;
			this.toAdd = toAdd;
			this.obsLength = obsLength;
		}
		
		/* (non-Javadoc)
		 * @see pyromaniac.Algorithm.ThreadedAlignment.PendingChange#performModification()
		 */
		public boolean performModification() 
		{
			try
			{
				this.toUpdate.obs.put(toAdd, obsLength);
				
				this.toUpdate.recordZeroForInsertionsBefore(toAdd);
				
				return true;
			}
			catch(Exception e)
			{	
				System.out.println("An exception occurred!!!");
				System.out.println(e.getMessage());
			}
			return false;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString()
		{
			StringBuilder sb = new StringBuilder();
			sb.append("AddObservation: " + this.toUpdate + ", value " + this.toUpdate.getValue() + " toAdd: " + toAdd.getID() + ", observation: " + this.obsLength);
			return sb.toString();
		}
	}
	
	/**
	 * Iterator.
	 *
	 * @return the iterator
	 */
	public Iterator <AlignmentColumn> iterator()
	{
		return new AlignmentIterator(this);
	}
	
	/**
	 * The Class AlignmentIterator.
	 */
	private class AlignmentIterator implements Iterator <AlignmentColumn>
	{
		
		/** The curr. */
		private AlignmentColumn curr;
		
		/** The it head. */
		private AlignmentColumn itHead;
		
		
		
		/**
		 * Instantiates a new alignment iterator.
		 *
		 * @param ta the ta
		 */
		public AlignmentIterator(RLEAlignmentIndelsOnly ta)
		{
			this.itHead = ta.head;
			curr = START_COL;
		}
		
		//I think in threaded alignment, it is assumed that the inner layer of ACs are processed.
		/* (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() 
		{
			if(curr == START_COL && itHead != null)
			{
				return true;
			}
			else if(curr.getChild() != null)
			{
				return true;
			}
			return false;
		}

		//completely skip the head node in here.
		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		public AlignmentColumn next() 
		{
			if(curr == START_COL)
			{
				curr = itHead;
			}
			else
			{
				this.curr = curr.getChild();
			}
			return curr;
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		public void remove() 
		{
			//I do nothing.
		}
	}


}
