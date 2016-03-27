package student_player;

import static student_player.mytools.MyTools.*;
import student_player.mytools.MyTools.*;

public class mmPlayer extends StudentPlayer {

    public mmPlayer()
    {
        super("mm_player");
        this.STRATEGY = Strategy.MINMAX;
        this.UTILITY = Utility.BOARDVALUE;
        this.MINMAX_TREE_DEPTH = 6;
    }
}
