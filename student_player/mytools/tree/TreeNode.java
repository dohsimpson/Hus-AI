package student_player.mytools.tree;

import hus.HusBoardState;
import hus.HusPlayer;
import hus.HusMove;

public class TreeNode
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
