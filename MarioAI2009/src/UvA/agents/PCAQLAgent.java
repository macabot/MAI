/*
 * TODO should pcam be trained with visitedStates that contains duplicate states
 */

package UvA.agents;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import UvA.stateSpaceReduction.PCAMeans;
import UvA.states.MarioState;
import UvA.states.PCAState;
import UvA.states.State;
import UvA.states.StateActionPair;
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
		this.seenRepresentations = new HashSet<double[]>();
	}//end constructors
	
	public PCAQLAgent(String name, Map<StateActionPair, Double> qValuesIn, 
			int numComponents, int clusterAmount, int iterations)
	{
		super(name);
		double[][] representations = extractRepresentations(qValuesIn);
		this.pcam = new PCAMeans(representations, numComponents, clusterAmount, iterations);
		super.qValues = projectQValues(qValuesIn);
	}
	
	/**
	 * Convert the states in 'qValues' with pcam and create new q-values.
	 * @param qValues dictionary that maps state-action pairs to their value
	 * @return projected q-values
	 */
	public Map<StateActionPair, Double> projectQValues(Map<StateActionPair, Double> qValues)
	{
		Map<StateActionPair, Double> projectedQValues = new HashMap<StateActionPair, Double>();
		for( StateActionPair sap: qValues.keySet() )
		{
			int index = pcam.sampleToMean(sap.state.getRepresentation());
			State projectedState = new PCAState(sap.state.getRepresentation(), index);
			StateActionPair projectedSap = new StateActionPair(projectedState, sap.action);
			projectedQValues.put(projectedSap, qValues.get(sap));
		}
		return projectedQValues;
	}
	
	public static double[][] extractRepresentations(Map<StateActionPair, Double> qValues)
	{
		double[][] representations = new double[qValues.size()][];
		int i=0;
		for( StateActionPair sap: qValues.keySet() )
		{
			representations[i] = sap.state.getRepresentation();
			i++;
		}
		return representations;
	}

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
			return new PCAState(environmentIn, pcam);
		} else if( stateType.equals("MarioState") ) 
		{
			return new MarioState(environmentIn);
		}
		else
		{
			System.out.printf("Unknown state-type: %s\n", stateType);
			return null;
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
	public void loadSeenRepresentations(String path)
	{
		try{
			this.seenRepresentations = (Set<double[]>) SLAPI.load(path);
		}catch( Exception e )
		{
			e.printStackTrace();
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
