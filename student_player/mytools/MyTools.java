package student_player.mytools;

import hus.HusBoardState;
import hus.HusPlayer;
import hus.HusMove;

import java.util.ArrayList;
import java.util.Comparator;

import student_player.mytools.tree.*;

public class MyTools {
    public static int MIN_VALUE = Integer.MIN_VALUE;
    public static int MAX_VALUE = Integer.MAX_VALUE;

    public static final int MINMAX             = (int) Math.pow(2, 0);
    public static final int ALPHABETA_PRUNING  = (int) Math.pow(2, 1);
    public static final int ORDER_MOVES        = (int) Math.pow(2, 2);
    public static final int FORWARD_PRUNING    = (int) Math.pow(2, 3);
    public static final int ITER_DEEPENING     = (int) Math.pow(2, 4);
    public static final int TREE_MEM           = (int) Math.pow(2, 5);
    public static final int QLEARNING          = (int) Math.pow(2, 6);

    public static final int BOARD_VALUE        = (int) Math.pow(2, 30);
    public static final int QVALUE             = (int) Math.pow(2, 29);

    public static final double EPSILON            = 0.05;
    public static final double EPSILON_ADJUSTMENT = 0.01;
    public static final double ALPHA              = 0.0000001;
    public static final double WIN_REWARD         = 1000.0;
    public static final double REWARD_DEC         = 0.4;
    public static final int NUMBER_OF_FEATURES    = 2;


    // strategy helper functions
    public static String strategyToString(int strategy)
    {
        String s = "";
        if ((strategy & FORWARD_PRUNING) != 0) {
            s += "f";
        }
        if ((strategy & ITER_DEEPENING) != 0) {
            s += "i";
        }
        if ((strategy & ORDER_MOVES) != 0) {
            s += "o";
        }
        if ((strategy & ALPHABETA_PRUNING) != 0) {
            s += "a";
        }
        if ((strategy & TREE_MEM) != 0) {
            s += "t";
        }
        if ((strategy & QLEARNING) != 0) {
            s += "l";
        }

        s += "$";
        if ((strategy & BOARD_VALUE) != 0) {
            s += "b";
        }
        if ((strategy & QVALUE) != 0) {
            s += "q";
        }
        return s;
    }

    // utility helper functions
    public static int utilityOfBoard(HusBoardState board, int strategy, int player_id, double[] theta)
    {
        int ret = 0;
        if ((strategy & BOARD_VALUE) != 0) {
            ret = boardValue2(board, player_id);
        }
        else if ((strategy & QVALUE) != 0) {
            if (theta == null) {
                System.err.println("error");
            }
            else {
                ret = (int) evaluateStatesCheckWin(getStates(board, player_id), board, player_id, theta);  // converting double to int is not nice, but it's much easier than changing everything to double
            }
        }
        else {
            System.err.println("unimplemented strategy!");
        }
        return ret;
    }

    // utility measure functions
    public static int boardValue(HusBoardState board, int player_id)
    {
        int ret = 0;
        int[][] pits = board.getPits();
        int[] my_pits = pits[player_id];
        for (int i : my_pits) {
            ret += i;
        }
        return ret;
    }

    public static int boardValue2(HusBoardState board, int player_id)
    {
        if (board.gameOver()) {
            if (board.getWinner() == player_id)
                return MAX_VALUE;
            else if (board.getWinner() == board.DRAW) {}
            else
                return MIN_VALUE;
        }
        return boardValue(board, player_id);
    }

    public static int leastOpponentMoves(HusBoardState board, int player_id)
    {
        int ret = 0;
        int[][] pits = board.getPits();
        int[] opponent_pits = pits[1 - player_id];
        for (int i : opponent_pits) {
            if (i > 1)
                ret--;
        }
        return ret;
    }

    public static int mostMyMoves(HusBoardState board, int player_id)
    {
        int ret = 0;
        int[][] pits = board.getPits();
        int[] my_pits = pits[player_id];
        for (int i : my_pits) {
            if (i > 1)
                ret++;
        }
        return ret;
    }

    public static double evaluateStatesCheckWin(int[] states, HusBoardState board, int player_id, double[] theta)
    {
        if (board.getWinner() == player_id) {
            return WIN_REWARD;
        }
        else if (board.getWinner() == (1 - player_id)) {
            return -WIN_REWARD;
        }

        assert states.length == NUMBER_OF_FEATURES;
        assert theta.length == NUMBER_OF_FEATURES + 1;
        return evaluateStates(states, theta);
    }

    public static double evaluateStates(int[] states, double[] theta)
    {
        double ret = 0;
        for (int i = 0; i < states.length; i++) {
            ret += theta[i] * states[i];
        }
        ret += theta[theta.length-1];

        assert states.length == theta.length - 1;

        return ret;
    }

    public static int[] getStates(HusBoardState board, int player_id)
    {
        // int number_of_features = 4*board.BOARD_WIDTH;
        int[] states = new int[NUMBER_OF_FEATURES];

        // feature 0 for 4 width game
        // int[] b = serializeBoard(board, this.player_id);
        // for (int i = 0; i < b.length; i++) {
        //     states[i] = b[i];
        // }

        // feature 1
        {
            states[0] = boardValue(board, player_id);
        }

        // feature 2
        {
            states[1] = leastOpponentMoves(board, player_id);
        }

        // // feature 3
        // {
        //     states[2] = mostMyMoves(board, this.player_id);
        // }

        // // feature 4
        // {
        //     states[3] = boardValue(board, 1 - this.player_id);
        // }

        return states;
    }

