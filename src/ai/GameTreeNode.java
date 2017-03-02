package ai;

import java.util.List;
import othello.Move;
import othello.Othello;

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
    protected int unexplored_branches;
    
    protected GameTreeNode(GameTree tree, Othello game) {
        this.tree = tree;
        this.game = game;
        this.parent = null;
        this.parent_index = -1;
        this.moves = game.possibleMoves();
        this.childs = new GameTreeNode[this.moves.size()];
        this.winning_branches = 0;
        this.explored_branches = 0;
        this.unexplored_branches = moves.size();
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
        this.unexplored_branches = moves.size();

        this.parent.childs[index] = this;
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
        if (game != null) {
            return game;
        }

        assert parent != null;

        Othello g = parent.getGame();
        if (parent.game != null) {
            // parent did return his own copy of the game, 
            // -> we cannot safely modifiy it
            g = new Othello(g);
        }

        g.makeMove(getMove());

        return g;
    }

    /**
     * Returns the list of this node's children.
     * @return 
     */
    public GameTreeNode[] childs() {
        return this.childs;
    }

    /**
     * Returns this node child associated with the given move.
     * @param m
     * @return 
     */
    public GameTreeNode getChild(Move m) {
        for (int i = 0; i < this.moves.size(); ++i) {
            if (moves.get(i).equals(m)) {
                return this.childs[i];
            }
        }

        return null;
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
    
    /**
     * Returns the number of explored branches starting from the game state 
     * represented by this node.
     * 
     * A branch is said to be explored if it explored until an end of game 
     * situation.
     * @return 
     */
    public int getExploredBranches()
    {
        return this.explored_branches;
    }
}
