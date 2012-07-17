package pyromaniac.Algorithm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;

import pyromaniac.DataStructures.FlowCycler;

//rename to table
public class IonTorrentOUCallFrequency implements OUFrequencyTable
{
	private NestedHash myProbs;

	public IonTorrentOUCallFrequency(String probFile)
	{
		this.myProbs = new NestedHash();

		if(probFile == null)
		{
			for(int i = 1; i <= MAX_PRELOAD;i++)
			{
				calculateProbabilities(i, NO_FLOW);
			}
		}
		else
		{
			loadProbabilities(probFile);
		}
	}

	@Override
	public void loadProbabilities(String probFile) 
	{
		// TODO Auto-generated method stub
		try
		{
			URL url = getClass().getResource(probFile);
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

			String header = in.readLine();

			if(header.split(",").length != 7)
			{
				throw new Exception("Call probabilities does not have seven columns as expected");
			}

			String line = in.readLine();

			while(line != null)
			{				
				String [] fields = line.split(",");

				int mode = Integer.parseInt(fields[0]);
				int cycleNumber = Integer.parseInt(fields[1]);
				int flowInCycleNumber = Integer.parseInt(fields[2]);
				char base = fields[3].charAt(0);
				double lessThan = Double.parseDouble(fields[4]);
				double equalTo = Double.parseDouble(fields[5]);
				double moreThan = Double.parseDouble(fields[6]);

				double [] probs = {lessThan, equalTo, moreThan};
				this.myProbs.put(mode,cycleNumber, flowInCycleNumber, base, probs);
				line = in.readLine();
			}
		}
		catch(NumberFormatException nfe)
		{
			nfe.printStackTrace();
		}
		catch(IOException ie)
		{
			ie.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}




	}

	@Override
	public void calculateProbabilities(int obsMode, int flowNumber) 
	{
		//from the model functions.

		// TODO Auto-generated method stub

	}

	@Override
	public double[] getProbabilities(HashMap<String, Object> factors, FlowCycler cycler) 
	{
		//cycler knows all..
		int flowPosition = (Integer)factors.get(OUFrequencyTable.FLOW_POSITION);

		int cycleNumber = cycler.flowPositionToCycleNumber(flowPosition);
		int posInCycle = cycler.flowPositionToPosInCycle(flowPosition);
		char nucleotide = cycler.getBaseAtCyclePos(posInCycle);
		return this.myProbs.get((Integer)factors.get(OUFrequencyTable.RLE_LENGTH), cycleNumber, posInCycle, nucleotide);
	}

	private class NestedHash
	{
		//RLE LENGTH, CYCLE POS, FLOW NUM, BASE
		private HashMap <Integer, HashMap <Integer, HashMap <Integer, HashMap<Character, double [] >>>> nesty;

		public NestedHash()
		{
			this.nesty = new HashMap <Integer, HashMap <Integer, HashMap <Integer, HashMap<Character, double [] >>>> ();
		}

		public void put(int rleLength, int cycleNumber, int flowInCycleNumber, char base, double [] probs)
		{
			HashMap <Integer, HashMap <Integer, HashMap <Character, double [] >>> rleHash;

			if(! nesty.containsKey(rleLength))
			{
				rleHash = new HashMap <Integer, HashMap <Integer, HashMap<Character, double [] >>> ();
				nesty.put(rleLength, rleHash);
			}
			else
			{
				rleHash = nesty.get(rleLength);
			}

			HashMap <Integer, HashMap <Character, double [] >> cycleNumberHash; 

			if(! rleHash.containsKey(cycleNumber))
			{
				cycleNumberHash = new HashMap <Integer, HashMap <Character, double [] >>();
				rleHash.put(cycleNumber, cycleNumberHash);
			}
			else
			{
				cycleNumberHash = rleHash.get(cycleNumber);
			}

			HashMap <Character, double []> flowNumberHash;

			if(! cycleNumberHash.containsKey(flowInCycleNumber))
			{
				flowNumberHash = new HashMap <Character, double []>();
				cycleNumberHash.put(flowInCycleNumber, flowNumberHash);
			}
			else
			{
				flowNumberHash = cycleNumberHash.get(flowInCycleNumber);
			}

			flowNumberHash.put(base, probs);
		}

		public double [] get(int rleLength, int cycleNumber, int flowInCycleNumber, char base)
		{
			try
			{
				return this.nesty.get(rleLength).get(cycleNumber).get(flowInCycleNumber).get(base);
			}
			catch(Exception e)
			{
				return null; //maybe return an estimated value instead
			}
		}
	}
}