    // others
    /** this method differs from boardValue in that it is the opponent's turn **/
    public static int leastBoardValueNextTurn(HusBoardState board, int player_id)
    {
        ArrayList<HusMove> moves = board.getLegalMoves();
        int minValue = MAX_VALUE;
        for (HusBoardState nextBoard : makeNextBoards(board, moves)) {
            int v = boardValue2(nextBoard, player_id);
            if (v < minValue)
                minValue = v;
        }
        return minValue;
    }

    public static int mostLostNextTurn(HusBoardState board, int player_id)
    {
        return (boardValue2(board, player_id) - leastBoardValueNextTurn(board, player_id));
    }

    public static HusBoardState makeNextBoard(HusBoardState board, HusMove move)
    {
        HusBoardState nextBoard = (HusBoardState) board.clone();
        nextBoard.move(move);
        return nextBoard;
    }
    public static ArrayList<HusBoardState> makeNextBoards(HusBoardState board, ArrayList<HusMove> moves)
    {
        ArrayList<HusBoardState> nextBoards = new ArrayList<HusBoardState>();
        for (HusMove m: moves) {
            nextBoards.add(makeNextBoard(board, m));
        }
        return nextBoards;
    }
    public static ArrayList<HusBoardState> makeNextBoards(HusBoardState board)
    {
        return makeNextBoards(board, board.getLegalMoves());
    }
    public static void sortBoards(ArrayList<HusBoardState> boards, final int player_id, final int strategy, final double[] theta)
    {
        boards.sort(new Comparator<HusBoardState>() {
            // sort by descending order
            public int compare(HusBoardState b1, HusBoardState b2) {
                return utilityOfBoard(b2, strategy, player_id, theta) - utilityOfBoard(b1, strategy, player_id, theta);
            }
        });
    }

    public static void sortMovesByBoards(ArrayList<HusMove> moves, final HusBoardState board, final int player_id, final int strategy, final double[] theta)
    {
        moves.sort(new Comparator<HusMove>() {
            // sort by descending order
            public int compare(HusMove m1, HusMove m2) {
                int ret;
                HusBoardState b1 = makeNextBoard(board, m1);
                HusBoardState b2 = makeNextBoard(board, m2);

                return utilityOfBoard(b2, strategy, player_id, theta) - utilityOfBoard(b1, strategy, player_id, theta);
            }
        });
    }

    /** recursively sum up number of all legal moves until depth.
     * Used for calculating the prune factor of the search tree. **/
    public static int getNumOfBoardNodes(HusBoardState board, int depth)
    {
        int branches = 0;
        if (depth <= 1)
            return branches;

        for (HusBoardState nextBoard : makeNextBoards(board)) {
            branches += getNumOfBoardNodes(nextBoard, depth - 1);
        }
        branches += board.getLegalMoves().size();

        return branches;
    }

    public static int[] serializeBoard(HusBoardState board, int player_id)
    {
        int[] ret = new int[board.BOARD_WIDTH*4];
        int[][] pits = board.getPits();
        int[] my_pits = pits[player_id];
        int[] opponent_pits = pits[1 - player_id];

        for (int i = 0; i < 2*board.BOARD_WIDTH; i++) {
            ret[i] = my_pits[i]+1;
        }
        for (int i = 0, j = 2*board.BOARD_WIDTH; i < 2*board.BOARD_WIDTH; i++, j++) {
            ret[j] = opponent_pits[i]+1;
        }

        return ret;
    }

    /** collect the trail of rewards for the trail of actions.
     * used by learning.
     **/
    public static double[] getRewardChain(ArrayList<int[]> states, int player_id, int winner)
    {
        double[] ret = new double[states.size()];
        double nextReward;
        if (winner == player_id) {
            nextReward = WIN_REWARD;
        }
        else if (winner != 0 && winner != 1) {
            System.out.println("DRAW");
            nextReward = 0;
        }
        else {
            nextReward = -WIN_REWARD;
        }

        for (int i = ret.length - 1; i >= 0; i--) {
            ret[i] = nextReward;
            nextReward -= REWARD_DEC;
        }

        assert ret[0] < ret[ret.length - 1];
        assert ret.length == states.size();

        return ret;
    }

    /** get the updated theta.
     * used by learning
     * input: alpha is the learning rate
    **/
    public static double[] getUpdatedTheta(double alpha, double[] theta, double reward, int[] states)
    {
        double evalUtil = evaluateStates(states, theta);
        double[] ret    = new double[theta.length];

        // System.out.println("look0:" + Arrays.toString(states));
        // System.out.println("look1:" + evalUtil);
        // System.out.println("look2:" + reward);
        assert NUMBER_OF_FEATURES == states.length;
        assert NUMBER_OF_FEATURES == theta.length - 1;
        assert NUMBER_OF_FEATURES == ret.length - 1;
        for (int i = 0; i < NUMBER_OF_FEATURES; i++) {
            ret[i] = theta[i] + alpha*(reward - evalUtil)*states[i];
        }
        ret[ret.length-1] = theta[theta.length-1] + alpha*(reward - evalUtil);

        return ret;
    }


}
