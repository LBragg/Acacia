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
 * The Class MID.
 */
public class MIDPrimerCombo 
{
	
	/** The MID. */
	String MID;
	
	/** The primer. */
	String primer;
	
	/** The descriptor. */
	String descriptor;
	
	/**
	 * Instantiates a new mID.
	 *
	 * @param MID the mID
	 * @param primer the primer
	 * @param descriptor the descriptor
	 */
	public MIDPrimerCombo(String MID, String primer, String descriptor)
	{
		this.MID = MID;
		this.primer = primer;
		this.descriptor = descriptor;
	}

	/**
	 * Gets the descriptor.
	 *
	 * @return the descriptor
	 */
	public String getDescriptor() 
	{
		return descriptor;
	}
	
	/**
	 * Gets the mID primer sequence.
	 *
	 * @return the mID primer sequence
	 */
	public String getMIDPrimerSequence()
	{
		return this.MID + this.primer;
	}

	/**
	 * Sets the descriptor.
	 *
	 * @param descriptor the new descriptor
	 */
	public void setDescriptor(String descriptor) 
	{
		this.descriptor = descriptor;
	}

	/**
	 * Gets the primer.
	 *
	 * @return the primer
	 */
	public String getPrimer()
	{
		return this.primer;
	}
	
	/**
	 * Sets the primer.
	 *
	 * @param primer the new primer
	 */
	public void setPrimer(String primer)
	{
		this.primer = primer;
	}
	
	/**
	 * Gets the mID.
	 *
	 * @return the mID
	 */
	public String getMID() {
		return MID;
	}

	/**
	 * Sets the mID.
	 *
	 * @param mID the new mID
	 */
	public void setMID(String mID) {
		MID = mID;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuilder midString = new StringBuilder();
		midString.append('\''+ descriptor + '\'');
		midString.append(":");
		midString.append('\'' + MID + '\'');
		return midString.toString();
	}
		
}
