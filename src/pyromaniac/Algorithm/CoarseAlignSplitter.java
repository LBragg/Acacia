package pyromaniac.Algorithm;

import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import pyromaniac.AcaciaConstants;
import pyromaniac.Algorithm.RLEAlignmentIndelsOnly.AlignmentColumn;
import pyromaniac.DataStructures.DistanceMatrix;
import pyromaniac.DataStructures.FlowCycler;
import pyromaniac.DataStructures.Pair;
import pyromaniac.DataStructures.Pyrotag;
import pyromaniac.IO.AcaciaLogger;

public class CoarseAlignSplitter 
{
	private static CoarseAlignSplitter instance;
	
	
	private CoarseAlignSplitter()
	{
	}
	
	public static CoarseAlignSplitter getInstance() 
	{
		if(instance == null)
			instance = new CoarseAlignSplitter();
		return instance;
	}
	
	public HashSet <HashSet<Pyrotag>> scanAlignmentForObviousDeviations
	(
			AcaciaLogger logger,
			HashMap<String, String> settings,
			HashMap<String, BufferedWriter> outputHandles,
			Pair<RLEAlignmentIndelsOnly, HashMap<Pyrotag, Pair<Integer, Character>>> alignRes,
			FlowCycler fc
	) 
	{
			RLEAlignmentIndelsOnly motherAlign = alignRes.getFirst();
			HashMap <Pyrotag, Pair <Integer, Character>> motherFlow = alignRes.getSecond();
			
			HashSet <Pyrotag> toProcess = motherAlign.getAllTags();	 //grabs the results of the last run	
	
			//running over ThreadedAlignment... always clone the flow hash, in preparation for error correction later
			//these are here should we change the way that the clustering value is predicted.
			//HashMap <Pyrotag, Pair <Integer,Character>> flowMap = AcaciaEngine.getEngine().cloneFlowHash(motherFlow); //this cloning operation is unavoidable unless huge changes made.
			//OUFrequencyTable table = AcaciaEngine.getErrorModel(settings);			
			
			Iterator <AlignmentColumn> it = motherAlign.iterator();

			int numNodes = 0;
	
			char [] bases = {'A', 'T', 'G', 'C'};
			
			while(it.hasNext())
			{
				AlignmentColumn curr = it.next();
				
				numNodes++;
				
				for(char base : bases)
				{
					AlignmentColumn ac = curr.getInsertionCorrespondingTo(base);
					if(ac != null)
					{
						numNodes++;
					}
				}
			}
			
			double [][] readCounts = new double [toProcess.size()][numNodes];

			for(int i = 0; i < readCounts.length; i++)
			{
				for(int j = 0; j < readCounts[i].length; j++)
				{
					readCounts[i][j] = -1;
				}
			}
				
			HashMap <Pyrotag, Integer> tagToRow = new HashMap <Pyrotag, Integer> ();
			HashMap <Integer, Pyrotag> rowToTag = new HashMap <Integer, Pyrotag> ();
			
			int colIndex = 0;
			int tagCounter = 0;
			
			
			it = motherAlign.iterator();
			
			while(it.hasNext())
			{
				AlignmentColumn curr = it.next();
				HashMap <Integer, HashSet <Pyrotag>> tags = curr.getHPLengthToTags(toProcess);
				
				for(int length: tags.keySet())
				{
						for(Pyrotag p: tags.get(length))
						{
							if(! tagToRow.containsKey(p))
							{
								tagToRow.put(p, tagCounter);
								rowToTag.put(tagCounter, p);
								tagCounter++;
							}
							
							int relRow = tagToRow.get(p);
							readCounts[relRow][colIndex] = length;
						}
				}
				colIndex++;
				
				for(char base: bases)
				{
					AlignmentColumn ac = curr.getInsertionCorrespondingTo(base);
					if(ac != null)
					{
						HashMap <Integer, HashSet <Pyrotag>> innerTags = ac.getHPLengthToTags(toProcess); 
						for(int length: innerTags.keySet())
						{
							for(Pyrotag p: innerTags.get(length))
							{
								if(! tagToRow.containsKey(p))
								{
									tagToRow.put(p, tagCounter);
									rowToTag.put(tagCounter, p);
									tagCounter++;
								}
								
								int relRow = tagToRow.get(p);
								readCounts[relRow][colIndex] = length;
							}
						}
						colIndex++;
					}
				}
			}
			return clusterAndSplit(settings, readCounts, rowToTag);
	}

