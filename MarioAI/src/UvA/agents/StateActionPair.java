package UvA.agents;

import java.io.Serializable;
import java.util.Arrays;

public class StateActionPair implements Serializable 
{
	State state;
	public boolean[] action;
	
	// constructors
	public StateActionPair(StateActionPair sap)
	{
		this(sap.state, sap.action);
	}
	public StateActionPair(State state, boolean[] actionIn)
	{
		this.state = state.clone();
		this.action = actionIn;
	}//end constructors
	
	@Override
	public boolean equals(Object o)
	{
		if( this == o ) 
			return true;
		if( o == null || getClass() != o.getClass() ) 
			return false;

		StateActionPair sap = (StateActionPair) o;

		if( sap.state.equals(this.state) && Arrays.equals(sap.action, this.action) )
			return true;
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return state.hashCode()*33+Arrays.hashCode(action)*33;
	}
	
	@Override
	public String toString()
	{
		String actionString = "";
		for(boolean b : action) 
		{
			actionString += b?"T":"F";
		}
		return String.format("%s & %s", state, actionString);
	}
	
}
