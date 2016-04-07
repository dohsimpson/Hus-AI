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

    public static final int MINMAX            = (int) Math.pow(2, 0);
    public static final int ALPHABETA_PRUNING = (int) Math.pow(2, 1);
    public static final int ORDER_MOVES       = (int) Math.pow(2, 2);
    public static final int FORWARD_PRUNING   = (int) Math.pow(2, 3);
    public static final int ITER_DEEPENING    = (int) Math.pow(2, 4);
    public static final int TREE_MEM          = (int) Math.pow(2, 5);

    public static final int BOARD_VALUE       = (int) Math.pow(2, 30);


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

        s += "$";
        if ((strategy & BOARD_VALUE) != 0) {
            s += "b";
        }
        return s;
    }

    // utility helper functions
    public static int utilityOfBoard(HusBoardState board, int strategy, int player_id)
    {
        int ret = 0;
        if ((strategy & BOARD_VALUE) != 0) {
            ret = boardValue2(board, player_id);
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
    public static void sortBoards(ArrayList<HusBoardState> boards, final int player_id, final int strategy)
    {
        boards.sort(new Comparator<HusBoardState>() {
            // sort by descending order
            public int compare(HusBoardState b1, HusBoardState b2) {
                return utilityOfBoard(b2, strategy, player_id) - utilityOfBoard(b1, strategy, player_id);
            }
        });
    }

    public static void sortMovesByBoards(ArrayList<HusMove> moves, final HusBoardState board, final int player_id, final int strategy)
    {
        moves.sort(new Comparator<HusMove>() {
            // sort by descending order
            public int compare(HusMove m1, HusMove m2) {
                int ret;
                HusBoardState b1 = makeNextBoard(board, m1);
                HusBoardState b2 = makeNextBoard(board, m2);

                return utilityOfBoard(b2, strategy, player_id) - utilityOfBoard(b1, strategy, player_id);
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
}
