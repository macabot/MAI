package UvA.agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.ai.BasicAIAgent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;


public class QLearnAgent extends BasicAIAgent implements Agent {

	// agent specific values
	static private final String name = "QLearnAgent";
	protected final String stateType = "MarioState";
	
	// used to create state
	State state = null;
	State oldState = null;
	
	// action to return
	protected boolean[] returnAction;
		
	// values used by qlearning
	protected Map<StateActionPair, Double> qValues;	// state-action values
	protected List<boolean[]> validActions;
 
	
	// settings for q learning
	final int initialValue = 20; // initial qvalues
	final double epsilon = 0.1; // epsilon used in picking an action
	final double gamma = 0.9; // gamma is penalty on delayed result
	final double alpha = 0.4; // learning rate
	
	// actions
	final boolean[] STAY = new boolean[Environment.numberOfButtons];
	final boolean[] JUMP = new boolean[Environment.numberOfButtons];
	final boolean[] SPEED = new boolean[Environment.numberOfButtons];
	final boolean[] JUMP_SPEED = new boolean[Environment.numberOfButtons];
	final boolean[] RIGHT = new boolean[Environment.numberOfButtons];
	final boolean[] RIGHT_JUMP = new boolean[Environment.numberOfButtons];
	final boolean[] RIGHT_SPEED = new boolean[Environment.numberOfButtons];
	final boolean[] RIGHT_JUMP_SPEED = new boolean[Environment.numberOfButtons];
	final boolean[] LEFT = new boolean[Environment.numberOfButtons];
	final boolean[] LEFT_JUMP = new boolean[Environment.numberOfButtons];
	final boolean[] LEFT_SPEED = new boolean[Environment.numberOfButtons];
	final boolean[] LEFT_JUMP_SPEED = new boolean[Environment.numberOfButtons];
	
	
	/**
	 *  Constructor of qlearn agent with a blank policy (to be learned)
	 */
	public QLearnAgent() {
		this(new HashMap<StateActionPair, Double>(), name);	
	} // end constructor without policy
	
	/**
	 * Constructor for a q learning agent with a given policy
	 * 
	 * @param plc is the policy the agent should handle
	 */
	public QLearnAgent(Map<StateActionPair, Double> qValuesIn, String name) {
		super(name);
		initiateValues();
		initialiseActions();
		this.qValues = qValuesIn;
	} // end constructor with policy
	

	/**
	 * getAction function is called by the engine to retrieve an action from mario
	 */
	public boolean[] getAction(Environment environment)
	{
		
		state = createState(environment);
		
		// update q and return action
		updateQValue();
		returnAction = eGreedyAction();
		
		// update oldState for updateQValue()
	    oldState = state.clone();
	    

	    return returnAction;

	} // end getAction()


	/**
	 * Get an action according to e-greedy. The greedy action is chosen with probability
	 * 1-e. The other actions get equal probability.
	 * 
	 * @return e-greedy action
	 */
	public boolean[] eGreedyAction()
	{
		// initialize variables
		Random generator = new Random();
		boolean[] bestAction = new boolean[Environment.numberOfButtons];
		double bestValue = 0;

		// find best action
		
		for(int i=0; i<validActions.size(); i++)
		{
			StateActionPair sap = new StateActionPair(state, validActions.get(i));
			double qValue = getStateActionValue(sap);
			if( qValue > bestValue )
			{
				bestAction = sap.action;
				bestValue = qValue;
			}
		}
		
		if( generator.nextDouble() < epsilon )	//  choose random action
		{
			List<boolean[]> randomActions = new ArrayList<boolean[]>(validActions);
			randomActions.remove(bestAction);	// don't choose the best action
			boolean[] randomAction = randomActions.get(generator.nextInt(randomActions.size()));
			return randomAction;
		}
		
		return bestAction;	// choose best action
	}

	/**
	 * Update the qValues according to Q learning methods
	 */
	public void updateQValue()
	{
		StateActionPair oldSap = new StateActionPair(oldState, returnAction);
		double oldQ = getStateActionValue(oldSap);
		
		double bestQValue = 0;
		for(int i=0; i<validActions.size(); i++)
		{
			StateActionPair sap = new StateActionPair(state, validActions.get(i));
			double Q = getStateActionValue(sap);
			if( Q > bestQValue )
				bestQValue = Q;
		}
		
		double reward = state.getReward();
		double updatedValue = oldQ + alpha*(reward + gamma*bestQValue - oldQ);
		
		qValues.put(oldSap, updatedValue);	// update qValue of State-action pair
	}
	
