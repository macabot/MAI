package UvA.agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.environments.Environment;

public class SarsaAgent extends BasicMarioAIAgent implements Agent {

	static private final String name = "SarsaAgent";
	private int oldDistance;
	final private int amountActions = 6; // actions to be learned (up right left etc)
	private boolean[] returnAction;
	private boolean[] chosenAction; // action chosen from policy by sarsa
		
	private Map<StateActionPair, Double> qValues;	// state-action values
	private List<boolean[]> validActions;
	private State newState; // retrieved from engine
	private State oldState; // used for learning
	private final String stateType = "MarioState"; 
	
	// settings for q learning
	final int initialValue = 2; // initial qvalues
	final double epsilon = 0.1; // epsilon used in picking an action
	final double gamma = 0.9; // gamma is penalty on delayed result
	final double alpha = 0.5; // learning rate
	
	
	/**
	 *  Constructor of sarsa agent with a blank policy (to be learned)
	 */
	public SarsaAgent() {
		this(new HashMap<StateActionPair, Double>());		
	} // end constructor without policy
	
	/**
	 * Constructor for a sarsa learning agent with a given policy
	 * 
	 * @param qValuesIn
	 */
	public SarsaAgent(Map<StateActionPair, Double> qValuesIn) {
		super(name);
		initiateQValues();
		this.qValues = qValuesIn;		
	} // end constructor with policy
	

	/**
	 * getAction function is called by the engine to retrieve an action from mario
	 */
	public boolean[] getAction()
	{
//	        byte[][] scene = observation.getLevelSceneObservation(/*1*/);
//	        byte[][] enemies = observation.getEnemiesObservation(/*0*/);
	    byte[][] scene = levelScene;
	    boolean [] input = new boolean[]{probe(-1, -1, scene), probe(0, -1, scene), probe(1, -1, scene),
	            probe(-1, 0, scene), probe(0, 0, scene), probe(1, 0, scene),
	            probe(-1, 1, scene), probe(0, 1, scene), probe(1, 1, scene),
	            probe(-1, -1, enemies), probe(0, -1, enemies), probe(1, -1, enemies),
	            probe(-1, 0, enemies), probe(0, 0, enemies), probe(1, 0, enemies),
	            probe(-1, 1, enemies), probe(0, 1, enemies), probe(1, 1, enemies),
	            isMarioOnGround ? true : false, isMarioAbleToJump ? true : false};
	    
	    // choose a' from s' using policy derived from Q with eGreedy
	    newState = createState(stateType, input);
	    returnAction = eGreedyAction();
	    
	    // update q values
	    updateQValue();
	    
	    // update state and action
	    oldState = newState;
	    chosenAction = returnAction;
	    
	    return chosenAction;
	} // end function getAction

	/**
	 * Get an action according to e-greedy. The greedy action is chosen with probability
	 * 1-e. The other actions get equal probability.
	 * 
	 * @return e-greedy action
	 */
	public boolean[] eGreedyAction()
	{
		Random generator = new Random();
		boolean[] bestAction = new boolean[Environment.numberOfKeys];
		double bestValue = 0;
		
		// find best action
		
		for(int i=0; i<validActions.size(); i++)
		{
			StateActionPair sap = new StateActionPair(newState, validActions.get(i));
			double qValue = getStateActionValue(sap);
			if( qValue > bestValue )
			{
				bestAction = sap.action;
				bestValue = qValue;
			}// end if
		}// end for
		
		if( generator.nextDouble() < epsilon )	//  choose random action
		{
			List<boolean[]> randomActions = new ArrayList<boolean[]>(validActions);
			randomActions.remove(bestAction);	// don't choose the best action
			boolean[] randomAction = randomActions.get(generator.nextInt(randomActions.size()));
			return randomAction;
		}// end if
		
		return bestAction;	// choose best action
	}// end function eGreedyAction

	/**
	 * Update the qValues according to Q learning methods
	 * @param action 	a
	 * @param nextAction   a'
	 */
	public void updateQValue()
	{
		StateActionPair oldSap = new StateActionPair(oldState, chosenAction);
		double oldQ = getStateActionValue(oldSap);
		
		int relativeReward = distance - oldDistance;
		oldDistance = distance; 
		
		StateActionPair newSap = new StateActionPair(newState, returnAction);
		double newQ = getStateActionValue(newSap);
		
		double updatedValue = oldQ + alpha*(relativeReward + gamma*newQ - oldQ);
		qValues.put(oldSap, updatedValue);	// update qValue of State-action pair
		
		System.out.printf("Updating state %s from reward %f to new reward %f \n", newState, oldQ, updatedValue);
	}// end function updateQValue
	
	/**
	 * This function returns the q value if present, else returns the initialValue
	 * @param sap state action pair
	 * @param initialValue the initial q value for all actions
	 * @return the action value
	 */
	public double getStateActionValue(StateActionPair sap){
		return (qValues.containsKey(sap))?
				qValues.get(sap):initialValue;
	}// end function getStateActionValue

	
	/**
	 * Incrementally add validactions to the list, in order to create a permutation of all keys
	 * @return all possible actions
	 */
	public List<boolean[]> getValidActions()
	{
		// initiate valid actions
		List<boolean[]> validActions = new ArrayList<boolean[]>();
		boolean[] validAction = new boolean[amountActions];
		
		// start with all false
		validActions.add(validAction);
		
		// for each actionKey double the amount of actions possible by adding a new action for each present action
		for(int actionKey = 0; actionKey < validAction.length; actionKey++) {
			validActions.addAll(getValidAction(validActions, actionKey));
		} // end for each actionkey
		
		return validActions;
	}// end function getValidActions
	
	public List<boolean[]> getValidAction(List<boolean[]> oldValidActions, int actionKey) {
		List<boolean[]> newValidActions = new ArrayList<boolean[]>();
		
		boolean[] validAction = new boolean[oldValidActions.get(0).length];
		
		// go through all present actions
		int actionSize = oldValidActions.size();
		for(int i = 0; i < actionSize; i++) {
			validAction = oldValidActions.get(i).clone();
			validAction[actionKey] = true;
			newValidActions.add(validAction);
		} // end each present valid action

		return newValidActions;
	}// end function getValidAction

	
	public void initiateQValues() {
		boolean[] fakeInput = new boolean[20];
		oldState = createState(stateType, fakeInput);
		returnAction = new boolean[Environment.numberOfKeys];
		validActions = getValidActions();
	} // end function initiateQValues
	
	public static State createState(String stateType, boolean[] input)
	{
		if( stateType.equals("MarioState") )
			return new MarioState(input);
		else
		{
			System.out.println("Unknown state-type");
			return null;
		}// end else
	}// end function createState
	
	/**
	 * Used in getAction for getting x and y positions relative to mario
	 * @param x: absolute x position
	 * @param y: absolute y position
	 * @param scene: scene object created in basicmarioAIagent
	 * @return
	 */
	private boolean probe(int x, int y, byte[][] scene)
	{
	    int realX = x + 11;
	    int realY = y + 11;
	    return (scene[realX][realY] != 0) ? true : false;
	} // end function probe
	
} // end class
