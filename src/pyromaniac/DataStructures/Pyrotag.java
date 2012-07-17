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

package pyromaniac.DataStructures;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import pyromaniac.AcaciaConstants;
import pyromaniac.AcaciaEngine;
import pyromaniac.DataStructures.FlowCycler.CycleIterator;
import pyromaniac.IO.AcaciaLogger;

// TODO: Auto-generated Javadoc
/**
 * The Class Pyrotag.
 */
public class Pyrotag 
{
	/** The id. */
	private String id;
	
	/** The qualities. */
	private int [] qualities;
	
	/** The avg quality. */
	private double avgQuality;
	
	/** The nucleotides. */
	private char [] nucleotides;
	
	/** The description */
	private String desc;
	
	/** The mid. */
	private MIDPrimerCombo mid;
	
	/** The num ambiguous bases. */
	private int numAmbiguousBases;
	
	/** The multiplex tag length. */
	private int multiplexTagLength;
	
	/** The trim to length. */
	private int trimToLength;
	
	/** The internal id. */
	private int internalID;
	
	/** The Constant VALID_BASES_IN_SEQUENCE. */
	public static final String VALID_BASES_IN_SEQUENCE = "ATGCN";
	
	/** The constant FlowCycler*/
	private FlowCycler cycler;
	
	/** The Constant CYCLE_START. */
	public static final Character CYCLE_START = 'T';

	/** The Constant NO_TRIM. */
	public static final int NO_TRIM = -1;
	
	/** The Constant NO_TAG. */
	public static final int NO_TAG = -1;
	
	/** The Constant NO_QUALS. */
	public static final int NO_QUALS = -1;
	
	/** The Constant NO_CORRESPONDING_FLOW. */
	public static final int NO_CORRESPONDING_FLOW = -1;
	
	/** The Constant NO_N. */
	public static final int NO_N = -1;

	
	
	/**
	 * Instantiates a new pyrotag.
	 *
	 * @param id the id
	 * @param desc the desc
	 * @param nucleotideSeq the nucleotide seq
	 * @param qualitySeq the quality seq
	 */
	public Pyrotag(String id, String desc, Sequence <Character> nucleotideSeq, Sequence <Integer> qualitySeq, FlowCycler cycler)
	{
		this.id = id;
		this.desc = desc;
		this.numAmbiguousBases = 0;
		
		assert(nucleotideSeq != null);
		
		this.nucleotides = new char [nucleotideSeq.length()];
		
		for(int i = 0; i < nucleotideSeq.length(); i++)
		{
			this.nucleotides[i] = nucleotideSeq.getValueAtIndex(i);
			
			if(this.nucleotides[i] == 'N')
			{
				this.numAmbiguousBases++;
			}
		}
			
		this.qualities = null;
		this.avgQuality = NO_QUALS;
		
		if(qualitySeq != null)
		{
			this.qualities = new int [qualitySeq.length()];
		
			for(int i = 0; i < qualitySeq.length(); i++)
			{
				this.qualities[i] = qualitySeq.getValueAtIndex(i);
				this.avgQuality += this.qualities[i];
			}
		}
		
		if(qualitySeq != null)
			this.avgQuality = this.avgQuality / this.qualities.length;
		this.mid = null;
		this.trimToLength = NO_TRIM;
		this.multiplexTagLength = NO_TAG;
		this.cycler = cycler;
	}
	
	public final FlowCycler getFlowCycler()
	{
		return this.cycler;
	}
	
	
	/**
	 * Gets the untrimmed avg quality.
	 *
	 * @return the untrimmed avg quality
	 */
	public double getUntrimmedAvgQuality()
	{
		return this.avgQuality;
	}
	
	/**
	 * Sets the internal id.
	 *
	 * @param id the new internal id
	 */
	public void setInternalID(int id)
	{
		this.internalID = id;
	}
	
	/**
	 * Gets the internal id.
	 *
	 * @return the internal id
	 */
	public int getInternalID()
	{
		return this.internalID;
	}
	
