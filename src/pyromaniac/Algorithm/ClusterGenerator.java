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

package pyromaniac.Algorithm;

import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.LinkedList;

import pyromaniac.DataStructures.Pyrotag;
import pyromaniac.IO.AcaciaLogger;

// TODO: Auto-generated Javadoc
/**
 * The Class Clustomatic.
 */
public abstract class ClusterGenerator 
{
	
	/** The initial clusters. */
	protected HashMap <String, LinkedList <Pyrotag>> initialClusters;
	
	/** The settings. */
	protected HashMap <String, String> settings;
	
	/** The logger. */
	protected AcaciaLogger logger;
	
	/** The output handles. */
	protected HashMap <String, BufferedWriter> outputHandles;

	
	//clustomatic MODIFIES the initialClusters
	/**
	 * Instantiates a new clustomatic.
	 */
	public ClusterGenerator() 
	{
		this.initialClusters = null;
		this.settings = null;
		this.logger = null;
		this.outputHandles = null;
	}
	
	/**
	 * Run clustering.
	 *
	 * @throws Exception the exception
	 */
	public abstract void runClustering() throws Exception;
	
	/**
	 * Inits the fundamental variables.
	 *
	 * @param initialClusters the initial clusters
	 * @param settings the settings
	 * @param logger the logger
	 * @param outputHandles the output handles
	 */
	public void initialise(HashMap <String, LinkedList <Pyrotag>> initialClusters,  
			HashMap <String, String> settings, AcaciaLogger logger, HashMap <String, BufferedWriter> outputHandles)
	
	{
		this.initialClusters = initialClusters;
		this.settings = settings;
		this.logger = logger;
		this.outputHandles = outputHandles;		
	}
}
