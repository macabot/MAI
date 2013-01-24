package UvA.states;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.engine.sprites.Sprite;
import ch.idsia.mario.environments.Environment;

import competition.icegic.robin.astar.LevelScene;


public class MarioStateNonStatic implements State 
{
	// necessarily for serializing
	private static final long serialVersionUID = 4326470085716280782L;
	
	// state representation, settable
	public int viewDim = 8;//max 20;// 	//size of statespace that  is represented

	public final int miscDims = 1; // dimensions for extra information about state

	
	// 2 windows that contain info on objects and enemies = (viewDim + 1) x viewDim (X x Y)
	// miscDims spaces for features mario mode
	private final int amountOfInput = (viewDim + 1)*(viewDim) + miscDims;
	private double[] representation = new double[amountOfInput];
	
	// used for reward calculation
	public transient double xPos = 32;
	public transient double oldXPos = 32;

	// Parameters for how important the reward for X is 
	private int REWARD_DISTANCE; //Positive for moving to right, negative for left
	private int REWARD_KILLED_STOMP;
	private int REWARD_KILLED_FIRE;
	private int REWARD_KILLED_SHELL;
	private int REWARD_COLLIDED; //Should be negative
	private int REWARD_FLOWER;
	private int REWARD_MUSHROOM;
	private int REWARD_COIN;
	private int REWARD_DIE; // should be negative
	
	// enemies killed total
	private int totalKilledByStomp = 0;
	private int totalKilledByFire = 0;
	private int totalKilledByShell = 0;
	
	// enemies killed in current scene
	private int killedByStomp = 0;
	private int killedByFire = 0;
	private int killedByShell = 0;
	
	private int marioMode = 2;
	private int lastMarioMode = 2;
	
	// used for heavy negative reward
	private int collided = 0;
	private boolean dieCheck = false;
	
	// following values are used in displaying total reward 
	// and calculating current reward
	// -SoFar values are for displaying total reward
	// Rest is used for storing current reward, at the end the total 
	/// reward is incremented with the current reward 
	
	private int gainedFlowersSoFar = 0;
	private int collectedFlowers = 0;
	
	private int gainedMushroomsSoFar = 0;
	private int collectedMushrooms = 0;
	
	private int gainedCoinsSoFar = 0;
	private int collectedCoins = 0;
	
	
	public double rewardSoFar = 0;
	public double currentReward = 0;
	
	public static Properties properties = new Properties();
	
	
	/**
	 * Constructor, creates state representation
	 * @param environment is the mario environment
	 */

	public MarioStateNonStatic(Environment environment) {
		if(environment != null) 
			updateRepresentation( (Environment) environment);
	} // end constructor env + xPosIn used by mario
	
	public MarioStateNonStatic(LevelScene levelScene) {
		if(levelScene != null) 
			updateRepresentation( levelScene);
	} // end constructor env + xPosIn used by mario
	
	public MarioStateNonStatic(Environment environment, State oldState){
		
		if(environment != null) 
			updateRepresentation( (Environment) environment);
	};
	
	/**
	 * Constructor for when environment was not available: input is representation alone
	 * Used by clone
	 */
	public MarioStateNonStatic(double[] reprIn){
		this.representation = new double[reprIn.length];
		System.arraycopy(reprIn, 0, this.representation, 0, reprIn.length);
	} // end constructor for representation input used by clone
	
	
	private void updateRepresentation(LevelScene levelScene)
	{

		// levenscene.level.map == getmerged without sprites
		byte[][] scene = levelScene.level.map;
	
		// add sprites
/*	     for (competition.icegic.robin.astar.sprites.Sprite sprite : levelScene.sprites)
	        {
	            if (sprite.mapX >= 0 &&
	                sprite.mapX > MarioXInMap - Environment.HalfObsWidth &&
	                sprite.mapX < MarioXInMap + Environment.HalfObsWidth &&
	                sprite.mapY >= 0 &&
	                sprite.mapY > MarioYInMap - Environment.HalfObsHeight &&
	                sprite.mapY < MarioYInMap + Environment.HalfObsHeight )
	            {
	                int obsX = sprite.mapY - MarioYInMap + Environment.HalfObsHeight;
	                int obsY = sprite.mapX - MarioXInMap + Environment.HalfObsWidth;
	                // quick fix TODO: handle this in more general way.
	                if (scene[obsX][obsY] != 14)
	                {
	                    byte tmp = ZLevelEnemyGeneralization(sprite.kind, ZLevelEnemies);
	                    if (tmp != Sprite.KIND_NONE)
	                        ret[obsX][obsY] = tmp;
	                }
	            }
	        } */
		
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
	        	} // end switch
	        		