	//might have to mask later columns too.
	private HashSet <HashSet <Pyrotag>> clusterAndSplit(HashMap <String, String> settings, double [][] hpMatrix, HashMap <Integer, Pyrotag> rowToTag)
	{ 
		int [] numNoObs = new int [hpMatrix[0].length];
		HashSet <Integer> toMask = new HashSet <Integer>();
		
		for(int col = 0; col < hpMatrix[0].length; col++)
		{
			for(int row = 0; row < hpMatrix.length; row++)
			{
				double value = hpMatrix[row][col];
				
				if(value < 0)
				{
					numNoObs[col] = numNoObs[col]  + 1;
					value = 0;
				}
				hpMatrix[row][col] =  Math.sqrt(value);
			}
			
			if(((double)numNoObs[col] / hpMatrix.length) > 0)
			{
				toMask.add(col);
			}
		}
		
		DistanceMatrix dm = null;
		
		try {
			dm = calculateScaledEuclideanDistanceForReads(hpMatrix, toMask, rowToTag);
		} catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return completeLinkageClustering(settings, dm, rowToTag);
	}	


	//get alot less observations with zero dist than in R.
	public DistanceMatrix calculateScaledEuclideanDistanceForReads(double[][] hpMatrix, HashSet<Integer> toMask, HashMap<Integer, Pyrotag> rowToTag) throws Exception
	{
		//have every reason to believe the standard deviation is correct.
		for(int col = 0; col < hpMatrix[0].length; col++)
		{
			double stdDevForCol = this.calculateStandardDeviationForColumn(hpMatrix, col);
			for(int row = 0; row < hpMatrix.length; row++)
			{
				hpMatrix[row][col] = hpMatrix[row][col] * stdDevForCol;
			}
		}

		DistanceMatrix dm = new DistanceMatrix(hpMatrix.length); //DM for reads
		
		for(int row1 = 0; row1 < hpMatrix.length - 1; row1++)
		{
			for(int row2 = row1 + 1; row2 < hpMatrix.length; row2++)
			{
				double sum = 0;
				
				for(int col = 0; col < hpMatrix[row1].length; col++)
				{
					if(toMask != null && toMask.contains(col))
					{
						sum += 0;
					}
					else
					{
						sum += (hpMatrix[row1][col] - hpMatrix[row2][col]) * (hpMatrix[row1][col] - hpMatrix[row2][col]);
					}
				}
				
				double dist = Math.sqrt(sum);
				dm.setValue(row1, row2, dist);
			}
		}
		return dm;
	}
	
	private double calculateStandardDeviationForColumn(double [][] matrix, int col)
	{
		double sumX = 0;	
		double xMinusXBar = 0;
	
		for(int i = 0; i < matrix.length; i++)
		{
			sumX += matrix[i][col];
		}
		
		double mean = sumX / (double)matrix.length;
		
		for(int i = 0; i < matrix.length; i++)
		{
			xMinusXBar += Math.pow(matrix[i][col] - mean,2);
		}
	
		double sd = Math.sqrt((1.0 / (((double)matrix.length) - 1.0) * xMinusXBar));
		return sd;
	}

	//this does it by column.
	public DistanceMatrix calculatePearsonsForColumns(double [][] matrix)
	{	
		DistanceMatrix dm = new DistanceMatrix(matrix[0].length);
		//first lets see if any of the features are correlated

		//so we have correlations between features...
		for(int col = 0; col < matrix[0].length; col++)
		{
			for(int innerCol = col + 1; innerCol < (matrix[0].length - 1); innerCol++)
			{
				double SumXprodY = 0;
				double sumX = 0;
				double sumY = 0;
				double sumXSquared = 0;
				double sumYSquared = 0;
				double n = matrix.length;				

				for(int row = 0; row < matrix.length; row++)
				{
					SumXprodY += (matrix[row][col] * matrix[row][innerCol]); 
					sumX += matrix[row][col];
					sumY += matrix[row][innerCol];
					sumXSquared += (matrix[row][col]  * matrix[row][col]);
					sumYSquared += (matrix[row][innerCol]  * matrix[row][innerCol]);
				}

				double numerator = ((n * SumXprodY) - (sumX * sumY)); 
				double denominator = (Math.sqrt((n * sumXSquared)  - (sumX * sumX)) * Math.sqrt((n * sumYSquared)  - (sumY * sumY))); 
				double ri =   (denominator == 0.0)? 0 : numerator / denominator; 
				
				
				
				try
				{
					dm.setValue(col, innerCol, ri);
				}
				catch(Exception e)
				{
					System.out.println(e.getMessage());
					System.exit(1);
				}
			}
		}
		return dm;
	}
	
