package pyromaniac;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import pyromaniac.RunCharacterisation;
import pyromaniac.DataStructures.FlowCycler;
import pyromaniac.DataStructures.MIDPrimerCombo;
import pyromaniac.DataStructures.Pyrotag;
import pyromaniac.IO.AcaciaLogger;
import pyromaniac.IO.TagImporter;

public class DataCleaner 
{
	private DataCleaner()
	{
	}
	
	/**
	 * SingletonHolder is loaded on the first execution of Singleton.getInstance() 
	 * or the first access to SingletonHolder.INSTANCE, not before.
	 */
	private static class DataCleanerHolder 
	{ 
		
		/** The Constant INSTANCE. */
		private static final DataCleaner INSTANCE = new DataCleaner();

		/**
		 * Gets the single instance of AcaciaUtilityHolder.
		 *
		 * @return single instance of AcaciaUtilityHolder
		 */
		public static DataCleaner getInstance() 
		{
			return DataCleanerHolder.INSTANCE;
		}
	}
	
	public static DataCleaner getDataCleaner()
	{
		return DataCleanerHolder.getInstance();
	}
	
	public 	RunCharacterisation initialiseRunCharacterisation(HashMap <String, String> settings, AcaciaLogger logger,
			LinkedList <MIDPrimerCombo> midsToUse) throws Exception
	{
	
		if(midsToUse.size() == 0)
			midsToUse.add(AcaciaConstants.NO_MID_GROUP);
			
		HashMap <MIDPrimerCombo, LinkedList <Pyrotag>> MIDToSequences = new HashMap <MIDPrimerCombo, LinkedList <Pyrotag> >();
		HashMap <MIDPrimerCombo, Integer> MIDseqLength = new HashMap <MIDPrimerCombo, Integer>();
		HashMap <MIDPrimerCombo, Integer> MIDcollapsedSeqLength = new HashMap <MIDPrimerCombo, Integer>();
		HashMap <MIDPrimerCombo, Double> MIDqualities = new HashMap <MIDPrimerCombo, Double>();
		
		int fileIndex = 0;
		
		logger.writeLog("Preparing sequence importer", AcaciaLogger.LOG_PROGRESS);
		TagImporter importer = AcaciaEngine.getEngine().getTagImporter(settings, logger);
		
		Pyrotag p = importer.getPyrotagAtIndex(fileIndex);
		
		//loading only sequences which have valid MID.
		
		int invalidMID = 0;
		int validMID = 0;
		
		while (p != null) 
		{
			MIDPrimerCombo matching = p.whichMID(midsToUse);
				
			if(matching == null)
			{
				fileIndex++;
				p = importer.getPyrotagAtIndex(fileIndex);
				invalidMID++;
				
				continue;
			}
			
			validMID++;
			
			p.setMIDPrimerCombo(matching); //may already be initialised?
			
			if(! MIDToSequences.containsKey(matching))
			{
				MIDToSequences.put(matching, new LinkedList <Pyrotag>());
			}
			MIDToSequences.get(matching).add(p);
			
			if(! MIDseqLength.containsKey(matching))
			{
				MIDseqLength.put(matching, 0);
			}
			
			MIDseqLength.put(matching, MIDseqLength.get(matching) + p.getLength());
			
			if(p.getQualities() != null)
			{
				if(! MIDqualities.containsKey(matching))
				{
					MIDqualities.put(matching, 0.0);
				}
				
				MIDqualities.put(matching, MIDqualities.get(matching) +  p.getUntrimmedAvgQuality());
			}

			char [] collapsedReadMinusMid = p.getCollapsedRead();
			
			if(!MIDcollapsedSeqLength.containsKey(matching))
			{
				MIDcollapsedSeqLength.put(matching, 0);
			}
			
			MIDcollapsedSeqLength.put(matching, MIDcollapsedSeqLength.get(matching) +  collapsedReadMinusMid.length);
			
			fileIndex++;
			p = importer.getPyrotagAtIndex(fileIndex);
		}	
		
		RunCharacterisation rc = new RunCharacterisation(MIDToSequences, MIDseqLength, MIDcollapsedSeqLength, MIDqualities, validMID, invalidMID);
			
		if(rc.getNumValidMIDS() == 0)
		{
			logger.writeLog("There were no valid MIDS in the file!", AcaciaLogger.LOG_ERROR);
			logger.writeLog("There were no valid MIDS in the file!", AcaciaLogger.LOG_PROGRESS);
			return null;
		}
		
		return rc;
	}

