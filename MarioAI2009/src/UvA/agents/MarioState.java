package UvA.agents;

import java.io.Serializable;
import java.util.Arrays;
import ch.idsia.mario.environments.Environment;


@SuppressWarnings("serial") // annoying warning
public class MarioState implements State, Serializable {


	// state representation
	private final int amountOfInput = 100;
	private double[] representation = new double[amountOfInput];
	
	protected transient float xPos = 32;
	protected transient float oldXPos = 32;
	
	/**
	 * Constructor, creates state representation
	 * @param environment is the mario environment
	 */
	public MarioState(Environment environment) {
		if(environment != null) 
			update(environment);
		else 
			System.out.println("Input environment when creating state is null, may only happen at the creation of mario");
			
	} // end constructor 


	/**
	 * Constructor for when environment was not available: input is representation alone
	 */
	public MarioState(double[] reprIn, float oldXPosIn){
		this.representation = reprIn;
		this.oldXPos = oldXPosIn;
	} // end constructor for representation input
	
	/**
	 * This function is called in order to update the state; 
	 */
	public void update(Object environment) {
		if(environment != null) {
			updateRepresentation( (Environment) environment);	
		}
	}
	
	
	/**
	 * updateRepresentation creates the representation of the state
	 */
	private void updateRepresentation(Environment environment) {
        byte[][] scene = environment.getLevelSceneObservation();
        byte[][] enemies = environment.getEnemiesObservation();
		
		
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
	    representation[representation.length - 2] = environment.mayMarioJump() ? 1 : 0;
	    representation[representation.length - 1] = environment.isMarioOnGround() ? 1 : 0;
	    
	    this.oldXPos = xPos;
	    xPos = environment.getMarioFloatPos()[0];

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
		return new MarioState(representation, oldXPos);
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
		for(int i = 0; i<representation.length; i++) 
		{
			string += String.format(" %d", representation[i]);
			if( ( (i+1) % 7) == 0)
				string += "\n";
			if( ( (i+1) % 49) == 0)
				string += "\n";
		}
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

	@Override
	public double[] getRepresentation() {
		return this.representation;
	}


} // end mariostate class
