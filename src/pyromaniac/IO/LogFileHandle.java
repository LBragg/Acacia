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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;

// TODO: Auto-generated Javadoc
/**
 * The Class LogFileHandle.
 */
public class LogFileHandle implements LoggerOutput 
{
	
	/** The out. */
	private BufferedWriter out;
	
	/** The closed. */
	boolean closed;
	
	/**
	 * Instantiates a new log file handle.
	 *
	 * @param out the out
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public LogFileHandle(BufferedWriter out) throws IOException
	{
		this.out = out;
		this.closed = false;
	}
	
	/* (non-Javadoc)
	 * @see pyromaniac.IO.LoggerOutput#write(java.lang.StringBuilder, int)
	 */
	public void write(StringBuilder buffer, int LOG_STYLE) throws IOException
	{
	//	System.out.println("Writing to buffer!");
		this.out.write(buffer.toString());
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
		//System.out.println("CLosing buffer");
		this.out.flush();
		this.out.close();
		this.closed = true;
	}

	/* (non-Javadoc)
	 * @see pyromaniac.IO.LoggerOutput#isClosed()
	 */
	public boolean isClosed() {
		return closed;
	}

	/* (non-Javadoc)
	 * @see pyromaniac.IO.LoggerOutput#flush()
	 */
	public void flush() throws Exception 
	{
		this.out.flush();
	}

}