	/**
	 * Sets the iD.
	 *
	 * @param id the new iD
	 */
	public void setID(String id)
	{
		this.id = id;
	}
	
	/**
	 * Sets the description.
	 *
	 * @param desc the new description
	 */
	public void setDescription(String desc)
	{
		this.desc = desc;
	}
	
	/**
	 * Sets the multiplex tag.
	 *
	 * @param midPrimer the new mID primer combo
	 */
	public void setMIDPrimerCombo(MIDPrimerCombo midPrimer)
	{
		this.mid = midPrimer;
		
		this.setMultiplexTagLength(midPrimer.getMIDPrimerSequence().length()); //mid and primer sequence.		
	}
	
	/**
	 * First occurrence of ambiguous base, should return the flow position.
	 *
	 * @return the int
	 */
	public int firstOccurrenceOfAmbiguous()
	{
		for(int i = 0; i < this.nucleotides.length; i++)
		{
			if(this.nucleotides[i] == 'N' || this.nucleotides[i] == 'n')
			{
				//base position
				
				return i;
			}
		}
		
		return NO_N;
	}
	
	
	//flows from zero.
	//assume you want first position after key, and MID, if present.
	/**
	 * Gets the flow for collapsed read pos.
	 *
	 * @param key the key
	 * @param nucPos the nuc pos
	 * @return the flow for collapsed read pos
	 * @throws NFoundAtReadStartException 
	 */
	
	//there has got to be a cleaner way??
	
	public int [] getFlowPositionForCallPriorToCollapsedReadStart(String key)
	{
		CycleIterator it = this.cycler.iterator();
		return _getFlowPositionForCallPriorToCollapsedReadStart(key, it);
	}
	
	public int [] getFlowPositionForCollapsedReadPosition(String key, int nucPos)throws ReadWithZeroLengthException
	{
		CycleIterator it = this.cycler.iterator();
		
		int [] lastFlow =  _getFlowPositionForCallPriorToCollapsedReadStart(key, it);
		char [] collapsed = this.getCollapsedRead(); //doesn't have the MID or primer in it. 
	
		if(collapsed.length == 0)
		{
			throw new ReadWithZeroLengthException(this);
		}
		
		int [] lastCalledFlow = lastFlow; //last called flow from MID or the key.		
		for(int i = 0; i <= nucPos && i < collapsed.length; i++)
		{
			//the first base in collapsed, 
			while(collapsed[i] != lastFlow[FlowCycler.FLOWED_BASE])
			{					
				if(collapsed[i] == 'N')
				{
					do
					{
						lastFlow = it.next();
					}
					while(lastFlow[FlowCycler.FLOWED_BASE] != lastCalledFlow[FlowCycler.FLOWED_BASE]); //for 454.
					break;
				}
				else
				{
					lastFlow = it.next();
				}
			}
	
			lastCalledFlow = lastFlow;
		}
		
		return lastFlow;
	}
	
