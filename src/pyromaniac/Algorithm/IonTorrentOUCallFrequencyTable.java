package pyromaniac.Algorithm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URL;
import java.util.HashMap;

import pyromaniac.AcaciaConstants;
import pyromaniac.DataStructures.FlowCycler;
import pyromaniac.DataStructures.Pair;
import pyromaniac.IO.AcaciaLogger;

//rename to table
public class IonTorrentOUCallFrequencyTable implements OUFrequencyTable
{
	private NestedHash myProbs;
	private HashMap <String, BigDecimal> coeffZeroH;
	private HashMap <String, Pair <BigDecimal, BigDecimal>> coeffOneH;
	private HashMap <String, Pair <BigDecimal, BigDecimal>> coeffOtherH;
	
	private static final int maxPreLoadCycles = 8;
	private static final int maxPreLoadHPLen = 6;
	private static final int SCALE = 12;
	
	
	public  int getScale()
	{
		return this.SCALE;
	}
	

	
	public IonTorrentOUCallFrequencyTable(HashMap <String, String> settings, AcaciaLogger logger, String probFile, String coeffZero, String coeffOne, String coeffOther) throws Exception
	{
		this.myProbs = new NestedHash();
		
		this.coeffZeroH = new HashMap <String, BigDecimal>();
		this.coeffOneH = new HashMap <String, Pair <BigDecimal, BigDecimal>>();
		this.coeffOtherH = new HashMap <String, Pair <BigDecimal, BigDecimal>>();
		
		loadCoeff(coeffZero, coeffOne, coeffOther);
		
		if(probFile == null)
		{
			bulkCalculateProbabilities(settings, logger);
		}
		else
		{
			loadProbabilities(probFile);
		}
	}
	
