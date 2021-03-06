package UvA.agents;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
	private int numComponents;
	private int clusterAmount;
	private int iterations;
	
	/**
	 * Create new PCAQLAgent. Q-values must be loaded with 
	 * loadQValues/1 (located in QLearnAgent) or loadAndProjectQValues/1.
	 * pcam must be loaded with loadPCAM/1. numComponents, clusterAmount and
	 * iterations are set with setAllProperties/1.
	 */
	public PCAQLAgent()
	{
		super(name);
		this.pcam = null;
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
			State projectedState = new PCAState(sap.state, index);
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
	 * Does not contain duplicates.
	 * @param qValues - dictionary mapping state-action pairs to their value
	 * @return double array containing representations of states in 'qValues'
	 */
	public static double[][] extractRepresentations(Map<StateActionPair, Double> qValues)
	{
		Map<Integer, double[]> uniqueRepresentations = new HashMap<Integer, double[]>();
		for( StateActionPair sap: qValues.keySet() )
		{
			double[] tempRepr = sap.state.getRepresentation();
			uniqueRepresentations.put(Arrays.hashCode(tempRepr), tempRepr);
		}
		Collection<double[]> representationSet = uniqueRepresentations.values();
		return collectionToArray(representationSet);
	}
	
	public static double[][] collectionToArray(Collection<double[]> col)
	{
		double[][] array = new double[col.size()][];
		int i=0;
		for(double[] element: col)
		{
			array[i] = element;
			i++;
		}
		return array;
	}
	
	@Override
	public State createState(Environment environmentIn, State oldState)
	{
		return new PCAState(environmentIn, oldState, pcam);			
	}
	
	/**
	 * Load q-values according to path and project the states in the state-action pairs
	 * @param path - path where the q-values are stored
	 */
	@SuppressWarnings("unchecked") // hack to remove annoying warning of casting
	public void loadAndProjectQValues(String path) 
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
	
	public void setPCAM(PCAMeans pcamIn)
	{
		this.pcam = pcamIn;
	}
	
	public PCAMeans getPCAM()
	{
		return pcam;
	}
	
	public void setNumComponents(int numComponentsIn)
	{
		this.numComponents = numComponentsIn;
	}
	
	public int getNumComponents()
	{
		return this.numComponents;
	}
	
	public void setClusterAmount(int clusterAmountIn)
	{
		this.clusterAmount = clusterAmountIn;
	}
	
	public int getClusterAmount()
	{
		return this.clusterAmount;
	}
	
	public void setIterations(int iterationsIn)
	{
		this.iterations = iterationsIn;
	}
	
	public int getIterations()
	{
		return this.iterations;
	}
	
	@Override
	public void setAllProperties(Properties properties){
		// set values QLearnAgent
		initialValue = Integer.parseInt(properties.getProperty("initialValue", "20"));
		epsilon = Double.parseDouble(properties.getProperty("epsilon", "0.1"));
		gamma = Double.parseDouble(properties.getProperty("gamma", "0.9"));
		alpha = Double.parseDouble(properties.getProperty("alpha", "0.3"));
		// set value PCAQLAgent
		this.numComponents = Integer.parseInt(properties.getProperty("numComponents", "2"));
		this.clusterAmount = Integer.parseInt(properties.getProperty("clusterAmount", "-1"));
		this.iterations = Integer.parseInt(properties.getProperty("iterations", "10"));
	}// end setAllProperties

}//end class
