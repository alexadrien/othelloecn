package ai;

import java.util.List;

import othello.Othello;
import othello.Move;

public abstract class AbstractAI {
    private String args;
    private OStream ostream;
    
    public interface OStream {
        public void write(String str);
    }
    
    public AbstractAI(String args_) {
        args = args_;
        ostream = null;
    }
    
    public String getArgs()
    {
        return args;
    }
        
    /**
     * This function is called by the interface to notify the AI that the 
     * it is about to call Othello.acknownledgePass() on behalf of the AI.
     */
    public abstract void notifyPass();
    
    /**
     * This function is called by the interface to notify the AI that 
     * the Othello.rewind() function was called.
     * This can be useful if the AI maintains an internal state that depends 
     * on the current state of the game.
     * @param n number of moves canceled by the call to rewind().
     */
    public abstract void notifyRewind(int n);
    
    /**
     * This function is called by the interface to notify the AI that the 
     * Othello.makeMove() function was called with the given move as 
     * argument.
     * @param m 
     */
    public abstract void notifyMove(Move m);
    
    /**
     * This function is called by the interface to notify the AI that the given 
     * game was loaded.
     * @param game 
     */
    public abstract void notifyLoad(Othello game);
    
    public abstract Move selectMoveWithTimeout(Othello game, List<Move> moves, int timeout);
    
    public Move selectMove(Othello game, List<Move> moves)
    {
        return selectMoveWithTimeout(game, moves, -1);
    }
    
    public OStream getOStream() {
        return ostream;
    }
    
    public void setOStream(OStream os) {
        ostream = os;
    }
    
    protected void print(String str) {
        if(ostream != null) {
            ostream.write(str);
        }
    }
}
