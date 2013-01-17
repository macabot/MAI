package UvA.agents;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.idsia.mario.environments.Environment;

import UvA.stateSpaceReduction.PCAMeans;

public class PCAQLAgent extends QLearnAgent 
{
	// agent specific values
	static private final String name = "PCAQLAgent";
	protected final String stateType = "PCAState";

	List<State> visitedStates;
	PCAMeans pcam;
	State pcaState;

	public PCAQLAgent(String pcamPath)
	{
		this(loadPCAM(pcamPath));
	}
	
	public PCAQLAgent(PCAMeans pcam)
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

		state = createState(environment, "MarioState");
		visitedStates.add(state);	// add MarioState

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
		if( stateType.equals("PCaState") ) {
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
			System.out.println("Unknown state-type");
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
