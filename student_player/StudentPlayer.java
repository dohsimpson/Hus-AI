package student_player;

import hus.HusBoardState;
import hus.HusPlayer;
import hus.HusMove;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static student_player.mytools.MyTools.*;
import student_player.mytools.MyTools.*;
import student_player.mytools.tree.*;

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

    public HusMove currentMove;
    public Node<TreeNode> playTree;

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
        this.STRATEGY = Strategy.ORDEREDALPHABETA;
        this.UTILITY = Utility.BOARDVALUE2;
        this.MINMAX_TREE_DEPTH = 6;
        this.ALPHABETA_TREE_DEPTH = 7;
        this.ORDERED_ALPHABETA_TREE_DEPTH = 6;
        this.FORWARD_ALPHABETA_TREE_DEPTH = 10;
        this.ITER_START_DEPTH = 5;

        this.timeout = false;
        this.TIMEOUT = 2000;
        this.GRACE_TIMEOUT = TIMEOUT - 100;
        this.firstMove = true;
        this.FIRST_MOVE_TIMEOUT = 30000;
        this.GRACE_FIRST_MOVE_TIMEOUT = FIRST_MOVE_TIMEOUT - 500;

        this.currentMove = null;
        this.playTree = null;
    }

    /** This is the primary method that you need to implement.
     * The ``board_state`` object contains the current state of the game,
     * which your agent can use to make decisions. See the class hus.RandomHusPlayer
     * for another example agent. */
    public HusMove chooseMove(HusBoardState board_state)
    {
        Timer timer = new Timer();
        resetTimer(timer);
        resetMove();
        resetPlayTree();

        switch (STRATEGY) {
            case MINMAX:
                searchMove(board_state, MINMAX_TREE_DEPTH, MINMAX_TREE_DEPTH, false, 0, 0, null);
                break;
            case ALPHABETA:
                searchMove(board_state, ALPHABETA_TREE_DEPTH, ALPHABETA_TREE_DEPTH, false, MIN_VALUE, MAX_VALUE, null);
                break;
            case ORDEREDALPHABETA:
                searchMove(board_state, ORDERED_ALPHABETA_TREE_DEPTH, ORDERED_ALPHABETA_TREE_DEPTH, false, MIN_VALUE, MAX_VALUE, this.playTree);
                break;
            case FORWARDORDEREDALPHABETA:
                searchMove(board_state, FORWARD_ALPHABETA_TREE_DEPTH, FORWARD_ALPHABETA_TREE_DEPTH, false, MIN_VALUE, MAX_VALUE, null);
                break;
            case ITER_ORDEREDALPHABETA:
                searchMove(board_state, ITER_START_DEPTH, ITER_START_DEPTH, true, MIN_VALUE, MAX_VALUE, null);
                break;
            default:
                currentMove = null;
                errLog("unimplemented strategy!");
                break;
        }

        // do an analysis on the search tree, this is usually slow
        analyzeTree(board_state, this.playTree);

        cancelTimeout();
        timer.cancel();

        // But since this is a placeholder algorithm, we won't act on that information.
        return currentMove;
    }

    /** a combined search method that can use minmax, alphabeta pruning, heuristic move ordering,
     * forward pruning, iterative deepening, and search tree memorization. **/
    public int searchMove(HusBoardState board, int original_depth, int depth, boolean isIterRoot, int alpha, int beta, Node<TreeNode> tree)
    {
        /* initialization */
        ArrayList<HusMove> moves     = board.getLegalMoves();
        boolean            isMax     = (original_depth - depth) % 2 == 0;
        boolean            isRoot    = original_depth == depth;
        HusMove            bestMove  = null;
        int                bestValue = 0;

        // return heuristic value
        if (this.timeout || depth <= 1 || moves.isEmpty()) {
            return utilityOfBoard(board);
        }

        // iterative deepening
        // inspiration: http://logic.stanford.edu/ggp/chapters/chapter_07.html
        if (isIterRoot) {
            int maxDepth = 200;
            int iterDepth = -1;

            // deepening
            while (!this.timeout && iterDepth < maxDepth) {
                iterDepth++;
                int v = searchMove(board, original_depth + iterDepth, original_depth + iterDepth, !isIterRoot, MIN_VALUE, MAX_VALUE, tree);
                if (!this.timeout) {
                    bestMove = currentMove;
                    bestValue = v;
                    // NOTE: these values are used at the end of the function for updating and returning
                }
            }
            debugLog("iter depth: " + iterDepth);
        }

        else {
            // min-max
            if (isMax)
                bestValue = MIN_VALUE;
            else
                bestValue = MAX_VALUE;

            // order moves
            if (STRATEGY == Strategy.ORDEREDALPHABETA || STRATEGY == Strategy.ITER_ORDEREDALPHABETA || STRATEGY == Strategy.FORWARDORDEREDALPHABETA)
                // TODO: optimize by returning the boards
                sortMovesByBoards(moves, board, player_id, UTILITY);

            /* build tree */
            for (HusMove m : moves) {
                int v;
                HusBoardState nextBoard = (HusBoardState) board.clone();
                nextBoard.move(m);
                Node<TreeNode> nextTree = null;

                // tree memorization
                if (tree != null) {
                    // save move
                    nextTree = tree.addChild(new Node<TreeNode>(new TreeNode(m)));
                }

                // forward pruning: check for quiescence
                if (STRATEGY == Strategy.FORWARDORDEREDALPHABETA && !isMax && utilityOfBoard(nextBoard) == utilityOfBoard(board)) {
                    v = utilityOfBoard(nextBoard);
                    // debugLog("forward pruning: quiescence state with value: " + v);
                }

                // recursion
                else {
                    v = searchMove(nextBoard, original_depth, depth - 1, isIterRoot, alpha, beta, nextTree);
                }

                // tree memorization
                if (tree != null) {
                    // save value
                    nextTree.value.setValue(v);
                }

                // update value and move
                if (isMax) {
                    if (v > bestValue) {
                        bestValue = v;
                        bestMove = m;
                    }
                }
                else {
                    if (v < bestValue) {
                        bestValue = v;
                        bestMove = m;
                    }
                }

                // alpha-beta pruning
                if (STRATEGY == Strategy.ORDEREDALPHABETA || STRATEGY == Strategy.ALPHABETA || STRATEGY == Strategy.FORWARDORDEREDALPHABETA || STRATEGY == Strategy.ITER_ORDEREDALPHABETA) {
                    if (isMax) {
                        if (bestValue >= beta)
                            break;
                        else
                            alpha = Integer.max(bestValue, alpha);
                    }
                    else {
                        if (bestValue <= alpha)
                            break;
                        else
                            beta = Integer.min(bestValue, beta);
                    }
                }
            }
        }

        /* return */
        // if at root level
        if (isRoot) {
            // update move
            if (bestMove == null)
                bestMove = moves.get(0);
            this.currentMove = bestMove;
            // log
            debugLogMove(bestMove, bestValue);
        }

        // return value
        return bestValue;
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

    private void resetTimer(Timer timer)
    {
        if (this.firstMove) {
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
    }

    private void cancelTimeout()
    {
        this.timeout = false;
    }

    private void resetMove()
    {
        this.currentMove = null;
    }

    public void resetPlayTree()
    {
        this.playTree = new Node<TreeNode>();
        this.playTree.setValue(new TreeNode());
    }

    protected void debugLog(String s)
    {
        if (DEBUG) {
            System.err.println("[" + player_id + "|" + STRATEGY.toString() + "|" + UTILITY.toString() + "] " + s);
        }
    }

    public void debugLogMove(HusMove move, int value)
    {
        if (DEBUG) {
            String s = String.format("<%d/%s/%s/%d/%d>", player_id, STRATEGY.toString(), UTILITY.toString(), move.getPit(), value);
            System.err.println(s);
        }
    }

    protected void errLog(String s)
    {
        System.err.println("[ERROR] " + s);
    }

    /** doing some analysis on a given tree **/
    public void analyzeTree(HusBoardState board, Node<TreeNode> tree)
    {
        if (tree != null) {
            double pruneFactor = (double) tree.getSize() / getNumOfBoardNodes(board, tree.getDepth());
            debugLog("prune factor(less=better): " + tree.getSize() + "/" + getNumOfBoardNodes(board, tree.getDepth()) + " = " + String.format("%.5f", pruneFactor));
        }
    }
}
