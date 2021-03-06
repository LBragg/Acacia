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
import java.io.RandomAccessFile;
import java.util.ArrayList;

import pyromaniac.IO.QualityFile.FormattingException;
import pyromaniac.DataStructures.Sequence;

// TODO: Auto-generated Javadoc
/**
 * The Class SeqReadingUtility.
 */
public class SeqReadingUtility 
{
	
	/** The Constant BEGINNING_FASTA_HEADER. */
	public static final char  BEGINNING_FASTA_HEADER = '>';
	
	/** The Constant ACCEPTIBLE_IUPAC_CHARS. */
	public static final String ACCEPTIBLE_IUPAC_CHARS = "ATGCNURYWSMKBHDV";
	
	
	/**
	 * Instantiates a new seq reading utility.
	 */
	public SeqReadingUtility()
	{
		
	}
	
	/**
	 * _read identifier line.
	 *
	 * @param tempIn the temp in
	 * @param ram the ram
	 * @param filename the filename
	 * @return the string
	 */
	public String _readIdentifierLine(BufferedReader tempIn, RandomAccessFile ram, String filename)
	{
		try
		{
			long initialPos = ram.getFilePointer();
			int charactersRead = 0;
			
			char start = (char)tempIn.read();
			charactersRead++;
			
			if(start != '>')
			{
				throw new SeqFormattingException("Expected '>', found (" + start + ") ", filename);
			}
			else
			{
				StringBuffer buff = new StringBuffer();
				
				int currInt = tempIn.read();
				charactersRead++;
				
				char curr = (char)currInt;
				while(!(curr == '\n' || curr == '\r') && currInt != -1)
				{
					buff.append(curr);
					currInt = tempIn.read();
					charactersRead++;
					curr = (char)currInt;
				}
				
				ram.seek(initialPos + charactersRead); //this SHOULD work...
				return buff.toString();
			}
		}
		catch(IOException ie)
		{
			ie.printStackTrace();
		}
		catch(SeqFormattingException sfe)
		{
			sfe.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Parses the identifier line.
	 *
	 * @param identifierLine the identifier line
	 * @return the string[]
	 */
	public String [] parseIdentifierLine(String identifierLine)
	{

		int posWhite = identifierLine.indexOf(" ");
		
		String [] IDAndDescription = new String [2];
		if(posWhite > 0)
		{
			IDAndDescription[0] = identifierLine.substring(0, posWhite);
			IDAndDescription[1] = identifierLine.substring(posWhite + 1, identifierLine.length());
		}
		else
		{
			IDAndDescription[0] = identifierLine;
			IDAndDescription[1] = "";
		}
		return IDAndDescription;
	}
	
	/**
	 * _read sequence.
	 *
	 * @param seq the seq
	 * @param ram the ram
	 * @param filename the filename
	 * @return the array list
	 */
	public ArrayList <Character> _readSequence(BufferedReader seq, RandomAccessFile ram, String filename)
	{
		try
		{
			ArrayList <Character> characters = new ArrayList <Character>();
			
			int charactersRead = 0;
			
			long initialPos = ram.getFilePointer();
			
			System.out.println("Initial position was : " + initialPos);
			
			int currInt = seq.read();
			charactersRead++;
			
			char curr = (char)currInt;
			
			while(currInt != -1 && curr != '>')
			{
				if(Character.isLetter((char)curr))
				{
					if(ACCEPTIBLE_IUPAC_CHARS.indexOf(curr) == -1)
					{
						throw new SeqFormattingException("Non-IUPAC character (" + curr + ") in sequence", filename);
					}
					else
					{
						characters.add(curr);
					}
				}
				
				if(seq.ready())
				{
					currInt = seq.read(); //reading in one character at a time...
					charactersRead++;
					curr = (char)currInt;
				}
				else
				{
					currInt = -1;
				}
			}

			System.out.println("Read " + charactersRead);
			
			if(curr == '>')
			{
				charactersRead--;
			}
			
			System.out.println("Before seeking, the curr position of ram was " + ram.getFilePointer());
			
			ram.seek(initialPos + charactersRead);
			
			return characters;
		}
		catch(SeqFormattingException sfe)
		{
			System.out.println("Error: " + sfe.getMessage());
			sfe.printStackTrace();
			//System.exit(1);
		}
		catch(IOException ie)
		{
			System.out.println("Error: " + ie.getMessage());
			ie.printStackTrace();
			//System.exit(1);
		}

		return null;
	}
	
	/**
	 * _read quality sequence.
	 *
	 * @param qual the qual
	 * @param ram the ram
	 * @param filename the filename
	 * @return the array list
	 */
	public ArrayList <Integer> _readQualitySequence(BufferedReader qual, RandomAccessFile ram, String filename)
	{
		try
		{
			ArrayList <Integer> scores = new ArrayList <Integer>();
			
			long initialPos = ram.getFilePointer();
			
			int charactersRead = 0;
			
			String currScore = "";
			
			char curr = (char)qual.read();
			charactersRead++;
			
			while(curr != -1 && curr != '>')
			{
				if(Character.isDigit(curr))
				{
					currScore = currScore + curr;
				}
				else if(!Character.isWhitespace(curr))
				{
					Integer value = Integer.parseInt(currScore);
					scores.add(value);
					currScore = "";
				}
				curr = (char)qual.read();
				charactersRead++;
			}
			
			if(curr == '>')
			{
				charactersRead--;
			}
			
			ram.seek(initialPos + charactersRead);

			
			return scores;
		}
		catch(IOException ie)
		{
			ie.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Gets the next seq.
	 *
	 * @param seq the seq
	 * @param filename the filename
	 * @return the next seq
	 */
	public Sequence <Character> getNextSeq(RandomAccessFile seq, String filename)
	{	
		String id = null;
		String desc = null;
		ArrayList <Character> sequence;
		
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(seq.getFD()));
			
			String identifierLine = this._readIdentifierLine(in,seq, filename);
			
			System.out.println("Identifier: " + identifierLine);
			
			String [] IDAndDescription = this.parseIdentifierLine(identifierLine);
			
			
			
			sequence = this._readSequence(in, seq, filename);
			
			System.out.println("Sequence: " + sequence);
			
			in = null;
			
			if(sequence != null && sequence.size() > 0)
			{
				return new Sequence <Character> (sequence, IDAndDescription[0], IDAndDescription[1]);
			}
		}
		catch(IOException ie)
		{
			System.out.println("Error: " + ie.getMessage());
			ie.printStackTrace();
			System.exit(1);
		}
		return null;
	}
	
	/**
	 * Checks for next seq.
	 *
	 * @param seq the seq
	 * @param filename the filename
	 * @return true, if successful
	 */
	public boolean hasNextSeq(RandomAccessFile seq, String filename)
	{
		try
		{	
			if(seq.length() == seq.getFilePointer())
			{
				System.out.println("Returning false?");
				return false;
			}
			
			BufferedReader in = new BufferedReader(new FileReader(seq.getFD()));
			
			System.out.println("Position before has next seq "  + seq.getFilePointer());
			
			long initialPos = seq.getFilePointer();
			char charactersRead = 0;
			
			int charInt = in.read();
			charactersRead++;
			
			//while there is white space, keep moving forward.
			while(charInt != -1 && Character.isWhitespace((char)charInt))
			{
				charInt = in.read();
				charactersRead++;
			}
		
			in = null;
			
			if(charInt == -1 || (char)charInt != this.BEGINNING_FASTA_HEADER)
			{
				System.out.println("Returning false, saw character " + (char)charInt);
				return false;
			}
			else	
			{
				System.out.println("Returning true");
				charactersRead--; //go back before > 
				seq.seek(initialPos + charactersRead); //have to reset it.
				return true;
			}
		}
		catch(IOException ie)
		{
			ie.printStackTrace();
	
		}
		return false;
	}
	
	/**
	 * Gets the next quality seq.
	 *
	 * @param qual the qual
	 * @param filename the filename
	 * @return the next quality seq
	 */
	public Sequence <Integer> getNextQualitySeq(RandomAccessFile qual, String filename)
	{
		String id = null;
		String desc = null;
		ArrayList <Integer> sequence;
		
		try
		{
			BufferedReader tempIn = new BufferedReader(new FileReader(qual.getFD()));
		
		
			String identifierLine = this._readIdentifierLine(tempIn, qual, filename);
			if(identifierLine == null)
			{
				tempIn = null;
				//tempIn.close();
				return null;
			}
			
			String [] IDAndDescription = this.parseIdentifierLine(identifierLine);
			
			sequence = this._readQualitySequence(tempIn, qual, filename);
			
			tempIn = null;
			
			if(sequence != null && sequence.size() > 0)
			{
				return new Sequence <Integer> (sequence, IDAndDescription[0], IDAndDescription[1]);
			}
		}
		catch(IOException ie)
		{
			System.out.println("Error: " + ie.getMessage());
			ie.printStackTrace();
			System.exit(1);
		}
		
		return null;
	}
	
	/**
	 * Checks for next quality seq.
	 *
	 * @param qual the qual
	 * @param filename the filename
	 * @return true, if successful
	 */
	public boolean hasNextQualitySeq(RandomAccessFile qual, String filename)
	{
		try
		{	
			BufferedReader in = new BufferedReader(new FileReader(qual.getFD()));
			
			int charactersRead = 0;
			long initialPos = qual.getFilePointer();
			int charInt = in.read();
			
			
			while(charInt != -1 && Character.isWhitespace((char)charInt))
			{
				charInt = in.read();
				charactersRead++;
			}
			
		
			if(charInt == -1 || (char)charInt != this.BEGINNING_FASTA_HEADER)
			{
				return false;
			}
			else	
			{
				charactersRead--;
				qual.seek(initialPos + charactersRead);
				return true;
			}
		}
		catch(IOException ie)
		{
			ie.printStackTrace();
	
		}
		return false;
	}
	
	/**
	 * The Class SeqFormattingException.
	 */
	public class SeqFormattingException extends Exception
	{
		
		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;
		
		/**
		 * Instantiates a new seq formatting exception.
		 *
		 * @param message the message
		 * @param filename the filename
		 */
		public SeqFormattingException(String message, String filename)
		{
			super("File: " + filename  + " FormattingException: " + message);
		}
	}
}
