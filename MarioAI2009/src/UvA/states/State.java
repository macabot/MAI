package UvA.states;

import java.io.Serializable;
import java.util.Properties;


public interface State extends Serializable
{	
	/**
	 * Get the reward of prey or predator based on the state.
	 * @return reward of mario
	 */
	public double getReward();
	
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
	
	public double[] getTotalReward();
	
	public void setAllProperties(Properties p);
	
	@Override
	public String toString();

	@Override
	public boolean equals(Object o);
		
	@Override
	public int hashCode();

}//end interface State