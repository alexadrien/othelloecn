package ai;

import java.util.ArrayList;
import java.util.List;
import othello.*;

/**
 *
 * @author Vincent
 */
public class GameTree {
    private GameTreeNode root;
    
    public GameTree(Othello game)
    {
        this.root = new GameTreeNode(this, game);
    }
    
    public GameTreeNode getRoot()
    {
        return root;
    }

    public int height() {
        return root.height();
    }
    
    public int nodeCount() {
        return root.nodeCount();
    }
}
