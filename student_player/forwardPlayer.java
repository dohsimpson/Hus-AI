package student_player;

import static student_player.mytools.MyTools.*;
import student_player.mytools.MyTools.*;

public class forwardPlayer extends StudentPlayer {

    public forwardPlayer()
    {
        super("forward_player");
        this.STRATEGY = Strategy.FORWARDORDEREDALPHABETA;
        this.UTILITY = Utility.BOARDVALUE2;
        this.FORWARD_ALPHABETA_TREE_DEPTH = 200;
    }
}
