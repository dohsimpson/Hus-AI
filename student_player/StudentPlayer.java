package student_player;

import hus.HusBoardState;
import hus.HusPlayer;
import hus.HusMove;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static student_player.mytools.MyTools.*;
import student_player.mytools.MyTools.*;

/** A Hus player submitted by a student. */
public class StudentPlayer extends HusPlayer {

    protected Strategy STRATEGY;
    protected Utility UTILITY;
    protected boolean DEBUG;

    public int MINMAX_TREE_DEPTH;
    public int ALPHABETA_TREE_DEPTH;
    public int ORDERED_ALPHABETA_TREE_DEPTH;
    public int FORWARD_ALPHABETA_TREE_DEPTH;
    public int ITER_START_DEPTH;

    private boolean timeout;
    public int TIMEOUT;
    public int GRACE_TIMEOUT;
    private boolean firstMove;
    public int FIRST_MOVE_TIMEOUT;
    public int GRACE_FIRST_MOVE_TIMEOUT;

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
        this.STRATEGY = Strategy.ITER_ORDEREDALPHABETA;
        this.UTILITY = Utility.BOARDVALUE2;
        this.MINMAX_TREE_DEPTH = 6;
        this.ALPHABETA_TREE_DEPTH = 7;
        this.ORDERED_ALPHABETA_TREE_DEPTH = 8;
        this.FORWARD_ALPHABETA_TREE_DEPTH = 200;
        this.ITER_START_DEPTH = 6;

