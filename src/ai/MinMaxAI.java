package ai;

import java.util.List;
import java.util.Random;
import othello.Move;
import othello.Othello;
import othello.Player;
import othello.State;
import othello.TokenColor;

/**
 * AI using the minmax algorithm.
 * 
 * @author Vincent
 */
public class MinMaxAI extends AbstractAI {
    public static final String NAME = "minmax";
    
    private int depth;
    private MinMaxTree tree;

    private class SymmetricEvalFunc implements EvaluationFunction
    {
        @Override
        public int evaluate(Othello game, Player p) {
                TokenColor mycolor = (p == Player.WhitePlayer ? TokenColor.WhiteToken : TokenColor.BlackToken);
            Player adv = (p == Player.WhitePlayer ? Player.BlackPlayer : Player.WhitePlayer);
            TokenColor adv_color = (p == Player.WhitePlayer ? TokenColor.BlackToken : TokenColor.WhiteToken);

            int tok_diff = game.getTokenCount(mycolor) - game.getTokenCount(adv_color);
            int corner_count = 0;
            int corners_pos[][] = {{0, 0}, {7, 7}, {7, 0}, {0, 7}};
            for (int i = 0; i < 4; i++) {
                TokenColor corn = game.getToken(corners_pos[i][0], corners_pos[i][1]);
                if (corn == mycolor) {
                    corner_count += 1;
                } else if (corn == adv_color) {
                    corner_count -= 1;
                }
            }

            return tok_diff * game.getTokenCount() + corner_count * 200;
        }
    }

    
    public MinMaxAI(String arg_string)
    {
        super(arg_string);
        
        depth = 3;
        EvaluationFunction eval_func  = new SymmetricEvalFunc(); 
        
        if(arg_string != null && !arg_string.isEmpty()) {
            String[] args = arg_string.split(",");
            for(String a : args) {
                if(a.startsWith("depth=")) {
                    try {
                        depth = Integer.parseInt(a.substring("depth=".length()));
                    } catch(Exception e) {
                        System.err.println("[minmax] Invalid format for 'depth' argument.");
                    }
                } else if(a.startsWith("func=")) {
                    a = a.substring("func=".length());
                    if(a.equals("ssymef")) {
                        eval_func  = new SymmetricEvalFunc(); 
                    } else {
                        System.err.println("[minmax] Invalid argument '" + a + "' for 'func' option.");
                    }
                } else {
                    System.err.println("[minmax] Unknown option '" + a + "'.");
                }
            }
        }
        
        tree = new MinMaxTree(new Othello(), eval_func);
    }
    
    public MinMaxAI(EvaluationFunction f, Player p) {
        super(null);
        depth = 3;
        tree = new MinMaxTree(new Othello(), f);
        tree.player = p;
    }

    public int getDepth()
    {
        return depth;
    }
    
    public void setDepth(int d)
    {
        if(d <= 0) {
            return;
        }
        
        depth = d;
    }
   
    
    @Override
    public void notifyPass()
    {
        // nothing to do
    }
    
    @Override
    public void notifyRewind(int n)
    {
        while(n > 0) {
            tree.current = tree.current.parent;
            n -= 1;
        }
    }
    
    @Override
    public void notifyMove(Move m)
    {
        tree.advance(m);
    }
    
    @Override
    public void notifyLoad(Othello game)
    {
        tree = new MinMaxTree(new Othello(game), tree.evaluator);
    }
    
    
    @Override
    public Move selectMoveWithTimeout(Othello game, List<Move> moves, int timeout) {
        if(moves.size() == 1) {
            return moves.get(0);
        }
        
        if(game.getState() == State.WhitePlayerTurn) {
            tree.player = Player.WhitePlayer;
        } else {
            tree.player = Player.BlackPlayer;
        }
        
        Random r = new Random();
        Move random_move = moves.get(r.nextInt(moves.size()));
        
        List<Move> calculated_moves = null;
        try {
            calculated_moves = tree.minmax(tree.current, depth, timeout);
        } catch(MinMaxTree.AlgorithmTimeout e) {
            calculated_moves = e.partial_result;
        }

        Move ret = calculated_moves.get(0);
        if(ret == null) {
            return random_move;
        }
        assert moves.contains(ret);
        return ret;
        
    }

}
