package UvA.agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import java.util.Properties;

import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.ai.BasicAIAgent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;
import UvA.states.*;

public class QLearnAgent extends BasicAIAgent implements Agent {

	// agent specific values
	static private final String name = "QLearnAgent";
	protected final Properties configFile;
	
	// used to create state
	protected State state = null;
	protected State oldState = null;
	
	// action to return
	protected boolean[] returnAction;
		
	// values used by qlearning
	protected Map<StateActionPair, Double> qValues;	// state-action values
	protected List<boolean[]> allActions;
 
	// settings for q learning
	static int initialValue = 20; // initial qvalues
	public static double epsilon = 0.1; // epsilon used in picking an action
	public static double gamma = 0.9; // gamma is penalty on delayed result
	public static double alpha = 0.3; // learning rate
	
	// actions
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
	
	public static double rewardSoFar;
	public static double currentReward;
	
	/**
	 *  Constructor of qlearn agent with a blank policy (to be learned)
	 *  Calls constructor with private string name
	 */
	public QLearnAgent() {
		this(name);	
	} // end constructor 
	
	/**
	 * Constructor with name, calls constructor with emtpy qvalues
	 * @param name -- the name of the agent
	 */
	public QLearnAgent(String name) {
		this(new HashMap<StateActionPair, Double>(), name);
	}
	
	/**
	 * Constructor for a q learning agent with a given qValues, initiates the agent
	 * @param qValuesIn are the qValues the agent will use
	 * @param name is the name of the agent
	 */
	public QLearnAgent(Map<StateActionPair, Double> qValuesIn, String name) {
		super(name);
		
		this.configFile = MarioState.readPropertiesFile(System.getProperty("user.dir") + "/config.properties");
		setAllProperties(configFile);
		
		initiateValues();
		this.qValues = qValuesIn;
	} // end constructor with policy
	

	/**
	 * getAction function is called by the engine to retrieve an action from mario
	 */
	public boolean[] getAction(Environment environment)
	{
		state = createState(environment, oldState); // added oldSTate for non static

		//update reward values in screen
		double reward = state.getReward();
		rewardSoFar += reward;
		currentReward = reward;
		
		// update q and return action
		updateQValue();
		
		// only pick a new action every 2nd question
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
		double bestValue = -10000;

		// find best action
		List<boolean[]> validActions = getValidActions();
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
		
		//  choose random action
		if( generator.nextDouble() < epsilon )	
		{
			List<boolean[]> randomActions = new ArrayList<boolean[]>(validActions);
			randomActions.remove(bestAction);	// don't choose the best action
			boolean[] randomAction = randomActions.get(generator.nextInt(randomActions.size()));
			return randomAction;
		}
		

		return bestAction;	// choose best action
	}

	public void updateQValue()
	{
		updateQValue(oldState, state);
	}
	
	/**
	 * Update the qValues according to Q learning methods, 
	 * 
	 */
	public void updateQValue(State oldState, State state)
	{
		// update according to reward of current state
		double reward = state.getReward();
		
		// get bets QValue for calculating updated qvalue
		List<boolean[]> actions = getValidActions();
		double bestQValue = 0;
		
		// for each action, get stateaction pair and compare highest qValue 
		// to return the future reward
		for(int i=0; i<actions.size(); i++)
		{
			StateActionPair sap = new StateActionPair(state, actions.get(i));
			double Q = getStateActionValue(sap);
			if( Q > bestQValue )
				bestQValue = Q;
		}

		// create state action pair
		StateActionPair oldSap = new StateActionPair(oldState, returnAction);
		double oldQ = getStateActionValue(oldSap);

		// calculate reward according to qLearn
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
	 * getValidActions returns the valid actions in mario
	 * @return a booleanarray[] representating actions
	 */
	public List<boolean[]> getValidActions()
	{
		List<boolean[]> validActions = new ArrayList<boolean[]>(allActions);
		//TODO remove actions that contain jump if environment.mayMarioJump() is false
		return validActions;
	}
	
	
	/** 
	 * Creates state for mario
	 * @param Environment is the new environment to create the state
	 * @param State is the oldState, from which the new state will calculate the reward from
	 * @return the state including information
	 */
	public State createState(Environment environmentIn, State oldState)
	{
		return new MarioState(environmentIn, oldState);
	} // end create state
	
	/**
	 * Function is used for declaring some values necessarily for qLearning, 
	 * such as states and actions, which needs to have a value
	 */
	public void initiateValues() {
		// initialise states and actions
		oldState = new MarioState(configFile);
		state =  new MarioState(configFile);
		
		returnAction = new boolean[Environment.numberOfButtons];
		allActions = getAllActions();
		
		// hardcoded set the possible actions
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

	} // end initiateValues

	
	/**
	 * Get list of all possible actions. Each action is a boolean array.
	 * @return list of all possible actions
	 */
	public List<boolean[]> getAllActions()
	{
		// initiate valid actions
		List<boolean[]> allActions = new ArrayList<boolean[]>();
		
		allActions.add(JUMP);
		allActions.add(SPEED);
		allActions.add(LEFT);
		allActions.add(LEFT_SPEED);
		allActions.add(LEFT_JUMP);
		allActions.add(LEFT_JUMP_SPEED);
		allActions.add(JUMP_SPEED);
		allActions.add(RIGHT);
		allActions.add(RIGHT_SPEED);
		allActions.add(RIGHT_JUMP);
		allActions.add(RIGHT_JUMP_SPEED);
		
		return allActions;
	} // end getValidActions()
	
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
	
	/**
	 * Set epsilon, used to test without exploring
	 * @param newEpsilon
	 */
	public void setEpsilon(int newEpsilon) {
		epsilon = newEpsilon;
		System.out.println("New epsilon is set: " + epsilon);
	}
	
	/**
	 * Set alpha, used to test without learning
	 * @param newAlpha
	 */
	public void setAlpha(int newAlpha) {
		alpha = newAlpha;
		//System.out.println("New alpha is set: " + alpha);
	}
	
	/**
	 * get total reward of mario, gets from state
	 * @return double[] result, of which the first element is the x position of mario, 
	 * and the second command is the reward according to mario
	 */
	public double[] getTotalReward() {
		double[] result = state.getTotalReward();
		return result;
	} // 
	/**
	 * Reset states and static values of states, overrides default reset (for mario agents)
	 * Is called by mario engine on every game start
	 */
	@Override
	public void reset(){
		state.reset();
		oldState.reset();
		returnAction = new boolean[Environment.numberOfButtons]; 
		rewardSoFar = 0;
		currentReward = 0;
	}// end reset
	
	public void setAllProperties(Properties properties){
		initialValue = Integer.parseInt(properties.getProperty("initialValue", "20"));
		epsilon = Double.parseDouble(properties.getProperty("epsilon", "0.1"));
		gamma = Double.parseDouble(properties.getProperty("gamma", "0.9"));
		alpha = Double.parseDouble(properties.getProperty("alpha", "0.3"));
	}// end function setAllProperties
	
	public int getViewDim()
	{
		MarioState mState = (MarioState) state;
		return mState.getViewDim();
	}

	public double getAlpha() {
		return alpha;
	}
	
	public Map<StateActionPair, Double> getQValues()
	{
		return qValues;
	}
	
} // end class
