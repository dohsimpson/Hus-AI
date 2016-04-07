package student_player.mytools;

import hus.HusPlayer;
import hus.HusBoardState;
import hus.HusMove;
import hus.RandomHusPlayer;
import boardgame.Move;
import boardgame.Board;
import student_player.*;
import student_player.mytools.MyTools;

public class Forever
{
    public static void main (String[] args) {
        StudentPlayer p1 = new StudentPlayer();
        p1.setColor(0);
        double p1Wins = 0;
        double p2Wins = 0;
        while (true) {
            StudentPlayer2 p2 = new StudentPlayer2();
            // RandomHusPlayer p2 = new RandomHusPlayer();
            p2.setColor(1);

            HusBoardState board = new HusBoardState();
            HusPlayer nextPlayer = p1;

            while ( board.getWinner() == Board.NOBODY ) {
                Move move = nextPlayer.chooseMove(board);
                // board.move(board.getRandomMove());
                board.move(move);
                if (nextPlayer == p1) {
                    nextPlayer = p2;
                }
                else {
                    nextPlayer = p1;
                }
            }
            p1.qlearn(board.getWinner());
            p1.gameOverCleanUp(board.getWinner());
            // p1.learningLog2(board.getWinner());
            if (board.getWinner() != Board.DRAW) {
                if (board.getWinner() == 0) { // player0 won
                    p1Wins++;
                }
                else if (board.getWinner() == 1) {
                    p2Wins++;
                }
            }

            // System.err.println("winner" + board.getWinner() + " p1 win rate: " + p1Wins/p2Wins);
            System.err.println("winner" + board.getWinner() + " p1/p2: " + p1Wins +"/" + p2Wins + " = " + p1Wins/p2Wins);
            System.err.println("epsilon: " + p1.epsilon);
            // try{
            //     Thread.sleep(1000);
            // }
            // catch (Exception e) {}

            // if (board.getWinner() == 0) {
            //     // get new params for p2
            //     p2My = MyTools.getRandomParams();
            //     p2Oppo = MyTools.getRandomParams();
            //     // update count
            //     tmpCount1 += 1;
            //     tmpCount2 = 0;
            //     // check best
            //     if (bestCount < tmpCount1) {
            //         bestMy = p1My;
            //         bestOppo = p1Oppo;
            //         bestCount = tmpCount1;
            //         p1.learningLog2(board.getWinner());
            //         System.err.println("best count updated: " + bestCount);
            //     }
            // }
            // else if (board.getWinner() == 1) {
            //     // get new params for p1
            //     p1My = MyTools.getRandomParams();
            //     p1My = MyTools.getRandomParams();
            //     // update count
            //     tmpCount2 += 1;
            //     tmpCount1 = 0;
            //     // check best
            //     if (bestCount < tmpCount2) {
            //         bestMy = p2My;
            //         bestOppo = p2Oppo;
            //         bestCount = tmpCount2;
            //         p2.learningLog2(board.getWinner());
            //         System.err.println("best count updated: " + bestCount);
            //     }
            // }
        }
    }
}
