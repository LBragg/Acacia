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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

// TODO: Auto-generated Javadoc
/**
 * The Class AcaciaLogger.
 */
public class AcaciaLogger 
{
	
	/** The closed. */
	private boolean closed;
	
	/** The time since last output. */
	private Date [] timeSinceLastOutput;
	
	/** The mode to listeners. */
	private HashMap <String, LinkedList <LoggerOutput>> modeToListeners;
	
	/** The Constant LOG_DEBUG. */
	public static final String LOG_DEBUG = "DEBUG";
	
	/** The Constant LOG_PROGRESS. */
	public static final String LOG_PROGRESS = "PROGRESS";
	
	/** The Constant LOG_ERROR. */
	public static final String LOG_ERROR = "ERROR";
	
	/** The Constant LOG_ALL. */
	public static final String LOG_ALL = "ALL";
	
	/** The Constant LOG_NONE. */
	public static final String LOG_NONE = "NONE";
	
	/** The Constant MAX_BUFFER_SIZE. */
	public static final int MAX_BUFFER_SIZE = 200;
	
	/** static hashmap log to index. */
	private static HashMap<String, Integer> LOG_TO_INDEX = new HashMap <String, Integer>();
	
	/** static hashmap index to log. */
	private static HashMap <Integer, String> INDEX_TO_LOG = new HashMap <Integer, String>();
	
	/** static hashmap containg the style of text used for the log type. */
	private static HashMap <String, Integer> LOG_TYPE_TO_STYLE = new HashMap <String, Integer>();
	
	static
	{
		LOG_TO_INDEX.put(LOG_DEBUG, 0);
		LOG_TO_INDEX.put(LOG_PROGRESS,1);
		LOG_TO_INDEX.put(LOG_ERROR,2);
		
		INDEX_TO_LOG.put(0, LOG_DEBUG);
		INDEX_TO_LOG.put(1, LOG_PROGRESS);
		INDEX_TO_LOG.put(2,LOG_ERROR);
		
		LOG_TYPE_TO_STYLE.put(LOG_DEBUG, LoggerOutput.LOG_STYLE_DEBUG);
		LOG_TYPE_TO_STYLE.put(LOG_PROGRESS, LoggerOutput.LOG_STYLE_PROGRESS );
		LOG_TYPE_TO_STYLE.put(LOG_ERROR, LoggerOutput.LOG_STYLE_ERROR);
	}
	
	/**
	 * Instantiates a new acacia logger.
	 */
	public AcaciaLogger()
	{
		this.modeToListeners = new HashMap <String, LinkedList<LoggerOutput>>();
		this.timeSinceLastOutput = new Date [LOG_TO_INDEX.size()];
	
		closed = false;
	}
	
	
	/**
	 * Adds the output.
	 *
	 * @param newOutput the new output
	 * @param mode the mode
	 */
	public void addOutput(LoggerOutput newOutput, String mode)
	{
		synchronized(this.modeToListeners)
		{
			if(mode == LOG_ALL)
			{
				for(String logType : AcaciaLogger.LOG_TO_INDEX.keySet())
				{
					if(! this.modeToListeners.containsKey(logType))
					{
						System.out.println("Adding " + logType + " for " + newOutput);
						this.modeToListeners.put(logType, new LinkedList <LoggerOutput>());
					}
					this.modeToListeners.get(logType).add(newOutput);
				}
			}
			else if (mode != LOG_NONE)
			{
				if(! this.modeToListeners.containsKey(mode))
				{
					this.modeToListeners.put(mode, new LinkedList <LoggerOutput>());
				}
				this.modeToListeners.get(mode).add(newOutput);	
			}
		}
	}
	
	/**
	 * Removes the output.
	 *
	 * @param removeMe is the LoggerOutput to remove from the logger
	 * @param mode is the mode of output for which removeMe is a subscriber 
	 */
	public void removeOutput(LoggerOutput removeMe, String mode)
	{
		synchronized(this.modeToListeners)
		{
			if(this.modeToListeners.containsKey(mode))
			{
				this.modeToListeners.get(mode).remove(removeMe);
			}
		}
	}
	
	//for now, no thread safety.
	
	/**
	 * Adds the time stamp.
	 *
	 * @param message the message
	 * @return the string
	 */
	public String addTimeStamp(String message)
	{
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss ");
		Calendar cal = Calendar.getInstance();
		String modified = (dateFormat.format(cal.getTime()) + message);
		return modified;
	}
	
