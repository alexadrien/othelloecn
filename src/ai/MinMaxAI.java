package ai;

import java.util.List;
import java.util.Random;
import othello.Move;
import othello.Othello;
import othello.Player;
import othello.TokenColor;

/**
 * AI using the minmax algorithm.
 * 
 * @author Vincent
 */
public class MinMaxAI extends AbstractAI {
    
    private int depth;
    private MinMaxTree tree;
    
    private class SymmetricEvalFunc implements EvaluationFunction
    {
        @Override
        public int evaluate
        (Othello game, Player p
        
            ) {
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

    
    public MinMaxAI(String args)
    {
        super(args);
        
        depth = 3;
        tree = new MinMaxTree(new Othello(), Player.BlackPlayer, new SymmetricEvalFunc());
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
        tree = new MinMaxTree(new Othello(game), Player.BlackPlayer, new SymmetricEvalFunc());
    }
    
    
    @Override
    public Move selectMoveWithTimeout(Othello game, List<Move> moves, int timeout) {
        if(moves.size() == 1) {
            return moves.get(0);
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