	private void loadCoeff(String coeffZero, String coeffOne, String coeffOther) throws Exception
	{
		URL url = getClass().getResource(coeffZero);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));	
		
		//BufferedReader in = new BufferedReader(new FileReader(coeffZero));
		
		String line = in.readLine();
		while(line != null)
		{
	//		System.out.println("Reading: " + line);
			line = line.replace("\"", "");
			
			String [] split = line.split(",");
			
			if(split[0].equals("(Intercept)"))
			{
				coeffZeroH.put("Intercept", new BigDecimal(split[1]));
			}
			else
			{
				coeffZeroH.put(split[0], new BigDecimal(split[1]));
			}
			line = in.readLine();
		}
		
		//manually put this in, as there is no 'coefficient' corresponding to baseline PIC0
		coeffZeroH.put("PIC0", new BigDecimal("0"));
		
		//this is a multinomial for ones
		url = getClass().getResource(coeffOne);
		
		//		System.out.println("Path; " + url.getPath());
				
		in = new BufferedReader(new InputStreamReader(url.openStream()));
		
		//in = new BufferedReader(new FileReader(coeffOne));
		
		line = in.readLine();
		
		while(line != null)
		{
			line = line.replace("\"", "");
		//	System.out.println("Reading " + line);
			String [] split = line.split(",");
			
			//format: variable, under, over
			if(split[0].equals("(Intercept)"))
			{
				coeffOneH.put("Intercept", new Pair<BigDecimal, BigDecimal> (new BigDecimal(split[1]), new BigDecimal(split[2])));
			}
			else
			{
				coeffOneH.put(split[0], new Pair<BigDecimal, BigDecimal> (new BigDecimal(split[1]), new BigDecimal(split[2])));
			}
			line = in.readLine();
		}
		
		//manually put this in, as there is no 'coefficient' corresponding to baseline PIC0
		coeffOneH.put("PIC0", new Pair <BigDecimal, BigDecimal>(new BigDecimal("0"), new BigDecimal("0")));
		
		//this is the multinomial for others
		
		url = getClass().getResource(coeffOther);
		
		//		System.out.println("Path; " + url.getPath());
				
		in = new BufferedReader(new InputStreamReader(url.openStream()));
		
		//in = new BufferedReader(new FileReader(coeffOther));
		
		line = in.readLine();
		
		while(line != null)
		{
			line = line.replace("\"", "");
	//		System.out.println("Reading " + line);
			String [] split = line.split(",");
			if(split[0].equals("(Intercept)"))
			{
				coeffOtherH.put("Intercept", new Pair<BigDecimal, BigDecimal> (new BigDecimal(split[1]), new BigDecimal(split[2])));
			}
			else
			{
				coeffOtherH.put(split[0], new Pair<BigDecimal, BigDecimal> (new BigDecimal(split[1]), new BigDecimal(split[2])));
			}
			line = in.readLine();
		}
		//manually put these in, as there is no 'coefficients' corresponding to baseline PIC0
		coeffOtherH.put("PIC0", new Pair <BigDecimal, BigDecimal>(new BigDecimal("0"),new BigDecimal("0")));
		coeffOtherH.put("Cycle:PIC0", new Pair <BigDecimal, BigDecimal>(new BigDecimal("0"), new BigDecimal("0")));		
		coeffOtherH.put("RefLen:PIC0", new Pair <BigDecimal, BigDecimal>(new BigDecimal("0"), new BigDecimal("0")));
	}
	
	@Override
	public void loadProbabilities(String probFile) 
	{
		try
		{
			URL url = getClass().getResource(probFile);
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

			//TESTING
			//BufferedReader in = new BufferedReader(new FileReader(probFile));
			
			String header = in.readLine();

			if(header.split(",").length != 6)
			{
				throw new Exception("Call probabilities does not have six columns as expected");
			}

			String line = in.readLine();

			while(line != null)
			{				
				String [] fields = line.split(",");				
				
				int mode = Integer.parseInt(fields[0]);
				int cycleNumber = Integer.parseInt(fields[1]);
				int flowInCycleNumber = Integer.parseInt(fields[2]);
				
				BigDecimal lessThan = new BigDecimal(fields[3]).setScale(SCALE, BigDecimal.ROUND_HALF_UP);
				BigDecimal equalTo = new BigDecimal(fields[4]).setScale(SCALE, BigDecimal.ROUND_HALF_UP);
				BigDecimal moreThan = new BigDecimal(fields[5]).setScale(SCALE, BigDecimal.ROUND_HALF_UP);
				
				BigDecimal summed = lessThan.add(equalTo).add(moreThan).setScale(SCALE, BigDecimal.ROUND_HALF_UP);
				
				//attempt to fix rounding errors (large ones) due to R precision.
				if(! summed.equals(new BigDecimal(1).setScale(SCALE, BigDecimal.ROUND_HALF_UP)))
				{
					//R flow arithemetic may result in prob > 1. So, take it out of the equal to class.
					lessThan = lessThan.divide(summed, SCALE, BigDecimal.ROUND_HALF_EVEN);
					equalTo = equalTo.divide(summed, SCALE, BigDecimal.ROUND_HALF_EVEN);
					moreThan = moreThan.divide(summed, SCALE, BigDecimal.ROUND_HALF_EVEN);
				}
				
				BigDecimal [] probs = {lessThan, equalTo, moreThan};
				this.myProbs.put(mode,cycleNumber, flowInCycleNumber, probs);
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
	
	public void bulkCalculateProbabilities(HashMap <String, String> settings, AcaciaLogger logger) throws Exception
	{
		String flowCycle = settings.get(AcaciaConstants.OPT_FLOW_CYCLE_ION_TORRENT);

		for(int i = 0; i <= maxPreLoadHPLen ;i++)
		{
			for(int j = 0; j < maxPreLoadCycles; j++)
			{
				for (int k = 0; k < flowCycle.length(); k++)
				{
					BigDecimal [] probs = _calculateProbabilitiesHelper(i, j, k);
					this.myProbs.put(i, j, k, probs);
				}
			}
		}
	}


	public BigDecimal [] calculateProbabilities(AcaciaLogger logger, HashMap <String, Object> factors, FlowCycler cycler) throws Exception
	{
		int flowPosition = (Integer)factors.get(OUFrequencyTable.FLOW_POSITION);
		int cycleNumber = cycler.flowPositionToCycleNumber(flowPosition);
		int posInCycle = cycler.flowPositionToPosInCycle(flowPosition);
		int obsMode = (Integer)factors.get(OUFrequencyTable.RLE_LENGTH);
		
		return _calculateProbabilitiesHelper(obsMode, cycleNumber, posInCycle);
	}
	
	public BigDecimal []  _calculateProbabilitiesHelper(int obsMode, int cycleNumber, int posInCycle) throws Exception
	{
		//need to calculate probability for zero
		//need to calculate probability for one
		//need to calculate probability for more than one
		BigDecimal [] res;
		switch(obsMode)
		{
			case 0:
				res = calculateProbsZero(cycleNumber, posInCycle);
				break;
			case 1:
				res = calculateProbsOne(cycleNumber, posInCycle);
				break;
			default:
				res = calculateProbsAll(obsMode, cycleNumber, posInCycle);
				break;
		}
		
		BigDecimal sum = new BigDecimal("0");
		sum = sum.add(res[0].add(res[1].add(res[2]))).setScale(SCALE);
		
		if(! sum.equals(new BigDecimal("1").setScale(SCALE)))
		{
			res[0] = res[0].divide(sum, SCALE, BigDecimal.ROUND_HALF_EVEN);
			res[1] = res[1].divide(sum, SCALE, BigDecimal.ROUND_HALF_EVEN);
			res[2] = res[2].divide(sum, SCALE, BigDecimal.ROUND_HALF_EVEN);
		}
		
		return res;
	}
	
	
	private  BigDecimal [] calculateProbsZero (int cycleNumber, int posInCycle) throws Exception
	{
		//to do the binomial model
		BigDecimal PICCoef = coeffZeroH.get("PIC" + posInCycle);
		BigDecimal cycleCoef = coeffZeroH.get("Cycle");
		BigDecimal intercept = coeffZeroH.get("Intercept");
		BigDecimal cyc = new BigDecimal(cycleNumber);
		
		BigDecimal probOfOvercall = new BigDecimal("1").divide(
				new BigDecimal("1").add(new BigDecimal("1").divide(
						new BigDecimal(Math.exp(intercept.add(PICCoef).add(cycleCoef.multiply(cyc)).doubleValue())), SCALE, BigDecimal.ROUND_HALF_EVEN)),
						SCALE, BigDecimal.ROUND_HALF_EVEN);
		
		BigDecimal [] probabilities = {new BigDecimal("0"), new BigDecimal("1").subtract(probOfOvercall), probOfOvercall};
		
		return probabilities;
	}
	
	private BigDecimal [] calculateProbsOne (int cycleNumber, int posInCycle) throws Exception
	{
		String picString = "PIC" + posInCycle;
		Pair <BigDecimal, BigDecimal> intercept = this.coeffOneH.get("Intercept");
		Pair <BigDecimal, BigDecimal> PICCoef = this.coeffOneH.get(picString);		
		Pair <BigDecimal, BigDecimal> cycleCoef = this.coeffOneH.get("Cycle");
		BigDecimal cyc = new BigDecimal(cycleNumber);
		
		BigDecimal predUnder = intercept.getFirst().add(cycleCoef.getFirst().multiply(cyc)).add(PICCoef.getFirst());
		BigDecimal predOver = intercept.getSecond().add(cycleCoef.getSecond().multiply(cyc)).add(PICCoef.getSecond());
		BigDecimal predAt = new BigDecimal("1").subtract(predUnder).subtract(predOver);
		
		BigDecimal probUnder = BigDecimalUtils.exp(predUnder, SCALE).divide((BigDecimalUtils.exp(predUnder, SCALE).add(BigDecimalUtils.exp(predOver, SCALE)).add(BigDecimalUtils.exp(predAt, SCALE))), SCALE, BigDecimal.ROUND_HALF_UP);
		
		BigDecimal probOver = BigDecimalUtils.exp(predOver, SCALE).divide(BigDecimalUtils.exp(predUnder, SCALE).add(BigDecimalUtils.exp(predOver, SCALE)).add(BigDecimalUtils.exp(predAt, SCALE)), SCALE, BigDecimal.ROUND_HALF_UP);
		//this is a multinomial
		return new BigDecimal [] {probUnder, new BigDecimal("1.0").subtract(probUnder).subtract(probOver).setScale(SCALE, BigDecimal.ROUND_HALF_UP), probOver}; //make sure this is returning the correct order.
	}
	
	private BigDecimal [] calculateProbsAll(int obsMode, int cycleNumber, int posInCycle)
	{
		String picString = "PIC" + posInCycle;
		
		BigDecimal obsModeBD = new BigDecimal(obsMode);
		BigDecimal cycleNumberBD = new BigDecimal(cycleNumber);
		
		//all the coefficient values
		Pair <BigDecimal, BigDecimal> intercept = this.coeffOtherH.get("Intercept");
		Pair <BigDecimal, BigDecimal> PICCoef = this.coeffOtherH.get(picString);
		Pair <BigDecimal, BigDecimal> cycleCoef = this.coeffOtherH.get("Cycle");
		Pair <BigDecimal, BigDecimal> reflenCoef = this.coeffOtherH.get("RefLen");
		Pair <BigDecimal, BigDecimal> reflenSquaredCoef = this.coeffOtherH.get("I(RefLen^2)");
		Pair <BigDecimal, BigDecimal> cyclePICCoef = this.coeffOtherH.get("Cycle:"+picString);
		Pair <BigDecimal, BigDecimal> reflenCycleCoef = this.coeffOtherH.get("RefLen:Cycle"); 
		Pair <BigDecimal,BigDecimal> reflenPICCoef = this.coeffOtherH.get("RefLen:"+ picString);
		
		BigDecimal predUnder = 
			intercept.getFirst().add( 
			reflenCoef.getFirst().multiply(obsModeBD)).add( 
			BigDecimalUtils.intPower(obsModeBD, 2, SCALE).multiply(reflenSquaredCoef.getFirst())).add(  
			cycleCoef.getFirst().multiply(cycleNumberBD)).add( 
			PICCoef.getFirst()).add( 
			cyclePICCoef.getFirst().multiply(cycleNumberBD)).add( 
			reflenCycleCoef.getFirst().multiply(obsModeBD).multiply(cycleNumberBD)).add( 
			reflenPICCoef.getFirst().multiply(obsModeBD));
				
		BigDecimal predOver = 
			intercept.getSecond().add( 
			reflenCoef.getSecond().multiply(obsModeBD)).add( 
			BigDecimalUtils.intPower(obsModeBD, 2, SCALE).multiply(reflenSquaredCoef.getSecond())).add(
			cycleCoef.getSecond().multiply(cycleNumberBD)).add( 
			PICCoef.getSecond()).add(
			cyclePICCoef.getSecond().multiply(cycleNumberBD)).add( 
			reflenCycleCoef.getSecond().multiply(obsModeBD).multiply(cycleNumberBD)).add( 
			reflenPICCoef.getSecond().multiply(obsModeBD));
		
		BigDecimal predAt = new BigDecimal("0");
		BigDecimal probUnder = BigDecimalUtils.exp(predUnder, SCALE).divide(
				BigDecimalUtils.exp(predUnder, SCALE).add(BigDecimalUtils.exp(predOver, SCALE)).add(
						BigDecimalUtils.exp(predAt, SCALE)), SCALE, BigDecimal.ROUND_HALF_UP
						
		
		);
		
		BigDecimal probOver = BigDecimalUtils.exp(predOver, SCALE).divide(
				BigDecimalUtils.exp(predUnder, SCALE).add(BigDecimalUtils.exp(predOver, SCALE)).add(
						BigDecimalUtils.exp(predAt, SCALE)), SCALE, BigDecimal.ROUND_HALF_UP
		);
		
		//this is a multinomial
		return new BigDecimal [] {probUnder, (new BigDecimal("1").subtract(probUnder).subtract(probOver).setScale(SCALE, BigDecimal.ROUND_HALF_UP)), probOver}; //make sure this is returning the correct order.
		//this is a multinomial.
	}

	@Override
	public BigDecimal [] getProbabilities(AcaciaLogger logger, HashMap<String, Object> factors, FlowCycler cycler) throws Exception
	{
		//cycler knows all..
		int flowPosition = (Integer)factors.get(OUFrequencyTable.FLOW_POSITION);
		int cycleNumber = cycler.flowPositionToCycleNumber(flowPosition);
		int posInCycle = cycler.flowPositionToPosInCycle(flowPosition);
		int refLen = (Integer)factors.get(OUFrequencyTable.RLE_LENGTH);
		
		boolean calculatedProbs = false;
		
		if(! this.myProbs.contains(refLen, cycleNumber, posInCycle))
		{
			BigDecimal [] res = calculateProbabilities(logger, factors, cycler); //stores it in the hash
			this.myProbs.put(refLen, cycleNumber, posInCycle, res);
			calculatedProbs = true;
		}
		
		BigDecimal sumProbs = new BigDecimal("0");
		
		for(BigDecimal prob: this.myProbs.get(refLen, cycleNumber, posInCycle))
		{
			sumProbs = sumProbs.add(prob);
		}
		
		if(! sumProbs.setScale(10, BigDecimal.ROUND_HALF_UP).equals(new BigDecimal("1").setScale(10, BigDecimal.ROUND_HALF_UP)))
		{
			throw new Exception("Sum of probabilities less than zero or greater than 1: Calculated? " + calculatedProbs);
		}
		
		return this.myProbs.get(refLen, cycleNumber, posInCycle);
	}
	
	@Override
	public BigDecimal [] estimateProbability(AcaciaLogger logger, HashMap<String, Object> factors, FlowCycler cycler)throws Exception 
	{
		return this.calculateProbabilities(logger, factors, cycler);
	}

	@Override
	public BigDecimal [] getEmpiricalProbability(AcaciaLogger logger, HashMap<String, Object> factors, FlowCycler cycler) throws Exception 
	{
		int flowPosition = (Integer)factors.get(OUFrequencyTable.FLOW_POSITION);
		int cycleNumber = cycler.flowPositionToCycleNumber(flowPosition);
		int posInCycle = cycler.flowPositionToPosInCycle(flowPosition);
		int rleLength = (Integer)factors.get(OUFrequencyTable.RLE_LENGTH);
		
		if(this.myProbs.contains(rleLength, cycleNumber, posInCycle))
		{
			return this.myProbs.get(rleLength, cycleNumber, posInCycle);
		}
		return null;
	}
	
	private class NestedHash
	{
		//RLE LENGTH, CYCLE POS, PIC POS,
		
		private HashMap <Integer, HashMap <Integer, HashMap <Integer, BigDecimal [] >>> nesty;

		public NestedHash()
		{				//RefLen            //Cycle           //PIC
			this.nesty = new HashMap <Integer, HashMap <Integer, HashMap <Integer, BigDecimal [] >>> ();
		}

		public void put(int rleLength, int cycleNumber, int flowInCycleNumber, BigDecimal [] probs) throws Exception
		{
			HashMap <Integer, HashMap <Integer, BigDecimal [] >> rleHash;
			
			if(! nesty.containsKey(rleLength))
			{
				rleHash = new HashMap <Integer, HashMap <Integer, BigDecimal [] >> ();
				nesty.put(rleLength, rleHash);
			}
			else
			{
				rleHash = nesty.get(rleLength);
			}

			HashMap <Integer, BigDecimal []> cycleNumberHash; 

			if(! rleHash.containsKey(cycleNumber))
			{
				cycleNumberHash = new HashMap <Integer, BigDecimal []>();
				rleHash.put(cycleNumber, cycleNumberHash);
			}
			else
			{
				cycleNumberHash = rleHash.get(cycleNumber);
			}
			
			cycleNumberHash.put(flowInCycleNumber, probs);
		}

		public BigDecimal [] get(int rleLength, int cycleNumber, int flowInCycleNumber) throws Exception
		{
			if(this.nesty.containsKey(rleLength) && this.nesty.get(rleLength).containsKey(cycleNumber) 
					&& this.nesty.get(rleLength).get(cycleNumber).containsKey(flowInCycleNumber))
				return this.nesty.get(rleLength).get(cycleNumber).get(flowInCycleNumber);
			return null;
		}
		
		public boolean contains(int rleLength, int cycleNumber, int flowInCycleNumber)
		{
			if(this.nesty.containsKey(rleLength))
			{
				if(this.nesty.get(rleLength).containsKey(cycleNumber))
				{
					if(this.nesty.get(rleLength).get(cycleNumber).containsKey(flowInCycleNumber))
					{
						return true;
					}
				}
			}
			return false;
		}
	}
	
//	TESTING
 /*
	public static void main(String [] args)
	{
		AcaciaLogger logger = new AcaciaLogger();
		FlowCycler cycler = new FlowCycler(AcaciaConstants.OPT_FLOW_CYCLE_ION_TORRENT, logger);
		
		try
		{
			//TODO: to ensure it found the file, had to change alot of code, which will need to change back.
			
			
			IonTorrentOUCallFrequencyTable it = new IonTorrentOUCallFrequencyTable(null, logger, 
				"E:\\GitRepo\\AcaciaEclipse\\Acacia\\src\\data\\100bpOneTouch_316_counts_complete.csv",
				"E:\\GitRepo\\AcaciaEclipse\\Acacia\\src\\data\\100bpOneTouch_316_counts_complete_model_zeroes.csv", 
				"E:\\GitRepo\\AcaciaEclipse\\Acacia\\src\\data\\100bpOneTouch_316_counts_complete_model_ones.csv", 
				"E:\\GitRepo\\AcaciaEclipse\\Acacia\\src\\data\\100bpOneTouch_316_counts_complete_model_others.csv");
	
			
			//probs zero works
			
			BigDecimal [] res;
			
			for(int i = 0; i <= 25; i++)
			{
				res = it._calculateProbabilitiesHelper(0, i, 0);
				System.out.println("Probabilities for i: "+ i + " " + res[0] + ", " + res[1] +", " + res[2]);
				
			}
						
			BigDecimal [] res2 = it._calculateProbabilitiesHelper(1,0,6);
			
			for(int i = 0; i < res2.length; i++)
				System.out.println(res2[i].setScale(SCALE,  BigDecimal.ROUND_HALF_UP));
			
			BigDecimal [] res3 = it._calculateProbabilitiesHelper(2, 3, 12);
			
			for(int i = 0; i < res3.length; i++)
				System.out.println(res3[i].setScale(SCALE,  BigDecimal.ROUND_HALF_UP));
			
			//okay all the get probabilities are tested. Interestingly should cache them so not calculating again.
			
			HashMap <String, Object> factors = new HashMap <String, Object>();
			factors.put(OUFrequencyTable.FLOW_POSITION, 33);
			factors.put(OUFrequencyTable.RLE_LENGTH, 2);
			
			System.out.println("Getting probabilities");
			
			//what I was actually looking up is PIC 1 in cycle 1 for reflen 2.
			BigDecimal [] res4 = it.getProbabilities(logger, factors, cycler);
			
			for(int i = 0; i < res4.length; i++)
				System.out.println(res4[i].setScale(SCALE,  BigDecimal.ROUND_HALF_UP));
			
			
			//pretend there is a cycle 12..
			HashMap <String, Object> factors2 = new HashMap <String, Object>();
			factors2.put(OUFrequencyTable.FLOW_POSITION, 386);
			factors2.put(OUFrequencyTable.RLE_LENGTH, 1);
			
			BigDecimal [] res5 = it.getProbabilities(logger, factors2, cycler);
			
			for(int i = 0; i < res5.length; i++)
				System.out.println(res5[i].setScale(SCALE,  BigDecimal.ROUND_HALF_UP));
			
			
		}
		catch(Exception e)
		{
			System.out.println("An exception ocurred: " + e.getMessage());
			e.printStackTrace();
			
		}
	}
	*/
}
