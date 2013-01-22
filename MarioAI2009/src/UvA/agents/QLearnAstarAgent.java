package UvA.agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import competition.icegic.robin.astar.AStarSimulator;

import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.ai.BasicAIAgent;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;
import UvA.states.*;


public class QLearnAstarAgent extends BasicAIAgent implements Agent {

	
	//for Astar
    protected boolean action[] = new boolean[Environment.numberOfButtons];
    private AStarSimulator sim;
    private int tickCounter = 0;
    private float lastX = 0;
    private float lastY = 0;
	
	// agent specific values
	static private final String name = "QLearnAstarAgent";
	protected final String stateType = "MarioState";
	private int run = 0; // used for choosing new actions every 1/2th time
	
	// used to create state
	State state = null;
	State oldState = null;
	
	// action to return
	protected boolean[] returnAction;
		
	// values used by qlearning
	protected Map<StateActionPair, Double> qValues;	// state-action values
	protected List<boolean[]> allActions;
 
	
	// settings for q learning
	final int initialValue = 1; // initial qvalues
	protected double epsilon = 0.1; // epsilon used in picking an action

	final double gamma = 0.9; // gamma is penalty on delayed result
	final double alpha = 0.3; // learning rate
	final double winReward = 100; // reward for winning
	
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
	
	public boolean useAstar = false;
	
	/**
	 *  Constructor of qlearn agent with a blank policy (to be learned)
	 */
	public QLearnAstarAgent() {
		this(new HashMap<StateActionPair, Double>(), name, "Astar");	
		
		
	} // end constructor without policy
	
	/**
	 * Constructor for a q learning agent with a given policy
	 * 
	 * @param plc is the policy the agent should handle
	 */
	public QLearnAstarAgent(Map<StateActionPair, Double> qValuesIn, String name) {
		super(name);
		initiateValues();
		initialiseActions();
		this.qValues = qValuesIn;
		action = new boolean[Environment.numberOfButtons];// Empty action
		sim = new AStarSimulator();
		
	} // end constructor with policy
	

