package student_player;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;

import hus.HusMove;
import hus.HusBoardState;

import static student_player.mytools.MyTools.*;
import student_player.mytools.MyTools.*;

public class RLPlayer extends StudentPlayer {
    public double[] theta;
    public ArrayList<int[]> stateChain;
    public double EPSILON;
    public double epsilon;
    public double ALPHA;
    public double WIN_REWARD;
    public double REWARD_DEC;

    public int NUMBER_OF_STATES;

    public RLPlayer()
    {
        super("RLearn");
        this.STRATEGY = MINMAX | ALPHABETA_PRUNING ;
        this.DEFAULT_TREE_DEPTH = 5;
        this.NUMBER_OF_STATES = 2;  // temporarily
        // this.NUMBER_OF_STATES = HusBoardState.BOARD_WIDTH*4 + 1;  // temporarily
        // this.theta = new double[]{5.61164674909717, 4.426228217314714, 6.473028357429804, 4.239019937722555, 5.396319740763512, 10.440031825215229, 4.4932309203585845, 3.571412972582871, 4.450797203665084, 7.441980141041836, 4.914973229969348, 7.392800571879196, 4.7651024770484804, 4.3487606186143415, 7.997118553034475, 7.839494773784197, 6.5664788015954265, 6.505112933172405, 6.929326757924454, 5.441875151384879, 7.746129548587167, 5.5802453524972675, 5.210962090177365, 1.907354112293727, 6.408968794880417, 4.646344040984057, 3.5700892368368615, 4.950455549077243, 5.484651454754554, 6.020407034647998, 4.181976955363406, 4.748819696485694, -3.8964400440012397, 4.392623772149769, -0.01334583574368724, -1.4513883178940021, 0.9142316551019726, -2.0401357337603683, 8.72998314481792, -1.8626182703187681, -3.2745809263030465, -1.9803216067896467, -0.6237941689013343, -0.8600620854866092, 5.627212903904896, 5.796299803105743, -2.066902274687877, 0.9665092177654804, 2.5257107772247434, 2.145472051107645, -2.7010428356046114, 2.4420842810795556, 1.918112775338784, -3.002977260255943, -8.749483203992044, -5.702440520589418, -1.1745767063701722, -3.9221632375039643, 1.5981902248272808, -0.8154411480344416, -1.7699191348979488, -0.27153891888751136, -5.674517538568504, 0.1275147688765233, 1.1191588689311527, 1};
        // this.theta = new double[NUMBER_OF_STATES+1];
        // for (int i = 0; i < theta.length; i++) {
        //     theta[i] = Math.random();
        // }
        this.theta = new double[]{10.970424267076455, -2.0651941948449832, -0.008980308573120838};
        this.stateChain = new ArrayList<int[]>();
        this.EPSILON = 0.05;
        this.epsilon = EPSILON;
        this.ALPHA = 0.0000001;

        WIN_REWARD = 1000.0;
        REWARD_DEC = 0.4;
    }

    public void cleanup()
    {
        this.stateChain = new ArrayList<int[]>();
    }

    @Override
    public HusMove chooseMove(HusBoardState board_state)
    {
        ArrayList<HusMove> moves = board_state.getLegalMoves();
        HusMove move = epsilonGreedyAction(moves, getTopMove(moves, board_state), this.epsilon);
        stateChain.add(getStates(board_state));
        this.epsilon = this.epsilon;
        return move;
    }

    // input: moves is the current legal moves
    // input: topMove is the action that yields the best utility state
    // input: epsilon is between 0 and 1, indicating likelihood to choose at random
    // return: the action
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

    public HusMove getTopMove(ArrayList<HusMove> moves, HusBoardState board)
    {
        double bestValue = -1000;
        HusMove bestMove = moves.get(0);
        // evaluate each moves and get the one with highest value
        for (int i = 0; i < moves.size(); i++) {
            HusMove move = moves.get(i);
            double v = evaluateStates(getStates(tryMove(board, move)));
            if (v > bestValue) {
                bestValue = v;
                bestMove = move;
            }
        }

        bestValue = searchMove(board, DEFAULT_TREE_DEPTH, MIN_VALUE, MAX_VALUE);
        bestMove = this.currentMove;
        resetMove();
        assert bestValue > -1000;
        return bestMove;
    }

    public HusBoardState tryMove(HusBoardState board, HusMove move)
    {
        HusBoardState nextBoard = (HusBoardState) board.clone();
        nextBoard.move(move);

        assert !nextBoard.equals(board);
        return nextBoard;
    }

    public int[] getStates(HusBoardState board)
    {
        // int number_of_features = 4*board.BOARD_WIDTH;
        int[] states = new int[NUMBER_OF_STATES];

        // feature 0 for 4 width game
        // int[] b = serializeBoard(board, this.player_id);
        // for (int i = 0; i < b.length; i++) {
        //     states[i] = b[i];
        // }

        // feature 1
        {
            // double v = (double) boardValue(board, this.player_id);
            // states[0] = (int) (v / (2 * 3 * board.BOARD_WIDTH - v) * 100);
            states[0] = boardValue(board, this.player_id);
            // states[NUMBER_OF_STATES - 1] = boardValue(board, this.player_id);
        }

        // feature 2
        {
            states[1] = leastOpponentMoves(board, this.player_id);
            // states[NUMBER_OF_STATES - 1] = leastOpponentMoves(board, this.player_id);
        }

        // // feature 3
        // {
        //     states[2] = mostMyMoves(board, this.player_id);
        // }

        // // feature 4
        // {
        //     states[3] = boardValue(board, 1 - this.player_id);
        // }

        // // feature 5
        // {
        //     // states[4] =
        // }

        return states;
    }

