package pyromaniac;

import java.util.HashMap;
import java.util.LinkedList;

import pyromaniac.DataStructures.MIDPrimerCombo;
import pyromaniac.DataStructures.Pyrotag;


/**
 * The Class RunCharacterisation. Retains information about the mean and standard deviation in both read length, and run-length encoded read length.
 */
public class RunCharacterisation
{
	
	/** Hashmap mapping sequences to their corresponding MID. */
	HashMap <MIDPrimerCombo, LinkedList <Pyrotag>> MIDToSequences;
	
	/** The MID seq length. */
	HashMap <MIDPrimerCombo, Integer> MIDseqLength;
	
	/** The MI dcollapsed seq length. */
	HashMap <MIDPrimerCombo, Integer> MIDcollapsedSeqLength;
	
	/** The MId qualities. */
	HashMap <MIDPrimerCombo, Double> MIDqualities;
	
	/** The invalid mids. */
	int invalidMIDS;

	/** The valid mids */
	int validMIDs;
	
	
	/**
	 * Instantiates a new run characterisation.
	 *
	 * @param MIDToSequences the mID to sequences
	 * @param MIDSeqLength the mID seq length
	 * @param MIDcollapsedSeqLength the mI dcollapsed seq length
	 * @param MIDqualities the mI dqualities
	 * @param invalidMIDS the invalid mids
	 * @param invalidMID 
	 */
	public RunCharacterisation(HashMap <MIDPrimerCombo, LinkedList <Pyrotag>> MIDToSequences, 
			HashMap <MIDPrimerCombo, Integer> MIDSeqLength,
			HashMap <MIDPrimerCombo, Integer> MIDcollapsedSeqLength, 
			HashMap <MIDPrimerCombo, Double> MIDqualities, int validMIDs, int invalidMIDS)
	{
		this.MIDToSequences = MIDToSequences;
		this.MIDseqLength = MIDSeqLength;
		this.MIDcollapsedSeqLength = MIDcollapsedSeqLength;
		this.MIDqualities = MIDqualities;
		this.invalidMIDS = invalidMIDS;
		this.validMIDs = validMIDs;
	}
	
	/**
	 * Gets the number of reads which did not match the MIDS.
	 *
	 * @return the number of invalid mids
	 */
	public int getNumInvalidMIDS()
	{
		return this.invalidMIDS;
	}
	
	public int getNumValidMIDS()
	{
		return this.validMIDs;
	}
	
	/**
	 * Calculate length standard dev for read.
	 *
	 * @param midsForCalc only reads with these MIDs will be included in the calculation
	 * @return standard deviation of read lengths for the midsForCalc
	 */
	public double calculateLengthStandardDevForRead(LinkedList <MIDPrimerCombo> midsForCalc)
	{
		return calculateStandardDeviation(midsForCalc, this.MIDseqLength);
	}
	
	/**
	 * Calculate collapsed length standard dev for read.
	 *
	 * @param midsForCalc only reads with these MIDSs will be included in the calculation
	 * @return the standard deviation in run length encoded (homopolymer collapsed) reads sequences
	 */
	public double calculateCollapsedLengthStandardDevForRead(LinkedList <MIDPrimerCombo> midsForCalc)
	{
		return calculateStandardDeviation(midsForCalc, this.MIDcollapsedSeqLength);
	}
	
	/**
	 * Gets the mean read length for mid.
	 *
	 * @param midsForCalc the mids for calc
	 * @return the mean read length for mid
	 */
	public double getMeanReadLengthForMID(LinkedList <MIDPrimerCombo> midsForCalc)
	{
		return getMeanLengthForMID(midsForCalc, this.MIDseqLength);
	}
	
	/**
	 * Gets the mean collapsed read length for mid.
	 *
	 * @param midsForCalc the mids for calc
	 * @return the mean collapsed read length for mid
	 */
	public double getMeanCollapsedReadLengthForMID(LinkedList <MIDPrimerCombo> midsForCalc)
	{
		return getMeanLengthForMID(midsForCalc, this.MIDcollapsedSeqLength);
	}
	
	
	/**
	 * Gets the mean length for mid.
	 *
	 * @param midsForCalc the mids for calc
	 * @param sumOfLengths the sum of lengths
	 * @return the mean length for mid
	 */
	private double getMeanLengthForMID(LinkedList <MIDPrimerCombo> midsForCalc, HashMap <MIDPrimerCombo, Integer> sumOfLengths)
	{
		
		LinkedList <MIDPrimerCombo> midsToProcess = midsForCalc;
		
		if(midsToProcess.size() == 1 && midsToProcess.getFirst() == AcaciaConstants.NO_MID_GROUP)
			midsToProcess = new LinkedList <MIDPrimerCombo>(this.MIDToSequences.keySet());
			
		int lengthSum = sumLengthsForMIDs(midsToProcess, sumOfLengths);
		int tagCount = getTagCountForMIDs(midsToProcess);
		
		if(tagCount == 0)
			return -1;
		
		double mean = lengthSum / tagCount;
		return mean;
	}
	
