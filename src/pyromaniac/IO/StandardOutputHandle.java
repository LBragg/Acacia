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

import java.io.IOException;

// TODO: Auto-generated Javadoc
/**
 * The Class StandardOutputHandle.
 */
public class StandardOutputHandle implements LoggerOutput 
{
	
	/** The closed. */
	boolean closed;

	/**
	 * Instantiates a new standard output handle.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public StandardOutputHandle() throws IOException
	{
		this.closed = false;
	}

	/* (non-Javadoc)
	 * @see pyromaniac.IO.LoggerOutput#write(java.lang.StringBuilder, int)
	 */
	public void write(StringBuilder buffer, int LOG_STYLE) throws IOException
	{
		System.out.println(buffer.toString());
	}

	/* (non-Javadoc)
	 * @see pyromaniac.IO.LoggerOutput#doHTMLMarkup()
	 */
	public boolean doHTMLMarkup() 
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see pyromaniac.IO.LoggerOutput#closeHandle()
	 */
	public void closeHandle() throws IOException
	{
		this.closed = true;
	}

	/* (non-Javadoc)
	 * @see pyromaniac.IO.LoggerOutput#isClosed()
	 */
	public boolean isClosed() 
	{
		return closed;
	}

	/* (non-Javadoc)
	 * @see pyromaniac.IO.LoggerOutput#flush()
	 */
	public void flush() throws Exception 
	{
		System.out.flush();
	}

}
