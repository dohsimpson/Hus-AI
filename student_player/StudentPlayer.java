package student_player;

import hus.HusBoardState;
import hus.HusPlayer;
import hus.HusMove;

import java.util.ArrayList;

import static student_player.mytools.MyTools.*;
import student_player.mytools.MyTools.*;

/** A Hus player submitted by a student. */
public class StudentPlayer extends HusPlayer {

    protected Strategy STRATEGY;
    protected Utility UTILITY;
    protected boolean DEBUG;

    public int MINMAX_TREE_DEPTH;
    public int ALPHABETA_TREE_DEPTH;

    /** You must modify this constructor to return your student number.
     * This is important, because this is what the code that runs the
     * competition uses to associate you with your agent.
     * The constructor should do nothing else. */
    public StudentPlayer() {
        this("260516739");
    }

    public StudentPlayer(String s) {
        super(s);
        this.DEBUG = true;
        this.STRATEGY = Strategy.ALPHABETA;
        this.UTILITY = Utility.LEASTOPPONENTMOVES;
        this.MINMAX_TREE_DEPTH = 6;
        this.ALPHABETA_TREE_DEPTH = 7;
    }

    /** This is the primary method that you need to implement.
     * The ``board_state`` object contains the current state of the game,
     * which your agent can use to make decisions. See the class hus.RandomHusPlayer
     * for another example agent. */
    public HusMove chooseMove(HusBoardState board_state)
    {
        // Get the contents of the pits so we can use it to make decisions.
        int[][] pits = board_state.getPits();

        // Use ``player_id`` and ``opponent_id`` to get my pits and opponent pits.
        int[] my_pits = pits[player_id];
        int[] op_pits = pits[opponent_id];

        // Use code stored in ``mytools`` package.
        getSomething();

        HusMove move;
        switch (STRATEGY) {
            case MINMAX:
                move = minmaxMove(board_state);
                break;
            case ALPHABETA:
                move = alphaBetaMove(board_state);
                break;
            default:
                move = null;
                errLog("unimplemented strategy!");
                break;
        }

        // But since this is a placeholder algorithm, we won't act on that information.
        return move;
    }

    // min-max
    public HusMove minmaxMove(HusBoardState board)
    {
        ArrayList<HusMove> moves = board.getLegalMoves();
        HusMove maxMove = moves.get(0);
        int maxValue = Integer.MIN_VALUE;
        for (HusMove m : moves) {
            HusBoardState nextBoard = (HusBoardState) board.clone();
            nextBoard.move(m);
            int v = minmaxValue(false, nextBoard, MINMAX_TREE_DEPTH - 1);
            if (v > maxValue) {
                maxValue = v;
                maxMove = m;
            }
        }
        debugLog("minmax move: " + maxMove.getPit());
        if (maxValue != Integer.MIN_VALUE)
            debugLog("minmax value: " + maxValue);

        return maxMove;
    }
    public int minmaxValue(boolean isMax, HusBoardState board, int depth)
    {
        ArrayList<HusMove> moves = board.getLegalMoves();

        if (depth <= 1 || moves.isEmpty())
            return utilityOfBoard(board);

        int maxValue = Integer.MIN_VALUE;
        int minValue = Integer.MAX_VALUE;
        for (HusMove m : moves) {
            HusBoardState nextBoard = (HusBoardState) board.clone();
            nextBoard.move(m);
            int v = minmaxValue(!isMax, nextBoard, depth - 1);
            if (isMax)
                maxValue = Integer.max(maxValue, v);
            else
                minValue = Integer.min(minValue, v);
        }
        if (isMax)
            return maxValue;
        else
            return minValue;
    }

    // alpha-beta pruning
    public HusMove alphaBetaMove(HusBoardState board)
    {
        ArrayList<HusMove> moves = board.getLegalMoves();
        HusMove maxMove = moves.get(0);
        int maxValue = Integer.MIN_VALUE;
        for (HusMove m : moves) {
            HusBoardState nextBoard = (HusBoardState) board.clone();
            nextBoard.move(m);
            int v = alphaBetaValue(false, nextBoard, ALPHABETA_TREE_DEPTH - 1, Integer.MIN_VALUE, Integer.MAX_VALUE);
            if (v > maxValue) {
                maxValue = v;
                maxMove = m;
            }
        }
        debugLog("alpha beta move: " + maxMove.getPit());
        if (maxValue != Integer.MIN_VALUE)
            debugLog("alpha beta value: " + maxValue);

        return maxMove;
    }
    public int alphaBetaValue(boolean isMax, HusBoardState board, int depth, int alpha, int beta)
    {
        ArrayList<HusMove> moves = board.getLegalMoves();

        if (depth <= 1 || moves.isEmpty())
            return utilityOfBoard(board);

        int maxValue = Integer.MIN_VALUE;
        int minValue = Integer.MAX_VALUE;
        for (HusMove m : moves) {
            HusBoardState nextBoard = (HusBoardState) board.clone();
            nextBoard.move(m);
            int v = alphaBetaValue(!isMax, nextBoard, depth - 1, alpha, beta);
            if (isMax) {
                maxValue = Integer.max(maxValue, v);
                if (maxValue >= beta)
                    break;
                else
                    alpha = Integer.max(maxValue, alpha);
            }
            else {
                minValue = Integer.min(minValue, v);
                if (minValue <= alpha)
                    break;
                else
                    beta = Integer.min(minValue, beta);
            }
        }
        if (isMax)
            return maxValue;
        else
            return minValue;
    }

    public int utilityOfBoard(HusBoardState board)
    {
        int ret;
        switch (UTILITY) {
            case BOARDVALUE:
                ret = boardValue(board, player_id);
                break;
            case LEASTOPPONENTMOVES:
                ret = leastOpponentMoves(board, player_id);
                break;
            default:
                ret = 0;
                errLog("unimplemented strategy!");
                break;
        }
        return ret;
    }

    // -----

    // others
    protected void debugLog(String s)
    {
        if (DEBUG) {
            System.err.println("[" + player_id + "|" + STRATEGY.toString() + "|" + UTILITY.toString() + "] " + s);
        }
    }

    protected void errLog(String s)
    {
        System.err.println("[ERROR] " + s);
    }
    // ---
}