        this.timeout = false;
        this.TIMEOUT = 2000;
        this.GRACE_TIMEOUT = TIMEOUT - 100;
        this.firstMove = true;
        this.FIRST_MOVE_TIMEOUT = 30000;
        this.GRACE_FIRST_MOVE_TIMEOUT = FIRST_MOVE_TIMEOUT - 500;
    }

    /** This is the primary method that you need to implement.
     * The ``board_state`` object contains the current state of the game,
     * which your agent can use to make decisions. See the class hus.RandomHusPlayer
     * for another example agent. */
    public HusMove chooseMove(HusBoardState board_state)
    {
        Timer timer = new Timer();
        if (firstMove) {
            timer.schedule(new TimerTask() {
                public void run() { timeOut(); }
            }, GRACE_FIRST_MOVE_TIMEOUT);
            this.firstMove = false;
        }
        else {
            timer.schedule(new TimerTask() {
                public void run() { timeOut(); }
            }, GRACE_TIMEOUT);
        }

        HusMove move;
        switch (STRATEGY) {
            case MINMAX:
                move = minmaxMove(board_state);
                break;
            case ALPHABETA:
                move = alphaBetaMove(board_state);
                break;
            case ORDEREDALPHABETA:
                move = orderedAlphaBetaMove(board_state);
                break;
            case FORWARDORDEREDALPHABETA:
                move = orderedAlphaBetaMove(board_state);
                break;
            case ITER_ORDEREDALPHABETA:
                move = iterOrderedAlphaBetaMove(board_state);
                break;
            default:
                move = null;
                errLog("unimplemented strategy!");
                break;
        }

        cancelTimeout();
        timer.cancel();

        // But since this is a placeholder algorithm, we won't act on that information.
        return move;
    }

    // min-max
    public HusMove minmaxMove(HusBoardState board)
    {
        ArrayList<HusMove> moves = board.getLegalMoves();
        HusMove maxMove = moves.get(0);
        int maxValue = MIN_VALUE;
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
        if (maxValue != MIN_VALUE)
            debugLog("minmax value: " + maxValue);

        return maxMove;
    }
    public int minmaxValue(boolean isMax, HusBoardState board, int depth)
    {
        ArrayList<HusMove> moves = board.getLegalMoves();

        if (depth <= 1 || moves.isEmpty())
            return utilityOfBoard(board);

        int maxValue = MIN_VALUE;
        int minValue = MAX_VALUE;
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
        int maxValue = MIN_VALUE;
        for (HusMove m : moves) {
            HusBoardState nextBoard = (HusBoardState) board.clone();
            nextBoard.move(m);
            int v = alphaBetaValue(false, nextBoard, ALPHABETA_TREE_DEPTH - 1, MIN_VALUE, MAX_VALUE);
            if (v > maxValue) {
                maxValue = v;
                maxMove = m;
            }
        }
        debugLog("alpha beta move: " + maxMove.getPit());
        if (maxValue != MIN_VALUE)
            debugLog("alpha beta value: " + maxValue);

        return maxMove;
    }
    public int alphaBetaValue(boolean isMax, HusBoardState board, int depth, int alpha, int beta)
    {
        ArrayList<HusMove> moves = board.getLegalMoves();

        if (depth <= 1 || moves.isEmpty())
            return utilityOfBoard(board);

        int maxValue = MIN_VALUE;
        int minValue = MAX_VALUE;
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

    // ordered alpha-beta pruning + forward pruning
    public HusMove orderedAlphaBetaMove(HusBoardState board)
    {
        return orderedAlphaBetaMove(board, ORDERED_ALPHABETA_TREE_DEPTH);
    }

    public HusMove orderedAlphaBetaMove(HusBoardState board, int depth)
    {
        ArrayList<HusMove> moves = board.getLegalMoves();
        sortMovesByBoards(moves, board, player_id, UTILITY);
        HusMove maxMove = moves.get(0);
        int maxValue = MIN_VALUE;
        for (HusMove m : moves) {
            HusBoardState nextBoard = (HusBoardState) board.clone();
            nextBoard.move(m);
            int v = orderedAlphaBetaValue(false, nextBoard, depth - 1, MIN_VALUE, MAX_VALUE);
            if (v > maxValue) {
                maxValue = v;
                maxMove = m;
            }
        }
        debugLog("orderd alpha beta move: " + maxMove.getPit());
        if (maxValue != MIN_VALUE)
            debugLog("orderd alpha beta value: " + maxValue);

        return maxMove;
    }
    public int orderedAlphaBetaValue(boolean isMax, HusBoardState board, int depth, int alpha, int beta)
    {
        ArrayList<HusMove> moves = board.getLegalMoves();

        if (this.timeout || depth <= 1 || moves.isEmpty())
            return utilityOfBoard(board);

        int maxValue = MIN_VALUE;
        int minValue = MAX_VALUE;
        ArrayList<HusBoardState> nextBoards = makeNextBoards(board, moves);
        sortBoards(nextBoards, player_id, UTILITY);

        for (HusBoardState nextBoard : nextBoards) {
            // forward pruning
            // test for quiescence
            int v;

            if (this.STRATEGY == Strategy.FORWARDORDEREDALPHABETA && !isMax && utilityOfBoard(nextBoard) == utilityOfBoard(board)) {
                v = utilityOfBoard(nextBoard);
                // debugLog("forward pruning: quiescence state with value: " + v);
            }
            else
                v = orderedAlphaBetaValue(!isMax, nextBoard, depth - 1, alpha, beta);

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

    // iterative deepening ordered alpha beta
    // inspiration: http://logic.stanford.edu/ggp/chapters/chapter_07.html
    public HusMove iterOrderedAlphaBetaMove(HusBoardState board)
    {
        int depth = ITER_START_DEPTH - 1;
        HusMove ret = board.getLegalMoves().get(0);

        while (!this.timeout && depth < 200) {
            depth++;
            HusMove m = orderedAlphaBetaMove(board, depth);
            if (!this.timeout)
                ret = m;
        }
        debugLog("iter depth: " + depth);
        return ret;
    }

    public int utilityOfBoard(HusBoardState board)
    {
        int ret;
        switch (UTILITY) {
            case BOARDVALUE:
                ret = boardValue(board, player_id);
                break;
            case BOARDVALUE2:
                ret = boardValue2(board, player_id);
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
    private void timeOut()
    {
        debugLog("timeout!");
        this.timeout = true;
    }

    private void cancelTimeout()
    {
        this.timeout = false;
    }

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
