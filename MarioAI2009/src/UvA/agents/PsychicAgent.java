package UvA.agents;

import java.util.List;

import UvA.states.MarioState;
import UvA.states.State;
import ch.idsia.mario.environments.Environment;

import competition.icegic.robin.astar.AStarSimulator;
import competition.icegic.robin.astar.LevelScene;

public class PsychicAgent extends QLearnAgent {

	protected AStarSimulator sim;
	
	// used to set x and y in simulator
	private float lastX = 0; 
    private float lastY = 0;
    
	public PsychicAgent()
	{
		super("StateAgent");
		sim = new AStarSimulator();
	}
	
	
	public boolean[] getAction(Environment observation)
	{
		
		int depth =3;
		byte[][] scene = observation.getLevelSceneObservation();
		float[] enemies = observation.getEnemiesFloatPos();

		// set mario position in simulator (MUST BE SET BEFORE SETLEVELPART)
		
		float[] f = observation.getMarioFloatPos();
		sim.levelScene.mario.x = f[0];
		sim.levelScene.mario.y = f[1];
		sim.levelScene.mario.xa = (f[0] - lastX) *0.89f;
		sim.levelScene.mario.ya = (f[1] - lastY) * 0.85f + 3f;
                
		// set lastx and lasty for new round
		lastX = f[0];
		lastY = f[1];
		
		sim.setLevelPart(scene, enemies);


		
		

		
		LevelScene oldScene = null;
		try {
			oldScene = (LevelScene) sim.levelScene.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		state = new  MarioState(oldScene, oldState);	//save old state
		State futureState = state.clone();
		List<boolean[]>listAction = getAllActions();//get all actions
		for(int i = 0; i < listAction.size();i++)//for all possible actions
		{
			futureState = state.clone();
			boolean[] tempAction = listAction.get(i);
			for(int j = 0 ; j < depth;j++)//repeat same action
			{ 
				State tempState = futureState.clone(); 
				sim.advanceStep(tempAction);//simulate action
				LevelScene simulatedScene = sim.levelScene;//get new state from action
				futureState = new  MarioState(simulatedScene, tempState);
				updateQValue(tempState, futureState); //update Q
			}
			sim.levelScene = oldScene; //reset scene

		}
		
		
		
		
		
		
		// regular qLearning
		state = createState(observation, oldState);
		
		// testing purposes, comparing states
		State aStarState = new MarioState(oldScene, oldState);

		// update q and return action
		updateQValue();
		
		returnAction = eGreedyAction();

		// update oldState for updateQValue()
	    oldState = state.clone();
	    
	    return returnAction;
		
	}//end getAction

	
}//end class
