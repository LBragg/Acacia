package pyromaniac.DataStructures;

import java.util.Iterator;

import pyromaniac.AcaciaConstants;
import pyromaniac.IO.AcaciaLogger;

public class FlowCycler 
{
//	private String cycle; //do I need to store this then?
	private final char [] cycle; 
	public static final int FLOWED_BASE = 0;
	public static final int FLOW_POSITION = 1;
	public static final int WITHIN_CYCLE_POSITION = 2;
	private AcaciaLogger logger;
	
	public FlowCycler(String cycleStr, AcaciaLogger logger)
	{
		this.cycle = cycleStr.toCharArray();
		this.logger = logger;
	}
	
	//okay, what the alignment needs to know is what flow would this be, given the last base was X, and the current base is X.
	
	//assumes NO N's.
	public int minPossibleFlowsBetweenFlowPosXAndCharY(int flowPosX, char y) throws Exception
	{
		//recall that there will be no Ns in the 454 at this point.
		int posInCycle = (flowPosX % cycle.length); 
	
		//does not make sense for consecutive bases to be passed?
		if(cycle[posInCycle] == y)
		{
			return 0;
		}

		//could choose to grab the iterator, and find this flow cycle pos.
		int position = posInCycle + 1;
		
		if(position == cycle.length)
			position = 0;
		
		//make sure this makes sense if x == y???
		int numFlows = 1;
		
		while(true)
		{
			char curr = cycle[position];
			
			if(curr == y)
				break;
			else
			{
				if(position == cycle.length - 1)
				{
					position = 0;
				}
				else
				{
					position++;
				}
				numFlows++;
			}
		}
		
		return numFlows;
	}
	
	public int flowPositionToPosInCycle (int flowPosition)
	{
		return flowPosition % this.cycle.length;
	}

	public int flowPositionToCycleNumber (int flowPosition)
	{
		return (flowPosition / this.cycle.length); 
	}
	
	
	public CycleIterator iterator()
	{
		return new CycleIterator();	
	}
	
	class CycleIterator implements Iterator <int []>
	{
		private int cyclePosition;
		private int flowPosition;
		
		public CycleIterator()
		{
			cyclePosition = 0;
			flowPosition = 0;
		}
		
		//its cyclic.
		public boolean hasNext() 
		{
			return true;
		}

		@Override
		public int [] next() 
		{
			char base = cycle[cyclePosition];
			
			int [] res = new int []{base, flowPosition, cyclePosition};
			cyclePosition = (cyclePosition == (cycle.length - 1))? 0: cyclePosition + 1;
			flowPosition++;

			return res;
		}

		@Override
		public void remove() 
		{
			return;
		}		
	}

	public char getBaseAtCyclePos(int positionInCycle) 
	{
		return this.cycle[positionInCycle];
	}
	
	/*
	public static void main (String [] args)
	{
		FlowCycler fc = new FlowCycler(AcaciaConstants.OPT_FLOW_CYCLE_454);
		
		String madeUp = "TCAGATACGA";
	
		try
		{
		
			int maxFlowsBetween = fc.minPossibleFlowsBetweenFlowPosXAndCharY(9, 'T');
			
			System.out.println("Max flows between position 9 and the next T is : " + maxFlowsBetween);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
		
		
		CycleIterator it = fc.iterator();
		
		int [] currFlow = it.next();
		
		for(int i = 0; i < madeUp.length(); i++)
		{
			char curr = madeUp.charAt(i);
			
			while(curr != currFlow[FlowCycler.FLOWED_BASE])
			{
				currFlow = it.next();
			}			
			System.out.println("Current base <" + curr + "> at position <" + i + "> maps to flow: " + currFlow[FlowCycler.FLOW_POSITION] + " within cycle position: " + currFlow[FlowCycler.WITHIN_CYCLE_POSITION]);
		}
		
	}
	*/
}