	public HashSet<HashSet<Pyrotag>> completeLinkageClustering (HashMap <String, String> settings, DistanceMatrix dm, HashMap <Integer, Pyrotag> rowToTag)
	{	
		int numObs = dm.getNumberOfObservations();
		
		HashMap <Integer, HashSet <Integer>> newClusters = new HashMap <Integer, HashSet <Integer>>();
		
		double maxDist = Double.parseDouble(settings.get(AcaciaConstants.OPT_MAX_COMPLETE_LINKAGE_DIST));
		
		for(int i = 0; i < numObs; i++)
		{
			boolean distBelowThresh = false;
			//problem with complete linkage is order of recruitment.
			
			for(int j = i + 1; j < numObs; j++)
			{
				if(dm.getValue(i, j) <= maxDist)
				{
					distBelowThresh = true;
					if(newClusters.containsKey(i) && newClusters.containsKey(j))
					{
						if(newClusters.get(i) != newClusters.get(j) && canBeRecruited(dm, newClusters.get(i), newClusters.get(j), maxDist))
						{
							HashSet <Integer> newCluster = new HashSet <Integer>();
							newCluster.addAll(newClusters.get(i));
							newCluster.addAll(newClusters.get(j));
							for(int tag: newCluster)
							{
								newClusters.put(tag, newCluster);
							}
						}
					}
					else if (newClusters.containsKey(i))
					{
						HashSet <Integer> singleton = new HashSet <Integer>();
						singleton.add(j);
						if(canBeRecruited(dm, newClusters.get(i), singleton, maxDist))
						{
							newClusters.get(i).add(j);
							newClusters.put(j, newClusters.get(i));
						}
						else
						{
							newClusters.put(j, singleton);
						}
					}
					else if (newClusters.containsKey(j))
					{
						HashSet <Integer> singleton = new HashSet <Integer>();
						singleton.add(i);
						if(canBeRecruited(dm, newClusters.get(j), singleton, maxDist))
						{
							newClusters.get(j).add(i);
							newClusters.put(i, newClusters.get(j));
						}
						else
						{
							newClusters.put(i, singleton);
						}
					}
					else
					{
						HashSet <Integer> newCluster = new HashSet <Integer>();
						newCluster.add(i);
						newCluster.add(j);
						newClusters.put(i, newCluster);
						newClusters.put(j, newCluster);
					}
				}
			}
			
			if(! distBelowThresh &! newClusters.containsKey(i))
			{
				HashSet <Integer> singleton = new HashSet <Integer>();
				singleton.add(i);
				newClusters.put(i, singleton);
			}
		}
		
		HashSet <HashSet <Pyrotag>> res = new HashSet <HashSet <Pyrotag>>();
		HashSet <HashSet <Integer>> processed = new HashSet <HashSet <Integer>>();
		
		for(int read: newClusters.keySet())
		{
			if(! processed.contains(newClusters.get(read)))
			{
				HashSet <Pyrotag> tagCluster = new HashSet <Pyrotag>();
				
				for(int member : newClusters.get(read))
				{
					tagCluster.add(rowToTag.get(member));
				}
				res.add(tagCluster);
				processed.add(newClusters.get(read));			
			}
		}
		
		return res;
	}
	
	public boolean canBeRecruited(DistanceMatrix dm, HashSet <Integer> firstCluster, HashSet <Integer> secondCluster, double maxDist)
	{
		for(int i: firstCluster)
		{
			for(int j: secondCluster)
			{
				if(dm.getValue(i, j) > maxDist)
				{
					return false;
				}
			}
		}
		return true;
	}
	
	
	public DistanceMatrix calculatePearsonsForRows(double [][] matrix)
	{	
		DistanceMatrix dm = new DistanceMatrix(matrix.length);
		//first lets see if any of the features are correlated
		//so we have correlations between features...
		for(int row = 0; row < matrix.length; row++)
		{
			for(int innerRow = row + 1; innerRow < (matrix.length - 1); innerRow++)
			{
				double SumXprodY = 0;
				double sumX = 0;
				double sumY = 0;
				double sumXSquared = 0;
				double sumYSquared = 0;
				double n = matrix.length;				

				for(int col = 0; col < matrix[0].length; col++)
				{
					SumXprodY += (matrix[row][col] * matrix[innerRow][col]); 
					sumX += matrix[row][col];
					sumY += matrix[innerRow][col];
					sumXSquared += (matrix[row][col]  * matrix[row][col]);
					sumYSquared += (matrix[innerRow][col]  * matrix[innerRow][col]);
				}

				double ri = ((n * SumXprodY) - (sumX * sumY)) / (Math.sqrt((n * sumXSquared)  - (sumX * sumX)) * Math.sqrt((n * sumYSquared)  - (sumY * sumY)));
				try
				{
					dm.setValue(row, innerRow, ri);
				}
				catch(Exception e)
				{
					System.out.println("An error occurred: " + e.getMessage());
				//	System.exit(1);
				}
			}
		}
		return dm;
	}
}
