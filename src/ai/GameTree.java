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
    private Player player;
    
    public GameTree(Othello game, Player player)
    {
        this.root = new GameTreeNode(this, game);
        this.player = player;
    }
    
    public GameTreeNode getRoot()
    {
        return root;
    }
    
    public Player getPlayer()
    {
        return player;
    }
    
}
