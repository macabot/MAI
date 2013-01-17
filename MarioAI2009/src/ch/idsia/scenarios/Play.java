package ch.idsia.scenarios;

import java.util.Random;

import UvA.agents.QLearnAgent;
import UvA.agents.SarsaAgent;
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

public class Play {
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


	private static String savePath = System.getProperty("user.dir") + "/showQlearn.ser";
	private static String loadPath = System.getProperty("user.dir") + "/showQlearn.ser";
	private static int amountRuns = 100;
	private static boolean saveB = true;
	private static boolean loadB = false;
	private static boolean trainB = false;
	private static boolean singleLev = true;
	private static int level = 0;
	
    public static void main(String[] args) {

    	// initialization
    	Random rand = new Random();
        EvaluationOptions options = new CmdLineOptions(args);
        Task task = new ProgressTask(options);
        QLearnAgent agent = (QLearnAgent) options.getAgent();
        
        // load values if true
        if(loadB)
        	agent.loadQValues(loadPath);       
        
        // regular options
        options.setLevelDifficulty(0);
        options.setLevelRandSeed(level);        
        
        ///// set options specific for learning if trainB is true
        if(trainB) {
        	System.out.printf("Starting training of %d runs\n", amountRuns);
//	        options.setVisualization(false);
	        options.setMaxFPS(true);
	        
	        //////// optionally load qvalues, dont forget to set path
	 
	        /// set options and

	        task.setOptions(options);
	        //// train
	        for (int i = 0; i < amountRuns; i++) { 
		        System.out.print("Training trial " + i + "... ");
		        if(!singleLev)
		        	options.setLevelRandSeed(rand.nextInt());
		        task.evaluate(agent);
		        System.out.print("Done!\n");
	        }
	        System.out.println("Done training");
        } // end if load
        
        //// reset options for visualization
        options.setVisualization(true);
        options.setMaxFPS(false);
        task.setOptions(options);
        
        agent.setEpsilon(0);
        
        //// and show the next game, learned agent
        System.out.println("Showing mario... ");
        for (int i = 0; i < 2; i++) {
        	System.out.print("run " + Integer.toString(i) + "... ");
        	if(!singleLev)
        		options.setLevelRandSeed(rand.nextInt());
        	task.evaluate(options.getAgent());
        	System.out.print("done! \n");
        }
        System.out.println("Done showing mario!");
        
        
        ///// write new qvalues to file if saveB is true
        if(saveB) 
        	agent.writeQValues(savePath);
        
        System.out.println("Done with simulation!");
    }
}
