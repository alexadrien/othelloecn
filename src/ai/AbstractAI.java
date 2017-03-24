package ai;

import java.util.List;

import othello.Othello;
import othello.Move;
import othello.Player;
import static othello.Player.BlackPlayer;
import static othello.Player.WhitePlayer;
import othello.State;

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
    
    public static int[] comp(AbstractAI p1, AbstractAI p2, int n) {
        int[] result = new int[]{0, 0, 0};
        for(int i = 0; i < n; ++i) {
            p1.notifyLoad(new Othello());
            p2.notifyLoad(new Othello());
            Player winner = comp(p1, p2);
            if(winner == BlackPlayer) {
                result[2] += 1;
            } else if(winner == WhitePlayer) {
                result[0] += 1;
            } else {
                result[1] += 1;
            }
        }
        return result;
    }
    
    public static Player comp(AbstractAI white, AbstractAI black) {
        Othello game = new Othello();

        while (game.getState() != State.GameOver) {
            if (game.getState() == State.WhitePlayerPass) {
                game.acknowledgePass(WhitePlayer);
                white.notifyPass();
            } else if (game.getState() == State.BlackPlayerPass) {
                game.acknowledgePass(BlackPlayer);
                black.notifyPass();
            } else if (game.getState() == State.BlackPlayerTurn) {
                Move m = black.selectMove(game, game.possibleMoves());
                game.makeMove(m);
                white.notifyMove(m);
                black.notifyMove(m);
            } else {
                Move m = white.selectMove(game, game.possibleMoves());
                game.makeMove(m);
                white.notifyMove(m);
                black.notifyMove(m);
            }
        }

        return game.getWinner();
    }
    
}

