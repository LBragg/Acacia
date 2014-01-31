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

import java.io.File;
import java.io.FileInputStream;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;

import org.apache.commons.math3.util.Pair;

import pyromaniac.DataStructures.FlowCycler;
import pyromaniac.DataStructures.MutableInteger;
import pyromaniac.DataStructures.Pyrotag;
import pyromaniac.DataStructures.Sequence;

// TODO: Auto-generated Javadoc
/**
 * The Class MMFastqImporter.
 */
public class MMFastqImporter extends TagImporter
{
	
	/** The logger. */
	private AcaciaLogger logger;
	
	/** The fastq file. */
	private String fastqFile;
	
	/** The record starts. */
	private ArrayList <Pair <Integer, Long>> recordStarts; //populate these first
	
	private long seqSizeLong;
	
	/** The decoder. */
	private CharsetDecoder decoder = Charset.forName(System.getProperty("file.encoding")).newDecoder();
	
	/** The Constant BEGINNING_FASTQ_SEQ. */
	public static final char  BEGINNING_FASTQ_SEQ = '@';
	
	/** The Constant BEGINNING_FASTQ_QUAL. */
	public static final char  BEGINNING_FASTQ_QUAL = '+';
	
	/** The Constant ACCEPTIBLE_IUPAC_CHARS. */
	public static final String ACCEPTIBLE_IUPAC_CHARS = "ATGCNURYWSMKBHDV";
	
	/** The record buffer. */
	private ArrayList<MappedByteBuffer> recordBuffers;
	
	private FlowCycler cycler;
	
	/**
	 * Instantiates a new mM fastq importer.
	 *
	 * @param fastqFile the fastq file
	 * @param logger the logger
	 */
	public MMFastqImporter(String fastqFile, String flowCycle, AcaciaLogger logger)
	{
		this.fastqFile = fastqFile;
		this.logger = logger;
		this.recordBuffers = new ArrayList<MappedByteBuffer>();
		this.cycler = new FlowCycler(flowCycle, logger);
		this.init();
	}
	
	/* (non-Javadoc)
	 * @see pyromaniac.IO.TagImporter#getNumberOfSequences()
	 */
	public int getNumberOfSequences()
	{
		return this.recordStarts.size();
	}
	
