package UvA.agents;


import java.util.HashMap;
import java.util.Map;

import ch.idsia.mario.environments.Environment;

public class SarsaAgent extends QLearnAgent{

	// agent specific values
	static private final String name = "SarsaAgent";
	
	// used to create state
	//State state;
	//State oldState;
	
	// action to return
	private boolean[] action;
	private boolean[] oldAction;
			
	// values used by sarsa
	//private Map<StateActionPair, Double> qValues;	// state-action values
 
		
	/**
	 *  Constructor of sarsa agent with a blank policy (to be learned)
	 */
	public SarsaAgent() {
		this(new HashMap<StateActionPair, Double>());	
	} // end constructor without policy
	
	/**
	 * Constructor for a q learning agent with a given policy
	 * 
	 * @param plc is the policy the agent should handle
	 */
	public SarsaAgent(Map<StateActionPair, Double> qValuesIn) {
		super();
		this.setName(name);
		initiateValues();
		super.qValues = qValuesIn;
	} // end constructor with policy
	

	/**
	 * getAction function is called by the engine to retrieve an action from mario
	 */
	@Override
	public boolean[] getAction(Environment environment)
	{
		// take action a, observe r, s'
		state.update(environment);
		
		// choose a' from s' with eGreedy
		action = eGreedyAction();
		
		// update q values
		updateQValue();
		
		// update s and a
		oldState = state.clone();
		oldAction = action;
		
		return action;
				
	} // end getAction()



	/**
	 * Update the qValues according to Q learning methods
	 */
	@Override
	public void updateQValue()
	{
		StateActionPair oldSap = new StateActionPair(oldState, oldAction);
		double oldQ = getStateActionValue(oldSap);
		
		StateActionPair newSap = new StateActionPair(state, action);
		double newQ = getStateActionValue(newSap);
		
		double reward = state.getReward();
		double updatedValue = oldQ + alpha*(reward + gamma*newQ - oldQ);
		qValues.put(oldSap, updatedValue);	// update qValue of State-action pair
		
		//System.out.printf("Updated state \n%s \n from %.2f to %.2f \n\n", oldState, oldQ, updatedValue);
	}
	
	/**
	 * Function is used for declaring some values necessarily for qLearning, 
	 * such as oldState, which needs to have a value
	 */
	@Override
	public void initiateValues() {
		oldState = createState(null);
		state = createState(null);
		oldAction = new boolean[Environment.numberOfButtons];
		action = new boolean[Environment.numberOfButtons];
		validActions = getValidActions();
	} // end initiateValues
	
} // end class
