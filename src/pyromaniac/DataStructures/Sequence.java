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
 * The Class Sequence.
 *
 * @param <T> the generic type
 */
public class Sequence <T>
{
	
	/** The sequence. */
	private Object [] sequence;
	
	/** The id. */
	private String id;
	
	/** The desc. */
	private String desc;
	
	/**
	 * Instantiates a new sequence.
	 *
	 * @param seqInit the seq init
	 * @param id the id
	 * @param descriptor the descriptor
	 */
	public Sequence(ArrayList <T> seqInit, String id,  String descriptor)
	{
		this.id = id;
		this.desc = descriptor;
		
		int seqInitSize = seqInit.size();
		
		sequence = new Object [seqInitSize];
		
		for(int i = 0; i <seqInitSize; i++)
		{
			sequence [i] = seqInit.get(i);
		}
	}

	/**
	 * Gets the sequence.
	 *
	 * @return the sequence
	 */
	public Object[] getSequence() 
	{
		return sequence;
	}

	/**
	 * Sets the sequence.
	 *
	 * @param sequence the new sequence
	 */
	public void setSequence(Object[] sequence) 
	{
		this.sequence = sequence;
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getId() 
	{
		return id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(String id) 
	{
		this.id = id;
	}

	/**
	 * Gets the desc.
	 *
	 * @return the desc
	 */
	public String getDesc() 
	{
		return desc;
	}

	/**
	 * Sets the desc.
	 *
	 * @param desc the new desc
	 */
	public void setDesc(String desc) 
	{
		this.desc = desc;
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
	 * Gets the value at index.
	 *
	 * @param index the index
	 * @return the value at index
	 */
	public T getValueAtIndex(int index)
	{
		assert(sequence != null && index < this.sequence.length);
		return (T) sequence[index];
	}
	
	/**
	 * Sets the value at index.
	 *
	 * @param index the index
	 * @param value the value
	 */
	public void setValueAtIndex(int index, T value)
	{
		assert(sequence != null && index < this.sequence.length);
		this.sequence[index] = value;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuffer result = new StringBuffer("");
		
		result.append(this.id + " " + this.desc);
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
