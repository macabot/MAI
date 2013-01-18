package UvA.states;

import java.io.Serializable;
import java.util.Arrays;

public class StateActionPair implements Serializable 
{
	private static final long serialVersionUID = -4351206083833990887L;
	
	final public State state;
	final public boolean[] action;
	
	// constructors
	public StateActionPair(StateActionPair sap)
	{
		this(sap.state, sap.action);
	}
	
	public StateActionPair(State state, boolean[] actionIn)
	{
		this.state = state.clone();
		this.action = new boolean[actionIn.length];
		System.arraycopy(actionIn, 0, this.action, 0, actionIn.length);
	}//end constructors
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StateActionPair other = (StateActionPair) obj;
		if (!Arrays.equals(action, other.action))
			return false;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(action);
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		return result;
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
