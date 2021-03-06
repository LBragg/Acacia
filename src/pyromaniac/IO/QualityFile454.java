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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import pyromaniac.DataStructures.QualitySequence;

// TODO: Auto-generated Javadoc
/**
 * The Class QualityFile454.
 */
public class QualityFile454 implements QualityFile
{
	
	/** The filename. */
	private String filename;
	
	/** The Constant BEGINNING_FASTA_HEADER. */
	public static final char  BEGINNING_FASTA_HEADER = '>';
	
	/**
	 * Instantiates a new quality file454.
	 *
	 * @param filename the filename
	 */
	public QualityFile454(String filename)
	{
		this.filename = filename;
	}
	
	/* (non-Javadoc)
	 * @see pyromaniac.IO.QualityFile#getFileName()
	 */
	public String getFileName()
	{
		return this.filename;
	}
	
	/* (non-Javadoc)
	 * @see pyromaniac.IO.QualityFile#iterator()
	 */
	public pyromaniac.IO.QualityFile454.QualityIterator iterator()
	{
		QualityIterator qi = new QualityIterator(this);
		
		return qi;
	}

	/**
	 * The Class QualityIterator.
	 */
	public class QualityIterator implements Iterator <QualitySequence>
	{
		
		/** The qf. */
		private QualityFile qf;
		
		/** The curr file pos. */
		private int currFilePos;
		
		/** The br. */
		private BufferedReader br;
		
		/** The curr line. */
		String currLine;
		
		/**
		 * Instantiates a new quality iterator.
		 *
		 * @param qf the qf
		 */
		protected QualityIterator(QualityFile454 qf)
		{
			try
			{
				this.qf = qf;
				this.br = new BufferedReader(new FileReader(this.qf.getFileName()));
				this.currLine = null;
				this.currFilePos = -1;
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		
		/* (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() 
		{
			try 
			{	
				if(this.currFilePos == -1)
				{
					currLine = br.readLine();
					currFilePos = 1;
				}
				
				if(currLine == null)
				{
					return false;
				}
				
				return true;
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			return false;
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		public QualitySequence next() 
		{
			try
			{
				//skip over whitespace.
				while(this.currLine != null && currLine.equals(""))
				{
					currLine = br.readLine();
					this.currFilePos++;
				}
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
			
			String id = null;
			String desc = null;
			ArrayList <Integer> sequence = new ArrayList <Integer>();
			
			if(this.currLine != null)
			{
				try
				{
					if(this.currLine.charAt(0) != BEGINNING_FASTA_HEADER)
					{
						throw new FormattingException("Expected '>', found " + this.currLine, 
								qf.getFileName(), currFilePos);
					}
					id = this.currLine.substring(1);
					id.trim();
					
					if(id.length() == 0)
					{
						throw new FormattingException("Sequence identifier has zero length",
								qf.getFileName(),currFilePos);
					}
					else
					{
						this.currLine = br.readLine();
						this.currFilePos++;
						
						if(this.currLine == null || this.currLine.equals(""))
						{
							throw new FormattingException("Expected quality sequence", qf.getFileName(), currFilePos);
						}
						
						while(this.currLine != null && this.currLine.charAt(0) != BEGINNING_FASTA_HEADER)
						{
							String [] results = this.currLine.split("[\\s\\t]");						
							for(int i = 0; i < results.length; i++)
							{
								String temp = results[i];
								try
								{
									Integer parsed = Integer.parseInt(temp);
									sequence.add(parsed);	
								}
								catch(NumberFormatException nfe)
								{
									throw new FormattingException("Expected integer, found " + temp, qf.getFileName(), currFilePos);
								}
							}
							this.currLine = br.readLine();
							this.currFilePos++;
						}
					}
				}
				catch(FormattingException fe)
				{
					System.out.println(fe.getMessage());
					return null;
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
			
			if(id.length() > 0)
			{
				int positionOfWhiteSpace = id.indexOf(" ");
				if(positionOfWhiteSpace != -1) //whitespace was found
				{
					desc = id.substring(positionOfWhiteSpace + 1, id.length());
					id = id.substring(0, positionOfWhiteSpace);
				}
			}
			
			if(sequence.size() > 0)
			{
				return new QualitySequence(id, desc, sequence);
			}
			
			return null;
		}
		
		//this does nothing
		/* (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		public void remove() 
		{
			
		}
	}
	
}
