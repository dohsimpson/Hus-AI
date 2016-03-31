package student_player;

import static student_player.mytools.MyTools.*;
import student_player.mytools.MyTools.*;

public class abPlayer extends StudentPlayer {

    public abPlayer()
    {
        super("ab_player");
        this.STRATEGY = Strategy.ORDEREDALPHABETA;
        this.UTILITY = Utility.BOARDVALUE2;
        this.ALPHABETA_TREE_DEPTH = 6;
    }
}
