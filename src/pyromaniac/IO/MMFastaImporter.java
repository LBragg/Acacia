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
import java.util.ArrayList;
import org.apache.commons.math3.util.Pair;
import pyromaniac.DataStructures.FlowCycler;
import pyromaniac.DataStructures.MutableInteger;
import pyromaniac.DataStructures.Pyrotag;
import pyromaniac.DataStructures.Sequence;




// TODO: Auto-generated Javadoc
/**
 * The Class MMFastaImporter.
 */
public class MMFastaImporter extends TagImporter
{
	
	/** The logger. */
	private AcaciaLogger logger;
	
	/** The seq file. */
	private String seqFile;
	
	/** The qual file. */
	private String qualFile;
	
	/** The length of the files as longs */
	private long qualSizeLong;
	private long seqSizeLong;
	
	private ArrayList <Pair<Integer, Long>> qualStartsLL; 
	private ArrayList <Pair<Integer, Long>> seqStartsLL;
	

	
	/** The Constant BEGINNING_FASTA_HEADER. */
	public static final char  BEGINNING_FASTA_HEADER = '>';
	
	/** The Constant ACCEPTIBLE_IUPAC_CHARS. */
	public static final String ACCEPTIBLE_IUPAC_CHARS = "ATGCNURYWSMKBHDV";
	
	/** The seq buffer. */
	private ArrayList <MappedByteBuffer> seqBuffers;
	
	/** The qual buffer. */
	private ArrayList <MappedByteBuffer> qualBuffers;
	
	private FlowCycler cycler;
	
	/**
	 * Instantiates a new mM fasta importer.
	 *
	 * @param seqFile the seq file
	 * @param qualFile the qual file
	 * @param flowCycle 
	 * @param logger the logger
	 */
	public MMFastaImporter(String seqFile, String qualFile, String flowCycle, AcaciaLogger logger)
	{
		this.seqFile = seqFile;
		this.qualFile = qualFile;
		this.logger = logger;
		this.seqBuffers = new ArrayList <MappedByteBuffer>();
		this.qualBuffers = new ArrayList <MappedByteBuffer>();
		this.cycler = new FlowCycler(flowCycle, logger);
		this.init();
	}
	
	/* (non-Javadoc)
	 * @see pyromaniac.IO.TagImporter#getNumberOfSequences()
	 */
	public int getNumberOfSequences()
	{
		return this.seqStartsLL.size();
	}
	
