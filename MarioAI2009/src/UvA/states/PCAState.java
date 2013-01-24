package UvA.states;

import UvA.stateSpaceReduction.PCAMeans;
import ch.idsia.mario.environments.Environment;

public class PCAState extends MarioState 
{
	private static final long serialVersionUID = -2154874387050576333L;
	
	private int meanIndex;
	
	/**
	 * Create new PCAState by creating a MarioState and converting its representation
	 * to an index using pcam.
	 * @param environmentIn - information needed to create MarioState
	 * @param pcam - object that dictates how to perform PCA and cluster the results
	 */
	public PCAState(Environment environmentIn, State oldState, PCAMeans pcam) 
	{
		super(environmentIn, oldState);
		if( pcam!=null )
			this.meanIndex = pcam.sampleToMean(super.getRepresentation());
	}
	
	/**
	 * Create new PCAState by initializing MarioState with 'reprIn' and setting the index
	 * @param reprIn - representation of MarioState
	 * @param pcam - object that dictates how to project and cluster a representation
	 */
	/* TODO remove
	public PCAState(double[] reprIn, PCAMeans pcam) 
	{
		super(reprIn);
		this.meanIndex = pcam.sampleToMean(super.getRepresentation());
	}
	*/
	
	public PCAState(State pcaState, int meanIndexIn)
	{
		this((MarioState) pcaState, meanIndexIn);
	}
	
	/**
	 * Create new PCAState by initializing MarioState with 'reprIn' and setting the index
	 * @param reprIn - representation of MarioState
	 * @param meanIndexIn - index of PCAState
	 */
	public PCAState(MarioState pcaState, int meanIndexIn)
	{
		super(pcaState);
		this.meanIndex = meanIndexIn;
	}//end constructors
	
	@Override
	public State clone()
	{
		return new PCAState(this, meanIndex);
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