	/**
	 * This function returns the q value if present, else returns the initialValue
	 * @param sap state action pair
	 * @param initialValue the initial q value for all actions
	 * @return the action value
	 */
	public double getStateActionValue(StateActionPair sap){
		return (qValues.containsKey(sap))?
				qValues.get(sap):initialValue;
	}


	/**
	 * This function initializes the possible actions manually	
	 */
	public void initialiseActions(){
		JUMP[Mario.KEY_JUMP] = true;
		SPEED[Mario.KEY_SPEED] = true;
		JUMP_SPEED[Mario.KEY_JUMP] = JUMP_SPEED[Mario.KEY_SPEED] = true;
		RIGHT[Mario.KEY_RIGHT] = true;
		RIGHT_JUMP[Mario.KEY_RIGHT] = RIGHT_JUMP[Mario.KEY_JUMP] = true;
		RIGHT_SPEED[Mario.KEY_RIGHT] = RIGHT_SPEED[Mario.KEY_SPEED] = true;
		RIGHT_JUMP_SPEED[Mario.KEY_RIGHT] = RIGHT_JUMP_SPEED[Mario.KEY_JUMP] = 
				RIGHT_JUMP_SPEED[Mario.KEY_SPEED] = true;
		LEFT[Mario.KEY_LEFT] = true;
		LEFT_JUMP[Mario.KEY_LEFT] = LEFT_JUMP[Mario.KEY_JUMP] = true;
		LEFT_SPEED[Mario.KEY_LEFT] = LEFT_SPEED[Mario.KEY_SPEED] = true;
		LEFT_JUMP_SPEED[Mario.KEY_LEFT] = LEFT_JUMP_SPEED[Mario.KEY_JUMP] = 
				LEFT_JUMP_SPEED[Mario.KEY_SPEED] = true;
	}// end function initialiseActions
	
	/**
	 * Get valid actions returns the valid actions by adding all actions possible manually
	 * @return validactions boolean array
	 */
	public List<boolean[]> getValidActions()
	{
		// initiate valid actions
		List<boolean[]> validActions = new ArrayList<boolean[]>();
		
		validActions.add(STAY);
		validActions.add(JUMP);
		validActions.add(SPEED);
		validActions.add(JUMP_SPEED);
		validActions.add(RIGHT);
		validActions.add(RIGHT_SPEED);
		validActions.add(RIGHT_JUMP);
		validActions.add(RIGHT_JUMP_SPEED);
		validActions.add(LEFT);
		validActions.add(LEFT_SPEED);
		validActions.add(LEFT_JUMP);
		validActions.add(LEFT_JUMP_SPEED);
		
		return validActions;
	} // end getValidActions()
	
	/**
	 * Function is used for declaring some values necessarily for qLearning, 
	 * such as oldState, which needs to have a value
	 */
	public void initiateValues() {
		oldState = createState(null);
		state =  createState(null);
		returnAction = new boolean[Environment.numberOfButtons];
		validActions = getValidActions();
	} // end getvalidactions
	
	/**
	 * Creates a state depending on type declared at top of file
	 * @param stateType The type of state to be created
	 * @param environmentIn The information state needs in order to create a state
	 * @return the correct state type including information
	 */
	public State createState(Environment environmentIn)
	{
		if( stateType.equals("MarioState") ) {
			MarioState curState = (MarioState) state;
			if(curState != null)
				return new MarioState(environmentIn, curState.xPos);
			else{
				MarioState.resetStatic(2);
				return new MarioState(environmentIn, 32);
			}
		}
		else
		{
			System.out.println("Unknown state-type");
			return null;
		}			
	}
	

	
	/**
	 * Load qvalues according to path, called from main run
	 * @param path is the path where the qvalues are stored
	 */
	@SuppressWarnings("unchecked") // hack to remove annoying warning of casting
	public void loadQValues(String path) {
		try {
			qValues = (HashMap<StateActionPair, Double>) SLAPI.load(path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	} // end loadQValues
	
	/**
	 * Save qvalues according to path, called from main run
	 * @param path is the path where the qvalues are to be saved
	 */
	public void writeQValues(String path) {
		try {
			SLAPI.save(qValues, path);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	} // end loadQValues
} // end class
