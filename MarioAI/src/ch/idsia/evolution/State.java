package ch.idsia.evolution;


public interface State 
{	
	/**
	 * Get the reward of prey or predator based on the state.
	 * @return reward of mario
	 */
	public int getReward();

	
	/**
	 * Return a clone of this state
	 * @return clone
	 */
	public State clone();
	
	/**
	 * Reset the positions of the agents.
	 */
	public void reset();
	
	@Override
	public String toString();

	@Override
	public boolean equals(Object o);
		
	@Override
	public int hashCode();

}//end interface State