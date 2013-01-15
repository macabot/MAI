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
	State state;
	State oldState;
	
	// action to return
	private boolean[] returnAction;
		
	// values used by qlearning
	protected Map<StateActionPair, Double> qValues;	// state-action values
	protected List<boolean[]> validActions;
 
	
	// settings for q learning
	final int initialValue = 20; // initial qvalues
	final double epsilon = 0.1; // epsilon used in picking an action
	final double gamma = 0.9; // gamma is penalty on delayed result
	final double alpha = 0.4; // learning rate
	
	// actions
	final boolean[] STAY = new boolean[Environment.numberOfButtons];;
	final boolean[] JUMP = new boolean[Environment.numberOfButtons];
	final boolean[] SPEED = new boolean[Environment.numberOfButtons];;
	final boolean[] JUMP_SPEED = new boolean[Environment.numberOfButtons];;
	final boolean[] RIGHT = new boolean[Environment.numberOfButtons];;
	final boolean[] RIGHT_JUMP = new boolean[Environment.numberOfButtons];;
	final boolean[] RIGHT_SPEED = new boolean[Environment.numberOfButtons];;
	final boolean[] RIGHT_JUMP_SPEED = new boolean[Environment.numberOfButtons];;
	final boolean[] LEFT = new boolean[Environment.numberOfButtons];;
	final boolean[] LEFT_JUMP = new boolean[Environment.numberOfButtons];;
	final boolean[] LEFT_SPEED = new boolean[Environment.numberOfButtons];;
	final boolean[] LEFT_JUMP_SPEED = new boolean[Environment.numberOfButtons];;
	
	
	/**
	 *  Constructor of qlearn agent with a blank policy (to be learned)
	 */
	public QLearnAgent() {
		this(new HashMap<StateActionPair, Double>());	
	} // end constructor without policy
	
	/**
	 * Constructor for a q learning agent with a given policy
	 * 
	 * @param plc is the policy the agent should handle
	 */
	public QLearnAgent(Map<StateActionPair, Double> qValuesIn) {
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
		
		// update state
		state.update(environment);
	    

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
		
		//System.out.printf("Updated state \n%s \n from %.2f to %.2f \n\n", oldState, oldQ, updatedValue);
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
	 * Incrementally add valid actions to the list, in order to create a permutation of all keys
	 * A recursion is just because altering the list while looping through is 
	 * causes unwanted behavior
	 * @return all possible actions
	 */
	/*
	public List<boolean[]> getValidActions()
	{
		// initiate valid actions
		List<boolean[]> validActions = new ArrayList<boolean[]>();
		boolean[] validAction = new boolean[Environment.numberOfButtons];
		
		// start with all false
		validActions.add(validAction);
		
		// for each actionKey double the amount of actions possible by adding a new action for each present action
		for(int actionKey = 0; actionKey < validAction.length; actionKey++) {
			validActions.addAll(getValidAction(validActions, actionKey));
		} // end for each actionkey
		
		return validActions;
	}
	*/
	
	/**
	 * getValidAction returns a list of actions possible, which is all buttons in 
	 * combinations: 2^5
	 * This is the recursion needed for altering the list while looping through is
	 * creates unwanted behavior
	 * 
	 * @param oldValidActions are the valid actions up till now 
	 * @param actionKey is number of key we are looping through
	 * @return is the new valid action
	 */
	/*
	public List<boolean[]> getValidAction(List<boolean[]> oldValidActions, int actionKey) {
		
		List<boolean[]> newValidActions = new ArrayList<boolean[]>();
		
		boolean[] validAction = new boolean[oldValidActions.get(0).length];
		
		// go through all present actions
		int actionSize = oldValidActions.size();
		for(int i = 0; i < actionSize; i++) {
			validAction = oldValidActions.get(i).clone();
			// add a new action in which the current actionkey is true
			validAction[actionKey] = true;
			newValidActions.add(validAction);
		} // end each present valid action

		return newValidActions;
	}
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
	}
	
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
		if( stateType.equals("MarioState") )
			return new MarioState(environmentIn);
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
	@SuppressWarnings("unchecked") // hack to remore annoying warning of casting
	public void loadQValues(String path) {
		try {
			qValues = (Map<StateActionPair, Double>) SLAPI.load(path);
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
