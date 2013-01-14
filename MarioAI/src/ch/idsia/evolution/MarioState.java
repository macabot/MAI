package ch.idsia.evolution;

import java.util.Arrays;

import ch.idsia.benchmark.mario.environments.Environment;

public class MarioState implements State {

	boolean[] info;
	
	public MarioState(boolean[] stateIn) {
		this.info = stateIn;
	} // end constructor 
	
	
	/**
	 * Get the reward of prey or predator based on the state.
	 * @return reward of mario
	 */ 
	public int getReward() {
		return 1;
	} // end getReward

	
	/**
	 * Return a clone of this state
	 * @return clone
	 */
	public State clone() {
		return new MarioState(info);
	} // end clone
	
	/**
	 * Reset the info to no info
	 */
	public void reset() {
		for(int i = 0; i < info.length; i++)
		info[i] = false;
	} // end reset
	
	
	@Override
	public String toString() {
		String string = "";
		for(boolean b : info) 
			string += " " + b + " ";
		return string; 
	} // end toString

	// TODO
	@Override
	public boolean equals(Object o) {
		if( this == o ) 
			return true;
		if( o == null || getClass() != o.getClass() ) 
			return false;

		MarioState state = (MarioState) o;

		if( state.info.length == (this.info.length) && 
				Arrays.equals(state.info, this.info) )
			return true;
		
		return false;

	} // end equals
		
	@Override
	public int hashCode() {
		return Arrays.hashCode(info);
	} // end hashcode
} // end mariostate class
