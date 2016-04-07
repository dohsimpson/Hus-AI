package student_player;

import static student_player.mytools.MyTools.*;
import student_player.mytools.MyTools.*;

public class StudentPlayer2 extends StudentPlayer {

    public StudentPlayer2()
    {
        super("player2");
        // this.STRATEGY = MINMAX | ALPHABETA_PRUNING | ORDER_MOVES | ITER_DEEPENING | USE_TIMEOUT | BOARD_VALUE;
        this.STRATEGY = MINMAX | ALPHABETA_PRUNING | ORDER_MOVES | BOARD_VALUE;

        this.DEFAULT_TREE_DEPTH = 6;
    }
}
