package UvA.states;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.engine.sprites.Sprite;
import ch.idsia.mario.environments.Environment;

import competition.icegic.robin.astar.LevelScene;


public class MarioState implements State 
{
	// necessarily for serializing
	private static final long serialVersionUID = 4326470085716280782L;

	// state representation, settable
	public transient int viewDim = 8;//max 20;// 	//size of statespace that  is represented
	public transient static final int miscDims = 1; // dimensions for extra information about state


	// 2 windows that contain info on objects and enemies = (viewDim + 1) x viewDim (X x Y)
	// miscDims spaces for features mario mode
	private transient final int amountOfInput = (viewDim + 1)*(viewDim) + miscDims;
	private double[] representation = new double[amountOfInput];

	// used for reward calculation
	public transient double xPos = 32;
	public transient double oldXPos = 32;

	// Parameters for how important the reward for X is, set in configFile
	private transient int REWARD_DISTANCE = 2; //Positive for moving to right, negative for left
	private transient int REWARD_KILLED_STOMP = 0;
	private transient int REWARD_KILLED_FIRE = 0;
	private transient int REWARD_KILLED_SHELL = 0;
	private transient int REWARD_COLLIDED = -1000; //Should be negative
	private transient int REWARD_FLOWER = 10;
	private transient int REWARD_MUSHROOM = 10;
	private transient int REWARD_COIN = 1;
	private transient int REWARD_DIE = -1000; // should be negative

	// enemies killed total
	private transient int totalKilledByStomp = 0;
	private transient int totalKilledByFire = 0;
	private transient int totalKilledByShell = 0;

	// enemies killed in current scene
	private transient int killedByStomp = 0;
	private transient int killedByFire = 0;
	private transient int killedByShell = 0;


	private transient int marioMode = 2;

	// used for heavy negative reward
	private transient int collided = 0;
	private transient int dieCheck = 0;

	// following values are used in displaying total reward 
	// and calculating current reward
	// -SoFar values are for displaying total reward
	// Rest is used for storing current reward, at the end the total 
	/// reward is incremented with the current reward 

	private transient int gainedFlowersSoFar = 0;
	private transient int collectedFlowers = 0;

	private transient int gainedMushroomsSoFar = 0;
	private transient int collectedMushrooms = 0;

	private transient int gainedCoinsSoFar = 0;
	private transient int collectedCoins = 0;

	public transient double rewardSoFar = 0;
	public transient double currentReward = 0;



	////////////////////////// end values in class  start constructors
	/**
	 * marioState constructor for the first mario creation: only initiates 
	 * the config file and sets the values to default (most of them 0)
	 * @param env is the env (in this case null)
	 * @param st is the oldstate (null aswell)
	 * @param configFile -- string where the config file is situated
	 */
	public MarioState(String configFile) {
		Properties properties = MarioState.readPropertiesFile(configFile);
		representation[representation.length - 1] = 2; // set mariomode to 2
		setAllProperties(properties); 
	} // end first constructors made by mario

	/**
	 * Constructor, creates state representation from environment
	 * @param environment is the mario environment
	 * @param oldState is the old state given by mario
	 */
	public MarioState(Environment environment, State oldState) {
		if(environment == null) 
			System.err.println("Trying to set mariostate with empty env but no configfile, should not happen!");

		setAllProperties(oldState);
		updateRepresentation(environment, oldState);
	} // end constructor env + xPosIn used by mario

	/**
	 * The constructor for astar created level scenes
	 * @param levelScene the level scene given by astar
	 * @param oldState the oldstate given by mario
	 */
	public MarioState(LevelScene levelScene, State oldState) {	
		if(levelScene == null) 
			System.err.println("Trying to set mariostate with empty levelScene but no configfile, should not happen!");

		setAllProperties(oldState);
		updateRepresentation(levelScene, oldState);
	} // end constructor env + xPosIn used by mario

