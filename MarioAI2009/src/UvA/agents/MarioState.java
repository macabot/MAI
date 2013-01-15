package UvA.agents;

import java.io.Serializable;
import java.util.Arrays;
import ch.idsia.mario.environments.Environment;


public class MarioState implements State, Serializable {

	/**
	 * serial version id, does not seem to be necessarily, but i hate warnings
	 */
	private static final long serialVersionUID = -499494658577348783L;

	// state representation
	private double[] representation;
	private Environment environment; // used for cloning 
	
	/* ------------ variables usable in state ---------------- */
	protected byte[][] scene;
	protected byte[][] enemies;
	protected byte[][] mergedObservation;

	protected float[] marioFloatPos = null;
	protected float[] enemiesFloatPos = null;

	private boolean isMarioAbleToJump;
	private boolean isMarioOnGround;
	
	protected int zLevelScene = 1;
	protected int zLevelEnemies = 0;
	
	protected float xPos = 32;
	protected float oldXPos = 32;
	
	/**
	 * Constructor, creates state representation
	 * @param environment is the mario environment
	 */
	public MarioState(Environment environment) {
		update(environment);
	} // end constructor 

	/**
	 * Constructor, creates state representation, when cloning, oldXPos is necessarily
	 * @param environment is the mario environment
	 */
	public MarioState(Environment environment, float oldXPos) {
		update(environment);
		this.oldXPos = oldXPos;
	} // end constructor 

	/**
	 * This function is called in order to update the state; 
	 */
	public void update(Object environment) {
		this.environment = (Environment) environment;
		updateValues();
		updateRepresentation();		
	}
	
	/**
	 * This function extracts values from environment
	 */
	private void updateValues() {
		/* setting info gotten from environment ===================================*/
		// info from environment 
	    scene = environment.getLevelSceneObservationZ(zLevelScene);
	    enemies = environment.getEnemiesObservationZ(zLevelEnemies);
	    mergedObservation = environment.getMergedObservationZ(1, 0);

	    this.marioFloatPos = environment.getMarioFloatPos();
	    this.enemiesFloatPos = environment.getEnemiesFloatPos();
	        
	    isMarioOnGround = environment.isMarioOnGround();
	    isMarioAbleToJump = environment.mayMarioJump();
	    
	    // set distances, in order to get relative distance
	    oldXPos = xPos;
	    xPos = environment.getMarioFloatPos()[0];
	    
	}

	/**
	 * updateRepresentation creates the representation of the state
	 */
	private void updateRepresentation() {
	    int which = 0;
	    for (int i = -3; i < 4; i++)
	    {
	        for (int j = -3; j < 4; j++)
	        {
	            representation[which++] = probe(i, j, scene);
	        }
	    }
	    for (int i = -3; i < 4; i++)
	    {
	        for (int j = -3; j < 4; j++)
	        {
	            representation[which++] = probe(i, j, enemies);
	        }
	    }
	    representation[representation.length + 1] = isMarioOnGround ? 1 : 0;
	    representation[representation.length + 1] = isMarioAbleToJump ? 1 : 0;

	} // end updateRepresentation
	
	/** TODO: get better reward function
	 * Get the reward of prey or predator based on the state.
	 * @return reward of mario
	 */ 
	public float getReward() {
		return xPos - oldXPos;
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
	    return (scene[realX][realY] != 0) ? 1 : 0;
	} // end probe
	
	
	/**
	 * Return a clone of this state
	 * @return clone
	 */
	public State clone() {
		return new MarioState(environment);
	} // end clone
	
	/**
	 * Reset the info to no info
	 */
	public void reset() {
		for(int i = 0; i < representation.length; i++)
		representation[i] = 0;
	} // end reset
	
	
	@Override
	public String toString() {
		String string = "";
		for(double d : representation) 
		{
			string += String.format(" %f", d);
		}
			//string += " " + b + " ";
		return string; 
	} // end toString

	@Override
	public boolean equals(Object o) {
		if( this == o ) 
			return true;
		if( o == null || getClass() != o.getClass() ) 
			return false;

		MarioState state = (MarioState) o;

		if( state.representation.length == (this.representation.length) && 
				Arrays.equals(state.representation, this.representation) )
			return true;
		
		return false;

	} // end equals
		
	@Override
	public int hashCode() {
		return Arrays.hashCode(representation);
	} // end hashcode


} // end mariostate class
