package competition.icegic.robin.astar;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import ch.idsia.mario.engine.GlobalOptions;
import ch.idsia.mario.engine.sprites.Mario;

import competition.icegic.robin.astar.level.Level;


public class AStarSimulator
{

	public LevelScene levelScene;
	public LevelScene workScene;
	public SearchPos bestPosition;
	float currentSearchStartingMarioXPos;
	ArrayList<SearchPos> posPool;
	private float maxMarioSpeed = 10.9090909f;
	private boolean requireReplanning = false;

	public int targetX = 0;
	public int targetY = 0;

	public int debugPos = 0;
	//private LevelScene lsCopy;

	//private int searchResolution = 10;

	private ArrayList<boolean[]> currentActionPlan;
	int ticksBeforeReplanning = 0;


	private class SearchPos
	{
		private int timeElapsed = 0;
		public float remainingTimeEstimated = 0;
		private float remainingTime = 0;

		private SearchPos parentPos = null;
		public LevelScene sceneSnapshot = null;
		public int distanceFromOrigin = 0;
		public boolean hasBeenHurt = false;

		boolean[] action;
		int repetitions = 1;


		public float calcRemainingTime(float marioX, float marioXA)
		{
			return getDistance(marioX, marioXA);
//			return (100000 - (maxForwardMovement(marioXA, 1000) + marioX)) 
//					/ maxMarioSpeed - 1000;
		}

		public float getRemainingTime()
		{
			if (remainingTime > 0) 
				return remainingTime;
			else
				return remainingTimeEstimated;
		}

		public float estimateRemainingTimeChild(boolean[] action, int repetitions)
		{
			float[] childbehaviorDistanceAndSpeed = estimateMaximumForwardMovement(
					levelScene.mario.xa, action, repetitions);
			return calcRemainingTime(targetX,targetY);//levelScene.mario.x + childbehaviorDistanceAndSpeed[0],
					//childbehaviorDistanceAndSpeed[1]);			
		}

		public SearchPos(boolean[] action, int repetitions, SearchPos parent)
		{
			this.parentPos = parent;
			if (parent != null)
			{
				this.remainingTimeEstimated = parent.estimateRemainingTimeChild(action, repetitions);
				this.distanceFromOrigin = parent.distanceFromOrigin+1;
			}
			else
				this.remainingTimeEstimated = calcRemainingTime(targetX, targetY);
			this.action = action;
			this.repetitions = repetitions;
			if (parent != null)
				timeElapsed = parent.timeElapsed + repetitions;
			else
				timeElapsed = 0;
		}

		public float simulatePos()
		{
			// set state to parents scene
			if (parentPos.sceneSnapshot == null)
			{
				System.out.println("DAMN! NO SNAPSHOT!");
			}
			levelScene = parentPos.sceneSnapshot;
			parentPos.sceneSnapshot = backupState();

			int initialDamage = getMarioDamage();
			for (int i = 0; i < repetitions; i++)
			{
				if (debugPos < 1000)
				{
					GlobalOptions.Pos[debugPos][0] = (int) levelScene.mario.x;
					GlobalOptions.Pos[debugPos][1] = (int) levelScene.mario.y;
					debugPos++;
				}
				advanceStep(action);
				if (debugPos < 1000)
				{
					GlobalOptions.Pos[debugPos][0] = (int) levelScene.mario.x;
					GlobalOptions.Pos[debugPos][1] = (int) levelScene.mario.y;
					debugPos++;
				}
				if (debugPos > 1000)
					debugPos = 0;
			}
			float goalPenalty = 0;
//			if (levelScene != null && levelScene.mario.x > levelScene.levelEndX)
//			{
//				goalPenalty = (17-(levelScene.mario.x - levelScene.levelEndX));
//				//System.out.println("goalpen2: "+ goalPenalty);
//				if (parentPos.timeElapsed > 24*60)
//				{
//					goalPenalty = 0;
//					//System.out.println("ping");
//				}
//			
//			}
			
			remainingTime = calcRemainingTime(targetX, targetY)
					+ (getMarioDamage() - initialDamage)*1000000;// * (1000000 - 100 * distanceFromOrigin) + goalPenalty;
			if(levelScene.mario.x > targetX && (this.action[1]))
					remainingTime += 100;
			if(levelScene.mario.x < targetX && (this.action[0]))
				remainingTime += 100;
			if((this.action[4]==true))
				remainingTime *= 0.9;
				

			hasBeenHurt = (getMarioDamage() - initialDamage) != 0;
			

			sceneSnapshot = backupState();

			return remainingTime;			
		}
		