	/**
	 * Inits the file.
	 */
	public void init()
	{
		try
		{
			_initFile();
		}
		catch (CharacterCodingException cce) 
		{  
			throw new RuntimeException(cce);  
		
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
		
	/**
	 * Helper function for init(). Scans this.fastq file for sequence starts and records their position.
	 * Multiple MappedByteBuffers are used to handle large files.
	 * @throws Exceptions relating to file reading and decoding.
	 */
	private void _initFile() throws Exception
	{
		FileInputStream tempStream = new FileInputStream(new File(this.fastqFile)); 
		FileChannel fcSeq = tempStream.getChannel();
		this.seqSizeLong = fcSeq.size();
		this.recordStarts = new ArrayList <Pair<Integer,Long>>();

		int state = -1;
		
        for (long startPosition = 0L; startPosition < this.seqSizeLong; startPosition += HALF_GIGA)
        {
        	MappedByteBuffer recordBuffer = fcSeq.map(FileChannel.MapMode.READ_ONLY, startPosition,  
        			Math.min(this.seqSizeLong - startPosition, HALF_GIGA));
        	this.recordBuffers.add(recordBuffer);
        	
        	int sbf_pos = this.recordBuffers.size() - 1;
        	
			int maxBuffer = 2048;
			int bufferSize = (recordBuffer.capacity() > maxBuffer)? maxBuffer: recordBuffer.capacity();
			
			recordBuffer.limit(bufferSize);
			recordBuffer.position(0);
			
			while(recordBuffer.position() != recordBuffer.capacity())
			{					
				int prevPos = recordBuffer.position();
				CharBuffer result = decoder.decode(recordBuffer);	
				recordBuffer.position(prevPos);
				
				for(int i = 0; i < result.capacity(); i++)
				{
					char curr = result.charAt(i);
					int posInFile = prevPos + i ;
	
					//I see a fastq header, I am either at beginning of file, or last saw the quality line...
					if(curr == BEGINNING_FASTQ_SEQ && (state == -1 || state == 4))
					{
						this.recordStarts.add(new Pair<Integer, Long>(sbf_pos, new Long(posInFile)));
						state = 1;
						
					}
					else if (curr == BEGINNING_FASTQ_QUAL && (state == 1))
					{
						state = 2;
					}
					else if((curr ==  '\n' || curr == '\r') & state == 2)
					{
						state = 3;
					}
					else if ((curr ==  '\n' || curr == '\r') & state == 3)
					{
						state = 4;
					}
				}
				
				int newPos = recordBuffer.limit();
				
				if(recordBuffer.limit() + bufferSize > recordBuffer.capacity())
					recordBuffer.limit(recordBuffer.capacity());
				else
					recordBuffer.limit(recordBuffer.limit() + bufferSize);
				recordBuffer.position(newPos);
			}
			recordBuffer.rewind();
        }
	}
	
	/**
	 * Checks if the char is printable
	 *
	 * @param c the c
	 * @return true, if is printable char
	 */
	public boolean isPrintableChar( char c ) 
	{
	    Character.UnicodeBlock block = Character.UnicodeBlock.of( c );
	    return (!Character.isISOControl(c)) &&
	            block != null &&
	            block != Character.UnicodeBlock.SPECIALS;
	}
	

	/* (non-Javadoc)
	 * @see pyromaniac.IO.TagImporter#getPyrotagAtIndex(int)
	 */
	public Pyrotag getPyrotagAtIndex(int index) throws Exception
	{
		if(index >= this.recordStarts.size())
			return null;
		
		char [] relRecordBlock = getBlock(this.recordStarts, index, this.recordBuffers);
		//construct the pyrotag in this block.
		Pyrotag p = processRecordBlock(relRecordBlock);
		p.setInternalID(index);
		return p;
	}
	
	/**
	 * Process record block.
	 *
	 * @param pyrotagBlock the pyrotag block
	 * @return the pyrotag
	 */
	public Pyrotag processRecordBlock(char [] pyrotagBlock)
	{	
		try
		{
			MutableInteger index = new MutableInteger(0);
			
			String identifier = _readIdentifierLine(pyrotagBlock,index); //read identifier and read sequence need to be fixed.
			String [] idComp = _parseIdentifierLine(identifier);
			
			ArrayList <Character> nucleotides = this._readSequence(pyrotagBlock, index);
			
			_readIdentifierLine(pyrotagBlock,index); //throw this result away for now, if I wanted to, could do a check to compare
			//headers
	
			ArrayList <Integer> qualities = this._readQualities(pyrotagBlock, index);
			
			Sequence <Character> pyrotagSeq = new Sequence<Character> (nucleotides, idComp[0], idComp[1]);
			Sequence <Integer> pyrotagQual = new Sequence <Integer>(qualities, idComp[0], idComp[1]);
			Pyrotag p = new Pyrotag(idComp[0],idComp[1], pyrotagSeq, pyrotagQual, this.cycler);
			
			return p;
		}
		catch(SeqFormattingException sfe)
		{
			System.out.println(sfe.getMessage());
			System.exit(1);
		}
		return null;
	}
	
	
	
	/**
	 * _parse identifier line.
	 *
	 * @param identifierLine the identifier line
	 * @return the string[]
	 */
	public String [] _parseIdentifierLine(String identifierLine)
	{
		int posAngle = identifierLine.indexOf(BEGINNING_FASTQ_SEQ);
		int posWhite = identifierLine.indexOf(" ");
		
		String [] IDAndDescription = new String [2];
		if(posWhite > 0)
		{
			IDAndDescription[0] = identifierLine.substring(posAngle + 1, posWhite);
			IDAndDescription[1] = identifierLine.substring(posWhite + 1, identifierLine.length());
		}
		else
		{
			IDAndDescription[0] = identifierLine.substring(posAngle + 1, identifierLine.length());
			IDAndDescription[1] = "";
		}
		return IDAndDescription;
	}
	
	/**
	 * _read sequence.
	 *
	 * @param pyrotagBlock the pyrotag block
	 * @param pos the pos
	 * @return the array list
	 * @throws SeqFormattingException the seq formatting exception
	 */
	public ArrayList <Character> _readSequence(char [] pyrotagBlock, MutableInteger pos) throws SeqFormattingException
	{
			ArrayList <Character> characters = new ArrayList <Character>();			
			char curr;
			int index = pos.value();
			
			//push through newlines
			while(index < pyrotagBlock.length)
			{
				curr = pyrotagBlock[index];
				
				if(! (curr == '\n' || curr == '\r'))
					break;
				
				index++;
			}
			
			//should be in nucleotides
			while(index < pyrotagBlock.length)
			{
				curr = pyrotagBlock[index];
				curr = Character.toUpperCase(curr);

				if(curr == '\n' || curr == '\r')
					break;
				
				if(Character.isLetter((char)curr))
				{
					if(ACCEPTIBLE_IUPAC_CHARS.indexOf(curr) == -1)
					{
						String seq = new String(pyrotagBlock);
						
						throw new SeqFormattingException("Non-IUPAC character (" + curr + ") in sequence : " + seq, this.fastqFile);
					}
					else
					{
						characters.add(curr);
					}
				}
				index++;
			}
			pos.update(index);
			return characters;
	}
	
	//FASTQ qualities need to be mapped back to integers.
	/**
	 * _read qualities.
	 *
	 * @param pyrotagBlock the pyrotag block
	 * @param pos the pos
	 * @return the array list
	 * @throws SeqFormattingException the seq formatting exception
	 */
	public ArrayList <Integer> _readQualities(char [] pyrotagBlock, MutableInteger pos) throws SeqFormattingException
	{
		ArrayList <Integer> qualities = new ArrayList <Integer>();
		char curr;
		
		int index = pos.value();
		
		//push through newlines
		while(index < pyrotagBlock.length)
		{
			curr = pyrotagBlock[index];
			
			if(! (curr == '\n' || curr == '\r'))
				break;
			
			index++;
		}
	
		//qualities should be here.
		while(index < pyrotagBlock.length)
		{
			curr = pyrotagBlock[index];	
			
			if(curr == '\n' || curr == '\r')
				break;
			
			int conv = curr - 33;
			qualities.add(conv);
			index++;
		}
		
		return qualities;
	}
	
	
	/**
	 * _read identifier line.
	 *
	 * @param pyrotagBlock the pyrotag block
	 * @param pos the pos
	 * @return the string
	 */
	public String _readIdentifierLine(char [] pyrotagBlock, MutableInteger pos)
	{
		StringBuffer buff = new StringBuffer();
		int currPos = pos.value();
		
		char curr = pyrotagBlock[currPos];
		while(curr == '\n' || curr == '\r')
		{
			currPos++;
			curr = pyrotagBlock[currPos];
		}
		
		
		while(!(curr == '\n' || curr == '\r'))
		{
			buff.append(curr);
			currPos++;
			curr = pyrotagBlock[currPos];
		}
		
		pos.update(currPos);
		return buff.toString();
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

	/* (non-Javadoc)
	 * @see pyromaniac.IO.TagImporter#closeFiles()
	 */
	public void closeFiles() 
	{
		this.recordBuffers.clear();
		
	}
}
