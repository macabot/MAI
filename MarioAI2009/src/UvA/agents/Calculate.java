package UvA.agents;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class Calculate {
	/**
	 * Calculate the mean of a set of times.
	 * @param timeList 	list of episode lengths
	 * @return mean of episode lengths
	 */
	public static double mean(int[] array)
	{
		double sum = 0;
		for(int i=0; i<array.length; i++)
			sum += array[i];
		return sum / array.length;
	}
	public static double mean(double[] array)
	{
		double sum = 0;
		for(int i=0; i<array.length; i++)
			sum += array[i];
		return sum / array.length;
	}
	
	public static double[] mean(int[][] episodes)
	{
		int sum[] = new int[episodes[0].length];
		for(int i=0; i<episodes.length; i++)
			sum = add(sum, episodes[i]);
		return divide(sum, episodes.length); // divide by runs
	}
	
	public static double[] standardDeviation(int[][] episodes, double[] mean)
	{
		double[] sqrdDif = new double[episodes[0].length];
		for(int i=0; i<episodes.length; i++)
		{
			double[] dif = subtract(episodes[i], mean);
			double[] tempSqrdDif = pow(dif, 2);
			sqrdDif = add(sqrdDif, tempSqrdDif);
		}
		double[] div = divide(sqrdDif, episodes.length);
		return sqrt(div);
	}
	
	/**
	 * Calculate the standard deviation of a set of times.
	 * @param timeList	a list of episode lengths
	 * @param mean		the mean of timeList
	 * @return standard deviation of episode lengths
	 */
	public static double standardDeviation(int[] array, double mean)
	{
		double dev = 0;
		for(int i=0; i<array.length; i++)
		{
			dev += Math.pow(((double)array[i]) - mean,2);
		}
		return Math.sqrt(dev / array.length);
	}
	
	public static double standardDeviation(double[] array, double mean)
	{
		double dev = 0;
		for(int i=0; i<array.length; i++)
		{
			dev += Math.pow(((double)array[i]) - mean,2);
		}
		return Math.sqrt(dev / array.length);
	}
	
	public static double[] sqrt(double[] a)
	{
		double[] b = new double[a.length];
		for(int i=0; i<a.length; i++)
			b[i] = Math.sqrt(a[i]);
		return b;
	}
	
	public static double[] pow(double[] a, double pow)
	{
		double[] b = new double[a.length];
		for(int i=0; i<a.length; i++)
			b[i] = Math.pow(a[i], pow);
		return b;
	}
	
	/**
	 * Return the sum of the values in array 'a' and 'b'.
	 * @param a array
	 * @param b array
	 * @return a+b
	 */
	public static int[] add(int[] a, int[] b)
	{
		int[] c = new int[a.length];
		for(int i=0; i<a.length; i++)
			c[i] = a[i]+b[i];
		return c;
	}
	public static double[] add(double[] a, double[] b)
	{
		double[] c = new double[a.length];
		for(int i=0; i<a.length; i++)
			c[i] = a[i]+b[i];
		return c;
	}
	
	public static double[] subtract(int[] a, double[] b)
	{
		double[] c = new double[a.length];
		for(int i=0; i<a.length; i++)
			c[i] = a[i]-b[i];
		return c;
	}
	
	/**
	 * Return the values in array 'a' divided by 'd'.
	 * @param a	array
	 * @param d division factor
	 * @return a/d
	 */
	public static double[] divide(int[] a, double d)
	{
		double[] b = new double[a.length];
		for(int i=0; i<a.length; i++)
			b[i] = ((double)a[i])/d;
		return b;
	}
	public static double[] divide(double[] a, double d)
	{
		double[] b = new double[a.length];
		for(int i=0; i<a.length; i++)
			b[i] = ((double)a[i])/d;
		return b;
	}
	
	/**
	 * Return values in array 'a' multiplied by values in 'b'
	 * @param a
	 * @param b
	 * @return a*b
	 */
	public static double[] times(double[] a, double[] b)
	{
		double[] c = new double[a.length];
		for(int i=0; i<a.length; i++)
			c[i] = a[i] * b[i];
		return c;
	}
	
	/**
	 * Print an array
	 * @param <E>
	 * @param a array
	 */
	public static <E> void printArray(E[] a)
	{
		List<E> list = Arrays.asList(a);
		System.out.println(list);
	}
	
	public static void printArray(int[] a)
	{
		for(int i=0; i<a.length; i++)
			System.out.printf("%d ", a[i]);
	}
	
	public static void printArray(double[] a)
	{
		for(int i=0; i<a.length; i++)
			System.out.printf("%f ", a[i]);
	}
	
	public static String arrayToString(double[] a)
	{
		String s = "";
		for(int i=0; i<a.length; i++)
			s += String.format("%f ", a[i]);
		s = s.replaceAll(",", ".");
		return s;
	}
	
	/**
	 * Print double array.
	 * @param <E>	
	 * @param a double array.
	 */
	public static <E> void printDoubleArray(E[][] a)
	{
		for(int i=0; i<a.length; i++)
			printArray(a[i]);
	}
	
	public static void printToFile(String fileName, double[][] a)
	{
		System.out.println("Printing to file: " + fileName);
		try {
			PrintWriter out = new PrintWriter(new FileWriter(fileName));
			for(int i=0; i<a.length; i++)
				out.println(arrayToString(a[i]));
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Tadaaaaa!");
	}
}
