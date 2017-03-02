package ai;

import java.util.List;

import othello.Othello;
import othello.Move;

public abstract class AbstractAI {
    private String args;
    
    public AbstractAI(String args_) {
        args = args_;
    }
    
    public String getArgs()
    {
        return args;
    }
    
    /**
     * This function is called by the interface to notify the IA that the 
     * it is about to call Othello.acknownledgePass() on behalf of the IA.
     */
    public abstract void notifyPass();
    
    /**
     * This function is called by the interface to notify the IA that 
     * the Othello.rewind() function was called.
     * This can be useful if the IA maintains an internal state that depends 
     * on the current state of the game.
     * @param n number of moves canceled by the call to rewind().
     */
    public abstract void notifyRewind(int n);
    
    /**
     * This function is called by the interface to notify the IA that the 
     * Othello.makeMove() function was called with the given move as 
     * argument.
     * @param m 
     */
    public abstract void notifyMove(Move m);
    
    public abstract Move selectMoveWithTimeout(Othello game, List<Move> moves, int timeout);
    
    public Move selectMove(Othello game, List<Move> moves)
    {
        return selectMoveWithTimeout(game, moves, -1);
    }
}
