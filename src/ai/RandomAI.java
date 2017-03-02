package ai;

import java.util.List;
import java.util.Random;
import othello.Move;
import othello.Othello;


public class RandomAI extends AbstractAI {
    
    
    public RandomAI(String args)
    {
        super(args);
    }
    
    @Override
    public void notifyPass()
    {
        // nothing to do
    }
    
    @Override
    public void notifyRewind(int n)
    {
        // nothing to do
    }
    
    @Override
    public void notifyMove(Move m)
    {
        // nothing to do
    }
    
    @Override
    public Move selectMoveWithTimeout(Othello game, List<Move> moves, int timeout) {
        if(moves.size() == 1) {
            return moves.get(0);
        }
        
        Random r = new Random();
        return moves.get(r.nextInt(moves.size()));
    }

    
}
