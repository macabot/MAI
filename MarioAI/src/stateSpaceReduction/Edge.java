// http://www.algolist.com/code/java/Dijkstra%27s_algorithm

package stateSpaceReduction;

public class Edge implements Comparable<Edge>
{
    public final Vertex target;
    public final double weight;
   
    public Edge(Vertex argTarget, double argWeight)
    { 
    	target = argTarget; weight = argWeight; 
    }

	@Override
	public int compareTo(Edge edge) 
	{
		if( this.weight < edge.weight ) //TODO test if correct
			return -1;
		else if( this.weight > edge.weight )
			return 1;
		else
			return 0;
	}
}