	private int [] _getFlowPositionForCallPriorToCollapsedReadStart(String key, CycleIterator it)
	{
		int [] lastFlow = it.next(); //starts it off.
		
		//previous is at the beginning of the key, move through the key now.
		for(int i = 0; i < key.length(); i++)
		{
			char curr = key.charAt(i); 
			while(lastFlow[FlowCycler.FLOWED_BASE] != curr) 
			{
				lastFlow = it.next();
			}
		}
				
		//account for the MID we clipped off, but are we clipping off MID only or MID AND PRIMER
		if(this.mid != null && this.mid.getMIDPrimerSequence().length() > 0)
		{
			//lastFlow = flowsBetweenLastFlowAndSeqStart(it, mid.MID);	
			//process the mid
			String midPrimer = mid.getMIDPrimerSequence(); //we require a perfect match in the MID and primer.
			for(int i = 0; i < midPrimer.length(); i++)
			{
				char curr = this.nucleotides[i];
				
				while(lastFlow[FlowCycler.FLOWED_BASE] != curr)
				{
					lastFlow = it.next();
				}
			}
		}
		return lastFlow;
	}
	
	
	//old
	/*
	public int [] getFlowForCollapsedReadPos(String key, int nucPos) throws NFoundAtReadStartException, Exception
	{
		CycleIterator it = this.cycler.iterator();

		int [] lastFlow = it.next(); //starts it off.
		
		//previous is at the beginning of the key, move through the key now.
		for(int i = 0; i < key.length(); i++)
		{
			char curr = key.charAt(i); 
			while(lastFlow[FlowCycler.FLOWED_BASE] != curr) 
			{
				lastFlow = it.next();
			}
		}
				
		//account for the MID we clipped off, but are we clipping off MID only or MID AND PRIMER
		if(this.mid != null && this.mid.getMIDPrimerSequence().length() > 0)
		{
			//lastFlow = flowsBetweenLastFlowAndSeqStart(it, mid.MID);	
			//process the mid
			String midPrimer = mid.getMIDPrimerSequence(); //we require a perfect match in the MID and primer.
			for(int i = 0; i < midPrimer.length(); i++)
			{
				char curr = this.nucleotides[i];
				
				while(lastFlow[FlowCycler.FLOWED_BASE] != curr)
				{
					lastFlow = it.next();
				}
			}
		}

		char [] collapsed = this.getCollapsedRead(); //doesn't have the MID or primer in it. 
		
		if(collapsed.length == 0)
		{
			//TODO: check outside this function that it is empty? Or throw empty string exception?	
			System.out.println(this.id);
			System.out.println("trim to Length" + this.trimToLength);
			System.out.println("multiplex tag length "+ this.multiplexTagLength);
		}
		
		int currentPosition = 0; //currentPosition in collapsed
		
		//1. how do we want to handle initial N's.
		if(collapsed[currentPosition] == 'N')
		{
			throw new NFoundAtReadStartException(this);
		}

		
		int [] lastCalledFlow = lastFlow; //last called flow from MID or the key.
		
		for(int i = 0; i <= nucPos && i < collapsed.length; i++)
		{
			//the first base in collapsed, 
			while(collapsed[i] != lastFlow[FlowCycler.FLOWED_BASE])
			{					
				if(collapsed[i] == 'N')
				{
					do
					{
						lastFlow = it.next();
					}
					while(lastFlow[FlowCycler.FLOWED_BASE] != lastCalledFlow[FlowCycler.FLOWED_BASE]); //for 454.
				}
				else
				{
					lastFlow = it.next();
				}
			}
	
			lastCalledFlow = lastFlow;
		}
		
		return lastFlow;
	}
	
	*/
	
	
	/**
	 * Flows between last flow and seq start. Inclusive of seq start flow
	 *
	 * @param lastFlow the last flow
	 * @param seq the seq
	 * @return the int
	 */
	
	/*
	public int flowsBetweenLastFlowAndSeqStart(FlowCycler.CycleIterator it, String seq)
	{
		
		
		
		return flowsBetweenLastFlowAndChar(lastFlow, seq.charAt(0));
	}
	*/
	
	
	/**
	 * Flows between last flow and char.
	 *
	 * @param lastFlow the last flow
	 * @param start the start
	 * @return the int
	 */
/*	public int flowsBetweenLastFlowAndChar(Character lastFlow, Character start)
	{
		Character curr = lastFlow;
		Character seqFirst = start;
		
		int offset = 0;
	
		try
		{
			while(! curr.equals(seqFirst))
			{
				if(seqFirst == null || !flowCycle.containsKey(seqFirst.charValue()))
				{
					throw new Exception("Illegal character in sequence: " + seqFirst.charValue() + ", " + this.getID());
				}
			
				curr = flowCycle.get(curr);
				
				if(curr == null)
				{
					throw new Exception("Curr was null: " + seqFirst.charValue());
				}	
				offset++;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println(e.getMessage());
			System.exit(1);
		}
		
		return offset;
	}
	
	*/
	
