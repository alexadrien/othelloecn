package ai;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import othello.Move;
import othello.Othello;
import othello.Player;
import othello.State;

/**
 * This class represents a node in the game tree.
 * 
 * It contains all fields required to implement a min-max or MonteCarlo tree.
 * @author Vincent
 */
public class GameTreeNode {
    protected GameTree tree;
    protected Othello game;
    protected GameTreeNode parent;
    protected int parent_index;
    protected List<Move> moves;
    protected GameTreeNode[] childs;
    protected int winning_branches;
    protected int explored_branches;
    protected int total_branches;
    
    protected GameTreeNode(GameTree tree, Othello game) {
        this.tree = tree;
        this.game = new Othello(game);
        this.parent = null;
        this.parent_index = -1;
        this.moves = game.possibleMoves();
        this.childs = new GameTreeNode[this.moves.size()];
        this.winning_branches = 0;
        this.explored_branches = 0;
        this.total_branches = moves.size();
    }

    protected GameTreeNode(GameTree tree, GameTreeNode parent, int index, List<Move> moves) {
        this.tree = tree;
        this.game = null;
        this.parent = parent;
        this.parent_index = index;
        this.moves = moves;
        this.childs = new GameTreeNode[this.moves.size()];
        this.winning_branches = 0;
        this.explored_branches = 0;
        this.total_branches = moves.size();

        this.parent.childs[index] = this;
    }
    
    public boolean isRoot() {
        return this.parent == null;
    }
    
    /**
     * Returns the tree this node belongs to.
     * @return 
     */
    public GameTree getTree()
    {
        return this.tree;
    }

    /**
     * Return the game associated with this node.
     * 
     * If the result is not cached in the field 'game', the result is computed 
     * recursively from its ancestors (a copy is created).
     * @return 
     */
    public Othello getGame() {
        return getGame(false);
    }
    
    public Othello getGame(boolean cache) {
        if (game != null) {
            return game;
        }

        assert parent != null;

        Othello g = parent.getGame();
        if (parent.game != null) {
            // parent did return his own copy of the game, 
            // -> we cannot safely modify it
            g = new Othello(g);
        }

        g.makeMove(getMove());
        
        while(g.getState() == State.WhitePlayerPass || g.getState() == State.BlackPlayerPass) {
            g.acknowledgePass(g.getState() == State.WhitePlayerPass ? Player.WhitePlayer : Player.BlackPlayer);
        }

        if(cache) {
            this.game = g;
        }

        return g;
    }

    /**
     * Returns the list of this node's children.
     * @return 
     */
    public GameTreeNode[] childs() {
        return this.childs;
    }
    
    public int childCount() {
        return this.childs.length;
    }

    /**
     * Returns this node child associated with the given move.
     * @param m
     * @return 
     */
    public GameTreeNode getChild(Move m) {
        for (int i = 0; i < this.moves.size(); ++i) {
            if (moves.get(i).equals(m)) {
                return getChild(i);
            }
        }

        return null;
    }
    
    public GameTreeNode getChild(int index) {
        if (this.childs[index] == null) {
            Othello game = this.getGame();
            game.makeMove(this.moves.get(index));
            while (game.getState() == State.WhitePlayerPass || game.getState() == State.BlackPlayerPass) {
                game.acknowledgePass(game.getState() == State.WhitePlayerPass ? Player.WhitePlayer : Player.BlackPlayer);
            }
            this.childs[index] = new GameTreeNode(this.tree, this, index, game.possibleMoves());
            game.popLastMove();
        }
        return this.childs[index];
    }
    
    public List<GameTreeNode> getChilds()
    {
        List<GameTreeNode> ret = new ArrayList<GameTreeNode>();
        for(int i = 0; i < this.childs.length; ++i) {
            ret.add(getChild(i));
        }
        return ret;
    }

    /**
     * Returns the list of moves that can be played.
     * @return 
     */
    public List<Move> getMoves() {
        return this.moves;
    }

    /**
     * Returns the move associated with this game state. 
     * @return 
     */
    public Move getMove() {
        assert this.parent != null;
        return this.parent.moves.get(this.parent_index);
    }
    
    public int height() {
        int h = 0;
        for(int i = 0; i < this.childs.length; ++i) {
            if(this.childs[i] == null) {
                continue;
            }
            h = Math.max(h, this.childs[i].height());
        }
        return 1+h;
    }
    
    public int nodeCount() {
        int ret = 1;
        for(int i = 0; i < this.childs.length; ++i) {
            if(this.childs[i] == null) {
                continue;
            }
            ret += this.childs[i].nodeCount();
        }
        return ret;
    }
    
    public int depth() {
        if(this.isRoot()) {
            return 0;
        }
        
        return 1 + this.parent.depth();
    }
    
    public boolean isLeaf() {
        return this.childs.length == 0;
    }
    
    /**
     * Returns a list of tree nodes representing all reachable game-states 
     * after exactly 'depth' moves.
     * Note that branches leading to a game-over situation after less than 
     * 'depth' moves are also returned by this function.
     * @param depth
     * @return 
     */
    public List<GameTreeNode> reachableGames(int depth)
    {
        List<GameTreeNode> result = new ArrayList<GameTreeNode>();
        
        int this_depth = this.depth();
        Queue<GameTreeNode> queue = new LinkedList<GameTreeNode>();
        queue.add(this);
        while(!queue.isEmpty()) {
            GameTreeNode n = queue.remove();
            if(n.depth() - this_depth == depth) {
                result.add(n);
            } else if(n.childCount() == 0) {
                result.add(n);
            } else {
                for(int i = 0; i < n.childCount(); ++i) {
                    queue.add(n.getChild(i));
                }
            }
        }
        
        return result;
    }
}
