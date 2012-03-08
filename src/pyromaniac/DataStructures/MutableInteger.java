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

// TODO: Auto-generated Javadoc
/**
 * The Class MutableInteger.
 */
public class MutableInteger
{
	
	/** The value. */
	private int value;
	
	/**
	 * Instantiates a new mutable integer.
	 *
	 * @param val the val
	 */
	public MutableInteger(int val)
	{
		this.value = val;
	}
	
	/**
	 * Increment.
	 */
	public void increment()
	{
		value++;
	}
	
	/**
	 * Decrement.
	 */
	public void decrement()
	{
		value--;
	}
	
	/**
	 * Update.
	 *
	 * @param newValue the new value
	 */
	public void update(int newValue)
	{
		this.value = newValue;
	}
	
	/**
	 * Value.
	 *
	 * @return the int
	 */
	public int value()
	{
		return this.value;
	}

	/**
	 * Adds the.
	 *
	 * @param toAdd the to add
	 */
	public void add(int toAdd) 
	{
		this.value += toAdd;
	}
}