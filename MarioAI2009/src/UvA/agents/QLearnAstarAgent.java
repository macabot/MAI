package UvA.agents;

import java.util.HashMap;
import java.util.Map;

import UvA.states.MarioState;
import UvA.states.StateActionPair;
import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

import competition.icegic.robin.astar.AStarSimulator;


public class QLearnAstarAgent extends QLearnAgent implements Agent {

	
	//for Astar
    protected boolean action[] = new boolean[Environment.numberOfButtons];
    protected AStarSimulator sim;
    private int tickCounter = 0;
    private float lastX = 0;
    private float lastY = 0;
    public boolean useAstar = false;
    
	// agent specific values
	static private final String name = "QLearnAstarAgent";
	protected final String stateType = "MarioState";
			
	/**
	 *  Constructor of qlearn agent with a blank policy (to be learned)
	 */
	public QLearnAstarAgent() {
		this(name);	
	} // end constructor without policy
	
	public QLearnAstarAgent(String name) {
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
		this.qValues = qValuesIn;
		action = new boolean[Environment.numberOfButtons];// Empty action
		sim = new AStarSimulator();
		
	} // end constructor with policy
	

	public QLearnAstarAgent(Map<StateActionPair, Double> qValuesIn, String name, String method) {
		super(name);
		initiateValues();
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
			returnAction = eGreedyAction();

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
	
	@Override
	public void reset(){
		state.reset();
		oldState.reset();
		action = new boolean[Environment.numberOfButtons];// Empty action
        sim = new AStarSimulator();
        MarioState.resetStatic();
	}// end reset

} // end class
