package student_player.mytools;

import hus.HusPlayer;

import java.io.IOException;

public class Forever
{
    protected static String p1 = "hus.RandomHusPlayer";
    protected static String p2 = "hus.RandomHusPlayer";

    public static void main(String args[])
    {
        int n_games;
        try{
            n_games = Integer.parseInt(args[0]);
            if(n_games < 1) {
                throw new Exception();
            }
        } catch (Exception e) {
            System.err.println(
                "First argument to Autoplay must be a positive int "
                + "giving the number of games to play.");
            return;
        }

        try {
            ProcessBuilder server_pb = new ProcessBuilder(
                "java", "-cp", "bin",  "boardgame.Server", "-ng", "-k");
            server_pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);

            Process server = server_pb.start();

            ProcessBuilder client1_pb = new ProcessBuilder(
                "java", "-cp", "bin", "-Xms520m", "-Xmx520m", "boardgame.Client", p1);
            client1_pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);

            ProcessBuilder client2_pb = new ProcessBuilder(
                "java", "-cp", "bin", "-Xms520m", "-Xmx520m", "boardgame.Client", p2);
            client2_pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);

            for (int i=0; i < n_games; i++) {
                System.out.println("Game "+i);

                try {
                    Thread.sleep(500);
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                Process client1 = ((i % 2 == 0) ? client1_pb.start() : client2_pb.start());

                try {
                    Thread.sleep(500);
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                Process client2 = ((i % 2 == 0) ? client2_pb.start() : client1_pb.start());

                try{
                    client1.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try{
                    client2.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            server.destroy();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

