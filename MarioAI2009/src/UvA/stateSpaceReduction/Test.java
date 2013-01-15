package UvA.stateSpaceReduction;

import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.KMeans;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;

public class Test 
{

	public static void main(String[] args) 
	{
		double[][] vectors = {{1,1,1},{1,1,1},{2,2,2},{2,2,2},{3,3,3},{3,3,3}};
		int numComponents = 2;
		int clusterAmount = 3;
		int iterations = 10;

		PCAMeans pcam = new PCAMeans(vectors, numComponents, clusterAmount, iterations);
		double[] sample = {1,1,1};
		int mean = pcam.sampleToMean(sample);
		System.out.println(mean);

	}//end main

	public static void testPCA()
	{
		/*double[][] sampleData = new double[][] {
        {1, 2, 3, 4, 5, 6},
        {6, 5, 4, 3, 2, 1},
        {2, 2, 2, 2, 2, 2}};*/
		double[][] sampleData = new double[][] {
				{6,7,6,5,7,6,5,6,3,1,2,5,2,3,1,2},
				{5,3,4,7,7,4,7,5,5,3,6,7,4,5,6,3},
				{3,2,4,1,5,2,2,4,6,7,6,7,5,6,5,7},
				{4,2,5,3,5,3,1,4,7,5,7,6,6,5,5,7}
		};
		int numSamples = sampleData.length;
		int sampleSize = sampleData[0].length;
		PrincipleComponentAnalysis pca = new PrincipleComponentAnalysis(numSamples, sampleSize);
		pca.addSamples(sampleData);
		int numComponents = 4;
		pca.computeBasis(numComponents);
		/*double[][] testData = new double[][] {
		{1, 2, 3, 4, 5, 6},
		{1, 2, 1, 2, 1, 2}};*/
		double[][] testData = sampleData;
		for(int i=0; i<testData.length; i++)
		{
			double[] reducedData = pca.sampleToEigenSpace(testData[i]);
			String output = "";
			for(int j=0; j<reducedData.length; j++)
			{
				output += String.format("%f ", reducedData[j]);
			}
			System.out.println(output);
		}
	}

	public static void testCluster()
	{
		double[][] sampleData = new double[][] {
				{1, 2, 3, 4, 5, 6},
				{6, 5, 4, 3, 2, 1},
				{2, 2, 2, 2, 2, 2},
				{1, 2, 3, 4, 5, 6},
				{6, 5, 4, 3, 2, 1},
				{2, 2, 2, 2, 2, 2},
				{1, 2, 3, 4, 5, 6},
				{6, 5, 4, 3, 2, 1},
				{2, 2, 2, 2, 2, 2}};

		///Dataset data = new DefaultDataset();
		Dataset data = new DefaultDataset();
		for (int i = 0; i < sampleData.length; i++) {
			Instance instance = new DenseInstance(sampleData[i]);
			data.add(instance);
		}

		int clusterAmount = 2;
		int iterations = 10;
		Clusterer km = new KMeans(clusterAmount, iterations);
		Dataset[] clusters = km.cluster(data);

		System.out.println(clusters);
	}

}