	/** 
	 * Constructor with own class as input, used for copying, very ugly but easiest soluation
	 * @param marioState is the marioState to return
	 */
	public MarioState(MarioState marioState) {
		if (marioState == null)
			System.err.println("Error, marioState to clone is empty! Should not happen, right?");

		this.representation = new double[marioState.representation.length];
		System.arraycopy(marioState.representation, 0, this.representation, 0, this.representation.length);

		setAllProperties(marioState);

		this.xPos = marioState.xPos;
		this.oldXPos = marioState.oldXPos;

		this.totalKilledByStomp = marioState.totalKilledByStomp;
		this.totalKilledByFire = marioState.totalKilledByFire;
		this.totalKilledByShell = marioState.totalKilledByShell;

		this.killedByStomp = marioState.killedByStomp;
		this.killedByFire = marioState.killedByFire;
		this.killedByShell = marioState.killedByShell;

		this.marioMode = marioState.marioMode;

		this.collided = marioState.collided;
		this.dieCheck = marioState.dieCheck;

		this.gainedFlowersSoFar = marioState.gainedFlowersSoFar;
		this.collectedFlowers = marioState.collectedFlowers;

		this.gainedMushroomsSoFar = marioState.gainedMushroomsSoFar;
		this.collectedMushrooms = marioState.collectedMushrooms;

		this.gainedCoinsSoFar = marioState.gainedCoinsSoFar;
		this.collectedCoins = marioState.collectedCoins;

		this.rewardSoFar = marioState.rewardSoFar;
		this.currentReward = marioState.currentReward;
	} // end constructor of input marioState


	///////////////////////////////////////// end constructors, starting update functions
	private void updateRepresentation(LevelScene levelScene, State oldState)
	{

		// levenscene.level.map == getmerged without sprites
		byte[][] scene = levelScene.level.map;

		int MarioXInMap = (int)levelScene.mario.x/16;
		int MarioYInMap = (int)levelScene.mario.y/16;

		int which = -1;
		for (int y = MarioYInMap-viewDim/2; y < (MarioYInMap + viewDim/2); y++)
		{
			for (int x = MarioXInMap-viewDim/2; x <= (MarioXInMap + viewDim/2); x++)
			{
				which++;

				if (x >=0 && x <= levelScene.level.xExit && y >= 0 && y < levelScene.level.height) {// if y is in map and x is in map

					// 2 = stompable enemy
					// 9 = not stompable enemy
					// 25 = fireball from mario
					// 34 = coin
					// -10 = border 
					// -11 = half border --> border
					// 21 = nice brick (coin/mush
					// Sprite.KIND_MUSHROOM = mushroom
					// Sprite.KIND_FIRE_FLOWER = flower
					// 16 = cheatingboxes = normal brick => question brick
					// 20 = flower pot/cannon ==> border

					double value = scene[x][y];

					// inverse of levelScene setLevel
					switch ((int) value)
					{
					case -106: value = 0; break;
					case 4: value = -10;break;
					case 9: value = -12;break;
					case -123: value = -10;break;//-76;break;
					case 10: value = -10;break;

					case 25:
						value = 0; // fireball becomes 0
						break;
					case -11:
						value = -10; // half border becomes border
						break;
					case Sprite.KIND_FIRE_FLOWER: 
						value = Sprite.KIND_MUSHROOM; // fireflower equals to mushroom
						break;
					case 21:
						value = 16; // nice brick same categorie as brick
						break;
					case 20:
						value = -10; // flower pot/cannon equals border

					}

					representation[which] = value;
				} // end if in range

			} // for y
		} // end for x

		representation[representation.length-1] = 2- levelScene.mario.damage;

		// TODO: add sprites

		// TODO: get from levelScene and oldState 
		/*
        oldXPos = xPos;
	    xPos = levelScene.mario.x;

	    // check for below point of no return
	    dieCheck = levelScene.mario.y > 225;

	    // update enemies killed
		killedByFire = levelScene.enemiesKilled - totalKilledByFire; 
		killedByStomp = levelScene.enemiesJumpedOn - totalKilledByStomp;
		killedByShell = levelScene.enemiesKilled - totalKilledByShell;
		totalKilledByFire = levelScene.enemiesKilled; 
		totalKilledByStomp = levelScene.enemiesJumpedOn;
		totalKilledByShell = levelScene.enemiesKilled;
		marioMode = 2 - levelScene.mario.damage;

		// calculate dynamic values

		// first set them to 0 (reset)
		collided = 0;
		collectedFlowers = 0;
		collectedMushrooms = 0;
		collectedCoins = 0;

		// check collided
		if(marioMode < lastMarioMode) {
			collided = 1; 
			lastMarioMode = marioMode; // set for next evaluation
		}

		// check pickup flower
		if(Mario.gainedFlowers > gainedFlowersSoFar) {
			collectedFlowers = Mario.gainedFlowers - gainedFlowersSoFar;
			gainedFlowersSoFar = Mario.gainedFlowers;
		}

		// check if pickup mushroom
		if(Mario.gainedMushrooms > gainedMushroomsSoFar) {
			collectedMushrooms = Mario.gainedMushrooms-gainedMushroomsSoFar;
	    	gainedMushroomsSoFar = Mario.gainedMushrooms;
		}

	    // check pickup coins
	    if(Mario.coins > gainedCoinsSoFar){
	    	collectedCoins = Mario.coins-gainedCoinsSoFar;
	    	gainedCoinsSoFar = Mario.coins;
	    }

		 */



	} // end update levelScene (a star)

