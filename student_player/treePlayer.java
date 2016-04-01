package student_player;

import static student_player.mytools.MyTools.*;
import student_player.mytools.MyTools.*;

import hus.HusBoardState;
import hus.HusPlayer;
import hus.HusMove;

import java.util.ArrayList;

public class treePlayer extends StudentPlayer {

    protected Node<TreeNode> playTree;
    public treePlayer(String s)
    {
        super(s);
        this.STRATEGY = Strategy.ORDEREDALPHABETA;
        this.UTILITY = Utility.BOARDVALUE2;
        this.ORDERED_ALPHABETA_TREE_DEPTH = 7;
        this.playTree = new Node<TreeNode>();
    }
    public treePlayer()
    {
        this("tree_player");
    }


    @Override
    public HusMove chooseMove(HusBoardState board_state)
    {
        resetPlayTree();

        HusMove move;
        move = orderedAlphaBetaMove(this.playTree, board_state);

        // this.playTree.prettyPrint("", true);

        // do some analysis here
        analyzeTree(board_state, this.playTree);

        // But since this is a placeholder algorithm, we won't act on that information.
        return move;
    }

    // ordered alpha-beta pruning
    public HusMove orderedAlphaBetaMove(Node<TreeNode> tree, HusBoardState board)
    {
        ArrayList<HusMove> moves = board.getLegalMoves();
        sortMovesByBoards(moves, board, player_id, UTILITY);
        HusMove maxMove = moves.get(0);
        int maxValue = MIN_VALUE;
        for (HusMove m : moves) {
            HusBoardState nextBoard = (HusBoardState) board.clone();
            nextBoard.move(m);

            Node<TreeNode> nextTree = tree.addChild(new Node<TreeNode>(new TreeNode(m)));
            int v = orderedAlphaBetaValue(nextTree, false, nextBoard, ORDERED_ALPHABETA_TREE_DEPTH - 1, MIN_VALUE, MAX_VALUE);
            nextTree.value.setValue(v);

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
    public int orderedAlphaBetaValue(Node<TreeNode> tree, boolean isMax, HusBoardState board, int depth, int alpha, int beta)
    {
        ArrayList<HusMove> moves = board.getLegalMoves();
        sortMovesByBoards(moves, board, player_id, UTILITY);

        if (depth <= 1 || moves.isEmpty())
            return utilityOfBoard(board);

        int maxValue = MIN_VALUE;
        int minValue = MAX_VALUE;
        for (HusMove m : moves) {
            HusBoardState nextBoard = (HusBoardState) board.clone();
            nextBoard.move(m);

            Node<TreeNode> nextTree = tree.addChild(new Node<TreeNode>(new TreeNode(m)));
            int v = orderedAlphaBetaValue(nextTree, !isMax, nextBoard, depth - 1, alpha, beta);
            nextTree.value.setValue(v);

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

    public void analyzeTree(HusBoardState board, Node<TreeNode> tree)
    {
        double pruneFactor = (double) tree.getSize() / getNumOfBoardNodes(board, tree.getDepth());
        debugLog("prune factor(less=better): " + tree.getSize() + "/" + getNumOfBoardNodes(board, tree.getDepth()) + " = " + String.format("%.5f", pruneFactor));
    }

    private static int getNumOfBoardNodes(HusBoardState board, int depth)
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

    public void resetPlayTree()
    {
        this.playTree = new Node<TreeNode>();
        this.playTree.setValue(new TreeNode());
    }

    public static class TreeNode
    {
        protected HusMove move;
        protected int value;

        public TreeNode()
        {
            this.move = new HusMove();
        }
        public TreeNode(HusMove move)
        {
            this.move = move;
        }

        public TreeNode(HusMove move, int value)
        {
            this(move);
            this.value = value;
        }

        public void setValue(int v)
        {
            this.value = v;
        }

        public String toString()
        {
            return String.format("%d(%d)", move.getPit(), value);
        }
    }

}

