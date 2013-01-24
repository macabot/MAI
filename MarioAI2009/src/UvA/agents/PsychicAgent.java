package UvA.agents;

import java.util.List;

import UvA.states.MarioState;
import UvA.states.State;
import ch.idsia.mario.environments.Environment;

import competition.icegic.robin.astar.LevelScene;

public class PsychicAgent extends QLearnAstarAgent {

	
	public PsychicAgent()
	{
		super("StateAgent");
	}
	
	public boolean[] getAction(Environment observation)
	{

		int depth =3;
		byte[][] scene = observation.getLevelSceneObservation();
		float[] enemies = observation.getEnemiesFloatPos();
		sim.setLevelPart(scene, enemies);
		
		LevelScene oldScene = null;
		try {
			oldScene = (LevelScene) sim.levelScene.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		state = new  MarioState(oldScene);	//save old state
		State futureState = state.clone();
		List<boolean[]>listAction = getAllActions();//get all actions
		for(int i = 0; i < listAction.size();i++)//for all possible actions
		{
			State tempState = state.clone();
			boolean[] tempAction = listAction.get(i);
			for(int j = 0 ; j < depth;j++)//repeat same action
			{ 
				tempState = futureState.clone(); 
				sim.advanceStep(tempAction);//simulate action
				LevelScene simulatedScene = sim.levelScene;//get new state from action
				futureState = new  MarioState(simulatedScene);
				updateQValue(tempState, futureState); //update Q
			}
			sim.levelScene = oldScene; //reset scene

		}

		// get an action
		returnAction = eGreedyAction();

		// update oldState for updateQValue()
		oldState = state.clone();

		return returnAction;
	}//end getAction

}//end class