	/**
	 * updateRepresentation creates the representation of the state
	 * @param environment is the environment created by mario engine
	 * @param stateIn the old state from which values are taken
	 */
	private void updateRepresentation(Environment environment, State stateIn) {
		MarioState oldState = (MarioState) stateIn;
		byte[][] scene = environment.getMergedObservationZ(1, 1);


		// 2 = stompable enemy
		// 9 = not stompable enemy
		// 25 = fireball from mario
		// 34 = coin
		// -10 = border 
		// -11 = half border --> border
		// 21 = nice brick (coin/mush
		// Sprite.KIND_MUSHROOM = mushroom
		// Sprite.KIND_FIRE_FLOWER = flower
		// 16 = cheatingboxes = normal brick => question brick
		// 20 = flower pot/cannon ==> border

		// returns representation
		int which = 0;
		for (int y = -viewDim/2; y < viewDim/2; y++)
		{
			for (int x = -viewDim/2; x <= viewDim/2; x++)
			{
				double value = probe(x,y,scene);
				switch((int) value) { 
				case 25:
					value = 0; // fireball becomes 0
					break;
				case -11:
					value = -10; // half border becomes border
					break;
				case Sprite.KIND_FIRE_FLOWER: 
					value = Sprite.KIND_MUSHROOM; // fireflower equals to mushroom
					break;
				case 21:
					value = 16; // nice brick same categorie as brick
					break;
				case 20:
					value = -10; // flower pot/cannon equals border
					break;
				} // end switch

				representation[which++] = value;	
			}
		}
		marioMode = environment.getMarioMode();
		representation[representation.length - 1] = marioMode;
		// end representation

		/////////////////// sets variables for reward function
		oldXPos = oldState.xPos;
		xPos = environment.getMarioFloatPos()[0];

		// check for below point of no return
		boolean tmpDieCheck = environment.getMarioFloatPos()[1] > 225;
		if(tmpDieCheck == true)
			dieCheck = 1;
		else
			dieCheck = 0;

		// update enemies killed
		totalKilledByFire = environment.getKillsByFire();
		totalKilledByStomp = environment.getKillsByStomp();
		totalKilledByShell = environment.getKillsByShell();

		killedByFire = totalKilledByFire - oldState.killedByFire;
		killedByStomp = totalKilledByStomp - oldState.killedByStomp;
		killedByShell = totalKilledByShell - oldState.killedByShell;


		// update stuff collected total and reward
		gainedFlowersSoFar = Mario.gainedFlowers;
		gainedMushroomsSoFar = Mario.gainedMushrooms;
		gainedCoinsSoFar = Mario.coins;
		rewardSoFar = oldState.rewardSoFar;

		// set current events
		collided = (oldState.marioMode > marioMode)? 1 : 0;
		collectedFlowers = gainedFlowersSoFar - oldState.gainedFlowersSoFar;
		collectedMushrooms = gainedMushroomsSoFar - oldState.gainedMushroomsSoFar;
		collectedCoins = gainedCoinsSoFar - oldState.gainedCoinsSoFar;
				
	} // end updateRepresentation

