package UvA.agents;

import java.util.List;

import UvA.states.MarioState;
import UvA.states.State;
import ch.idsia.mario.environments.Environment;

import competition.icegic.robin.astar.LevelScene;

public class StateAgent extends QLearnAstarAgent {

	public boolean[] getAction(Environment observation)
	{

		int depth =3;
		byte[][] scene = observation.getLevelSceneObservation();
		float[] enemies = observation.getEnemiesFloatPos();

		LevelScene oldScene = sim.levelScene;
		oldState = new  MarioState(oldScene);//save old state
		State futureState = oldState.clone();
		List<boolean[]>listAction = getAllActions();//get all actions
		for(int i = 0; i < listAction.size();i++)//for all possible actions
		{
			State tempOldState = oldState.clone();
			action = listAction.get(i);
			for(int j = 0 ; j < depth;j++)//repeat same action
			{ 
				tempOldState = futureState.clone(); 
				sim.advanceStep(action);//simulate action
				LevelScene simulatedScene = sim.levelScene;//get new state from action
				futureState = new  MarioState(simulatedScene);
				updateQValue(tempOldState, futureState); //update Q
			}
			sim.levelScene = (LevelScene) oldState.clone(); //reset state to old

		}
		sim.setLevelPart(scene, enemies);

		// only pick a new action every 2nd question
		returnAction = eGreedyAction();

		// update oldState for updateQValue()
		oldState = state.clone();

		return returnAction;
	}

}
