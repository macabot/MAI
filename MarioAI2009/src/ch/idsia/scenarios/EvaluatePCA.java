package ch.idsia.scenarios;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import UvA.agents.Calculate;
import UvA.agents.PCAQLAgent;
import UvA.agents.SLAPI;
import UvA.stateSpaceReduction.PCAMeans;
import UvA.states.PCAState;
import UvA.states.StateActionPair;
import ch.idsia.ai.tasks.ProgressTask;
import ch.idsia.ai.tasks.Task;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationOptions;

/**
 * Created by IntelliJ IDEA.
 * User: julian
 * Date: May 5, 2009
 * Time: 12:46:43 PM
 */

/**
 * The <code>Play</code> class shows how simple is to run an iMario benchmark.
 * It shows how to set up some parameters, create a task,
 * use the CmdLineParameters class to set up options from command line if any.
 * Defaults are used otherwise.
 *
 * @author  Julian Togelius, Sergey Karakovskiy
 * @version 1.0, May 5, 2009
 * @since   JDK1.0
 */

public class EvaluatePCA {
	/**
	 * <p>An entry point of the class.
	 *
	 * @param args input parameters for customization of the benchmark.
	 *
	 * @see ch.idsia.scenarios.MainRun
	 * @see ch.idsia.tools.CmdLineOptions
	 * @see ch.idsia.tools.EvaluationOptions
	 *
	 * @since   iMario1.0
	 */

	private static String loadPath = null;
	private static String savePath = null;
	private static boolean save = false;

	private static int amountTrain = 1000;

	// Set these for plotting
	public static String agentType = "PCAQLAgent"; // set name if you change the agent
	private static int episodes = 10; // evaluation will be run X times to average results
	private static int steps = 50; // evaluation will be done every X steps TODO

	private static double[] printAverageDistance = new double[amountTrain/steps];
	private static double[] printStdDistance = new double[amountTrain/steps];
	private static double[] printAverageReward = new double[amountTrain/steps];
	private static double[] printStdReward = new double[amountTrain/steps];

	public Properties properties = new Properties();


	@SuppressWarnings("unchecked") // hack to remove annoying warning of casting
	public static void main(String[] args) {

		//read properties
		String configFilePath = System.getProperty("user.dir") + "/config.properties";
		Properties properties = PCAState.readPropertiesFile(configFilePath);

		//set all properties
		//QLearnAgent.setAllProperties(properties);
		//PCAQLAgent.setAllProperties(properties);
		setAllProperties(properties); // for Evaluation

		// initialization
		EvaluationOptions options = new CmdLineOptions(args);
		Task task = new ProgressTask(options);
		PCAQLAgent agent = (PCAQLAgent) options.getAgent();

		// regular options
		options.setLevelDifficulty(1);
		options.setLevelRandSeed(15);      
		//options.setLevelLength(2560);

		// set options specific for learning
		options.setVisualization(false);
		options.setMaxFPS(true);
		task.setOptions(options);

		agent.setAllProperties(properties);

		int amountEval = amountTrain/steps +1;
		double[][] tmpDistance = new double[episodes][amountEval];
		double[][] tmpReward = new double[episodes][amountEval];

		// TODO: create states
		// TODO: initiate alpha, initial value and viewdim in config file 
		// TODO: SET NRCOMPONENTS IN CONFIG FILE
		// TODO: set correct loadPath in config.properties

		Map<StateActionPair, Double> qValuesTemp = null;
		try {
			qValuesTemp = (HashMap<StateActionPair, Double>) SLAPI.load(loadPath);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);		// stop program
		}

		double[][] states = PCAQLAgent.extractRepresentations(qValuesTemp);

		int[] nrClusters = {-1, states.length/10, states.length/100}; 
		// nrCompononents:
		// ? Sammie
		// ? Anna
		// ? Richard
		// ? SP
		for (int nrCluster = 0; nrCluster< nrClusters.length; nrCluster++) {

			agent.setClusterAmount(nrClusters[nrCluster]);
			
			// recalculate clusters
			System.out.print("Clustering....");
			PCAMeans pcam = new PCAMeans(states, agent.getNumComponents(), agent.getClusterAmount(), agent.getIterations());
			System.out.println("Done!");
			
			agent.setPCAM(pcam);

			for (int ep=0; ep < episodes; ep++){
				long start = System.currentTimeMillis();
				
				System.out.print("Episode: " + ep + 
						" amount of clusters " + nrClusters[nrCluster] + 
						"... ");
				
				
				// reset qValues
				agent.resetQValues();

				
				try{ 
					for (int i = 0; i <= amountTrain; i++) { 
						// set values

						
						task.evaluate(agent);

						int modI = i%steps;

						//don't learn and just evaluate the results
						if(modI == 0){

							// set alpha to 0 so there will be no learning
							agent.setAlpha(0);
							agent.setEpsilon(0); 

							//System.out.println("Saving results # " + i + "... " + i/steps);
							task.evaluate(agent);
							double[] currentReward = agent.getTotalReward();
							double distance = currentReward[0];
							double totalReward = currentReward[1];

							tmpDistance[ep][i/steps] = distance;
							tmpReward[ep][i/steps] = totalReward;

							//System.out.println("distance: " + distance);
							//System.out.println("reward: " + totalReward);

							//System.out.print("Done evaluating!\n");
						}// end if
						//reset alpha and continue learning
						agent.setAlpha(Double.parseDouble(properties.getProperty("alpha", "0.3")));
						agent.setEpsilon(Double.parseDouble(properties.getProperty("epsilon", "0.1")));
					}// end for learning/evaluating

					//end try
				} catch( Exception e )
				{
					e.printStackTrace();
				}// end catch
				
				System.out.printf("This episode took %d miliseconds \n", System.currentTimeMillis() - start);
				
			}// end for episodes
			
			// add tmpDistance[ep]
			double[] averageDistance = Calculate.mean(tmpDistance);
			double[] stdDistance = Calculate.standardDeviation(tmpDistance, averageDistance);
			double[] averageReward = Calculate.mean(tmpReward);
			double[] stdReward = Calculate.standardDeviation(tmpReward, averageReward);

			printAverageDistance = averageDistance;
			printStdDistance = stdDistance;
			printAverageReward = averageReward;
			printStdReward = stdReward;

			// saving values
			if(save)
				agent.writeQValues(savePath);

			// printing/saving stuff
			double[][] toPrint = new double[4][printAverageDistance.length];
			toPrint[0] = printAverageDistance;
			toPrint[1] = printStdDistance;
			toPrint[2] = printAverageReward;
			toPrint[3] = printStdReward;
			String fileName = String.format("%s_It%dNC%dCA%dTraining%dEps%dSteps%d.txt", agentType, agent.getIterations(), 
					agent.getNumComponents(), agent.getClusterAmount(), amountTrain, episodes, steps);
			fileName = fileName.replaceAll(",", ".");
			Calculate.printToFile(fileName, toPrint);

		} // end loop nrClusters
	}// end main

	/**
	 * Set all the properties via a config.properties file
	 * @param properties
	 */
	public static void setAllProperties(Properties properties){
		loadPath = System.getProperty("user.dir") + "/" + properties.getProperty("loadPath");
		savePath = System.getProperty("user.dir") + "/" + properties.getProperty("savePath");
	}//end function setAllProperties

}// end class Play
