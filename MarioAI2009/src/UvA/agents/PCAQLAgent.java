package UvA.agents;

import java.util.HashMap;
import java.util.Map;

import UvA.stateSpaceReduction.PCAMeans;
import UvA.states.PCAState;
import UvA.states.State;
import UvA.states.StateActionPair;
import ch.idsia.mario.environments.Environment;

public class PCAQLAgent extends QLearnAgent 
{
	// agent specific values
	static private final String name = "PCAQLAgent";
	protected final String stateType = "PCAState";

	protected PCAMeans pcam;
	
	/**
	 * Create new PCAQLAgent. Q-values and pcam must be loaded with 
	 * loadQValues/1 (located in QLearnAgent) and loadPCAM/1. 
	 */
	public PCAQLAgent()
	{
		super(name);
		pcam = null;
	}//end constructor
	
	/**
	 * Convert the states in 'qValues' to PCAState using pcam 
	 * and return the new q-values.
	 * @param qValues - dictionary that maps state-action pairs to their value
	 * @return converted q-values
	 */
	public Map<StateActionPair, Double> projectQValues(Map<StateActionPair, Double> qValues)
	{
		
		Map<StateActionPair, Integer> frequencies = new HashMap<StateActionPair, Integer>();
		Map<StateActionPair, Double> projectedQValues = new HashMap<StateActionPair, Double>();
		for( StateActionPair sap: qValues.keySet() )
		{
			int index = pcam.sampleToMean(sap.state.getRepresentation()); // convert State to PCAState
			State projectedState = new PCAState(sap.state.getRepresentation(), index);
			StateActionPair projectedSap = new StateActionPair(projectedState, sap.action);
			if( projectedQValues.containsKey(projectedSap) )
			{
				int newFreq = frequencies.get(projectedSap)+1;	// add 1 to frequency
				frequencies.put(projectedSap, newFreq);	
				// newAverage = (newValue + (freq-1)*average) / freq
				double newQValue = (qValues.get(sap) + (newFreq-1)*projectedQValues.get(projectedQValues)) / newFreq;	// take average over all qValues
				projectedQValues.put(projectedSap, newQValue);
			}else
			{
				frequencies.put(projectedSap, 1);
				projectedQValues.put(projectedSap, qValues.get(sap));
			}
		}
		return projectedQValues;
	}//end projectQValues
	
	/**
	 * Extract all the states in 'qValues' and return their representations.
	 * @param qValues - dictionary mapping state-action pairs to their value
	 * @return double array containing representations of states in 'qValues'
	 */
	private static double[][] extractRepresentations(Map<StateActionPair, Double> qValues)
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
	
	@Override
	public State createState(Environment environmentIn)
	{
		return new PCAState(environmentIn, pcam);			
	}
	
	/**
	 * Load q-values according to path and project the states in the state-action pairs
	 * @param path - path where the q-values are stored
	 */
	@SuppressWarnings("unchecked") // hack to remove annoying warning of casting
	public void loadAndProjectQValues(String path, int numComponents, int clusterAmount, int iterations) 
	{
		try {
			Map<StateActionPair, Double> qValuesTemp = (HashMap<StateActionPair, Double>) SLAPI.load(path);
			double[][] representations = extractRepresentations(qValuesTemp);
			this.pcam = new PCAMeans(representations, numComponents, clusterAmount, iterations);
			super.qValues = projectQValues(qValuesTemp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	} 
	
	/**
	 * Load pcam according to path
	 * @param path - path where the pcam is stored
	 */
	public void loadPCAM(String path) {
		try {
			this.pcam = (PCAMeans) SLAPI.load(path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	} // end loadQValues
	
	/**
	 * Save pcam according to path
	 * @param path - path where the pcam is to be saved
	 */
	public void writePCAM(String path) {
		try {
			SLAPI.save(pcam, path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	} // end loadQValues

}//end class