	/**
	 * Flows between seq start and seq end.
	 *
	 * @param seq the seq
	 * @return the int
	 * @throws NFoundAtReadStartException 
	 */
/*	private int flowsBetweenSeqStartAndSeqEnd(String seq)
	{
		int flowOffset = 0;
		int seqIndex = 0;
		
		Character prev = seq.charAt(seqIndex);
		seqIndex++;
		
		while(seqIndex < seq.length())
		{
			Character curr = seq.charAt(seqIndex);
			while(!prev.equals(curr))
			{
				prev = flowCycle.get(prev); 
				flowOffset++;
			}
			seqIndex++;
		}
		return flowOffset;
	}
	*/
	
	public int flowPosToBasePos (int flowPos, String key) throws NFoundAtReadStartException
	{
		CycleIterator it = this.cycler.iterator();
		
		int [] lastFlow = it.next();
	
		
		//previous is at the beginning of the key, move through the key now.
		//all reads go through the key
		for(int i = 0; i < key.length(); i++)
		{
			char curr = key.charAt(i); 
			while(lastFlow[FlowCycler.FLOWED_BASE] != curr) //does this automagically cast to char?
			{
				lastFlow = it.next();
			}
		}
		
		//now for the MID
		if(this.mid != null && this.mid.MID.length() > 0)
		{
			//process the mid
			
			for(int i = 0; i < mid.MID.length(); i++)
			{
				char curr = mid.MID.charAt(i);
				while(lastFlow[FlowCycler.FLOWED_BASE] != curr)
				{
					lastFlow = it.next();
				}
			}
		}
		else
		{
			//do nothing. 
			//if you take the MIDS and primer away... there needs to be a default position?
		}	
		
		//base position only corresponds to what is represented in the collapsed string
		//collapsed string has the MID and PRIMER removed, and is trimmed.
		
		char [] collapsed = this.getCollapsedRead();
		
		if(collapsed[0] == 'N')
		{
			throw new NFoundAtReadStartException(this);
		}
		
		int [] lastCalledFlow = lastFlow;
		
		for(int i = 0; i < collapsed.length; i++)
		{
			if(lastFlow[FlowCycler.FLOW_POSITION] >= flowPos)
			{
				return i - 1; //last called base
			}
				
			while(collapsed[i] != lastFlow[FlowCycler.FLOWED_BASE])
			{
				if(collapsed[i] == 'N')
				{
					do
					{
						lastFlow = it.next();
					}
					while(lastFlow[FlowCycler.FLOWED_BASE] != lastCalledFlow[FlowCycler.FLOWED_BASE]);
				}
				else
				{
					lastFlow = it.next();
				}
			}
			lastCalledFlow = lastFlow;
		}
		
		return collapsed.length - 1; //last base is this. 
		
	}
	
	
	/**
	 * Flow to base pos.
	 *
	 * @param flowPos the flow pos
	 * @param key the key
	 * @return the int
	 */
	/*
	public int flowToBasePos(int flowPos, String key)
	{
		int pos = flowsBetweenLastFlowAndSeqStart(Pyrotag.CYCLE_START, key); //incase the key does not start with the first nucleotide
		
		if(pos == 0)
		{
			pos = 1; //should begin at 1.
		}
		
		char prev = key.charAt(0);

		for(int i = 1; i < key.length(); i++)
		{
			char curr = key.charAt(i);
			
			while(prev != curr)
			{
	//			System.out.println("Prev: " + prev);
				pos++;				
				prev = flowCycle.get(prev);					
			}
		}
		
		for(int i= 0; i < nucleotides.length; i++)
		{
			while(prev != nucleotides[i])
			{
				pos++;
//				System.out.println("Nucleotide: " + prev + " flow " + pos);
				
				prev = flowCycle.get(prev);				
				if(pos >= flowPos)
				{
					return i; //
				}
			}
//			System.out.println("Nucleotide: " + prev + " flow " + pos);
		}
		return Pyrotag.NO_CORRESPONDING_FLOW;
	}
	*/
	
	/**
	 * Sets the multiplex tag length.
	 *
	 * @param length the new multiplex tag length
	 */
	private void setMultiplexTagLength(int length)
	{
		this.multiplexTagLength = length;
	}
	
