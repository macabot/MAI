package UvA.agents;

import java.util.ArrayList;
import java.util.List;

import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.ai.BasicAIAgent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy
 * Date: Apr 25, 2009
 * Time: 12:27:07 AM
 * Package: ch.idsia.ai.agents.ai;
 * 
 * Edited so that is saves all its states
 */

public class SaveStatesAgent extends BasicAIAgent implements Agent 
{
	List<double[]> representations;

    public SaveStatesAgent()
    {
        super("ForwardJumpingAgent");
        reset();
        this.representations = new ArrayList<double[]>();
    }

    public void reset()
    {
        action = new boolean[Environment.numberOfButtons];
        action[Mario.KEY_RIGHT] = true;
        action[Mario.KEY_SPEED] = true;
    }

    public boolean[] getAction(Environment observation)
    {
    	representations.add(new MarioState(observation).getRepresentation());
        action[Mario.KEY_SPEED] = action[Mario.KEY_JUMP] =  observation.mayMarioJump() || !observation.isMarioOnGround();
        return action;
    }
}
