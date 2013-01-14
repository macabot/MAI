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

	}//end main

	public void testPCA()
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
		PrincipleComponentAnalysis pca = new PrincipleComponentAnalysis();
		pca.setup(sampleData.length, sampleData[0].length);
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

}