	/**
	 * Append time stamp.
	 *
	 * @param message the message
	 */
	public void appendTimeStamp(StringBuilder message)
	{
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss ");
		Calendar cal = Calendar.getInstance();
		String timeStamp = dateFormat.format(cal.getTime());
		message.append(timeStamp);
	}
	
	/**
	 * Write log.
	 *
	 * @param message the message
	 * @param logType the log type
	 * @throws Exception the exception
	 */
	public void writeLog(String message, String logType) throws Exception
	{
		synchronized(this.modeToListeners)
		{
			if(logType == LOG_ALL)
			{
					if(this.modeToListeners.containsKey(logType))
					{
						_write(message, logType);
					}
			}
			else
			{
				if(logType == LOG_NONE)
				{
					return;
				}
	
				if(logType == LOG_DEBUG)
				{
					_write(message, LOG_DEBUG);
				}
				else if(logType == LOG_PROGRESS)
				{
					_write(message, LOG_PROGRESS);
				}
				else if(logType == LOG_ERROR)
				{
					_write(message, LOG_ERROR);
				}
				else
				{
					return; //TODO: an unknown option!
				}
			}	
		}
	}

	/**
	 * Adds the markup.
	 *
	 * @param toModify the to modify
	 * @param logType the log type
	 * @return the string builder
	 */
	private StringBuilder addMarkup(String toModify, String logType) 
	{
		StringBuilder improved = new StringBuilder();
		this.appendTimeStamp(improved);
		improved.append(" " + logType);
		improved.append(": ");
		improved.append(toModify);
		improved.append(System.getProperty("line.separator"));
		return improved;
	}

	/**
	 * _write.
	 *
	 * @param originalMessage the original message
	 * @param logType the log type
	 * @throws Exception the exception
	 */
	private void _write(String originalMessage, String logType) throws Exception
	{
		synchronized(this.modeToListeners)
		{
			StringBuilder modified = this.addMarkup(originalMessage, logType);

			if(this.modeToListeners.get(logType) == null)
				return;

			LinkedList <LoggerOutput> los = this.modeToListeners.get(logType);
			for(LoggerOutput lo: los)
			{
				lo.write(modified, AcaciaLogger.LOG_TYPE_TO_STYLE.get(logType));
			}
			this.modeToListeners.notifyAll();
		}
		this.timeSinceLastOutput[AcaciaLogger.LOG_TO_INDEX.get(logType)] = Calendar.getInstance().getTime(); 
	}

	
	/**
	 * Removes the log files.
	 *
	 * @throws Exception the exception
	 */
	public void removeLogFiles() throws Exception
	{
		synchronized (this.modeToListeners)
		{
			System.out.println("Removing log files: " + Thread.currentThread().getName());
			for(String mode: this.modeToListeners.keySet())
			{
				LinkedList <LoggerOutput> forMode = this.modeToListeners.get(mode);

				LinkedList <LoggerOutput> toRemove = new LinkedList <LoggerOutput>();
				for(LoggerOutput lo : forMode)
				{
					if(lo instanceof LogFileHandle || lo instanceof StandardOutputHandle)
					{
						toRemove.add(lo);
						
					}
					lo.flush();
				}

				for(LoggerOutput lo: toRemove)
				{
					lo.closeHandle();
					forMode.remove(lo);
				}
			}
			System.out.println("Finished removing log files: " + Thread.currentThread().getName());
			this.modeToListeners.notifyAll();
		}
	}


	/**
	 * Close logger.
	 *
	 * @throws Exception the exception
	 */
	public void closeLogger() throws Exception
	{
		synchronized(this.modeToListeners)
		{
			System.out.println("Closing logger: " + Thread.currentThread().getName());
			//this.flushBuffers();
			for(String mode: this.modeToListeners.keySet())
			{
				LinkedList <LoggerOutput> los = this.modeToListeners.get(mode);
				for(LoggerOutput lo: los)
				{
					if(!lo.isClosed())
						lo.closeHandle();
				}
			}
			System.out.println("Finished closing logger: " + Thread.currentThread().getName());
		}
		this.closed = true;
	}

	/**
	 * Flush logs.
	 *
	 * @throws Exception the exception
	 */
	public void flushLogs() throws Exception
	{

		synchronized(this.modeToListeners)
		{
			System.out.println("Closing logger: " + Thread.currentThread().getName());
			//this.flushBuffers();
			for(String mode: this.modeToListeners.keySet())
			{
				LinkedList <LoggerOutput> los = this.modeToListeners.get(mode);
				for(LoggerOutput lo: los)
				{
					if(!lo.isClosed())
					{
						lo.flush();
					}
				}
			}
		}
	}
}