	///////////////////////////////////////// end update function, start getReward function
	/** 
	 * Get the reward of prey or predator based on the state.
	 * @return reward of mario
	 */ 
	public double getReward() {
		double reward;
		double distance = xPos - oldXPos;

		reward = (double) (distance*REWARD_DISTANCE + killedByStomp*REWARD_KILLED_STOMP + 
				killedByFire*REWARD_KILLED_FIRE + killedByShell*REWARD_KILLED_SHELL + 
				collided*REWARD_COLLIDED + collectedFlowers*REWARD_FLOWER + collectedMushrooms*REWARD_MUSHROOM +
				collectedCoins*REWARD_COIN + dieCheck*REWARD_DIE) - 1;

		rewardSoFar += reward; // used in mario engine for displaying total reward
		//currentReward = reward;
		//System.out.println("RewardSoFar: " + rewardSoFar);
		//System.out.println("currentReward: " + currentReward);

		return reward;
	} // end getReward


	/**
	 * Used in setting representation for getting x and y positions relative to mario
	 * @param x: absolute x position
	 * @param y: absolute y position
	 * @param scene: scene object created in basicmarioAIagent
	 * @return
	 */
	private double probe(int x, int y, byte[][] scene)
	{
		int realX = x + Environment.HalfObsWidth;
		int realY = y + Environment.HalfObsHeight;
		return (double) scene[realY][realX];

		//return (scene[realX][realY] != 0) ? 1.0 : 0.0;
	} // end probe


	/**
	 * Return a clone of this state
	 * @return clone
	 */ 
	public State clone() {
		return new MarioState(this);
	} // end clone


	/**
	 * Overrides toString method, simply prints a dim x dim matrix of the representation
	 * @return A string representation the object
	 */
	@Override
	public String toString() {
		String string = String.format("%.0f ", representation[0]);
		for(int i = 1; i<representation.length; i++) 
		{
			if( ( (i) % (viewDim+1)) == 0 )
				string += "\n";

			string += String.format("%.0f ", representation[i]);

			if( (i+1) % ((viewDim + 1)*viewDim) == 0 )
				string += "\n";
		}
		return string += "\n\n"; 
	} // end toString

	/**
	 * Equals overriddes normal equals, used for hashmapping, 
	 * automatically created by eclipse
	 * 
	 * @param a object to compare with
	 * @return -- a boolean, whether object is equal to input or not
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MarioState other = (MarioState) obj;
		if (amountOfInput != other.amountOfInput)
			return false;
		if (!Arrays.equals(representation, other.representation))
			return false;
		return true;
	}

	/**
	 * hashCode() overrides hashcode for hashmapping, 
	 * automatically created by ecipse
	 * @return an int (the hashcode)
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + amountOfInput;
		result = prime * result + Arrays.hashCode(representation);
		return result;
	}

	/**
	 * Override getrepresentation, returns representation of the state
	 * @return A double array (double[]), the representation of the mario state
	 */
	@Override
	public double[] getRepresentation() {
		return this.representation;
	}

