/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import static java.lang.Integer.min;
import junit.framework.TestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import othello.Move;
import othello.Othello;
import othello.Player;
import static othello.Player.BlackPlayer;
import static othello.Player.WhitePlayer;
import static othello.State.BlackPlayerPass;
import static othello.State.BlackPlayerTurn;
import static othello.State.GameOver;
import static othello.State.WhitePlayerPass;
import static othello.State.WhitePlayerTurn;
import utils.Point2D;

/**
 *
 * @author Vincent
 */
public class TestMonteCarloAI extends TestCase {
    
    public TestMonteCarloAI(String testName) {
        super(testName);
    }


    @BeforeClass
    public static void setUpClass() {

    }

    @AfterClass
    public static void tearDownClass() {

    }

    @Test
    public void testMonteCarlo() {
        System.out.println("TestMonteCarloAI.testMonteCarlo()");

        Othello game = new Othello();
        MonteCarloAI ai = new MonteCarloAI(null);
        ai.setExplorationCount(64);
        
        while(game.getState() != GameOver) {
            if(game.getState() == WhitePlayerPass) {
                game.acknowledgePass(WhitePlayer);
                ai.notifyPass();
            } else if(game.getState() == BlackPlayerPass) {
                game.acknowledgePass(BlackPlayer);
            } else if(game.getState() == WhitePlayerTurn) {
                Move m = ai.selectMove(game, game.possibleMoves());
                game.makeMove(m);
                ai.notifyMove(m);
            } else if(game.getState() == BlackPlayerTurn) {
                Move m = game.possibleMoves().get(0);
                game.makeMove(m);
                ai.notifyMove(m);
            }
        }
        
        System.out.println("--- test completed ---");
    }
    
    @Test
    public void testRewind() {
        System.out.println("TestMonteCarloAI.testRewind()");

        Othello game = new Othello();
        MonteCarloAI ai = new MonteCarloAI(null);
        ai.setExplorationCount(64);
        
        int plays[] = {10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60};
        int rewind[] = {5, 10, 5, 1, 10, 5, 5, 5, 5, 5, 10};

        for(int i = 0; i < rewind.length; ++i) {
            int played = 0;
            
            while (played < plays[i] && game.getState() != GameOver) {
                if (game.getState() == WhitePlayerPass) {
                    game.acknowledgePass(WhitePlayer);
                    ai.notifyPass();
                } else if (game.getState() == BlackPlayerPass) {
                    game.acknowledgePass(BlackPlayer);
                } else if (game.getState() == WhitePlayerTurn) {
                    Move m = ai.selectMove(game, game.possibleMoves());
                    game.makeMove(m);
                    ai.notifyMove(m);
                } else if (game.getState() == BlackPlayerTurn) {
                    Move m = game.possibleMoves().get(0);
                    game.makeMove(m);
                    ai.notifyMove(m);
                }
                
                played += 1;
            }
            
            game.rewind(min(rewind[i], game.getMoves().size()));
            ai.notifyRewind(min(rewind[i], game.getMoves().size()));
        }
        
        
        System.out.println("--- test completed ---");
    }
    
    static public void play(Othello game, String moves) {
        while (!moves.isEmpty()) {
            String m = moves.substring(0, 3);
            Player p = m.startsWith("+") ? WhitePlayer : BlackPlayer;
            Point2D pos = game.stringToPos(m.substring(1));
            game.makeMove(pos.getX(), pos.getY(), p);
            moves = moves.substring(3);
        }
    }

    @Test
    public void testLoadAndPass() {
        System.out.println("TestMonteCarloAI.testLoadAndPass()");
        
        // the following game leads to a whiteplayerpass state
        // +e3-d3+c2-d2+c5-f4+d1-d6+g4-c1+b1-c3+e2-b4+c4-h4+f5-f1+a4-f6+g5-b5
        // +c6-a3+a2-e1+g1-h6+a5-e6+f3-b2+b6-f2+b3-b7+g3-h2+g7-f8+h8-a6+a8-e7
        // +e8-d7+a7-f7+c7-g2+g6-h3+g8-d8+c8-h1+h5-h7

        Othello game = new Othello();
        
        play(game, "+e3-d3+c2-d2+c5-f4+d1-d6+g4-c1+b1-c3+e2-b4+c4-h4+f5-f1+a4-f6+g5-b5");
        play(game, "+c6-a3+a2-e1+g1-h6+a5-e6+f3-b2+b6-f2+b3-b7+g3-h2+g7-f8+h8-a6+a8-e7");
        play(game, "+e8-d7+a7-f7+c7-g2+g6-h3+g8-d8+c8-h1+h5");
        
        MonteCarloAI ai = new MonteCarloAI(null);
        ai.setExplorationCount(64);
        ai.notifyLoad(game);
        
        game.makeMove(7, 6, BlackPlayer);
        ai.notifyMove(new Move(7, 6, BlackPlayer));
        
        while (game.getState() != GameOver) {
            if (game.getState() == WhitePlayerPass) {
                game.acknowledgePass(WhitePlayer);
                ai.notifyPass();
            } else if (game.getState() == BlackPlayerPass) {
                game.acknowledgePass(BlackPlayer);
            } else if (game.getState() == WhitePlayerTurn) {
                Move m = ai.selectMove(game, game.possibleMoves());
                game.makeMove(m);
                ai.notifyMove(m);
            } else if (game.getState() == BlackPlayerTurn) {
                Move m = game.possibleMoves().get(0);
                game.makeMove(m);
                ai.notifyMove(m);
            }
        }

        
        System.out.println("--- test completed ---");
    }
 
    @Test
    public void testSelectMoveWithTimeout() {
        System.out.println("TestMonteCarloAI.testSelectMoveWithTimeout()");

        Othello game = new Othello();
        MonteCarloAI ai = new MonteCarloAI(null);
        ai.setExplorationCount(64);
        
        int timeout_index = 0;
        int[] timeouts = {1000, 750, 500, 250, 100, 50, 25, 5, 1};
        
        while(game.getState() != GameOver) {
            int timeout = timeouts[timeout_index % timeouts.length];
            timeout_index += 1;
            
            if(game.getState() == WhitePlayerPass) {
                game.acknowledgePass(WhitePlayer);
                ai.notifyPass();
            } else if(game.getState() == BlackPlayerPass) {
                game.acknowledgePass(BlackPlayer);
            } else if(game.getState() == WhitePlayerTurn) {
                Move m = ai.selectMoveWithTimeout(game, game.possibleMoves(), timeout);
                game.makeMove(m);
                ai.notifyMove(m);
            } else if(game.getState() == BlackPlayerTurn) {
                Move m = game.possibleMoves().get(0);
                game.makeMove(m);
                ai.notifyMove(m);
            }
        }

        System.out.println("--- test completed ---");
    }
}
