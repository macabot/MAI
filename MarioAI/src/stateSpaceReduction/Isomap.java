package stateSpaceReduction;

import java.util.List;

import mdsj.MDSJ;

public class Isomap 
{
	Vertex[] graphKNN;
	Vertex[] graphSD;
	double [][] dissMatrix; //dissimilarity matrix
	double[][] means;
	
	public Isomap(List states, int kNN, int dims, int kMeans)
	{
		graphKNN = kNearestNeighbourGraph(states, kNN);
		graphSD = shortestDistanceGraph(graphKNN);
		dissMatrix = dissimilarityMatrix(graphSD);
		double[][] scaledData = MDSJ.classicalScaling(dissMatrix, dims); 
		means = kMeans(dissMatrix, kMeans);
	}
	
	//TODO
	private Vertex[] kNearestNeighbourGraph(List states, int k)
	{
		return null;
	}
	
	//TODO
	private Vertex[] shortestDistanceGraph(Vertex[] graph)
	{
		return null;
	}
	
	//TODO
	private double[][] dissimilarityMatrix(Vertex[] graph)
	{
		return null;
	}
	
	//TODO
	private double[][] kMeans(double[][] scaledData, int k)
	{
		return null;
	}
	
}//end class Isomap
