package ch.idsia.scenarios;

import UvA.agents.QLearnAgent;
import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.AgentsPool;
import ch.idsia.ai.agents.human.HumanKeyboardAgent;
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

	static String loadPath = null; // path to saved QValues
	static String savePath = null;
	
    public static void main(String[] args) {
    	
    	///// initialization
        EvaluationOptions options = new CmdLineOptions(args);
        Task task = new ProgressTask(options);

        // regular options
        options.setLevelDifficulty(1);
        
        ///// set options specific for learning
//        options.setLevelRandSeed((int) (Math.random () * Integer.MAX_VALUE));
//        options.setVisualization(false);
//        options.setMaxFPS(true);
//
//        //////// optionally load qvalues, dont forget to set path
//        QLearnAgent agent = (QLearnAgent) options.getAgent();
//        agent.loadQValues(loadPath);
//        
//        /// set options and
//        task.setOptions(options);
//
//        //// train
//        for (int i = 0; i < 100; i++) { 
//        System.out.print("Training trial " + i + "... ");
//        task.evaluate(options.getAgent());
//        System.out.print("Done!\n");
//        }
        
        //// reset options for visualization
        options.setVisualization(true);
        task.setOptions(options);
        
        //// and show the next game, learned agent
        System.out.print("Showing improvement... ");
        task.evaluate(options.getAgent());
        System.out.println("Done!");
        
        ///// write new qvalues to file
//        agent.writeQValues(savePath);
    }
}
