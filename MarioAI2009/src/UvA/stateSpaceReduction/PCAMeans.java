package UvA.stateSpaceReduction;

import java.util.List;

import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.KMeans;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import UvA.agents.State;

public class PCAMeans 
{
	PrincipleComponentAnalysis pca;
	Dataset means;
	
	/**
	 * Constructor performs PCA on states and clusters the eigen space projections.
	 * @param states	training data for PCA
	 * @param numComponents		number of components for PCA
	 * @param clusterAmount		amount of clusters for kMeans clustering
	 * @param iterations		amount of iterations for kMeans clustering
	 */
	public PCAMeans(List<State> states, int numComponents, int clusterAmount, int iterations)
	{
		// create vectors for PCA
		int numSamples = states.size();
		int sampleSize = states.get(0).getRepresentation().length;
		double[][] vectors = new double[numSamples][sampleSize];
		for(int i=0; i<vectors.length; i++)
			vectors[i] = states.get(i).getRepresentation();
		
		// perform PCA
		this.pca = new PrincipleComponentAnalysis(numSamples, sampleSize);
		pca.addSamples(vectors);
		pca.computeBasis(numComponents);
		double[][] projections = pca.samplesToEigenSpace(vectors);
		
		// cluster PCA results		
		this.means = calculateMeans(projections, clusterAmount, iterations);
	}//end constructor

	/**
	 * Calculate the means of the clusters found in 'vectors'.
	 * @param vectors	n vectors with m dimensions
	 * @param clusterAmount		amount of clusters for kMeans clustering
	 * @param iterations		amount of iterations for kMeans clustering
	 * @return Dataset containing means of clusters
	 */
	public static Dataset calculateMeans(double[][] vectors, int clusterAmount, int iterations)
	{
		Clusterer km = new KMeans(clusterAmount, iterations);
		Dataset data = new DefaultDataset();
		for (int i = 0; i < vectors.length; i++) 
		{
			Instance instance = new DenseInstance(vectors[i]);
			data.add(instance);
		}
		Dataset[] clusters = km.cluster(data);
		return calcMeans(clusters);
	}
	
	/**
	 * Calculate the means of clusters
	 * @param clusters kMeans clusters
	 * @return Dataset containing means of clusters
	 */
	public static Dataset calcMeans(Dataset[] clusters)
	{
		Dataset means = new DefaultDataset();
		for(Dataset dataset: clusters)
		{
			Instance sum = new DenseInstance(new double[dataset.size()]);
			for(Instance instance: dataset)
			{
				sum.add(instance);
			}
			means.add(sum.divide(dataset.size()));
		}
		return means;
	}
	
}//end class