	/**
	 * Initialises the qual and seq starts.
	 */
	public void init()
	{
		//essentially all I want to do is look for delimiters in the file.
		try
		{
			_initSeq();
			
			if(!(this.qualFile == null || this.qualFile.trim().length() == 0 || this.qualFile.equals("null")))
			{
				_initQual();
			}
			else
			{
				this.qualFile = null; 
			}
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
	 * _init qual.
	 *
	 * @throws Exception the exception
	 */
	private void _initQual() throws Exception
	{
		FileInputStream tempStream = new FileInputStream(new File(this.qualFile)); 
		FileChannel fcQual = tempStream.getChannel();
		this.qualSizeLong = fcQual.size();

		//qual starts LL contains pairs, marking file #no (in  qualBuffers) and position #no (in the buffer).
    	this.qualStartsLL = new ArrayList <Pair<Integer,Long>>();
		
        for (long startPosition = 0L; startPosition < this.qualSizeLong; startPosition += HALF_GIGA)
        {
        	MappedByteBuffer qualBuffer = fcQual.map(FileChannel.MapMode.READ_ONLY, startPosition, Math.min(this.qualSizeLong - startPosition, HALF_GIGA)); //map half a gig to this channel.
        	this.qualBuffers.add(qualBuffer);
        	int qbf_pos = qualBuffers.size() - 1;
        	int maxBuffer = 2048;
        	int bufferSize = (qualBuffer.capacity() > maxBuffer)? maxBuffer: qualBuffer.capacity();
		
        	qualBuffer.limit(bufferSize);
        	qualBuffer.position(0);
		
        	while(qualBuffer.position() != qualBuffer.capacity())
        	{					
        		int prevPos = qualBuffer.position();
        		CharBuffer result = decoder.decode(qualBuffer);	
        		qualBuffer.position(prevPos);
			
        		for(int i = 0; i < result.capacity(); i++)
        		{
        			char curr = result.charAt(i);
        			int posInFile = prevPos + i ;
        			
        			if(curr == BEGINNING_FASTA_HEADER)	
        			{        				
        				qualStartsLL.add(new Pair<Integer, Long>(qbf_pos, new Long(posInFile)));
        			}		
        		}
			
        		int newPos = qualBuffer.limit();
        		
        		if(qualBuffer.limit() + bufferSize > qualBuffer.capacity())
        			qualBuffer.limit(qualBuffer.capacity());
        		else
        			qualBuffer.limit(qualBuffer.limit() + bufferSize);
        		qualBuffer.position(newPos);
        	}
        	qualBuffer.rewind();
        }
	}
	
	/**
	 * _init seq.
	 *
	 * @throws Exception the exception
	 */
	private void _initSeq() throws Exception
	{
		FileInputStream tempStream = new FileInputStream(new File(this.seqFile)); 
		FileChannel fcSeq = tempStream.getChannel();
		this.seqSizeLong = fcSeq.size();
		this.seqStartsLL = new ArrayList <Pair<Integer,Long>>();
		
        for (long startPosition = 0L; startPosition < this.seqSizeLong; startPosition += HALF_GIGA)
        {
        	MappedByteBuffer seqBuffer = fcSeq.map(FileChannel.MapMode.READ_ONLY, startPosition,
        			Math.min(this.seqSizeLong - startPosition, HALF_GIGA));
        	
        	this.seqBuffers.add(seqBuffer);
        	int sbf_pos = seqBuffers.size() - 1;
        	int maxBuffer = 2048;
        	int bufferSize = (seqBuffer.capacity() > maxBuffer)? maxBuffer: seqBuffer.capacity();
		
        	seqBuffer.limit(bufferSize);
        	seqBuffer.position(0);
		
        	while(seqBuffer.position() != seqBuffer.capacity())
        	{					
        		int prevPos = seqBuffer.position();
        		CharBuffer result = decoder.decode(seqBuffer);	
        		seqBuffer.position(prevPos);
			
        		for(int i = 0; i < result.capacity(); i++)
        		{
        			char curr = result.charAt(i);
        			int posInFile = prevPos + i ;
        			
        			if(curr == BEGINNING_FASTA_HEADER)	
        			{
        				seqStartsLL.add(new Pair <Integer, Long>(sbf_pos, new Long(posInFile)));
        			}	
        		}
			
        		int newPos = seqBuffer.limit();
			
        		if(seqBuffer.limit() + bufferSize > seqBuffer.capacity())
        			seqBuffer.limit(seqBuffer.capacity());
        		else
        			seqBuffer.limit(seqBuffer.limit() + bufferSize);
        		seqBuffer.position(newPos);
        	}
        	seqBuffer.rewind();
        }
        
        
	}

	//pyrotag at index.
	/* (non-Javadoc)
	 * @see pyromaniac.IO.TagImporter#getPyrotagAtIndex(int)
	 */
	public Pyrotag getPyrotagAtIndex(int index) throws Exception
	{
		if(index >= this.seqStartsLL.size())
			return null;
		
		char [] relSeqBlock = getBlock(this.seqStartsLL, index, this.seqBuffers);
		
		//construct the pyrotag in this block.
		Sequence <Character> pyrotagSeq = processSeqBlock(relSeqBlock);
		Sequence <Integer> qualitySeq = null;
		
		if(this.qualFile != null)
		{
			char [] relQualBlock = getBlock(this.qualStartsLL, index, this.qualBuffers);
			qualitySeq = processQualBlock(relQualBlock);
		}
		
		Pyrotag p = new Pyrotag(pyrotagSeq.getId(), pyrotagSeq.getDesc(), pyrotagSeq, qualitySeq, this.cycler);
		
		p.setInternalID(index);
		return p;
	}
	
	
	/**
	 * Process seq block.
	 *
	 * @param pyrotagBlock the pyrotag block
	 * @return the sequence
	 */
	public Sequence <Character> processSeqBlock(char [] pyrotagBlock)
	{	
		try
		{
			MutableInteger index = new MutableInteger(0);
			String identifier = _readIdentifier(pyrotagBlock,index); //read identifier and read sequence need to be fixed.
			String [] idComp = parseIdentifierLine(identifier);
			ArrayList <Character> nucleotides = this._readSequence(pyrotagBlock, index);
			
			Sequence <Character> pyrotagSeq = new Sequence<Character> (nucleotides, idComp[0], idComp[1]);
			return pyrotagSeq;
		}
		catch(SeqFormattingException sfe)
		{
			System.out.println(sfe.getMessage());
			System.exit(1);
		}
		return null;
	}
	
	/**
	 * Process qual block.
	 *
	 * @param qualBlock the qual block
	 * @return the sequence
	 */
	public Sequence <Integer> processQualBlock(char [] qualBlock)
	{
		try
		{
			MutableInteger index = new MutableInteger(0);
			String identifier = _readIdentifier(qualBlock,index);
			String [] idComp = parseIdentifierLine(identifier);
			ArrayList <Integer> qualities = this._readQualities(qualBlock, index);
			
			Sequence <Integer> pyrotagQual = new Sequence<Integer> (qualities, idComp[0], idComp[1]);
			return pyrotagQual;
		}
		catch(SeqFormattingException sfe)
		{
			System.out.println(sfe.getMessage());
			System.exit(1);
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
		int posAngle = identifierLine.indexOf(BEGINNING_FASTA_HEADER);
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
			
			while(index < pyrotagBlock.length)
			{
				curr = pyrotagBlock[index];
				curr = Character.toUpperCase(curr); //reads are read in upper case
				if(Character.isLetter((char)curr))
				{
					if(ACCEPTIBLE_IUPAC_CHARS.indexOf(curr) == -1)
					{
						throw new SeqFormattingException("Non-IUPAC character (" + curr + ") in sequence", this.seqFile);
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
		
		String currInt = "";
		char curr;
		
		int index = pos.value();
		try
		{
			while(index < pyrotagBlock.length)
			{

				curr = pyrotagBlock[index];
				
				if(Character.isLetterOrDigit(curr) || Character.isWhitespace(curr))
				{
					if(Character.isLetter(curr))
					{
						throw new SeqFormattingException("Non-numeric quality score encountered: " + curr, this.qualFile);
					}
					else if(Character.isWhitespace(curr) && currInt.length() > 0)
					{
						qualities.add(Integer.parseInt(currInt));
						currInt = "";
					}
					else if(Character.isDigit(curr))
					{
						currInt = currInt + curr;
					}
				}
				else  if (currInt.length() > 0)
				{
					qualities.add(Integer.parseInt(currInt));
					currInt = "";
				}
				index++;
			}
			if(currInt.length() > 0)
			{
				qualities.add(Integer.parseInt(currInt));
			}
			return qualities;
		}
		catch(NumberFormatException nfe)
		{
			throw new SeqFormattingException("Quality score: " + currInt + " is not an integer ", this.qualFile);
		}
	}
	
	
	/**
	 * _read identifier.
	 *
	 * @param pyrotagBlock the pyrotag block
	 * @param pos the pos
	 * @return the string
	 */
	public String _readIdentifier(char [] pyrotagBlock, MutableInteger pos)
	{
		StringBuffer buff = new StringBuffer();
		
		int currPos = pos.value();
		
		char curr = pyrotagBlock[currPos];
		
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
		//TODO: check for side-effects
		this.qualBuffers.clear();
		this.seqBuffers.clear();
		
	}
}
