/*
 * TODO should pcam be trained with visitedStates that contains duplicate states
 */

package UvA.agents;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import UvA.stateSpaceReduction.PCAMeans;
import UvA.states.*;
import ch.idsia.mario.environments.Environment;

public class PCAQLAgent extends QLearnAgent 
{
	// agent specific values
	static private final String name = "PCAQLAgent";
	protected final String stateType = "PCAState";

	protected Set<double[]> seenRepresentations; // representations of visited states
	protected final PCAMeans pcam;
	
	public PCAQLAgent()
	{
		this(name);
	}
	
	public PCAQLAgent(String name)
	{
		this(null, name);
	}
	
	public PCAQLAgent(PCAMeans pcam)
	{
		this(pcam, name);
	}
	
	public PCAQLAgent(PCAMeans pcam, String name)
	{
		this(new HashMap<StateActionPair, Double>(), pcam, name);		
	}

	public PCAQLAgent(Map<StateActionPair, Double> qValuesIn, PCAMeans pcam, String name) 
	{
		super(qValuesIn, name);
		this.pcam = pcam;
	}//end constructors

	/**
	 * getAction function is called by the engine to retrieve an action from mario
	 */
	public boolean[] getAction(Environment environment)
	{

		state = createState(environment);
		seenRepresentations.add(state.getRepresentation());	// add MarioState

		// update q and return action
		updateQValue();
		returnAction = eGreedyAction();

		// update oldState for updateQValue()
		oldState = state.clone();

		return returnAction;

	} // end getAction()
	
	public State createState(Environment environmentIn)
	{
		return createState(environmentIn, stateType);
	}
	
	public State createState(Environment environmentIn, String stateType)
	{
		if( stateType.equals("PCAState") ) {
			PCAState curState = (PCAState) state;
			if(curState != null)
				return new PCAState(environmentIn, curState.xPos, pcam);
			else
				return new PCAState(environmentIn, 32, pcam);
		} else if( stateType.equals("MarioState") ) 
		{
			MarioState curState = (MarioState) state;
			if(curState != null)
				return new MarioState(environmentIn, curState.xPos);
			else
				return new MarioState(environmentIn, 32);
		}
		else
		{
			System.out.printf("Unknown state-type: %s\n", stateType);
			return null;
		}			
	}
	
	public void representationsToText(String path)
	{
		String s = "";
		for(double[] vector: seenRepresentations)
		{
			s += Arrays.toString(vector).replace(", ", " ") + "\n";
		}
		BufferedWriter out;
		try {
			out = new BufferedWriter(new FileWriter(path));
			out.write(s);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Load pcam according to path
	 * @param path is the path where the pcam is stored
	 */
	public static PCAMeans loadPCAM(String path) {
		try {
			return (PCAMeans) SLAPI.load(path);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	} // end loadQValues
	
	@SuppressWarnings("unchecked")
	public static Set<double[]> loadSeenRepresentations(String path)
	{
		try{
			return (Set<double[]>) SLAPI.load(path);
		}catch( Exception e )
		{
			e.printStackTrace();
			return null;
		}		
	}
	
	/**
	 * Save pcam according to path
	 * @param path is the path where the pcam is to be saved
	 */
	public void writePCAM(String path) {
		try {
			SLAPI.save(pcam, path);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	} // end loadQValues
	
	public void writeSeenRepresentations(String path)
	{
		try {
			SLAPI.save(seenRepresentations, path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}//end class