	public QLearnAstarAgent(Map<StateActionPair, Double> qValuesIn, String name, String method) {
		super(name);
		initiateValues();
		initialiseActions();
		this.qValues = qValuesIn;
		if(method.equals("Astar"))
		{
			this.useAstar = true;
		}
			action = new boolean[Environment.numberOfButtons];// Empty action
			sim = new AStarSimulator();
		
	} // end constructor with policy
	
	
	/**
	 * getAction function is called by the engine to retrieve an action from mario
	 */
	public boolean[] getAction(Environment observation)
	{
		if(!this.useAstar)
		{
		state = createState(observation);
		
		// update q and return action
		updateQValue();
		
		// only pick a new action every 2nd question
		if(run % 2 == 0)
			returnAction = eGreedyAction();
		run++;

		// update oldState for updateQValue()
	    oldState = state.clone();
	    

	    return returnAction;
		}
		else
	    {
			state = createState(observation);
			
			// update q and return action
			updateQValue();
			
	    	tickCounter++;
	    	String s = "Fire";
	    	if (!sim.levelScene.mario.fire)
	    		s = "Large";
	    	if (!sim.levelScene.mario.large)
	    		s = "Small";
	    	if (sim.levelScene.verbose > 0) System.out.println("Next action! Tick " + tickCounter + " Simulated Mariosize: " + s);

	    	boolean[] ac = new boolean[5];
	    	ac[Mario.KEY_RIGHT] = true;
	    	ac[Mario.KEY_SPEED] = true;
	    	
	    	//byte[][] scene = observation.getCompleteObservation();//observation.getLevelSceneObservation(0);
	    	byte[][] scene = observation.getLevelSceneObservation();
	    	float[] enemies = observation.getEnemiesFloatPos();
	    	
	    	//observation.getCompleteObservation();
	    	//System.out.println("Clean scene:");
	    	//printLevel(scene);
	    	
	    	//System.out.println("Complete Obs:");
	    	//printLevel(observation.getCompleteObservation());
	    	
	    	if (sim.levelScene.verbose > 2) System.out.println("Simulating using action: " + sim.printAction(action));
	        sim.advanceStep(action);   
	    	
	        if (sim.levelScene.verbose > 5) System.out.println("Simulated sprites: ");
	        if (sim.levelScene.verbose > 5) sim.levelScene.dumpSprites();
	        
	    	//System.out.println("Internal scene after sim:");
	        //printLevel(sim.levelScene.levelSceneObservation(0));
	        
	        sim.setLevelPart(scene, enemies);
	        //printLevel(sim.levelScene.levelSceneObservation(0));
			float[] f = observation.getMarioFloatPos();
			if (sim.levelScene.verbose > 5)
				System.out.println("Sim Mario Pos: " 
						+ sim.levelScene.mario.x + " " + sim.levelScene.mario.y + " " +
						" a: " + sim.levelScene.mario.xa + " " + sim.levelScene.mario.ya );
			if (sim.levelScene.mario.x != f[0] || sim.levelScene.mario.y != f[1])
			{
				if (f[0] == lastX && f[1] == lastY)
					return ac;
				//System.out.print("i");
				if (sim.levelScene.verbose > 0) System.out.println("INACURATEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE!");
				if (sim.levelScene.verbose > 0) System.out.println("Real: "+f[0]+" "+f[1]
				      + " Est: "+ sim.levelScene.mario.x + " " + sim.levelScene.mario.y +
				      " Diff: " + (f[0]- sim.levelScene.mario.x) + " " + (f[1]-sim.levelScene.mario.y));
				sim.levelScene.mario.x = f[0];
				sim.levelScene.mario.y = f[1];
				sim.levelScene.mario.xa = (f[0] - lastX) *0.89f;
				sim.levelScene.mario.ya = (f[1] - lastY) * 0.85f + 3f;
				//errCount++;
				//if (errCount > 1)
				//	errAgent.lastX++;
			}
			lastX = f[0];
			lastY = f[1];
			
			MarioComponent MC = (MarioComponent)observation;
			//sim.targetX = (MC.mouseListener.goal.x/4 + (int)sim.levelScene.xCamO);
			//sim.targetY = MC.mouseListener.goal.y/4;
			sim.targetX = (MC.mouseListener.goal.x + (int)sim.levelScene.xCamO);
			sim.targetY = MC.mouseListener.goal.y;
			//System.out.println(sim.targetX);
	        action = sim.optimise(MC.mouseListener.goal);        
	        
	        
	     // update oldState for updateQValue()
		    oldState = state.clone();
	        if (sim.levelScene.verbose > 1) System.out.println("Returning action: " + sim.printAction(action));
	        return action;
	    }

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
	 * Update the qValues according to Q learning methods, 
	 * first calculates reward and then updates with updateQValue(reward)
	 */
	public void updateQValue()
	{
		// update according to reward of current state
		updateQValue(state.getReward());
	}
	
	/**
	 * This function actually updates the qvalue according to reward given
	 * @param reward is the reward that comes with the new state
	 */
	public void updateQValue(double reward) {
		
		// get bets QValue for calculating updated qvalue
		List<boolean[]> actions = getValidActions();
		double bestQValue = 0;
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

		// calculate reward
		double updatedValue = oldQ + alpha*(reward + gamma*bestQValue - oldQ);
		
		qValues.put(oldSap, updatedValue);	// update qValue of State-action pair

	} // end updateQValue(reward);
	
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
	public List<boolean[]> getAllActions()
	{
		// initiate valid actions
		List<boolean[]> allActions = new ArrayList<boolean[]>();
		
		allActions.add(STAY);
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
	
	public List<boolean[]> getValidActions()
	{
		List<boolean[]> validActions = new ArrayList<boolean[]>(allActions);
		//TODO remove actions that contain jump if environment.mayMarioJump() is false
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
		allActions = getAllActions();
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
//				// first creation of the state
				return new MarioState(environmentIn, 32);
			}
		}
		else
		{
			System.out.println("Unknown state-type");
			return null;
		}			
	} // end create state
	

	/**
	 * Evaluate end, so that dieing can be punished
	 * @param won is a boolean whether mario has won or not
	 */
	public void evaluateEnd(boolean won) {
		if(won)
			updateQValue(winReward);
		else
			updateQValue(-winReward);
	} // end evaluateEnd
	
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
		this.epsilon = newEpsilon;
		System.out.println("New epsilon is set: " + this.epsilon);
	}
	
	/**
	 * Reset oldXPos
	 */
	@Override
	public void reset(){
		state.reset();
		oldState.reset();
		action = new boolean[Environment.numberOfButtons];// Empty action
        sim = new AStarSimulator();
	}// end reset

} // end class