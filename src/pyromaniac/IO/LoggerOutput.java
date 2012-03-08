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

// TODO: Auto-generated Javadoc
/**
 * The Interface LoggerOutput.
 */
public interface LoggerOutput 
{
	
	/** The LO g_ styl e_ error. */
	public static int LOG_STYLE_ERROR = 0;
	
	/** The LO g_ styl e_ progress. */
	public static int LOG_STYLE_PROGRESS = 1;
	
	/** The LO g_ styl e_ debug. */
	public static int LOG_STYLE_DEBUG = 2;
	
	/**
	 * Write.
	 *
	 * @param buffer the buffer
	 * @param LOG_STYLE the lO g_ style
	 * @throws Exception the exception
	 */
	public void write(StringBuilder buffer, int LOG_STYLE) throws Exception;
	
	/**
	 * Do html markup.
	 *
	 * @return true, if successful
	 */
	public boolean doHTMLMarkup();
	
	/**
	 * Close handle.
	 *
	 * @throws Exception the exception
	 */
	public void closeHandle() throws Exception;
	
	/**
	 * Checks if is closed.
	 *
	 * @return true, if is closed
	 */
	public boolean isClosed();
	
	/**
	 * Flush.
	 *
	 * @throws Exception the exception
	 */
	public void flush() throws Exception;
}