	/**
	 * Average quality for mids.
	 *
	 * @param midsForCalc the mids for calc
	 * @return the double
	 */
	public double averageQualityForMIDs(LinkedList <MIDPrimerCombo> midsForCalc)
	{
		double qualitySum = 0;
		int numTags = 0;
		
		LinkedList <MIDPrimerCombo> midsToProcess = midsForCalc;
		
		if(MIDqualities.size() == 0)
			return 0;
		
		if(midsToProcess.size() == 1 && midsToProcess.getFirst() == AcaciaConstants.NO_MID_GROUP)
		{
			midsToProcess = new LinkedList <MIDPrimerCombo>(this.MIDToSequences.keySet());
		}
		
		for(MIDPrimerCombo mid: midsToProcess)
		{
			if(MIDqualities.containsKey(mid))
			{
				qualitySum += MIDqualities.get(mid);
				numTags += MIDToSequences.get(mid).size();
			}
		}
		
		if(numTags == 0)
		{
			return -1;
		}
		
		return (qualitySum / numTags);
	}
	
	/**
	 * Calculate standard deviation.
	 *
	 * @param midsForCalc the mids for calc
	 * @param sumOfLengths the sum of lengths
	 * @return the double
	 */
	private double calculateStandardDeviation(LinkedList <MIDPrimerCombo> midsForCalc, HashMap <MIDPrimerCombo, Integer> sumOfLengths)
	{

		int tagCount = 0;
		int lengthSum = 0;
		double mean = 0;
		double sumXMinusXBarSqr = 0;

		LinkedList <MIDPrimerCombo> midsToProcess = midsForCalc;
	
		if(midsToProcess.size() == 1 && midsToProcess.getFirst() == AcaciaConstants.NO_MID_GROUP)
			midsToProcess = new LinkedList <MIDPrimerCombo> (this.MIDToSequences.keySet());
		
	
		lengthSum = sumLengthsForMIDs(midsToProcess, sumOfLengths);
		tagCount = getTagCountForMIDs(midsToProcess);

		if(tagCount == 0)
			return -1;
		
		mean = (lengthSum / tagCount);

		for(MIDPrimerCombo mid: midsToProcess)
		{
			if(!this.MIDToSequences.containsKey(mid))
			{
				continue;
			}
			
			LinkedList <Pyrotag> relevantPyrotags = this.MIDToSequences.get(mid);

		
			for(Pyrotag p : relevantPyrotags)
			{
				double length = p.getLength();
				double sqrdist = (length - mean) * (length - mean);		
				sumXMinusXBarSqr += sqrdist;
			}
		}
		double sampleStdDev = Math.sqrt(sumXMinusXBarSqr / tagCount);
		return sampleStdDev;

	}
	
	/**
	 * Gets the tag count for mi ds.
	 *
	 * @param midsForCalc the mids for calc
	 * @return the tag count for mi ds
	 */
	public int getTagCountForMIDs(LinkedList <MIDPrimerCombo> midsForCalc)
	{
		int tagCount = 0;
		
		LinkedList <MIDPrimerCombo> midsToProcess = midsForCalc;
		
		if(midsToProcess.size() == 1 && midsToProcess.getFirst() == AcaciaConstants.NO_MID_GROUP)
			midsToProcess = new LinkedList <MIDPrimerCombo> (this.MIDToSequences.keySet());
		
		for(MIDPrimerCombo mid: midsToProcess)
		{
			if(MIDToSequences.containsKey(mid))
			{
				tagCount += MIDToSequences.get(mid).size();
			}
		}
		return tagCount;
	}
	
	/**
	 * Sum lengths for mids.
	 *
	 * @param midsForCalc the mids for calc
	 * @param sumOfLengths the sum of lengths
	 * @return the int
	 */
	private int sumLengthsForMIDs(LinkedList <MIDPrimerCombo> midsForCalc, HashMap <MIDPrimerCombo, Integer> sumOfLengths)
	{
		int lengthSum = 0;
		
		LinkedList <MIDPrimerCombo> midsToProcess = midsForCalc;
		
		if(midsToProcess.size() == 1 && midsToProcess.getFirst() == AcaciaConstants.NO_MID_GROUP)
			midsToProcess = new LinkedList <MIDPrimerCombo>(this.MIDToSequences.keySet());
		
		for(MIDPrimerCombo mid: midsForCalc)
		{
			if(sumOfLengths.containsKey(mid))
			{
				lengthSum += sumOfLengths.get(mid);
			}
		}
		return lengthSum;
	}
}	