package student_player;

import hus.HusBoardState;
import hus.HusPlayer;
import hus.HusMove;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Arrays;

import static student_player.mytools.MyTools.*;
import student_player.mytools.MyTools.*;
import student_player.mytools.tree.*;

/** A Hus player submitted by a student. */
public class StudentPlayer extends HusPlayer {

    public int STRATEGY;
    public String STRATEGY_STRING;
    public boolean DEBUG;

    public int DEFAULT_TREE_DEPTH;
    public int MAX_TREE_DEPTH;

    public Timer timer;
    private boolean timeout;
    public int TIMEOUT;
    public int GRACE_TIMEOUT;
    private boolean firstMove;
    public int FIRST_MOVE_TIMEOUT;
    public int GRACE_FIRST_MOVE_TIMEOUT;

    public HusMove currentMove;
    public Node<TreeNode> playTree;

    public double[] theta;
    public ArrayList<int[]> stateChain;
    public double epsilon;

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
        // this.STRATEGY = MINMAX | ALPHABETA_PRUNING | ORDER_MOVES | ITER_DEEPENING | USE_TIMEOUT | QLEARNING | QVALUE;
        this.STRATEGY = MINMAX | ALPHABETA_PRUNING | ORDER_MOVES | QVALUE;
        this.STRATEGY_STRING = strategyToString(STRATEGY);
        this.DEFAULT_TREE_DEPTH = 6;
        this.MAX_TREE_DEPTH = 200;

        this.timer                    = null;
        this.timeout                  = false;
        this.TIMEOUT                  = 2000;
        this.GRACE_TIMEOUT            = TIMEOUT - 100;
        this.firstMove                = false;  // TODO: change this back
        this.FIRST_MOVE_TIMEOUT       = 30000;
        this.GRACE_FIRST_MOVE_TIMEOUT = FIRST_MOVE_TIMEOUT - 500;

        this.currentMove = null;
        this.playTree = null;

        if ((STRATEGY & QVALUE) != 0) {
            this.theta = new double[]{16.846993064132395, 5.405659445092267, 1.6387093565793258, -0.9353409755101405, -0.42860517112238866};
            // this.theta = new double[]{0, -1, 1, 1, 1};
        }
        else {
            this.theta = null;
        }