	/**
	 * Gets the num ambiguous bases.
	 *
	 * @return the num ambiguous bases
	 */
	public int getNumAmbiguousBases() 
	{
		return numAmbiguousBases;
	}
	
	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription()
	{
		return this.desc;
	}

	/**
	 * Gets the qualities.
	 *
	 * @return the qualities
	 */
	public int [] getQualities()
	{
		return this.qualities;
	}
	
	/**
	 * Trim using min quality.
	 *
	 * @param minQuality the min quality
	 */
	public void trimUsingMinQuality(int minQuality)
	{
		//will set the trimLength..
	}
	
	/**
	 * Gets the tag.
	 *
	 * @return the tag
	 */
	public char [] getTag()
	{
		if(this.multiplexTagLength != NO_TAG)
		{
			char [] tag = new char [this.multiplexTagLength];
			
			for(int i = 0; i < tag.length; i++)
			{
				tag[i] = this.nucleotides[i];
			}
			
			return tag;
		}
		return null;
	}
	
	
	/**
	 * Gets the base.
	 *
	 * @param index the index
	 * @return the base
	 */
	public char getBase(int index)
	{
		assert(index < nucleotides.length);
		return nucleotides[index];
	}
	
	/**
	 * Gets the length.
	 *
	 * @return the length
	 */
	public int getLength()
	{
		return this.nucleotides.length;
	}
	
	/**
	 * Gets the quality.
	 *
	 * @param index the index
	 * @return the quality
	 */
	public int getQuality(int index)
	{
		assert(index < qualities.length);
		return qualities[index];
	}
	
	/**
	 * Gets the read string.
	 *
	 * @return the read string
	 */
	public char [] getReadString()
	{
		return this.nucleotides;
	}
	
	
	//changing this code to trim to a given length, ignoring the presence/absence of a MID.
	
	/**
	 * Gets the processed string.
	 *
	 * @return the processed string
	 */
	public char [] getProcessedString()
	{
		char [] processed;
		
		if(this.trimToLength == NO_TRIM && this.multiplexTagLength == NO_TAG)
		{
			processed = new char [this.nucleotides.length];
			
			for(int i = 0; i < processed.length; i++)
			{
				processed[i] = Character.toUpperCase(this.nucleotides[i]);
			}
			return processed;
		}

		
		int start = (this.multiplexTagLength == NO_TAG)? 0 : this.multiplexTagLength;		
		int end = this.nucleotides.length; //default for now.
		
		if(this.trimToLength != NO_TRIM)
		{
			if(this.trimToLength < start)
			{
				end = start;
			}
			else if (this.trimToLength > this.nucleotides.length)
			{
				end = this.nucleotides.length;
			}
			else
			{
				end = this.trimToLength;
			}
		}	

		char [] processedString = new char [end - start];
		
		for(int i = start; i < end; i++)
		{
			processedString[i - start] = Character.toUpperCase(this.nucleotides[i]);
		}
		
		return processedString;
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		int width = 20;
		
		sb.append(this.id + " " + this.desc);
		sb.append(System.getProperty("line.separator"));
		
		int i = 0;
		
		while((i + width) <= nucleotides.length)
		{
			sb.append(nucleotides, i, width);
			sb.append(System.getProperty("line.separator"));
			
			
			if(qualities != null)
			{
				for(int j = i; j < (i + width); j++)
				{
					sb.append(qualities[j]);
					if(j + 1 < (i + width))
					{
						sb.append(" ");
					}
				}
				sb.append(System.getProperty("line.separator"));	
			}
			
			
			i+= width;
			if((i + width - 1) >= nucleotides.length)
			{
				width = nucleotides.length - i;
				
				if(width == 0)
				{
					break;
				}
			}
		}
		return sb.toString();
	}
	