		public float getDistance(float marioX, float marioY)
		{
			return Math.abs(marioX - levelScene.mario.x) + Math.abs(marioY - levelScene.mario.y);
		}

		public ArrayList<SearchPos> generateChildren()
		{
			ArrayList<SearchPos> list = new ArrayList<SearchPos>();
			ArrayList<boolean[]> possibleActions = createPossibleActions(this);

			for (boolean[] action: possibleActions)
			{
				list.add(new SearchPos(action, repetitions, this));
			}			
			return list;
		}

	}


	public AStarSimulator()
	{
		initialiseSimulator();

	}


	private boolean canJumpHigher(SearchPos currentPos)
	{
		return currentPos.sceneSnapshot.mario.mayJump() || (currentPos.sceneSnapshot.mario.jumpTime > 0);
	}

	private ArrayList<boolean[]> createPossibleActions(SearchPos currentPos)
	{
		ArrayList<boolean[]> possibleActions = new ArrayList<boolean[]>();
		// do nothing
		//possibleActions.add(createAction(false, false, false, false, false));

		//possibleActions.add(createAction(false, false, true, false, false));
		// jump
		if (canJumpHigher(currentPos)) possibleActions.add(createAction(false, false, false, true, true));
		if (canJumpHigher(currentPos)) possibleActions.add(createAction(false, false, false, true, false));


		// run right
		possibleActions.add(createAction(false, true, false, false, true));
		if (canJumpHigher(currentPos))  possibleActions.add(createAction(false, true, false, true, true));
		possibleActions.add(createAction(false, true, false, false, false));
		if (canJumpHigher(currentPos))  possibleActions.add(createAction(false, true, false, true, false));
		

		// run left
		possibleActions.add(createAction(true, false, false, false, true));
		if (canJumpHigher(currentPos))  possibleActions.add(createAction(true, false, false, true, true));
		possibleActions.add(createAction(true, false, false, false, false));
		if (canJumpHigher(currentPos))  possibleActions.add(createAction(true, false, false, true, false));

		
		


		// duck (probably sucks always)

		return possibleActions;
	}

	private boolean[] createAction(boolean left, boolean right, boolean down, boolean jump, boolean speed)
	{
		boolean[] action = new boolean[5];
		action[Mario.KEY_DOWN] = down;
		action[Mario.KEY_JUMP] = jump;
		action[Mario.KEY_LEFT] = left;
		action[Mario.KEY_RIGHT] = right;
		action[Mario.KEY_SPEED] = speed;
		return action;
	}

	public float[] estimateMaximumForwardMovement(float currentAccel, boolean[] action, int ticks)
	{
		float dist = 0;
		float runningSpeed =  action[Mario.KEY_SPEED] ? 1.2f : 0.6f;
		int dir = 0;
		if (action[Mario.KEY_LEFT]) dir = -1;
		if (action[Mario.KEY_RIGHT]) dir = 1;
		for (int i = 0; i < ticks; i++)
		{
			currentAccel += runningSpeed * dir;
			dist += currentAccel;
			//System.out.println("Estimator of Fastforward Speed, Tick "+i+" speed: "+currentAccel);
			currentAccel *= 0.89f;
		}    	
		float[] ret = new float[2];
		ret[0] = dist;
		ret[1] = currentAccel;
		return ret;
	}

	// distance covered at maximum acceleration with initialSpeed for ticks timesteps 
	private float maxForwardMovement(float initialSpeed, int ticks)
	{
		float y = ticks;
		float s0 = initialSpeed;
		return (float) (99.17355373 * Math.pow(0.89,y+1)
				-9.090909091*s0*Math.pow(0.89,y+1)
				+10.90909091*y-88.26446282+9.090909091*s0);
	}

	private int getMarioDamage()
	{
		return levelScene.mario.damage;
	}



