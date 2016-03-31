package student_player.mytools;

import hus.HusBoardState;
import hus.HusPlayer;
import hus.HusMove;

import java.util.ArrayList;
import java.util.Comparator;

public class MyTools {
    public enum Strategy { MINMAX, ALPHABETA, ORDEREDALPHABETA }
    public enum Utility { BOARDVALUE, BOARDVALUE2, LEASTOPPONENTMOVES }

    public static double getSomething(){
        return Math.random();
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
                return Integer.MAX_VALUE;
            else if (board.getWinner() == board.DRAW) {}
            else
                return Integer.MIN_VALUE;
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

    // others
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
    public static void sortBoards(ArrayList<HusBoardState> boards, final int player_id, final Utility util)
    {
        boards.sort(new Comparator<HusBoardState>() {
            // sort by descending order
            public int compare(HusBoardState b1, HusBoardState b2) {
                int ret;
                switch (util) {
                    case BOARDVALUE:
                        ret = boardValue(b2, player_id) - boardValue(b1, player_id);
                        break;
                    case LEASTOPPONENTMOVES:
                        ret = leastOpponentMoves(b2, player_id) - leastOpponentMoves(b1, player_id);
                        break;
                    default:
                        ret = 0;
                        break;
                }
                return ret;
            }
        });
    }

    public static void sortMovesByBoards(ArrayList<HusMove> moves, final HusBoardState board, final int player_id, final Utility util)
    {
        moves.sort(new Comparator<HusMove>() {
            // sort by descending order
            public int compare(HusMove m1, HusMove m2) {
                int ret;
                HusBoardState b1 = makeNextBoard(board, m1);
                HusBoardState b2 = makeNextBoard(board, m2);

                switch (util) {
                    case BOARDVALUE:
                        ret = boardValue(b2, player_id) - boardValue(b1, player_id);
                        break;
                    case LEASTOPPONENTMOVES:
                        ret = leastOpponentMoves(b2, player_id) - leastOpponentMoves(b1, player_id);
                        break;
                    default:
                        ret = 0;
                        break;
                }
                return ret;
            }
        });
    }

}
