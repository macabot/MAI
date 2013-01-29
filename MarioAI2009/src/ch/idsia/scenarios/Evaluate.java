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


	private static String savePath = null;
	private static boolean save = false;
	
	private static int amountTrain = 1000; // TODO: RESET BACK TO 1000
	
	// Set these for plotting
	public static String agentType = "QLearnAgent"; // set name if you change the agent
	private static int episodes = 50; // evaluation will be run X times to average results
	private static int steps = 50; // evaluation will be done every X steps TODO: RESET
	
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
    	setAllProperties(properties); // for Evaluation
    	
    	// initialization
        EvaluationOptions options = new CmdLineOptions(args);
        Task task = new ProgressTask(options);
        QLearnAgent agent = (QLearnAgent) options.getAgent();

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
        
        // TODO: SET CONFIGFILE CORRECTLY (view dimensions)
        double[] alphas = {0.1, 0.3};
        int[] initialValue = {20};
        //viewdim:
        // 4 Sammie
        // 6 Michael
        // 8 Anna
        
        for (int iV=0; iV < initialValue.length; iV++){
	        for (int a=0; a < alphas.length; a++){
		        for (int ep=0; ep < episodes; ep++){
		        	
		        	// reset agent qValues to prevent learning to be transferred to next variable testing
		        	agent.resetQValues();
		        	
		        	try{ 
		        		for (int i = 0; i <= amountTrain; i++) { 
			        		// set alpha and initial value
			        		agent.setAlpha(alphas[a]);
			        		agent.setInitialValue(initialValue[iV]);
			        		
			        		System.out.print("Episode: " + ep + " Alpha: " + alphas[a] + 
			        				" Initial Value: " + initialValue[iV] + 
			        				" Training trial " + i + "... ");
			        		task.evaluate(agent);
			        		System.out.print("Done learning!\n");
			        		int modI = i%steps;
			
			        		//don't learn and just evaluate the results
			        		if(modI == 0){
			
			        			// set alpha to 0 so there will be no learning
			        			agent.setAlpha(0);
			
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
			        		agent.setAlpha(alphas[a]);
			        	}// end for learning/evaluating
			        	
		        	//end try
		        	} catch( Exception e )
		        	{
		        		e.getStackTrace();
		        	}// end catch
		
		        }// end for
		
		        // add tmpDistance[ep]
		        double[] averageDistance = Calculate.mean(tmpDistance);
		        double[] stdDistance = Calculate.standardDeviation(tmpDistance, averageDistance);
		        double[] averageReward = Calculate.mean(tmpReward);
		        double[] stdReward = Calculate.standardDeviation(tmpReward, averageReward);
		        
		        //TODO remove print statements
		        //System.out.print("\n avgDistance : ");
		        //Calculate.printArray(averageDistance);
		        //System.out.print("\n stdDistance : ");
		        //Calculate.printArray(stdDistance);
		        //System.out.print("\n avgReward : ");
		        //Calculate.printArray(averageReward);
		        //System.out.print("\n stdReward : ");
		        //Calculate.printArray(stdReward);
		        //System.out.print("\n");
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
		        String fileName = String.format("%s_A%dVDim%.1fG%.1fE%.1fIV%dTraining%dEps%dSteps%d.txt", agentType, QLearnAgent.alpha, 
		        		agent.getViewDim(), QLearnAgent.gamma, QLearnAgent.epsilon, QLearnAgent.initialValue, amountTrain, episodes, steps);
		        fileName = fileName.replaceAll(",", ".");
		        Calculate.printToFile(fileName, toPrint);
	        }// end for loop alpha
	    }// end for loop initial value
    }// end main
    
	/**
	 * Set all the properties via a config.properties file
	 * @param properties
	 */
    public static void setAllProperties(Properties properties){
    	//loadPath = System.getProperty("user.dir") + "/" + properties.getProperty("loadPath");
    	savePath = System.getProperty("user.dir") + "/" + properties.getProperty("savePath");
    }//end function setAllProperties
	
}// end class Play