	private void search(int repetitions, long startTime, Point goal)
	{
		SearchPos current = bestPosition;
		SearchPos furthest = bestPosition;
		boolean currentGood = false;
		int ticks = 0;
		while(posPool.size() != 0 
				&& (!(levelScene.mario.x == targetX)&&!(levelScene.mario.y == targetY) ||/*(levelScene.mario.x - currentSearchStartingMarioXPos < 176) ||*/ !currentGood) 
				&& (System.currentTimeMillis() - startTime < 38))
			//&& (ticks < 500))
			{
			ticks++;
			current = pickBestPos(posPool, goal);
			currentGood = false;
			float realRemainingTime = current.simulatePos();
			bestPosition = current;
			    		if (realRemainingTime < 0)
			    		{
			    			continue;
			    		}
			    		else if (realRemainingTime - current.remainingTimeEstimated > 0.1)
			    		{
			    			// current item is not as good as anticipated. put it back in pool and look for best again
			    			current.remainingTimeEstimated = realRemainingTime;
			    			posPool.add(current);
			    		}
			    		else
			    		{
							//System.out.println("Simulated bestPos accepted! Est remaining time: "
							//		+current.remainingTimeEstimated+" real: "+ realRemainingTime);
							currentGood = true;
							posPool.addAll(current.generateChildren());    			
			    		}
//			    		if (currentGood) 
//			    		{
//			    			bestPosition = current;
//			    			if (current.sceneSnapshot.mario.x >= bestPosition.sceneSnapshot.mario.x)
//			    				furthest = current;
//			    		}
			}
//		    	if (levelScene.mario.x - currentSearchStartingMarioXPos < 176)
//		    	{
//		    		// Couldnt plan till end of screen, take furthest
//		    		bestPosition = furthest;
//		    	}

		//bestPosition = current;
		if (levelScene.verbose > 1) System.out.println("Search stopped. Remaining pool size: "+ posPool.size() + " Current remaining time: " + current.remainingTime);

		levelScene = current.sceneSnapshot;
	}

	private void startSearch(int repetitions)
	{    	
		if (levelScene.verbose > 1) System.out.println("Started search.");
		SearchPos startPos = new SearchPos(null, repetitions, null);
		startPos.sceneSnapshot = backupState();

		posPool = new ArrayList<SearchPos>();
		posPool.addAll(startPos.generateChildren());
		currentSearchStartingMarioXPos = levelScene.mario.x; 
		bestPosition = startPos;
		debugPos = 0;


	}

	private ArrayList<boolean[]> extractPlan()
	{
		ArrayList<boolean[]> actions = new ArrayList<boolean[]>();

		// just move forward if no best position exists
		if (bestPosition == null)
		{
			if (levelScene.verbose > 1) System.out.println("NO BESTPOS!");
			for (int i = 0; i < 10; i++)
			{
				actions.add(createAction(false, true, false, false, true));        		
			}
			return actions;
		}
		if (levelScene.verbose > 2) System.out.print("Extracting plan (reverse order): ");
		SearchPos current = bestPosition;
		while (current.parentPos != null)
		{
			for (int i = 0; i < current.repetitions; i++)
				actions.add(0, current.action);
			if (levelScene.verbose > 2) 
				System.out.print("[" 
						+ (current.action[Mario.KEY_DOWN] ? "d" : "") 
						+ (current.action[Mario.KEY_RIGHT] ? "r" : "")
						+ (current.action[Mario.KEY_LEFT] ? "l" : "")
						+ (current.action[Mario.KEY_JUMP] ? "j" : "")
						+ (current.action[Mario.KEY_SPEED] ? "s" : "") 
						+ (current.hasBeenHurt ? "-" : "") + "]");
			if (current.hasBeenHurt)
			{
				requireReplanning = true;    			
			}
			current = current.parentPos;
		}
		if (levelScene.verbose > 2) System.out.println();
		return actions;
	}

	public String printAction(boolean[] action)
	{
		String s = "";
		if (action[Mario.KEY_RIGHT]) s+= "Forward ";
		if (action[Mario.KEY_LEFT]) s+= "Backward ";
		if (action[Mario.KEY_SPEED]) s+= "Speed ";
		if (action[Mario.KEY_JUMP]) s+= "Jump ";
		if (action[Mario.KEY_DOWN]) s+= "Duck";
		return s;
	}

	private SearchPos pickBestPos(ArrayList<SearchPos> posPool, Point goal)
	{
		SearchPos bestPos = null;
		float bestDistance = 10000000;

		//System.out.println("Searching fitnesses.");
		for (SearchPos current: posPool)
		{

//			float distance = Math.abs(levelScene.mario.xa - goal.y) + Math.abs(levelScene.mario.xa - goal.x);
//			float marioY = levelScene.mario.y;
//			float marioX = levelScene.mario.x;
//			float jumpModifier = 0;    		
//			if (current.action[Mario.KEY_JUMP]) jumpModifier = -0.001f;
//			if (current.sceneSnapshot != null && current.sceneSnapshot.mario.y > 200) jumpModifier += 0.001f * (300-current.sceneSnapshot.mario.y);
			//if (current.sceneSnapshot != null)distance = Math.abs(current.sceneSnapshot.mario.y - goal.y) + Math.abs(current.sceneSnapshot.mario.x - goal.x);
			float distance = current.getRemainingTime();
					//+ current.timeElapsed * 0.79f + jumpModifier; // slightly bias towards furthest positions 
			//distance;
			//System.out.println("Looking at pos with elapsed time "+current.timeElapsed+" est time: "
			//		+ current.getRemainingTime() + " actions: " + printAction(current.action));
			if (distance < bestDistance)
			{
				bestPos = current;
				bestDistance = distance;
			}
		}
		posPool.remove(bestPos);
		//System.out.println("Best Pos: elapsed time "+bestPos.timeElapsed+" est time: "
		//			+ bestPos.getRemainingTime() + " actions: " + printAction(bestPos.action));
		return bestPos;
	}

