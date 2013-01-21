/*
 * TODO test if projection matrix is the same for a set of states
 * that does contains duplicates and a set of states that doesn't.
 * 
 */

package UvA.stateSpaceReduction;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.KMeans;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import UvA.states.State;

public class PCAMeans implements Serializable
{
	private static final long serialVersionUID = -2478983458498184932L;

	private PrincipleComponentAnalysis pca;
	private Dataset means;

	private int verbose = 0;
	private String path = System.getProperty("user.dir") + "/debugPCAM.db";;

	/**
	 * Constructor performs PCA on states and clusters the eigen space projections.
	 * @param states	training data for PCA
	 * @param numComponents		number of components for PCA
	 * @param clusterAmount		amount of clusters for kMeans clustering
	 * @param iterations		amount of iterations for kMeans clustering
	 */
	public PCAMeans(List<State> states, int numComponents, int clusterAmount, int iterations)
	{
		this(statesToVectors(states), numComponents, clusterAmount, iterations);
	}
	
	public PCAMeans(double[][] vectors, int numComponents, int clusterAmount, int iterations)
	{
		// perform PCA
		int numSamples = vectors.length;
		int sampleSize = vectors[0].length;
		this.pca = new PrincipleComponentAnalysis(numSamples, sampleSize);
		pca.addSamples(vectors);
		pca.computeBasis(numComponents);
		double[][] projections = pca.samplesToEigenSpace(vectors);

		// cluster PCA results		
		this.means = calculateMeans(projections, clusterAmount, iterations);
	}
	//end constructors

	public static double[][] statesToVectors(List<State> states)
	{
		// create vectors for PCA
		int numSamples = states.size();
		int sampleSize = states.get(0).getRepresentation().length;
		double[][] vectors = new double[numSamples][sampleSize];
		for(int i=0; i<vectors.length; i++)
			vectors[i] = states.get(i).getRepresentation();

		return vectors;
	}

	/**
	 * Calculate the means of the clusters found in 'vectors'.
	 * @param vectors	n vectors with m dimensions
	 * @param clusterAmount		amount of clusters for kMeans clustering
	 * @param iterations		amount of iterations for kMeans clustering
	 * @return Dataset containing means of clusters
	 */
	public Dataset calculateMeans(double[][] vectors, int clusterAmount, int iterations)
	{
		Clusterer km = new KMeans(clusterAmount, iterations);
		Dataset data = new DefaultDataset();
		for (int i = 0; i < vectors.length; i++) 
		{
			Instance instance = new DenseInstance(vectors[i]);
			data.add(instance);
		}
		Dataset[] clusters = km.cluster(data);
		if( verbose==1 )
			clustersToFile(clusters);
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
			Instance sum = new DenseInstance(new double[dataset.get(0).noAttributes()]);
			for(Instance instance: dataset)
			{
				sum = sum.add(instance);
			}
			means.add(sum.divide(dataset.size()));
		}
		return means;
	}

	public void clustersToFile(Dataset[] clusters)
	{
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(path));
			for(int c=0; c<clusters.length; c++)
			{
				Dataset cluster = clusters[c];
				out.write(String.format("C%d = [",c));
				for(int i=0; i<cluster.size(); i++)
				{
					Instance vector = cluster.get(i);
					out.write(instanceToString(vector));
					if( i!=cluster.size()-1 )
						out.write("; ");
				}

				out.write("]\n");
			}

			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	public static String instanceToString(Instance vector)
	{
		String s = vector.toString();		
		String[] toReplace = {"{", "}", "[", "]", ";", "null"};
		for(String repl: toReplace)
		{
			s = s.replace(repl, "");
		}
		return s;
	}

	public int sampleToMean(double[] sample)
	{
		Instance projection = new DenseInstance(pca.sampleToEigenSpace(sample));
		int nearestMeanIndex = 0;
		double bestDist = distance(projection, means.get(nearestMeanIndex));
		for(int i=1; i<means.size(); i++)
		{
			double tempDist = distance(projection, means.get(i)); 
			if( tempDist < bestDist)
			{
				nearestMeanIndex = i;
				bestDist = tempDist;
			}
		}
		return nearestMeanIndex;
	}

	public static double distance(Instance a, Instance b)
	{
		Instance diff = a.minus(b);
		Instance squaredDiff = diff.multiply(diff);
		double sum = 0;
		for(int i=0; i<squaredDiff.noAttributes(); i++)
			sum += squaredDiff.get(i);

		return Math.sqrt(sum);		
	}



}//end class
