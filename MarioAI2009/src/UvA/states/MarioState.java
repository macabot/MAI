package UvA.states;

import java.util.Arrays;

import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.engine.sprites.Sprite;
import ch.idsia.mario.environments.Environment;


public class MarioState implements State 
{
	private static final long serialVersionUID = 4326470085716280782L;
	
	// state representation
	public static final int viewDim = 6;//max 20;// 	//size of statespace that  is represented
	public static final int miscDims = 1; // dimensions for extra information about state

	// 2 windows that contain info on objects and enemies = viewDim x viewDim
	// miscDims spaces for features such as mayMarioJump() and isMarioOnGround()
	private final int amountOfInput = (viewDim + 1)*(viewDim + 1) + miscDims;
	private double[] representation = new double[amountOfInput];
	
	public static transient double xPos = 32;
	public static transient double oldXPos = 32;
	
	
	// enemies killed total
	private static int totalKilledByStomp = 0;
	private static int totalKilledByFire = 0;
	private static int totalKilledByShell = 0;
	
	// enemies killed in current scene
	private static int killedByStomp = 0;
	private static int killedByFire = 0;
	private static int killedByShell = 0;
	
	private static int marioMode = 2;
	private static int lastMarioMode = 2;
	private static int collided = 0;
	
	private static int gainedFlowersSoFar = 0;
	private static int collectedFlowers = 0;
	
	private static int gainedMushroomsSoFar = 0;
	private static int collectedMushrooms = 0;
	
	private static int gainedCoinsSoFar = 0;
	private static int collectedCoins = 0;
	
	
	public static double rewardSoFar = 0;
	public static double currentReward = 0;
	private boolean dieCheck;

	// Parameters for how important the reward for X is 
	private final int REWARD_DISTANCE = 1; //Positive for moving to right, negative for left
	private final int REWARD_KILLED_STOMP = 0;
	private final int REWARD_KILLED_FIRE = 0;
	private final int REWARD_KILLED_SHELL = 0;
	private final int REWARD_COLLIDED = -100; //Should be negative
	private final int REWARD_FLOWER = 0;
	private final int REWARD_MUSHROOM = 0;
	private final int REWARD_COIN = 0;
	private final int REWARD_FALL = -1000;
	
	
	/**
	 * Constructor, creates state representation
	 * @param environment is the mario environment
	 */

	public MarioState(Environment environment) {
		if(environment != null) 
			updateRepresentation( (Environment) environment);
			
	} // end constructor env + xPosIn used by mario
	
