package student_player;

import hus.HusBoardState;
import hus.HusPlayer;
import hus.HusMove;

import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

import student_player.mytools.MyTools;

/** A Hus player submitted by a student. */
public class KeyboardPlayer extends StudentPlayer {

    private final String ANSI_RESET  = "\u001B[0m";
    private final String ANSI_BLACK  = "\u001B[30m";
    private final String ANSI_RED    = "\u001B[31m";
    private final String ANSI_GREEN  = "\u001B[32m";
    private final String ANSI_YELLOW = "\u001B[33m";
    private final String ANSI_BLUE   = "\u001B[34m";
    private final String ANSI_PURPLE = "\u001B[35m";
    private final String ANSI_CYAN   = "\u001B[36m";
    private final String ANSI_WHITE  = "\u001B[37m";

    protected Scanner scanner;

    /** You must modify this constructor to return your student number.
     * This is important, because this is what the code that runs the
     * competition uses to associate you with your agent.
     * The constructor should do nothing else. */
    public KeyboardPlayer() {
        super("keyboard");
        scanner = new Scanner(System.in);
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

        debugLog("board(" + board_state.getTurnNumber() + "): \n" + convertBoardToString(board_state));

        // Get the legal moves for the current board state.
        ArrayList<HusMove> moves = board_state.getLegalMoves();
        debugLog("my moves: " + convertMovesToString(moves));

        HusMove move = null;

        // get move from keyboard
        while (move == null) {
            boolean dryRun = false;
            int inputInt = readFromKeyboard();

            if (inputInt >= 100) {
                inputInt %= 100;
                dryRun = true;
            }

            if (inputInt < 0) {
                switch (inputInt) {
                    case -1:
                        move = super.chooseMove(board_state);
                        break;
                }
                dryRun = true;
            }

            for (HusMove m: moves) {
                if (m.getPit() == inputInt) {
                    move = m;
                    break;
                }
            }

            if (move == null) {
                debugLog("illegalMove");
            }
            else if (dryRun) {
                // We can see the effects of a move like this...
                HusBoardState cloned_board_state = (HusBoardState) board_state.clone();
                cloned_board_state.move(move);
                debugLog("board(" + cloned_board_state.getTurnNumber() + ")*: " + move.getPit() + "\n" + convertUpdatedBoardToString(board_state, cloned_board_state));
                move = null;
            }
        }

        return move;
    }

    protected String convertMovesToString(List<HusMove> moves)
    {
        String s = "";
        for (HusMove m : moves) {
            s += m.getPit() + " ";
        }
        return s;
    }

    protected String convertBoardToString(HusBoardState board)
    {
        // Get the contents of the pits so we can use it to make decisions.
        int[][] pits = board.getPits();

        // Use ``player_id`` and ``opponent_id`` to get my pits and opponent pits.
        int[] my_pits = pits[player_id];
        int[] op_pits = pits[opponent_id];

        StringBuilder sb = new StringBuilder();

        sb.append(boardInfoStr(board));

        for(int i = op_pits.length / 2 - 1, firstRound = 1; i >= 0; i--){
            if(firstRound == 0)
                sb.append("|");
            else
                firstRound = 0;
            sb.append(padRight(Integer.toString(op_pits[i]), 3));
        }
        sb.append("\n");

        for(int i = op_pits.length / 2, firstRound = 1; i < my_pits.length; i++){
            if(firstRound == 0)
                sb.append("|");
            else
                firstRound = 0;
            sb.append(padRight(Integer.toString(op_pits[i]), 3));
        }
        sb.append("\n");

        sb.append("-------------------------------------------------------------\n");
        for(int i = my_pits.length - 1, firstRound = 1; i >= my_pits.length / 2; i--){
            if(firstRound == 0)
                sb.append("|");
            else
                firstRound = 0;
            sb.append(padRight(Integer.toString(my_pits[i]), 3));
        }
        sb.append("\n");

        for(int i = 0, firstRound = 1; i < my_pits.length / 2; i++){
            if(firstRound == 0)
                sb.append("|");
            else
                firstRound = 0;
            sb.append(padRight(Integer.toString(my_pits[i]), 3));
        }
        sb.append("\n");

        return sb.toString();
    }

    protected String convertUpdatedBoardToString(HusBoardState board, HusBoardState board2)
    {
        // Get the contents of the pits so we can use it to make decisions.
        int[][] pits = board.getPits();

        // Use ``player_id`` and ``opponent_id`` to get my pits and opponent pits.
        int[] my_pits = pits[player_id];
        int[] op_pits = pits[opponent_id];

        // Get the contents of the pits so we can use it to make decisions.
        int[][] pits2 = board2.getPits();

        // Use ``player_id`` and ``opponent_id`` to get my pits and opponent pits.
        int[] my_pits2 = pits2[player_id];
        int[] op_pits2 = pits2[opponent_id];

        StringBuilder sb = new StringBuilder();

        sb.append(boardInfoStr(board2));

        for(int i = op_pits.length / 2 - 1, firstRound = 1; i >= 0; i--){
            if(firstRound == 0)
                sb.append("|");
            else
                firstRound = 0;
            if (op_pits[i] == op_pits2[i]) {
                sb.append(padRight(Integer.toString(op_pits2[i]), 3));
            }
            else {
                sb.append(makeRed(padRight(Integer.toString(op_pits2[i]), 3)));
            }
        }
        sb.append("\n");

        for(int i = op_pits.length / 2, firstRound = 1; i < my_pits.length; i++){
            if(firstRound == 0)
                sb.append("|");
            else
                firstRound = 0;
            if (op_pits[i] == op_pits2[i]) {
                sb.append(padRight(Integer.toString(op_pits2[i]), 3));
            }
            else {
                sb.append(makeRed(padRight(Integer.toString(op_pits2[i]), 3)));
            }
        }
        sb.append("\n");

        sb.append("-------------------------------------------------------------\n");
        for(int i = my_pits.length - 1, firstRound = 1; i >= my_pits.length / 2; i--){
            if(firstRound == 0)
                sb.append("|");
            else
                firstRound = 0;
            if (my_pits[i] == my_pits2[i]) {
                sb.append(padRight(Integer.toString(my_pits2[i]), 3));
            }
            else {
                sb.append(makeRed(padRight(Integer.toString(my_pits2[i]), 3)));
            }
        }
        sb.append("\n");

        for(int i = 0, firstRound = 1; i < my_pits.length / 2; i++){
            if(firstRound == 0)
                sb.append("|");
            else
                firstRound = 0;
            if (my_pits[i] == my_pits2[i]) {
                sb.append(padRight(Integer.toString(my_pits2[i]), 3));
            }
            else {
                sb.append(makeRed(padRight(Integer.toString(my_pits2[i]), 3)));
            }
        }
        sb.append("\n");

        return sb.toString();
    }

    protected String boardInfoStr(HusBoardState board)
    {
        StringBuilder s = new StringBuilder();
        s.append("my board value: ");
        s.append(MyTools.boardValue(board, player_id));
        s.append("\n");
        return s.toString();
    }

    protected int readFromKeyboard()
    {
        int i = scanner.nextInt();
        return i;
    }

    private String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", s);
    }

    private String makeRed(String s) {
        return ANSI_RED + s + ANSI_RESET;
    }
}