	public void mouseClicked(MouseEvent e) {
		PointerInfo a = MouseInfo.getPointerInfo();
		Point point = new Point(a.getLocation());
		SwingUtilities.convertPointFromScreen(point, e.getComponent());
		int x=(int) point.getX();
		int y=(int) point.getY();
		System.out.print("Y: "+y);
		System.out.print("X: "+x+"\n");
	}

	public void initialiseSimulator()
	{
		levelScene = new LevelScene();
		levelScene.init();	
		levelScene.level = new Level(500,15);
	}

	public void setLevelPart(byte[][] levelPart, float[] enemies)
	{
		if (levelScene.setLevelScene(levelPart))
		{
			requireReplanning = true;
		}
		requireReplanning = levelScene.setEnemies(enemies);
	}

	public LevelScene backupState()
	{
		LevelScene sceneCopy = null;
		try
		{
			sceneCopy = (LevelScene) levelScene.clone();
		} catch (CloneNotSupportedException e)
		{
			e.printStackTrace();
		}

		return sceneCopy;
	}



	public void restoreState(LevelScene l)
	{
		levelScene = l;
	}

	public void advanceStep(boolean[] action)
	{

		levelScene.mario.keys = action;
		if (levelScene.verbose > 8) System.out.print("[" 
				+ (action[Mario.KEY_DOWN] ? "d" : "") 
				+ (action[Mario.KEY_RIGHT] ? "r" : "")
				+ (action[Mario.KEY_LEFT] ? "l" : "")
				+ (action[Mario.KEY_JUMP] ? "j" : "")
				+ (action[Mario.KEY_SPEED] ? "s" : "") + "]");
		levelScene.tick();
	}

	public boolean[] optimise(Point goal)
	{
		// do stuff
		long startTime = System.currentTimeMillis();
		LevelScene currentState = backupState();
		if (workScene == null)
			workScene = levelScene;

		int planAhead = 2;
		int stepsPerSearch = 2;

		ticksBeforeReplanning--;
		requireReplanning = false;
		if (ticksBeforeReplanning <= 0 || currentActionPlan.size() == 0 || requireReplanning)
		{
			for (int i = 0; i < 1000; i++)
			{
				GlobalOptions.Pos[i][0] = 0;
				GlobalOptions.Pos[i][1] = 0;
			}
			currentActionPlan = extractPlan(); 
			if (currentActionPlan.size() < planAhead)
			{
				if (levelScene.verbose > 2) System.out.println("Warning!! currentActionPlan smaller than planAhead! plansize: "+currentActionPlan.size());
				planAhead = currentActionPlan.size();
			}
			/*
        	if (requireReplanning) 
        	{
        		workScene = backupState();
        		if (levelScene.verbose > 3) System.out.println("RequireReplanning event!");
            	startSearch(2);
                search(2, startTime - 15);
            	currentActionPlan = extractPlan(); 
            	planAhead = 2;
            	if (currentActionPlan.size() < planAhead)
            	{
            		if (levelScene.verbose > 3) System.out.println("Warning!! currentActionPlan smaller than planAhead! plansize: "+currentActionPlan.size());
            		planAhead = currentActionPlan.size();
            	}
            	restoreState(workScene);
        	}*/

			// simulate ahead to predicted future state, and then plan for this future state 
			if (levelScene.verbose > 3) System.out.println("Advancing current state ... ");
			for (int i = 0; i < planAhead; i++)
			{
				advanceStep(currentActionPlan.get(i));        		
			}
			workScene = backupState();
			startSearch(stepsPerSearch);
			ticksBeforeReplanning = planAhead;
		}
		restoreState(workScene);
		search(stepsPerSearch, startTime, goal);
		workScene = backupState();

		boolean[] action = new boolean[5];
		if (currentActionPlan.size() > 0)
			action = currentActionPlan.remove(0);

		long e = System.currentTimeMillis();
		if (levelScene.verbose > 0) System.out.println("Simulation took "+(e-startTime)+"ms.");
		//if ((e-startTime) > 40) System.out.println("Overtime warning: "+(e-startTime));
		restoreState(currentState);       
		return action;
	}



}