	/**
	 * Returns reward of current state
	 * @return - of which the first element is the x position of mario, 
	 * and the second command is the reward according to mario
	 */
	public double[] getTotalReward() {
		double[] reward = {this.xPos, this.rewardSoFar};
		return reward;
	} // end get total reward

	public static Properties readPropertiesFile(String configFilePath){
		//String configFilePath = "D:/settings.properties";
		// load the properties file
		Properties properties = new Properties();
		try {
			FileInputStream  fis = new FileInputStream(configFilePath);
			properties.load(fis);

			// close file input stream
			if (fis != null){
				fis.close();
			}// end if

		} catch (IOException e) {
			System.out.println("Unknown properties file as input! Using default values.");
			e.printStackTrace();
		}

		return properties;
	}// end function readPropertiesFile

	public void setAllProperties(Properties properties){
		// rewards
		this.REWARD_DISTANCE = Integer.parseInt(properties.getProperty("reward_distance", "2"));;
		this.REWARD_KILLED_STOMP = Integer.parseInt(properties.getProperty("reward_stomp", "0"));;
		this.REWARD_KILLED_FIRE = Integer.parseInt(properties.getProperty("reward_fire", "0"));
		this.REWARD_KILLED_SHELL = Integer.parseInt(properties.getProperty("reward_shell", "0"));
		this.REWARD_COLLIDED = Integer.parseInt(properties.getProperty("reward_collided", "-1000"));
		this.REWARD_FLOWER = Integer.parseInt(properties.getProperty("reward_flower", "10"));
		this.REWARD_MUSHROOM = Integer.parseInt(properties.getProperty("reward_mushroom","10"));
		this.REWARD_COIN = Integer.parseInt(properties.getProperty("reward_coin","1"));
		this.REWARD_DIE = Integer.parseInt(properties.getProperty("reward_die", "-1000"));
		// representation
		this.viewDim = Integer.parseInt(properties.getProperty("viewDim", "8"));
	}

	public void setAllProperties(State state){
		// rewards
		MarioState marioState = (MarioState) state;
		this.REWARD_DISTANCE = marioState.REWARD_DISTANCE;
		this.REWARD_KILLED_STOMP = marioState.REWARD_KILLED_STOMP;
		this.REWARD_KILLED_FIRE = marioState.REWARD_KILLED_FIRE;
		this.REWARD_KILLED_SHELL = marioState.REWARD_KILLED_SHELL;
		this.REWARD_COLLIDED = marioState.REWARD_COLLIDED;
		this.REWARD_FLOWER = marioState.REWARD_FLOWER;
		this.REWARD_MUSHROOM = marioState.REWARD_MUSHROOM;
		this.REWARD_COIN = marioState.REWARD_COIN;
		this.REWARD_DIE = marioState.REWARD_DIE;
		// representation
		this.viewDim = marioState.viewDim;
	}

	/**
	 * Reset the info to zero, ugly but it works and i'm too lazy to make it better
	 */
	public void reset() {
		this.representation = new double[amountOfInput];

		this.xPos = 32;
		oldXPos = 32;

		this.totalKilledByStomp = 0;
		this.totalKilledByFire = 0;
		this.totalKilledByShell = 0;

		this.killedByStomp = 0;
		this.killedByFire = 0;
		this.killedByShell = 0;

		this.marioMode = 2;

		this.collided = 0;
		this.dieCheck = 0;

		this.gainedFlowersSoFar = 0;
		this.collectedFlowers = 0;

		this.gainedMushroomsSoFar = 0;
		this.collectedMushrooms = 0;

		this.gainedCoinsSoFar = 0;
		this.collectedCoins = 0;

		this.rewardSoFar = 0;
		this.currentReward = 0;
	} // end reset

	public int getViewDim()
	{
		return viewDim;
	}

} // end marioState class