        if ((STRATEGY & QLEARNING) != 0) {
            this.stateChain = new ArrayList<int[]>();
        }
        if ((STRATEGY & USE_EPSILON) != 0) {
            this.epsilon = EPSILON;
        }
        else {
            this.stateChain = null;
        }
    }

    /** This is the primary method that you need to implement.
     * The ``board_state`` object contains the current state of the game,
     * which your agent can use to make decisions. See the class hus.RandomHusPlayer
     * for another example agent. */
    public HusMove chooseMove(HusBoardState board_state)
    {
        // init
        if ((STRATEGY & USE_TIMEOUT) != 0) {
            timer = new Timer();
            resetTimer(timer);
        }

        resetMove();

        // setup parameters
        boolean isIterRoot = false;
        int depth = DEFAULT_TREE_DEPTH;
        if ((STRATEGY & TREE_MEM) != 0) {
            resetPlayTree();
        }
        if ((STRATEGY & ITER_DEEPENING) != 0) {
            isIterRoot = true;
        }
        if ((STRATEGY & FORWARD_PRUNING) != 0) {
            depth = MAX_TREE_DEPTH;
        }

        // search
        searchMove(board_state, depth, depth, isIterRoot, MIN_VALUE, MAX_VALUE, this.playTree);
        if ((STRATEGY & USE_EPSILON) != 0) {
            ArrayList<HusMove> moves = board_state.getLegalMoves();
            this.currentMove = epsilonGreedyAction(moves, this.currentMove, this.epsilon);
        }

        // analysis
        // analyze search tree (this is usually slow)
        if ((STRATEGY & TREE_MEM) != 0) {
            analyzeTree(board_state, this.playTree);
        }

        // learn
        if ((STRATEGY & QLEARNING) != 0) {
            stateChain.add(getStates(board_state, this.player_id));
        }

        // cleanup
        if ((STRATEGY & USE_TIMEOUT) != 0) {
            cancelTimeout();
            timer.cancel();
        }

        // return
        return currentMove;
    }

    /** use epsilon greedy to choose move
    * input: moves is the current legal moves
    * input: topMove is the action that yields the best utility state
    * input: epsilon is between 0 and 1, indicating likelihood to choose at random
    * return: the action
    **/
    public HusMove epsilonGreedyAction(ArrayList<HusMove> moves, HusMove topMove, double epsilon)
    {
        double r = Math.random();
        HusMove ret;

        if (r < epsilon) {
            int index = (int) Math.random() * moves.size();
            ret = moves.get(index);
        }
        else {
            ret = topMove;
        }

        return ret;
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
            // not using search tree
            if (isRoot && this.currentMove == null) {
                this.currentMove = sortMovesByBoards(moves, board, player_id, STRATEGY, this.theta).get(0).move;
            }
            return utilityOfBoard(board, STRATEGY, player_id, this.theta);
        }

        // iterative deepening
        // inspiration: http://logic.stanford.edu/ggp/chapters/chapter_07.html
        if (isIterRoot) {
            int maxDepth = MAX_TREE_DEPTH;
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
            ArrayList<MoveWithBoard> moveWithBoards = null;
            if ((STRATEGY & ORDER_MOVES) != 0) {
                moveWithBoards = sortMovesByBoards(moves, board, player_id, STRATEGY, this.theta);
            }
            else {
                moveWithBoards = makeMoveWithBoards(board, moves, player_id, STRATEGY, this.theta);
            }

            /* build tree */
            for (int i = 0; i < moveWithBoards.size(); i++) {
                int v;
                HusMove m = moveWithBoards.get(i).move;
                HusBoardState nextBoard = moveWithBoards.get(i).board;
                Node<TreeNode> nextTree = null;

                // tree memorization
                if (tree != null) {
                    // save move
                    nextTree = tree.addChild(new Node<TreeNode>(new TreeNode(m)));
                }

                // forward pruning: check for quiescence
                if (((STRATEGY & FORWARD_PRUNING) != 0) && !isMax && utilityOfBoard(nextBoard, STRATEGY, player_id, this.theta) == utilityOfBoard(board, STRATEGY, player_id, this.theta)) {
                    v = utilityOfBoard(nextBoard, STRATEGY, player_id, this.theta);
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
                if ((STRATEGY & ALPHABETA_PRUNING) != 0) {
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
            // debugLogMove(bestMove, bestValue);
        }

        // return value
        return bestValue;
    }

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

    public void resetMove()
    {
        this.currentMove = null;
    }

    public void resetPlayTree()
    {
        this.playTree = new Node<TreeNode>();
        this.playTree.setValue(new TreeNode());
    }

    public void qlearn(int winner)
    {
        if ((STRATEGY & QLEARNING) != 0) {
            double[] rewardChain = getRewardChain(this.stateChain, this.player_id, winner);

            // update theta
            debugLog("original: " + Arrays.toString(this.theta));
            for (int i = 0; i < this.stateChain.size(); i++) {
                // terminal
                if (i == this.stateChain.size() - 1) {
                    this.theta = getUpdatedTheta(ALPHA, this.theta, rewardChain[i], rewardChain[i], stateChain.get(i));
                }
                // non-terminal
                else {
                    this.theta = getUpdatedTheta(ALPHA, this.theta, rewardChain[i], rewardChain[i+1], stateChain.get(i));
                }
                // System.out.println(i + ": " + Arrays.toString(this.theta));
            }

            // adjust epsilon
            // if (winner == this.player_id) {
            //     this.epsilon -= EPSILON_ADJUSTMENT;  // less likely to explore
            // }
            // else {
            //     this.epsilon += EPSILON_ADJUSTMENT;  // more likely to explore
            // }
        }
        else {
            System.err.println("qlearning disabled");
        }
    }

    // this is called after learning is done
    public void gameOverCleanUp(int winner)
    {
        // empty stateChain
        this.stateChain = new ArrayList<int[]>();
    }

    public void debugLog(String s)
    {
        if (DEBUG) {
            System.err.println(String.format("[%s] %s", STRATEGY_STRING, s));
        }
    }

    public void debugLogMove(HusMove move, int value)
    {
        if (DEBUG) {
            String s = String.format("<%d/%s/%d/%d>", player_id, STRATEGY_STRING, move.getPit(), value);
            System.err.println(s);
        }
    }

    public void errLog(String s)
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
