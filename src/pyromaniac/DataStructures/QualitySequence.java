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

import java.util.ArrayList;

// TODO: Auto-generated Javadoc
/**
 * The Class QualitySequence.
 */
public class QualitySequence 
{
	
	/** The sequence. */
	private Integer [] sequence;
	
	/** The id. */
	private String id;
	
	/** The descriptor. */
	private String descriptor;
	
	/**
	 * Instantiates a new quality sequence.
	 */
	public QualitySequence()
	{
		sequence = null;
		id = null;
		descriptor = null;
	}
	
	/**
	 * Instantiates a new quality sequence.
	 *
	 * @param id the id
	 * @param descriptor the descriptor
	 * @param sequence the sequence
	 */
	@SuppressWarnings("unchecked")
	public QualitySequence(String id, String descriptor, ArrayList <Integer> sequence)
	{
		try
		{
			this.sequence = new Integer [sequence.size()]; 
			sequence.toArray(this.sequence);
			this.id = id;
			this.descriptor = descriptor;
		}
		catch(Exception e)
		{
			System.out.println("Quality sequence: " + e.getMessage());
		}
	}
	
	
	//returns a clone of the arraylist
	/**
	 * Gets the sequence.
	 *
	 * @return the sequence
	 */
	@SuppressWarnings("unchecked")
	public Integer []  getSequence()
	{
		return this.sequence;
	}
	
	/**
	 * Gets the quality at base.
	 *
	 * @param index the index
	 * @return the quality at base
	 */
	public int getQualityAtBase(int index)
	{
		assert(sequence != null && index < this.sequence.length);
		return sequence[index];
	}
	
	/**
	 * Sets the quality at base.
	 *
	 * @param index the index
	 * @param qual the qual
	 */
	public void setQualityAtBase(int index,int qual)
	{
		assert(sequence != null && index < this.sequence.length);
		this.sequence[index] = qual;
	}
	
	/**
	 * Gets the descriptor.
	 *
	 * @return the descriptor
	 */
	public String getDescriptor()
	{
		return this.descriptor;
	}
	
	/**
	 * Length.
	 *
	 * @return the int
	 */
	public int length()
	{
		return this.sequence.length;
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
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuffer result = new StringBuffer("");
		
		result.append(this.id + " " + this.descriptor);
		result.append(System.getProperty("line.separator"));
		for(int i = 0; i < sequence.length; i++)
		{
			result.append(sequence[i]);
			if(i != 0 && i % 70 == 0)
			{
				result.append(System.getProperty("line.separator"));
			}
			if(i + 1 != sequence.length)
			{
				result.append(" ");
			}
		}
		return result.toString();
	}
}