	        	representation[which++] = value;	
	        }
	    }
	    
	    /////////////////// sets variables for reward function
	    
	    // TODO
	    oldXPos = xPos;
	    xPos = levelScene.mario.mapX;
	    
	    // check for below point of no return
	    dieCheck = levelScene.mario.mapY > 225;
	    
	    // update enemies killed
		killedByFire = levelScene.enemiesKilled - totalKilledByFire; // TODO: wrong
		killedByStomp = levelScene.enemiesJumpedOn - totalKilledByStomp;
		killedByShell = levelScene.enemiesKilled - totalKilledByShell; // TODO: wrong
		totalKilledByFire = levelScene.enemiesKilled; // TODO: wrong
		totalKilledByStomp = levelScene.enemiesJumpedOn;
		totalKilledByShell = levelScene.enemiesKilled; // TODO: wrong
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
		
		


	} // end update levelScene (astar)
	
	/**
	 * updateRepresentation creates the representation of the state
	 * @param environment is the environment created by mario engine
	 */
	private void updateRepresentation(Environment environment) {
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
	        	} // end switch
	        		
	        	representation[which++] = value;	
	        }
	    }
	    
	    representation[representation.length - 1] = environment.getMarioMode();
	    // end representation
	    
	    /////////////////// sets variables for reward function
	    
	    oldXPos = xPos;
	    xPos = environment.getMarioFloatPos()[0];
	    
	    // check for below point of no return
	    dieCheck = environment.getMarioFloatPos()[1] > 225;
	    
	    // update enemies killed
		killedByFire = environment.getKillsByFire() - totalKilledByFire;
		killedByStomp = environment.getKillsByStomp() - totalKilledByStomp;
		killedByShell = environment.getKillsByShell() - totalKilledByShell;
		totalKilledByFire = environment.getKillsByFire();
		totalKilledByStomp = environment.getKillsByStomp();
		totalKilledByShell = environment.getKillsByShell();
		marioMode = environment.getMarioMode();
		
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
	    
	} // end updateRepresentation
		

	/** 
	 * Get the reward of prey or predator based on the state.
	 * @return reward of mario
	 */ 
	public double getReward() {
		// If dieing, return the reward for dieing
		if(dieCheck) {
			//System.out.print("Dieing! Reward: " + REWARD_DIE + " ");
			return REWARD_DIE;
		}
		
		
		double distance = xPos - oldXPos;
		
		double reward = (double) (distance*REWARD_DISTANCE + killedByStomp*REWARD_KILLED_STOMP + 
				killedByFire*REWARD_KILLED_FIRE + killedByShell*REWARD_KILLED_SHELL + 
				collided*REWARD_COLLIDED + collectedFlowers*REWARD_FLOWER + collectedMushrooms*REWARD_MUSHROOM +
				collectedCoins*REWARD_COIN) - 1;
		
		rewardSoFar += reward; // used in mario engine for displaying total reward
		currentReward = reward;
	
		//if(collided!= 0)
		//	System.out.print("Collided! Reward: " + reward + " ");
		return reward;
	} // end getReward

	/**
	 * Used in getAction for getting x and y positions relative to mario
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
		return new MarioStateNonStatic(representation);
	} // end clone
	
	/**
	 * Reset the info to zero
	 */
	public void reset() {
		for(int i = 0; i < representation.length; i++){
			representation[i] = 0.0;
		}
	} // end reset
		
	
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
		MarioStateNonStatic other = (MarioStateNonStatic) obj;
		if (amountOfInput != other.amountOfInput)
			return false;
		if (!Arrays.equals(representation, other.representation))
			return false;
		return true;
	}
		
	/**
	 * hashCode() overrides hashcode for hashmapping, 
	 * automatically created by ecipse
	 * 
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
		double[] reward = {xPos, rewardSoFar};
		return reward;
	} // end get total reward
	
	public static Properties readPropertiesFile(String configFilePath){
		//String configFilePath = "D:/settings.properties";
		// load the properties file
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
		REWARD_DISTANCE = Integer.parseInt(properties.getProperty("reward_distance", "2"));;
		REWARD_KILLED_STOMP = Integer.parseInt(properties.getProperty("reward_stomp", "0"));;
		REWARD_KILLED_FIRE = Integer.parseInt(properties.getProperty("reward_fire", "0"));
		REWARD_KILLED_SHELL = Integer.parseInt(properties.getProperty("reward_shell", "0"));
		REWARD_COLLIDED = Integer.parseInt(properties.getProperty("reward_collided", "-1000"));
		REWARD_FLOWER = Integer.parseInt(properties.getProperty("reward_flower", "10"));
		REWARD_MUSHROOM = Integer.parseInt(properties.getProperty("reward_mushroom","10"));
		REWARD_COIN = Integer.parseInt(properties.getProperty("reward_coin","1"));
		REWARD_DIE = Integer.parseInt(properties.getProperty("reward_die", "-1000"));
		// representation
		viewDim = Integer.parseInt(properties.getProperty("viewDim", "8"));
	}

} // end mariostate class
