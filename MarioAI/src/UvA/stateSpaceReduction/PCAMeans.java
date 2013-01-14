package UvA.stateSpaceReduction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.KMeans;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import UvA.agents.State;

public class PCAMeans 
{
	private int numComponents;
	private int clusterAmount;
	private int iterations;

	PrincipleComponentAnalysis pca;
	List<double[]> means;
	List<double[]> states;

	public PCAMeans(int numComponents, int clusterAmount, int iterations)
	{
		this.numComponents = numComponents;
		this.clusterAmount = clusterAmount;
		this.iterations = iterations;
		this.pca = new PrincipleComponentAnalysis();
		this.means = new ArrayList<double[]>();
		this.states = new ArrayList<double[]>();
	}

	public void addState(State state)
	{
		states.add(state.representation);
		pca.addSample(state.representation);
	}

	public void performPCA()
	{
		pca.computeBasis(numComponents);
	}

	public void createMeans()
	{
		Clusterer km = new KMeans(clusterAmount, iterations);
		Dataset data = new DefaultDataset();
		for (int i = 0; i < states.size(); i++) 
		{
			Instance instance = new DenseInstance(states.get(i));
			data.add(instance);
		}
		Dataset[] clusters = km.cluster(data);
		means = calcMeans(clusters);
	}
	
	public static List<double[]> calcMeans(Dataset[] clusters)
	{
		List<double[]> means = new ArrayList<double[]>();
		
		return means;
	}

}
