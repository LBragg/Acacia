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

import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;

import org.apache.commons.math3.util.Pair;

import pyromaniac.DataStructures.Pyrotag;

// TODO: Auto-generated Javadoc
/**
 * The Interface TagImporter.
 */
public abstract class TagImporter 
{
	public static final long HALF_GIGA = 536870912;
	/** The decoder. */
	static CharsetDecoder decoder = Charset.forName(System.getProperty("file.encoding")).newDecoder();
	/**
	 * Close files.
	 */
	public abstract void closeFiles();
	
	/**
	 * Gets the number of sequences.
	 *
	 * @return the number of sequences
	 */
	public abstract int getNumberOfSequences();
	
	/**
	 * Gets the pyrotag at index.
	 *
	 * @param index the index
	 * @return the pyrotag at index
	 * @throws Exception 
	 */
	public abstract Pyrotag getPyrotagAtIndex(int index) throws Exception;
	
	public char [] getBlock( ArrayList<Pair<Integer, Long>> starts, int index, ArrayList <MappedByteBuffer> buffers) throws Exception
	{
		StringBuilder sb = new StringBuilder("");
		
		if(index  >= starts.size())
		{
			return null;
		
		}
		
		//most cases will be contained within the same memory-mapped chunk.
		Pair <Integer, Long> startingBlock = starts.get(index);
		
		//next block can be undefined.
		Pair <Integer, Long> nextBlock = (starts.size() > index + 1)? starts.get(index + 1): null;
		
		//very unlikely that a sequence would stretch over half a mega block, but future-proofing...
		for(int mm_block = startingBlock.getFirst(); mm_block < buffers.size()  && 
				(nextBlock == null || mm_block <= nextBlock.getFirst()); mm_block++) 
		{
			long blockStart;
			long blockEnd;
			
			if(mm_block == startingBlock.getFirst()) //at the start
			{
				blockStart = startingBlock.getSecond();
				blockEnd = blockStart;
				if(nextBlock != null && nextBlock.getFirst() == startingBlock.getFirst())
				{
					blockEnd = nextBlock.getSecond(); //just up to the start of the next sequence in this block
				}
				else
				{
					blockEnd = buffers.get(mm_block).capacity(); //all the way to the end of this block
				}
			}
			else
			{
				blockStart = 0L; // the start of the next memory mapped file
				if(nextBlock.getFirst() == mm_block)//we are in the last block
				{
					blockEnd = nextBlock.getSecond();
				}
				else
				{
					blockEnd = buffers.get(mm_block).capacity(); //all the way to the end of this block.
				}
			}
			try
			{
				buffers.get(mm_block).limit((int)blockEnd);
				buffers.get(mm_block).position((int)blockStart);
				CharBuffer resBuffer = decoder.decode(buffers.get(mm_block));
				sb.append(resBuffer);
				buffers.get(mm_block).rewind();
			}
			catch(Exception e)
			{
			    String errMes = "Attempted to decode block " + blockStart + " for " + (blockEnd - blockStart + 1) + " chars. Error occurred: " + e.getMessage();
			    throw new ImportException(errMes);
			}
		}
		return sb.toString().toCharArray();
	}
	
	
	/**
	 * The Class ImportException.
	 */
	public class ImportException extends Exception
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