    public double evaluateStates(int[] states)
    {
        double ret = 0;
        for (int i = 0; i < NUMBER_OF_STATES; i++) {
            ret += this.theta[i] * states[i];
        }
        ret += this.theta[this.theta.length-1];

        assert NUMBER_OF_STATES == theta.length - 1;

        return ret;
    }

    public double[] getRewardChain(ArrayList<int[]> states, int winner)
    {
        double[] ret = new double[states.size()];
        double nextReward;
        if (winner == this.player_id) {
            nextReward = WIN_REWARD;
        }
        else if (winner != 0 && winner != 1) {
            System.out.println("DRAW");
            nextReward = 0;
        }
        else {
            nextReward = -1.0;
        }

        for (int i = ret.length - 1; i >= 0; i--) {
            ret[i] = nextReward;
            nextReward -= REWARD_DEC;
        }

        assert ret[0] < ret[ret.length - 1];
        assert ret.length == states.size();

        return ret;
    }

    // input: alpha is the learning rate
    public void updateTheta(double alpha, double[] theta, double reward, int[] states)
    {
        double evalUtil = evaluateStates(states);
        // System.out.println("look0:" + Arrays.toString(states));
        // System.out.println("look1:" + evalUtil);
        // System.out.println("look2:" + reward);
        assert NUMBER_OF_STATES == states.length;
        assert NUMBER_OF_STATES == theta.length - 1;
        for (int i = 0; i < NUMBER_OF_STATES; i++) {
            theta[i] = theta[i] + alpha*(reward - evalUtil)*states[i];
        }
        theta[theta.length-1] = theta[theta.length-1] + alpha*(reward - evalUtil);
    }

    public void learn(int winner)
    {
        if (winner != HusBoardState.NOBODY) {
            double[] rewardChain = getRewardChain(stateChain, winner);

            // update theta
            System.out.println("original: " + Arrays.toString(this.theta));
            for (int i = 0; i < this.stateChain.size(); i++) {
                updateTheta(this.ALPHA, this.theta, rewardChain[i], stateChain.get(i));
                // System.out.println(i + ": " + Arrays.toString(this.theta));
            }
        }
    }

    public double searchMove(HusBoardState board, int depth, double alpha, double beta)
    {
        /* initialization */
        int original_depth = this.DEFAULT_TREE_DEPTH;
        boolean isIterRoot = false;
        ArrayList<HusMove> moves     = board.getLegalMoves();
        boolean            isMax     = (original_depth - depth) % 2 == 0;
        boolean            isRoot    = original_depth == depth;
        HusMove            bestMove  = null;
        double                bestValue = 0;

        // return heuristic value
        if (depth <= 1 || moves.isEmpty()) {
            return evaluateStates(getStates(board));
        }

        // iterative deepening
        // inspiration: http://logic.stanford.edu/ggp/chapters/chapter_07.html
        if (isIterRoot) {
            int maxDepth = MAX_TREE_DEPTH;
            int iterDepth = -1;

            // deepening
            // while (iterDepth < maxDepth) {
            //     iterDepth++;
            //     double v = searchMove(board, original_depth + iterDepth, MIN_VALUE, MAX_VALUE);
            //     // if (!this.timeout) {
            //     //     bestMove = currentMove;
            //     //     bestValue = v;
            //     //     // NOTE: these values are used at the end of the function for updating and returning
            //     // }
            // }
            // debugLog("iter depth: " + iterDepth);
        }

        else {
            // min-max
            if (isMax)
                bestValue = MIN_VALUE;
            else
                bestValue = MAX_VALUE;

            // order moves
            if ((STRATEGY & ORDER_MOVES) != 0)
                // TODO: optimize by returning the boards
                sortMovesByBoards(moves, board, player_id, STRATEGY);

            /* build tree */
            for (HusMove m : moves) {
                double v;
                HusBoardState nextBoard = (HusBoardState) board.clone();
                nextBoard.move(m);
                // Node<TreeNode> nextTree = null;

                // tree memorization

                // forward pruning: check for quiescence
                // if (((STRATEGY & FORWARD_PRUNING) != 0) && !isMax && utilityOfBoard(nextBoard, STRATEGY, player_id) == utilityOfBoard(board, STRATEGY, player_id)) {
                //     v = utilityOfBoard(nextBoard, STRATEGY, player_id);
                //     // debugLog("forward pruning: quiescence state with value: " + v);
                // }

                // recursion
                // else {
                    v = searchMove(nextBoard, depth - 1, alpha, beta);
                // }


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
                            alpha = Double.max(bestValue, (double) alpha);
                    }
                    else {
                        if (bestValue <= alpha)
                            break;
                        else
                            beta = Double.min(bestValue, (double) beta);
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
            // debugLogMove(bestMove, (int) bestValue);
        }

        // return value
        return bestValue;
    }
}
