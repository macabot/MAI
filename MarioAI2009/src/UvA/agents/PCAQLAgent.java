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

	protected PCAMeans pcam;
	
	public PCAQLAgent()
	{
		super(name);
		pcam = null;
	}
	
	/**
	 * Convert the states in 'qValues' with pcam and create new q-values.
	 * @param qValues dictionary that maps state-action pairs to their value
	 * @return projected q-values
	 */
	public Map<StateActionPair, Double> projectQValues(Map<StateActionPair, Double> qValues)
	{
		
		Map<StateActionPair, Integer> frequencies = new HashMap<StateActionPair, Integer>();
		Map<StateActionPair, Double> projectedQValues = new HashMap<StateActionPair, Double>();
		for( StateActionPair sap: qValues.keySet() )
		{
			int index = pcam.sampleToMean(sap.state.getRepresentation());
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
	 * Load qvalues according to path, called from main run
	 * @param path is the path where the qvalues are stored
	 */
	@SuppressWarnings("unchecked") // hack to remove annoying warning of casting
	public void loadAndProjectQValues(String path, int numComponents, int clusterAmount, int iterations) 
	{
		try {
			Map<StateActionPair, Double> qValuesTemp = (HashMap<StateActionPair, Double>) SLAPI.load(path);
			double[][] representations = extractRepresentations(qValuesTemp);
			this.pcam = new PCAMeans(representations, numComponents, clusterAmount, iterations);
			super.qValues = projectQValues(qValuesTemp); //TODO change
			// if clusterAmount = -1 then just use index of Dataset.. if not, just cluster
		} catch (Exception e) {
			e.printStackTrace();
		}
	} // end loadQValues
	
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
