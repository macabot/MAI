package UvA.agents;


import ch.idsia.mario.environments.Environment;
import UvA.states.*;

public class SarsaAgent extends QLearnAgent{

	// agent specific values
	static private final String name = "SarsaAgent";
	//private final String configFile = System.getProperty("user.dir") + "/config.properties";
	
	// action to return
	private boolean[] action;
	private boolean[] oldAction;
			
	// values used by sarsa
	//private Map<StateActionPair, Double> qValues;	// state-action values
 
	/**
	 *  Constructor of sarsa agent with a blank policy (to be learned)
	 *  Calls constructor with private string name
	 */
	public SarsaAgent() {
		super(name);
	} // end constructor 

	/**
	 * getAction function is called by the engine to retrieve an action from mario
	 */
	@Override
	public boolean[] getAction(Environment environment)
	{
		// take action a, observe r, s'
		state = createState(environment, oldState);
		
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
		// initialise states and actions
		oldState = new MarioState(null,null, configFile);
		state =  new MarioState(null,null, configFile);

		oldAction = new boolean[Environment.numberOfButtons];
		action = new boolean[Environment.numberOfButtons];
		allActions = getAllActions();

		
	} // end initiateValues
	
} // end class
