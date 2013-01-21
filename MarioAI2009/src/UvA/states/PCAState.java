package UvA.states;

import UvA.stateSpaceReduction.PCAMeans;
import ch.idsia.mario.environments.Environment;

public class PCAState extends MarioState 
{
	private static final long serialVersionUID = -2154874387050576333L;
	
	private int meanIndex;
	
	public PCAState(Environment environmentIn, PCAMeans pcam) 
	{
		super(environmentIn);
		if( pcam!=null )
			this.meanIndex = pcam.sampleToMean(super.getRepresentation());
	}
	
	public PCAState(Environment environmentIn, int meanIndexIn)
	{
		super(environmentIn);
		this.meanIndex = meanIndexIn;
	}
	
	public PCAState(double[] reprIn, PCAMeans pcam) 
	{
		super(reprIn);
		this.meanIndex = pcam.sampleToMean(super.getRepresentation());
	}
	
	public PCAState(double[] reprIn, int meanIndexIn)
	{
		super(reprIn);
		this.meanIndex = meanIndexIn;
	}
	
	@Override
	public State clone()
	{
		return new PCAState(getRepresentation(), meanIndex);
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
	
	@Override
	public String toString()
	{
		return String.format("%d", this.meanIndex);
	}


}//end class
