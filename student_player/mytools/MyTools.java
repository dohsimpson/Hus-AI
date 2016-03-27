package student_player.mytools;

import hus.HusBoardState;
import hus.HusPlayer;
import hus.HusMove;

import java.util.ArrayList;

public class MyTools {
    public enum Strategy { MINMAX, ALPHABETA }
    public enum Utility { BOARDVALUE, LEASTOPPONENTMOVES }

    public static double getSomething(){
        return Math.random();
    }

    // utility measure functions
    public static double boardValue(HusBoardState board, int player_id)
    {
        double ret = 0;
        int[][] pits = board.getPits();
        int[] my_pits = pits[player_id];
        for (int i : my_pits) {
            ret += i;
        }
        return ret;
    }

    public static double boardValue2(HusBoardState board, int player_id)
    {
        return 0;
    }

    public static double leastOpponentMoves(HusBoardState board, int player_id)
    {
        double ret = 0;
        int[][] pits = board.getPits();
        int[] opponent_pits = pits[1 - player_id];
        for (int i : opponent_pits) {
            if (i > 1)
                ret++;
        }
        return ret;
    }
}