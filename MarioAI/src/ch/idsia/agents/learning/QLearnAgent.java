package ch.idsia.agents.learning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ch.idsia.evolution.MarioState;
import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.evolution.StateActionPair;

public class QLearnAgent extends BasicMarioAIAgent implements Agent {

	static private final String name = "QLearnAgent";
	final private int amountKeys = 15;
	final private int amountActions = 6; // actions to be learned (up right left etc)
	private boolean[] returnAction;
		
	private Map<StateActionPair, Double> qValues;	// state-action values
	private List<boolean[]> validActions;
	private MarioState newState; // retrieved from engine
	private MarioState oldState; // used for learning

	
	// settings for q learning
	final int initialValue = 1500; // initial qvalues
	final double epsilon = 0.1; // epsilon used in picking an action
	final double gamma = 0.9; // gamma is penalty on delayed result
	final double alpha = 0.4; // learning rate
	
	
	/**
	 *  Constructor of qlearn agent with a blank policy (to be learned)
	 */
	public QLearnAgent() {
		super(name);
		initiateQValues();
		this.qValues = new HashMap<StateActionPair, Double>();
	} // end constructor without policy
	
	/**
	 * Constructor for a q learning agent with a given policy
	 * 
	 * @param plc is the policy the agent should handle
	 */
	public QLearnAgent(Map<StateActionPair, Double> qValuesIn) {
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
	        
	    newState = new MarioState(input);
	    // update q values and return new action
	    updateQValue();
	    returnAction = eGreedyAction();
	    
	    // set old state for updating values
	    oldState = newState; 
	    
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
			StateActionPair newSap = new StateActionPair(newState, validActions.get(i));
			double newQ = getStateActionValue(newSap);
			if( newQ > bestQValue )
				bestQValue = newQ;
		}
		
		
		int relativeReward = newState.getReward() - oldState.getReward();
				
		double updatedValue = oldQ + alpha*(relativeReward + gamma*bestQValue - oldQ);
		qValues.put(oldSap, updatedValue);	// update qValue of State-action pair
		
		System.out.printf("Updating state %s from reward %f to new reward %f \n", newState, oldQ, updatedValue);
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
	}
	
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
	}

	
	public void initiateQValues() {
		boolean[] fakeInput = new boolean[20];
		oldState = new MarioState(fakeInput);
		returnAction = new boolean[Environment.numberOfKeys];
		validActions = getValidActions();
	} // end getvalidactions
	
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
	} // end probe
	
} // end class