package UvA.states;

import UvA.stateSpaceReduction.PCAMeans;
import ch.idsia.mario.environments.Environment;

public class PCAState extends MarioState 
{
	private static final long serialVersionUID = -2154874387050576333L;
	
	final private int meanIndex;
	
	public PCAState(Environment environmentIn, float xPosIn, PCAMeans pcam) 
	{
		super(environmentIn, xPosIn);
		if( pcam!=null )
			this.meanIndex = pcam.sampleToMean(super.getRepresentation());
		else
			this.meanIndex = 0;
	}
	
	public PCAState(Environment environmentIn, float xPosIn, int meanIndexIn)
	{
		super(environmentIn, xPosIn);
		this.meanIndex = meanIndexIn;
	}
	
	public PCAState(double[] reprIn, float oldXPosIn, PCAMeans pcam) 
	{
		super(reprIn, oldXPosIn);
		this.meanIndex = pcam.sampleToMean(super.getRepresentation());
	}
	
	public PCAState(double[] reprIn, float oldXPosIn, int meanIndexIn)
	{
		super(reprIn, oldXPosIn);
		this.meanIndex = meanIndexIn;
	}
	
	@Override
	public State clone()
	{
		return new PCAState(getRepresentation(), oldXPos, meanIndex);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + meanIndex;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PCAState other = (PCAState) obj;
		if (meanIndex != other.meanIndex)
			return false;
		return true;
	}


}//end class
