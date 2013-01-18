package UvA.states;

import java.io.Serializable;


public interface State extends Serializable
{	
	/**
	 * Get the reward of prey or predator based on the state.
	 * @return reward of mario
	 */
	public float getReward();
	
	/**
	 * Return a clone of this state
	 * @return clone
	 */
	public State clone();
	
	/**
	 * Reset the positions of the agents.
	 */
	public void reset();
	
	public double[] getRepresentation();
	
	@Override
	public String toString();

	@Override
	public boolean equals(Object o);
		
	@Override
	public int hashCode();

}//end interface State