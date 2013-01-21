/*
 * TODO should pcam be trained with visitedStates that contains duplicate states
 */

package UvA.agents;

import java.util.HashMap;
import java.util.Map;

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

	protected final PCAMeans pcam;
	
	public PCAQLAgent(Map<StateActionPair, Double> qValuesIn, 
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
			String error = String.format("Unknown state-type: %s", stateType);
			throw new IllegalArgumentException(error);
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

}//end class
