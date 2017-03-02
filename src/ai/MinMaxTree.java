package ai;

import static java.lang.Integer.max;
import static java.lang.Integer.min;
import java.util.ArrayList;
import java.util.List;
import othello.Move;
import othello.Othello;
import othello.Player;
import othello.State;

/**
 *
 * @author Vincent
 */
public class MinMaxTree extends GameTree {
 
    protected GameTreeNode current;
    protected EvaluationFunction evaluator;
    private int timeout;
    private long start_time;
    
    public class AlgorithmTimeout extends RuntimeException {
        public List<Move> partial_result;
        public AlgorithmTimeout(List<Move> moves) {
            partial_result = moves;
        }
    }
    
    public MinMaxTree(Othello game, Player p, EvaluationFunction f)
    {
        super(game, p);
        current = getRoot();
        evaluator = f;
    }
    
    
    private boolean timeoutExpired()
    {
        if(timeout < 0) {
            return false;
        }
        
        if(System.currentTimeMillis() - start_time > timeout) {
            return true;
        }
        
        return false;
    }
    
    private void debug(String str) {
        System.out.println(str);
    }
    
    public List<Move> minmax(GameTreeNode node, int depth, int timeout)
    {
        start_time = System.currentTimeMillis();
        this.timeout = timeout;
        
        // we need to make a copy of the game because when rec_minmax throws 
        // the played move are not cancelled.
        Othello game = new Othello(node.getGame());
        List<Move> moves = new ArrayList<Move>();
        for(int i = 0; i < depth; ++i) {
            moves.add(null);
        }
        int dummy = rec_minmax(game, node, depth, moves, null, null);
        return moves;
    }
    
    public int rec_minmax(Othello game, GameTreeNode node, int depth, List<Move> result, Integer alpha, Integer beta)
    {
        if(timeoutExpired()) {
            throw new AlgorithmTimeout(result);
        }
        
        if(game.getState() == State.GameOver || depth == 0) {
            return evaluator.evaluate(game, this.getPlayer());
        }
        
        
        boolean is_min = isMin(game);
        List<Move> moves = node.getMoves();
        Integer val = null;
        int selected = 0;
        for(int i = 0; i < moves.size(); ++i)
        {
            game.makeMove(moves.get(i));

            // we skip any node that might correspond to a 'pass' situation
            // this type of 'nodes' might lead to a 'game over' situation,
            // which is handled at the begining of the func and will result 
            // in a game tree node having a 'childs' array of size zero.
            while(game.getState() == State.WhitePlayerPass || game.getState() == State.BlackPlayerPass) {
                game.acknowledgePass(game.getState() == State.WhitePlayerPass ? Player.WhitePlayer : Player.BlackPlayer);
            }
            
            if(node.childs[i] == null) {
                node.childs[i] = new GameTreeNode(this, node, i, game.possibleMoves());
            }
            
            if(val == null) {
                val = rec_minmax(game, node.childs[i], depth-1, result, alpha, beta);
            } else {
                int new_val = rec_minmax(game, node.childs[i], depth-1, result, alpha, beta);
                if(is_min) {
                    if(new_val < val) {
                        selected = i;
                        val = new_val;
                    }
                    
                } else {
                    if(new_val > val) {
                        selected = i;
                        val = new_val;
                    }

                }
            }
            
            // once the branch has been visited, we must cancel the move that 
            // was used to reach it
            game.popLastMove();
           
            /*******
            Alpha-Beta
            ********/
            if (is_min && alpha != null) {
                if (alpha >= val) {
                    // we know that this node will have a value less 
                    // than val, but val is alreay too small to be 
                    // considered (not in range [alpha, beta])
                    // -> we can return early with a value that will be ignored
                    return val;
                }
            } else if (beta != null) {
                assert is_min == false;
                if (beta <= val) {
                    // we know that this node will have a value greater 
                    // than val, but val is alreay too big to be 
                    // considered (not in range [alpha, beta])
                    // -> we can return early with a value that will be ignored
                    return val;
                }
            }
        }
        
        // if we reach this point, we know that alpha-beta did not cut back 
        // this branch
        // this means that we currently hold the min or max branch
        // we save the move that lead to the selected branch
        // buggy : moves.set(moves.size() - depth, moves.get(selected));
        result.set(result.size() - depth, moves.get(selected));
        return val;
    }
    
    protected boolean isMin(Othello game)
    {
        if(this.getPlayer() == Player.BlackPlayer)
        {
            if((game.getState() == State.WhitePlayerPass || game.getState() == State.WhitePlayerTurn)) {
               return true; 
            }
            return false;
        }
        
        return (this.getPlayer() == Player.WhitePlayer && 
                (game.getState() == State.BlackPlayerPass || game.getState() == State.BlackPlayerTurn));
    }
    
        
    /**
     * Sets as current node the nodes that corresponds to the current game state
     * @param m the move that led to the current game state
     */
    public void advance(Move m)
    {
        int index = current.moves.indexOf(m);
        if(index < 0) {
            debug("[Warning] advance() failed");
            return;
        }
        Othello game = new Othello(current.getGame());
        game.makeMove(m);
        while(game.getState() == State.WhitePlayerPass || game.getState() == State.BlackPlayerPass) {
            game.acknowledgePass(game.getState() == State.WhitePlayerPass ? Player.WhitePlayer : Player.BlackPlayer);
        }
        if(current.childs[index] == null) {
            current.childs[index] = new GameTreeNode(this, current, index, game.possibleMoves());
        }
        current = current.childs[index];
        current.game = game;
    } 
    
}
