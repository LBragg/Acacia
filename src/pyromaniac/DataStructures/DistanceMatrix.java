package pyromaniac.DataStructures;

import java.util.Iterator;

import ch.usi.inf.sape.hac.experiment.DissimilarityMeasure;
import ch.usi.inf.sape.hac.experiment.Experiment;

public class DistanceMatrix implements Iterable, Experiment, DissimilarityMeasure
{
	double [][] distMat;
	
	public DistanceMatrix(int numCompared)
	{
		this.distMat = new double [numCompared - 1][];
			
		for(int i = 0; i < this.distMat.length; i++)
		{
			this.distMat[i] = new double [i + 1];
		}
	}
	
	public int numCompared()
	{
		return this.distMat.length + 1;
	}
	
	public double getValue (int indexI, int indexJ)
	{
		int row = (indexI < indexJ)? indexJ: indexI;
		int col = (indexI < indexJ)? indexI: indexJ;
		
		row--;
		
		return this.distMat[row][col];
	}
	
	public void setValue (int indexI, int indexJ, double value) throws Exception
	{

		int row = (indexI < indexJ)? indexJ: indexI;
		int col = (indexI < indexJ)? indexI: indexJ; 
		
		//whatever the object is, you need to recall that col 0 actually corresponds to object 2?
		row--;
		
		if(row >= this.distMat.length || col >= this.distMat[row].length)
		{
			throw new Exception("Trying to set value outside of range: " + row +", " + col +" when matrix has dimensions " + this.distMat.length + ", " + this.distMat[row].length);
		}
		
		this.distMat[row][col] = value;
	}

	@Override
	public DMIterator iterator() 
	{
		return new DMIterator(this);
	}
	

	public class DMIterator implements Iterator <DMCell>
	{
		private int currRow = 0;
		private int currCol = 0;
		private DistanceMatrix dm;
		
		public DMIterator(DistanceMatrix dm)
		{
			this.dm = dm;
		}

		public boolean hasNext() 
		{
			return(currRow < (dm.distMat.length - 1)  || (currRow < dm.distMat.length && currCol < dm.distMat[currRow].length));
		}

		@Override
		public DMCell next() 
		{
			//now, curr col related to the matrix index, not the actual obj.
			DMCell retObj = new DMCell(currRow + 1, currCol, this.dm.distMat[currRow][currCol]);
			
			if(currCol + 1 < dm.distMat[currRow].length)
			{
				currCol++;
			}
			else
			{
				currRow++;
			}
			return retObj;
		}

		@Override
		public void remove() 
		{
			// TODO Auto-generated method stub
			
		}	
	}


	@Override
	public int getNumberOfObservations() 
	{
		return this.distMat.length + 1;
	}

	@Override
	public double computeDissimilarity(Experiment arg0, int arg1, int arg2) 
	{
		return this.getValue(arg1,arg2);
	}
	


}