	/**
	 * To fasta.
	 *
	 * @return the string
	 */
	public String toFASTA()
	{
		char [] processedStr = this.getProcessedString();
		StringBuffer sb = new StringBuffer();
		int width = 80;
		
		sb.append(">" + this.id + " " + this.desc);
		sb.append(System.getProperty("line.separator"));
		
		sb.append(new String(processedStr));
		sb.append(System.getProperty("line.separator"));
		
		/*
		for(int i = 0; (i + width) < processedStr.length; i+= width)
		{
			sb.append(processedStr, i, width);
			sb.append(System.getProperty("line.separator"));
					
			
			if(i + width > processedStr.length)
			{
				width = processedStr.length - i;
			}
		}
		*/
		return sb.toString();
		
	}
	

	/**
	 * Gets the iD.
	 *
	 * @return the iD
	 */
	public String getID() 
	{
		return this.id;
	}

	/**
	 * Checks for ns.
	 *
	 * @return true, if successful
	 */
	public boolean hasNs()
	{
		for(int i = 0; i < this.nucleotides.length; i++)
		{
			if(nucleotides[i] == 'N')
				return true;
		}
		return false;
	}
	
	
	/**
	 * Gets the trimmed average quality.
	 *
	 * @return the trimmed average quality
	 */
	public int getTrimmedAverageQuality() 
	{
			if(this.qualities == null)
			{
				return NO_QUALS;
			}
		
			int posStart = (this.multiplexTagLength == NO_TAG)? 0 : this.multiplexTagLength;
			int posEnd = (this.trimToLength == NO_TRIM)? this.nucleotides.length: this.trimToLength;
			
			int currPos = posStart;
			int sum = 0;
			while(currPos < posEnd)
			{
				sum += this.qualities[currPos];
				currPos++;
			}
			
			int average = (sum / (posEnd - posStart + 1));
			return average;
	}
	
	/**
	 * To flow gram.
	 *
	 * @param flowCycle the flow cycle
	 * @param flowStart the flow start
	 * @param numFlows the num flows
	 * @param key the key
	 * @return the int[]
	 */
	public int [] toFlowGram(HashMap <Character, Character> flowCycle, char flowStart, int numFlows, String key)
	{
		int [] flowGram = new int [numFlows * 4];
		
		int actualFlowPos = 0;
		char currNucleotide = flowStart;
		
		//assuming the key is not present, dont care about the hp counts or anything.
		for(int i = 0; i < key.length(); i++)
		{
			while(currNucleotide !=  key.charAt(i))
			{
				currNucleotide = flowCycle.get(currNucleotide);
				actualFlowPos++;
			}
		}
		actualFlowPos++; //bring us up to position four.
		currNucleotide = flowCycle.get(currNucleotide);
		
		for(int i = 0; i < flowGram.length; i++)
		{
			flowGram[i] = 0;
		}
		
		//last base was currNucleotide
		
		//similarly
		
		int offset = 0;
		for(int i = 0; i < this.nucleotides.length; i++)
		{	
			while(currNucleotide != this.nucleotides[i])
			{
				offset++;
				currNucleotide = flowCycle.get(currNucleotide);
			}
			
			int j = i + 1;
			
			while(j < this.nucleotides.length && this.nucleotides[j] == this.nucleotides[i])
			{
				j++;
			}
			
			flowGram[offset] = j - i;
			i = j - 1;
		}
		
		int [] flowGramShort = new int [offset];
		
		for(int i = 0; i < flowGramShort.length; i++)
		{
			flowGramShort[i] = flowGram[i];
		}
		
		return flowGramShort;
	}
	
	//MIDoffset is first base afterMID
	/**
	 * Gets the collapsed read.
	 *
	 * @return the collapsed read
	 */
	public char [] getCollapsedRead()
	{
		char [] collapsed = new char [this.nucleotides.length - this.multiplexTagLength];
		
		int indexCollapsed = 0;
		char prev = 'z';
		
		int maxPos = (this.trimToLength != Pyrotag.NO_TRIM)? ((this.nucleotides.length < this.trimToLength)? this.nucleotides.length : this.trimToLength): this.nucleotides.length;
		
		
		for(int i = this.multiplexTagLength; i < maxPos; i++)
		{
			if(prev != this.nucleotides[i])
			{
				collapsed[indexCollapsed] = this.nucleotides[i];
				indexCollapsed++;
			}
			prev = this.nucleotides[i];
		}
		
		return Arrays.copyOf(collapsed, indexCollapsed);
	}
	
