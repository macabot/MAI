package ch.idsia.scenarios;

import java.util.Properties;

import UvA.agents.*;
import UvA.states.*;
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

public class Evaluate {
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


	private static String loadPath = null; // path to saved QValues
	private static String savePath = null;
	
	private static boolean load = false;
	private static boolean save = true;
	private static int amountTrain = 100;
	private static int amountTest = 1;
	
	// Set these for plotting
	public static String agentType = "QLearnAgent";
	private static boolean plot = true;
	private static int episodes = 10; // evaluation will be run X times to average results 
	private static int steps = 10; // evaluation will be done every X steps
	
	private static double[] printAverageDistance = new double[amountTrain/steps];
	private static double[] printStdDistance = new double[amountTrain/steps];
	private static double[] printAverageReward = new double[amountTrain/steps];
	private static double[] printStdReward = new double[amountTrain/steps];
	
	public Properties properties = new Properties();
		
	
    public static void main(String[] args) {
    	
    	//read properties
    	String configFilePath = System.getProperty("user.dir") + "/config.properties";
    	Properties properties = MarioState.readPropertiesFile(configFilePath);
    	
    	//set all properties
    	//QLearnAgent.setAllProperties(properties);
    	//PCAQLAgent.setAllProperties(properties);
    	setAllProperties(properties); // for Play
    	
    	// initialization
        EvaluationOptions options = new CmdLineOptions(args);
        Task task = new ProgressTask(options);
        QLearnAgent agent = (QLearnAgent) options.getAgent();
        //SarsaAgent agent = (SarsaAgent) options.getAgent();
        //QLearnAstarAgent agent = (QLearnAstarAgent) options.getAgent();
        //PCAQLAgent agent = (PCAQLAgent) options.getAgent();
        //PsychicAgent agent = (PsychicAgent) options.getAgent();

	   //optionally load qvalues, don't forget to set path\
       if(plot == true)
    	   load = false;
	   if(load)
		   agent.loadQValues(loadPath);       
	        
	   // regular options
	   options.setLevelDifficulty(1);
	   options.setLevelRandSeed(12);      
	   options.setLevelLength(2560);
	 
	   // set options specific for learning
	   options.setVisualization(false);
	   options.setMaxFPS(true);
	   task.setOptions(options);
	   
	   agent.setAllProperties(properties);

	   if(plot == false){
	        // train
	        for (int i = 0; i < amountTrain; i++) { 
		        System.out.print("Training trial " + i + "... ");
		        task.evaluate(agent);
		        //MarioState.resetStatic(2);
		        System.out.print("Done!\n");
	        }
	        
	        //// reset options for visualization
	        options.setVisualization(true);
	        options.setMaxFPS(true);
	        task.setOptions(options);
	        
	        //// and show the next game, learned agent
	        System.out.print("Showing improvement... ");
	        for (int i = 0; i < amountTest; i++) {
	        	task.evaluate(agent);
	        	//MarioState.resetStatic(2);
	        }
	        System.out.println("Done!");
	        
	        ///// write new qvalues to file
	        if(save)
	        	agent.writeQValues(savePath);
	        System.out.println("Done with simulation!");
        }// end if plot == false
        else if(plot == true){
        	QLearnAgent plotAgent = (QLearnAgent) options.getAgent();
        	int amountEval = amountTrain/steps +1;
        	double[][] tmpDistance = new double[episodes][amountEval];
        	double[][] tmpReward = new double[episodes][amountEval];
        	for (int ep=0; ep < episodes; ep++){
        		System.out.println("Episode: " + ep);
	        	for (int i = 0; i <= amountTrain; i++) { 
			        System.out.print("Training trial " + i + "... ");
			        task.evaluate(plotAgent);
			        System.out.print("Done learning!\n");
			        int modI = i%steps;
			        
			        //don't learn and just evaluate the results
			        if(modI == 0){

			        	// set alpha to 0 so there will be no learning
			        	plotAgent.setAlpha(0);

			        	//System.out.println("Saving results # " + i + "... " + i/steps);
			        	task.evaluate(plotAgent);
			        	double[] currentReward = plotAgent.getTotalReward();
			        	double distance = currentReward[0];
			        	double totalReward = currentReward[1];
			        	
			        	tmpDistance[ep][i/steps] = distance;
			        	tmpReward[ep][i/steps] = totalReward;
			        	
			        	System.out.println("distance: " + distance);
			        	System.out.println("reward: " + totalReward);

			        	System.out.print("Done evaluating!\n");
			        }// end if
			        //reset alpha and continue learning
			        plotAgent.setAllProperties(properties);
	        	}// end for learning/evaluating
	        	
        	}// end for
        	
        	// add tmpDistance[ep]
        	double[] averageDistance = Calculate.mean(tmpDistance);
        	double[] stdDistance = Calculate.standardDeviation(tmpDistance, averageDistance);
        	double[] averageReward = Calculate.mean(tmpReward);
        	double[] stdReward = Calculate.standardDeviation(tmpReward, averageReward);
        	//TODO remove printstatements
        	System.out.print("\n avgDistance : ");
        	Calculate.printArray(averageDistance);
        	System.out.print("\n stdDistance : ");
        	Calculate.printArray(stdDistance);
        	System.out.print("\n avgReward : ");
        	Calculate.printArray(averageReward);
        	System.out.print("\n stdReward : ");
        	Calculate.printArray(stdReward);
        	System.out.print("\n");
        	//System.out.println("stdDistance: " + stdDistance);
        	//System.out.println("averageReward: " + averageReward);
        	//System.out.println("stdReward: " + stdReward);
        	printAverageDistance = averageDistance;
        	printStdDistance = stdDistance;
        	printAverageReward = averageReward;
        	printStdReward = stdReward;
        	
            // saving values
            if(save)
            	agent.writeQValues(savePath);
            
            double[][] toPrint = new double[4][printAverageDistance.length];
			toPrint[0] = printAverageDistance;
			toPrint[1] = printStdDistance;
			toPrint[2] = printAverageReward;
			toPrint[3] = printStdReward;
			//TODO make better filename
			String fileName = String.format("%s_Alpha%.1fGamma%.1fEpsilon%.1fTraining%dEpisodes%dSteps%d.txt", agentType,
					QLearnAgent.alpha, QLearnAgent.gamma, QLearnAgent.epsilon, amountTrain, episodes, steps);
			fileName = fileName.replaceAll(",", ".");
			Calculate.printToFile(fileName, toPrint);
        }// end else if (plot==true)

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