	//this modifies the pyrotag reads, and also removes reads from the run characterisation that did not satisfy requirements.
	public void filterAndTrimReads(HashMap <String, String> settings, AcaciaLogger logger, RunCharacterisation rc, 
			LinkedList <MIDPrimerCombo> midsToUse, HashMap <String, BufferedWriter> outputHandles) throws Exception
	{
		//perhaps better handling of no valid ids.
		if(rc.getNumValidMIDS() == 0)
		{
			outputHandles.get(AcaciaConstants.STAT_OUT_FILE).write("Number of reads with invalid MID: " + rc.getNumInvalidMIDS() + System.getProperty("line.separator"));
			logger.writeLog("There were no valid MIDS in the file!", AcaciaLogger.LOG_ERROR);
			logger.writeLog("There were no valid MIDS in the file!", AcaciaLogger.LOG_PROGRESS);
			throw new Exception("No valid reads");
		}
		
		double meanLength = rc.getMeanReadLengthForMID(midsToUse);
		double stdDevRead = rc.calculateLengthStandardDevForRead(midsToUse);
		double stdDevCollapsed = rc.calculateCollapsedLengthStandardDevForRead(midsToUse);
				
		
		outputHandles.get(AcaciaConstants.STAT_OUT_FILE).write("Mean length (before filtering): " + meanLength + System.getProperty("line.separator"));
		outputHandles.get(AcaciaConstants.STAT_OUT_FILE).write("Length SD (before filtering):  " + stdDevRead + System.getProperty("line.separator"));
		outputHandles.get(AcaciaConstants.STAT_OUT_FILE).write("Length SD collapsed (before filtering):  " + stdDevCollapsed + System.getProperty("line.separator"));
		outputHandles.get(AcaciaConstants.STAT_OUT_FILE).write("Number of reads with invalid MID: " + rc.getNumInvalidMIDS() + System.getProperty("line.separator"));
	
		double numStdDev = Double.parseDouble(settings.get(AcaciaConstants.OPT_MAX_STD_DEV_LENGTH));
		
		int minReadLength = (int) (meanLength - (numStdDev * stdDevRead));
		int maxReadLength = (int) (meanLength + (numStdDev * stdDevRead));	
		
		if(minReadLength < 0)
			minReadLength = 0;
		
		logger.writeLog("Accepting reads in the range: " + minReadLength + " - " + maxReadLength, AcaciaLogger.LOG_PROGRESS);
		
		int minCollapsedSize = AcaciaConstants.DEFAULT_OPT_TRIM_COLLAPSED;
		int minQual = Integer.parseInt(settings.get(AcaciaConstants.OPT_MIN_AVG_QUALITY));
		int usableSeqs = 0;
		int unusableSeqs = 0;
		int lowQuality = 0;
		int outsideLengthRange = 0;
		int hasNs = 0;
		int hasWobble = 0;
		int collapsedTooShort = 0;
		int trimLengthGeneral = this.getTrim(settings);
		
		HashMap <String, Integer> dereplicated = new HashMap<String, Integer>();
		
		boolean verbose = false;
		
		//another outfile
		LinkedList <MIDPrimerCombo> midsToProcess = midsToUse;
				
		if(midsToProcess.size() == 1 && midsToProcess.getFirst() == AcaciaConstants.NO_MID_GROUP)
		{
			midsToProcess = new LinkedList <MIDPrimerCombo>(rc.MIDToSequences.keySet());
		}
		
		for(MIDPrimerCombo midPrimer: midsToProcess)
		{
			//there were no tags for that MID
			if(!rc.MIDToSequences.containsKey(midPrimer))
			{
				continue;
			}
			
			LinkedList <Pyrotag> seqs = rc.MIDToSequences.get(midPrimer);	
			LinkedList <Pyrotag> toRemove = new LinkedList <Pyrotag>();
			
			for(Pyrotag p: seqs)
			{	
				int trimLength = trimLengthGeneral; //overall trim length.
				boolean satisfyOverallLength = (p.getReadString().length >= minReadLength) && (p.getReadString().length <= maxReadLength);
				
				//trim read to a particular flow position
				if(!
						(settings.get(AcaciaConstants.OPT_TRUNCATE_READ_TO_FLOW) == null || settings.get(AcaciaConstants.OPT_TRUNCATE_READ_TO_FLOW).equals("null")
						|| settings.get(AcaciaConstants.OPT_TRUNCATE_READ_TO_FLOW).equals("")))
				{
					int flowToTrimTo = Integer.parseInt(settings.get(AcaciaConstants.OPT_TRUNCATE_READ_TO_FLOW));
					int basePosForFlow = p.flowPosToBasePos(flowToTrimTo, settings.get(AcaciaConstants.OPT_FLOW_KEY));
	
					if(basePosForFlow != Pyrotag.NO_CORRESPONDING_FLOW)
					{
						if(trimLength > 0)
						{
							trimLength = (trimLength < basePosForFlow)? trimLength : basePosForFlow;
						}	
						else
						{
							trimLength = basePosForFlow;
						}
					}
				}
				
				//trim to first N.
				//note that if the first N occurs straight after the MID primer, the read will have length zero.
				int firstN = p.firstOccurrenceOfAmbiguous(); //
				
				if(trimLength > 0)
				{
					p.setTrimToLength(trimLength);
				}
				
				//so why do I set MID primer now?
				p.setMIDPrimerCombo(midPrimer);
				 
				char [] collapsed = p.getCollapsedRead();
							
				int minNFlowPos = Integer.parseInt(settings.get(AcaciaConstants.OPT_FILTER_READS_WITH_N_BEFORE_POS)); //default is 350
				
				//firstly, this pertains to first N position, of which there may be zero.
				int [] firstFlowForNs = null;
				
				if(firstN != Pyrotag.NO_N && collapsed.length > 0)
				{
						firstFlowForNs = p.getFlowPositionForCollapsedReadPosition(settings.get(AcaciaConstants.OPT_FLOW_KEY), firstN);
				}
				
				//this should take care of the fact that N's can occur at the beginning.
				if(firstN != Pyrotag.NO_N && (trimLength < 0 || trimLength > firstN))
				{
					trimLength = firstN;
					p.setTrimToLength(trimLength);
					collapsed = p.getCollapsedRead();
				}

				/* 1. The collapsed read needs to satisfy the minimum collapsed size
				 * 2. There are no quality thresholds or the untrimmed average quality is greater than the min quality
				 * 3. There are no wobbles in the processed string
				 * 4. Either there are no N's or the first flow for N's is greater than the min N flow position.
				 */
				
				if(satisfyOverallLength && 
						collapsed.length >=  minCollapsedSize
						&& (p.getQualities() == null || p.getUntrimmedAvgQuality() >= minQual) 
						&& ! p.hasWobbleInProcessedString() 
						&& (firstN == Pyrotag.NO_N || (firstFlowForNs[FlowCycler.FLOW_POSITION] > minNFlowPos))
					)
				{					
					usableSeqs++;
				}
				else
				{	
					toRemove.add(p);
					
					if(!satisfyOverallLength)
					{
						outsideLengthRange++;
						if(verbose)
							logger.writeLog("Outside acceptable size range [ " + minReadLength + " - " + maxReadLength + " ]: " + p.getID(), AcaciaLogger.LOG_DEBUG);
					}
					
					if(firstN > 0  && firstFlowForNs[FlowCycler.FLOW_POSITION] < minNFlowPos)
					{
						if(verbose)
							logger.writeLog("Has N's: " + p.getID(), AcaciaLogger.LOG_DEBUG);
						
						hasNs++;
					}
					
					if(p.hasWobbleInProcessedString())
					{
						if(verbose)
							logger.writeLog("Has wobbles: " + p.getID(), AcaciaLogger.LOG_DEBUG);
						hasWobble++;
					}
					
					if(p.getQualities() != null  && p.getUntrimmedAvgQuality() < minQual)
					{
						lowQuality++;
						
						if(verbose)
							logger.writeLog("Low quality:" + p.getID(), AcaciaLogger.LOG_DEBUG);
					}
					
					if(collapsed.length < minCollapsedSize)
					{
						collapsedTooShort++;
						if(verbose)
							logger.writeLog("Collapsed too short: " + p.getID(), AcaciaLogger.LOG_DEBUG);
					}
					//too small, throw out.
					unusableSeqs++;
				}
			}
			
			if(usableSeqs == 0)
			{
				logger.writeLog("MID: " + midPrimer.getDescriptor() + " had no reads", AcaciaLogger.LOG_PROGRESS);	
			}
			
			//remove the unusable pyrotags, sounds good.
			seqs.removeAll(toRemove);
		}
		
		outputHandles.get(AcaciaConstants.STAT_OUT_FILE).write("# Seqs usable: " + usableSeqs + System.getProperty("line.separator"));
		outputHandles.get(AcaciaConstants.STAT_OUT_FILE).write("# Seqs thrown out: " + unusableSeqs + System.getProperty("line.separator"));
		outputHandles.get(AcaciaConstants.STAT_OUT_FILE).write("# Low quality: " + lowQuality + System.getProperty("line.separator"));
		outputHandles.get(AcaciaConstants.STAT_OUT_FILE).write("# Outside length range [ " + minReadLength + " - " + maxReadLength + " ]: " + outsideLengthRange + System.getProperty("line.separator"));
		outputHandles.get(AcaciaConstants.STAT_OUT_FILE).write("# with early N's: " + hasNs + System.getProperty("line.separator"));
		outputHandles.get(AcaciaConstants.STAT_OUT_FILE).write("# with wobble's: " + hasWobble + System.getProperty("line.separator")); //really should never happen.
		outputHandles.get(AcaciaConstants.STAT_OUT_FILE).write("# collapsed too short: " + collapsedTooShort + System.getProperty("line.separator"));
		outputHandles.get(AcaciaConstants.STAT_OUT_FILE).write("# Unique sequences: " + dereplicated.size() + System.getProperty("line.separator"));
		
		if(usableSeqs == 0)
		{
			logger.writeLog("No sequences satisified all filters", AcaciaLogger.LOG_PROGRESS);
			throw new Exception("No sequences satisfied filters");
		}
	}

	/**
	 * Gets the trim length.
	 *
	 * @param settings the Acacia settings
	 * @return the trim length
	 * @throws NumberFormatException Any formatting exception that occurs while parsing the trim length parameter
	 */
	public int getTrim(HashMap <String, String> settings) throws NumberFormatException 
	{
		String sTrimLength = settings.get(AcaciaConstants.OPT_TRIM_TO_LENGTH);
		int trimLength = 0;
		if (sTrimLength != null && !sTrimLength.equals("")) 
		{
			trimLength = Integer.parseInt(sTrimLength);
			return trimLength;
		}
		return -1; // fail
	}
}