	/**
	 * Gets the trim to length.
	 *
	 * @return the trim to length
	 */
	public int getTrimToLength() 
	{
		return this.trimToLength;
	}

	/**
	 * Sets the trim to length.
	 *
	 * @param trimToLength the new trim to length
	 */
	public void setTrimToLength(int trimToLength) 
	{
		this.trimToLength = trimToLength;
		
		if(this.nucleotides.length > trimToLength && trimToLength > 0)
		{
			_trimRead();
		}
	}

	//TODO: add this function, see how it goes
	
	/**
	 * _trim read.
	 */
	private void _trimRead()
	{
		char [] trimmedRead = Arrays.copyOf(nucleotides, this.trimToLength);
		
		this.nucleotides = trimmedRead;
		
		if(this.qualities != null)
		{
			int [] trimmedQualities = Arrays.copyOf(qualities, this.trimToLength);
			this.qualities = trimmedQualities;
		}
	}
	
	
	/**
	 * Which mid.
	 *
	 * @param validTags the valid tags
	 * @return the mID
	 */
	public MIDPrimerCombo whichMID(LinkedList <MIDPrimerCombo> validTags)
	{
		HashMap <MIDPrimerCombo, Boolean> notMatched = new HashMap <MIDPrimerCombo, Boolean>();
		
		int endPos = -1;
		
		for(int i = 0; i < this.nucleotides.length; i++)
		{
			boolean anyThisLong = false;
			for(MIDPrimerCombo midObj: validTags)
			{
				String midPrimerSeq = midObj.MID + midObj.primer;
				
				if(midPrimerSeq.length() > i)
				{
					char [] acceptableSubs = {midPrimerSeq.charAt(i)}; //assume that there are NO substitutions applicable.
					if(AcaciaConstants.IUPAC_AMBIGUOUS_MAPPINGS.containsKey(midPrimerSeq.charAt(i))) //have a wobble
					{
						acceptableSubs = AcaciaConstants.IUPAC_AMBIGUOUS_MAPPINGS.get(midPrimerSeq.charAt(i));
					}
					
					boolean anyMatched = false;
						
					for(char possBase : acceptableSubs)
					{
		//				System.out.println("Comparing to possible base: " + possBase);
						if(possBase == this.nucleotides[i])
						{
							anyMatched = true;
						}
					}
						
					if(! anyMatched)
					{
						notMatched.put(midObj, false);
						endPos = i;
					}
					else
					{
						anyThisLong = true;
					}
				}
			}
			if(! anyThisLong)
				break;
		}
		
		for(MIDPrimerCombo mid: validTags)
		{
			if(! notMatched.containsKey(mid))
				return mid;
		}
		return null;
	}

	/**
	 * Gets the multiplex tag.
	 *
	 * @return the multiplex tag
	 */
	public MIDPrimerCombo getMultiplexTag() 
	{
		return this.mid;
	}
	
	
//this is fine, but if I ever allowed non valid MID sequences to use this function, it would not check whether there were wobbles in the MID.
	
	/**
 * Checks for wobble in processed string.
 *
 * @return true, if successful
 */
public boolean hasWobbleInProcessedString() 
	{
		char [] processedString = this.getProcessedString();
		
		for(char curr : processedString)
		{
			if(VALID_BASES_IN_SEQUENCE.indexOf(Character.toUpperCase(curr)) == -1)
			{
				return true;
			}
		}
		return false;
	}


public class NFoundAtReadStartException extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public NFoundAtReadStartException(Pyrotag p)
	{
		super("First base of " + p.getID() + " is an N");
	}
}
class ReadWithZeroLengthException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ReadWithZeroLengthException(Pyrotag p)
	{
		super("When stripped of key, mid " + p.getID() + " has zero length!");
	}
}

}
