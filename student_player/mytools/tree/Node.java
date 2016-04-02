package student_player.mytools.tree;

import java.util.ArrayList;

// tree implementation
// source: http://stackoverflow.com/questions/3522454/java-tree-data-structure
public class Node<T> {
    public T value;
    public ArrayList<Node<T>> children;

    public Node()
    {
        this.children = new ArrayList<Node<T>>();
    }

    public Node(T v)
    {
        this.value = v;
        this.children = new ArrayList<Node<T>>();
    }

    public void setValue(T v)
    {
        this.value = v;
    }

    public Node<T> addChild(Node<T> c)
    {
        this.children.add(c);
        return c;
    }

    public int getDepth()
    {
        int max = 0;
        for (Node<T> nextTree : this.children) {
            int nextDepth = nextTree.getDepth();
            if (nextDepth > max)
                max = nextDepth;
        }
        return 1 + max;
    }

    public int getSize()
    {
        int size = this.children.size();
        for (Node<T> nextTree : this.children) {
            size += nextTree.getSize();
        }
        return size;
    }

    public void prettyPrint(String indent, boolean last)
    {
        System.out.print(indent);
        if (last)
        {
            System.out.print("\\-");
            indent += "  ";
        }
        else
        {
            System.out.print("|-");
            indent += "| ";
        }
        System.out.println(this.value.toString());

        for (int i = 0; i < this.children.size(); i++)
            this.children.get(i).prettyPrint(indent, i == this.children.size() - 1);
    }
}
