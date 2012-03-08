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

package pyromaniac.IO;

import pyromaniac.DataStructures.Pyrotag;

// TODO: Auto-generated Javadoc
/**
 * The Interface TagImporter.
 */
public interface TagImporter 
{
	
	/**
	 * Close files.
	 */
	public void closeFiles();
	
	/**
	 * Gets the number of sequences.
	 *
	 * @return the number of sequences
	 */
	public int getNumberOfSequences();
	
	/**
	 * Gets the pyrotag at index.
	 *
	 * @param index the index
	 * @return the pyrotag at index
	 */
	public Pyrotag getPyrotagAtIndex(int index);
	
	
	/**
	 * The Class ImportException.
	 */
	public abstract class ImportException extends Exception
	{
		
		/**
		 * Instantiates a new import exception.
		 *
		 * @param errMessage the err message
		 */
		public ImportException(String errMessage)
		{
			super(errMessage);
		}
	}
}
