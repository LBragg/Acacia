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

package pyromaniac;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import pyromaniac.DataStructures.MIDPrimerCombo;
import pyromaniac.GUI.TagInputPanel;
import pyromaniac.IO.AcaciaLogger;

// TODO: Auto-generated Javadoc
/**
 * The Class ErrorCorrectionWorker.
 */
@SuppressWarnings("restriction")
public class ErrorCorrectionWorker extends SwingWorker<Void, Void> 
{
	
	/** The run settings. */
	HashMap <String, String> runSettings;
	
	/** The logger. */
	AcaciaLogger logger;
	
	/** The valid mids. */
	LinkedList <MIDPrimerCombo> validMIDS;
	
	/** The parent. */
	TagInputPanel parent;
	
	/** The worker finished. */
	Boolean workerFinished;
	
	//hidden constructor
	/**
	 * Instantiates a new error correction worker.
	 */
	@SuppressWarnings("unused")
	private ErrorCorrectionWorker()
	{
		
	}
	
	/**
	 * Instantiates a new error correction worker.
	 *
	 * @param settings the run time settings
	 * @param logger the AcaciaLogger
	 * @param validMIDS the valid MIDS for processing
	 * @param parent the parent TagInputPanel which instantiated this worked
	 */
	public ErrorCorrectionWorker(HashMap <String, String> settings, AcaciaLogger logger, LinkedList <MIDPrimerCombo> validMIDS, TagInputPanel parent)
	{
		//set up the worker
		this.runSettings = settings;
		this.logger = logger;
		this.validMIDS = validMIDS;
		this.parent = parent;
		this.workerFinished = false;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Void doInBackground() throws Exception 
	{
		ArrayList <String> filesCreated = new ArrayList <String> ();
		boolean exceptionOccurred = false;
		try
		{
			AcaciaEngine.getEngine().initLogFiles(this.runSettings, this.logger,true, validMIDS);
			AcaciaEngine.getEngine().runAcacia(this.runSettings, this.validMIDS, logger, this, AcaciaEngine.getVersion());
			logger.flushLogs();
			logger.removeLogFiles(); //TODO: this was changed.
		}
		catch(InterruptedException ie)
		{
			System.out.println(ie.getMessage());
			ie.printStackTrace();
			exceptionOccurred = true;
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
			exceptionOccurred = true;
		}
		finally
		{
			if(! exceptionOccurred)
			{
				System.out.println("No exceptions occurred");
				SwingUtilities.invokeLater(new FinishedRunnable(this.parent));
				return null;
			}
			for(String filename: filesCreated)
			{
				File f = new File(filename);
				
				if(f.exists())
				{
					//this file was successfully opened apparently
					
					if(!f.canWrite())
					{
						throw new IOException("File from cancelled run could not be removed.");
					}
					else
					{
						f.delete();
					}
				}
			}			
			SwingUtilities.invokeLater(new CancelRunnable(this.parent));
		}
		return null;
	}
	
	/**
	 * The Class CancelRunnable.
	 */
	private class CancelRunnable implements Runnable
	{
		
		/** The parent. */
		private TagInputPanel parent;
		
		/**
		 * Instantiates a new cancel runnable.
		 *
		 * @param parent the parent
		 */
		public CancelRunnable(TagInputPanel parent)
		{
			this.parent = parent;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run()
		{
			this.parent.workerCancelled();
		}
	}
	
	/**
	 * The Class FinishedRunnable.
	 */
	private class FinishedRunnable implements Runnable
	{
		
		/** The parent. */
		private TagInputPanel parent;
		
		/**
		 * Instantiates a new finished runnable.
		 *
		 * @param parent the parent
		 */
		public FinishedRunnable(TagInputPanel parent)
		{
			this.parent = parent;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run()
		{
			this.parent.workerFinished();
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#done()
	 */
	public void done()
	{
		//this is the clean up method after the thread has finished running
	}
	
	/**
	 * Prints the cancellation message.
	 */
	public void printCancellationMessage()
	{
		synchronized(this.workerFinished)
		{
			try
			{
				this.workerFinished = true;
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			this.workerFinished.notifyAll();
		}
	}
	
	/**
	 * Gets the worker finished.
	 *
	 * @return the worker finished
	 */
	public Boolean getWorkerFinished()
	{
		return this.workerFinished;
	}
}