	/**
	 * Constructor for when environment was not available: input is representation alone
	 * Used by clone
	 */
	public MarioState(double[] reprIn){
		this.representation = new double[reprIn.length];
		System.arraycopy(reprIn, 0, this.representation, 0, reprIn.length);
	} // end constructor for representation input used by clone
	
	
	/**
	 * updateRepresentation creates the representation of the state
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
		
		int which = 0;
	    for (int i = -viewDim/2; i <= viewDim/2; i++)
	    {
	        for (int j = -viewDim/2; j <= viewDim/2; j++)
	        {
	        	double value = probe(i,j,scene);
	        	switch((int) value) { 
	        	case 25:
	        		value = 0; // fireball becomes 0
	        	case -11:
	        		value = -10; // half border becomes border
	        	case Sprite.KIND_FIRE_FLOWER: 
	        		value = Sprite.KIND_MUSHROOM; // fireflower equals to mushroom
	        	case 21:
	        		value = 16; // nice brick same categorie as brick
	        	case 20:
	        		value = -10; // flower pot/cannon equals border
	        		
	        	} // end switch
	        		
	        	representation[which++] = value;
	        	
	        }
	        
	    }
	    representation[representation.length - 1] = environment.getMarioMode();
	    
	    oldXPos = xPos;
	    xPos = environment.getMarioFloatPos()[0];
	    dieCheck = environment.getMarioFloatPos()[1] > 225;
	    
	    // update enemies killed
		killedByFire = environment.getKillsByFire() - totalKilledByFire;
		killedByStomp = environment.getKillsByStomp() - totalKilledByStomp;
		killedByShell = environment.getKillsByShell() - totalKilledByShell;
		totalKilledByFire = environment.getKillsByFire();
		totalKilledByStomp = environment.getKillsByStomp();
		totalKilledByShell = environment.getKillsByShell();
		marioMode = environment.getMarioMode();
		
		// calculate if collided (lose mario mode)
	    if(marioMode < lastMarioMode){
	    	collided = 1;
	    	System.out.println("Collided!");
	    	lastMarioMode = marioMode;
	    }
	    else
	    	collided = 0;
	    
	    // calculate if picked up a flower
	    int flowers = Mario.gainedFlowers;
	    if(flowers > gainedFlowersSoFar){
	    	collectedFlowers = flowers-gainedFlowersSoFar;
	    	gainedFlowersSoFar = flowers;
	    }
	    else
	    	collectedFlowers = 0;
	    
	    // calculate if picked up a mushroom
	    int mushrooms = Mario.gainedMushrooms;
	    if(mushrooms > gainedMushroomsSoFar){
	    	collectedMushrooms = mushrooms-gainedMushroomsSoFar;
	    	gainedMushroomsSoFar = mushrooms;
	    }
	    else
	    	collectedMushrooms = 0;
	    
	    // calculate coins collected
	    int coins = Mario.coins;
	    if(coins > gainedCoinsSoFar){
	    	collectedCoins = coins-gainedCoinsSoFar;
	    	gainedCoinsSoFar = coins;
	    }
	    else
	    	collectedCoins = 0;

	} // end updateRepresentation
		

	/** 
	 * Get the reward of prey or predator based on the state.
	 * @return reward of mario
	 */ 
	public double getReward() {
		int fall = 0;
		if(dieCheck) {
			System.out.println("Dieing!!!!!");
			fall = 1;
		} // end hack to check if gonna die
		double distance = xPos - oldXPos;
		
		double reward = (double) (distance*REWARD_DISTANCE + killedByStomp*REWARD_KILLED_STOMP + 
				killedByFire*REWARD_KILLED_FIRE + killedByShell*REWARD_KILLED_SHELL + 
				collided*REWARD_COLLIDED + collectedFlowers*REWARD_FLOWER + collectedMushrooms*REWARD_MUSHROOM +
				collectedCoins*REWARD_COIN + REWARD_FALL*fall);
		
		rewardSoFar += reward;
		currentReward = reward;

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
	    return (double) scene[realX][realY];
	    //return (scene[realX][realY] != 0) ? 1.0 : 0.0;
	} // end probe
	
	
	/**
	 * Return a clone of this state
	 * @return clone
	 */
	public State clone() {
		return new MarioState(representation);
	} // end clone
	
	/**
	 * Reset the info to no info
	 */
	public void reset() {
		for(int i = 0; i < representation.length; i++){
			representation[i] = 0.0;
		}
	} // end reset
	
	public static void resetStatic(int mode){
		totalKilledByStomp = 0;
		totalKilledByFire = 0;
		totalKilledByShell = 0;
		killedByStomp = 0;
		killedByFire = 0;
		killedByShell = 0;
		marioMode = mode;
		lastMarioMode = marioMode;
		collided = 0;
		rewardSoFar = 0;
		currentReward = 0;
		gainedFlowersSoFar = 0;
		collectedFlowers = 0;
		gainedMushroomsSoFar = 0;
		collectedMushrooms = 0;
		gainedCoinsSoFar = 0;
		collectedCoins = 0;
		xPos = 32;
		oldXPos = 32;
	}// end resetStatic
	
	
	@Override
	public String toString() {
		String string = "";
		for(int i = 0; i<representation.length; i++) 
		{
			string += String.format(" %.0f", representation[i]);
			if( ( (i+1) % viewDim) == 0 )
				string += "\n";
			if( (i+1) % (viewDim*viewDim) == 0 )
				string += "\n";
		}
		return string; 
	} // end toString

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
	 */
	@Override
	public double[] getRepresentation() {
		return this.representation;
	}

} // end mariostate class
