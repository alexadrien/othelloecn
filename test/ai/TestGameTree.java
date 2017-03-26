package ai;

import java.util.List;
import junit.framework.TestCase;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import othello.Move;
import othello.Othello;
import static othello.Player.WhitePlayer;
import static othello.Player.BlackPlayer;
import othello.State;
import othello.Player;
import static othello.State.BlackPlayerTurn;
import static othello.State.WhitePlayerTurn;
import static othello.TokenColor.BlackToken;
import static othello.TokenColor.WhiteToken;
import utils.Point2D;

/**
 *
 * @author Vincent
 */
public class TestGameTree extends TestCase {
    
    public TestGameTree(String testName) {
        super(testName);
    }

@BeforeClass
    public static void setUpClass() {
        
    }
    
    @AfterClass
    public static void tearDownClass() 
	{
		
    }
    
    @Test
    public void testGameTree() {
        System.out.println("TestGameTree.testGameTree()");
        
        /*
        Simple game tree
        | e3
            | f3
                | c5
                | d6
                | f4
                | g3
            | d3
                | c2
                | c3
                | c4
                | c5
                | c6
            | f5
                | c6
                | d6
                | e6
                | f6
                | g6
        */

        Othello game =  new Othello();
        GameTree tree = new GameTree(new Othello());
        GameTreeNode root = tree.getRoot();
        assertEquals(game.possibleMoves().size(), root.getMoves().size());
        
        Point2D pos = game.stringToPos("e3");
        game.makeMove(pos.getX(), pos.getY(), WhitePlayer);
        GameTreeNode current = root.getChild(new Move(pos.getX(), pos.getY(), WhitePlayer));
        assertEquals(current.getGame(), game);
        assertEquals(new Move(pos.getX(), pos.getY(), WhitePlayer), current.getMove());
        
        pos = game.stringToPos("f3");
        game.makeMove(pos.getX(), pos.getY(), BlackPlayer);
        current = current.getChild(new Move(pos.getX(), pos.getY(), BlackPlayer));
        assertEquals(current.getGame(), game);
        assertEquals(new Move(pos.getX(), pos.getY(), BlackPlayer), current.getMove());
        
        List<GameTreeNode> cnodes = current.getChilds();
        assertEquals(4, cnodes.size());
        String[] pmoves = {"c5", "d6", "f4", "g3"};
        for(String m : pmoves) {
            pos = game.stringToPos(m);
            boolean found = false;
            for(GameTreeNode n : cnodes) {
                if(n.getMove().equals(new Move(pos.getX(), pos.getY(), WhitePlayer))) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }

        assertTrue(current.getTree() == tree);
        assertEquals(2, current.depth());
        assertEquals(4, tree.height());
        assertEquals(7, tree.nodeCount());
        
        System.out.println("--- test completed ---");
    }
    
    @Test
    public void testReachabilityFromRoot() {
        System.out.println("TestGameTree.testReachabilityFromRoot()");

        GameTree tree = new GameTree(new Othello());
        GameTreeNode root = tree.getRoot();
        
        int mcount = 9;
        List<GameTreeNode> rnodes = root.reachableGames(mcount);
        System.out.println(rnodes.size() + " possible game-states in " + mcount + " moves.");
        int tt_nodecount = tree.nodeCount();
        System.out.println("This represents a " + tt_nodecount + "-node tree.");
        
        assertEquals(tree.height(), mcount + 1);
        
        System.out.println("--- test completed ---");
    }
    
    static public void play(Othello game, String moves) {
        while(!moves.isEmpty()) {
            String m = moves.substring(0, 3);
            Player p = m.startsWith("+") ? WhitePlayer : BlackPlayer;
            Point2D pos = game.stringToPos(m.substring(1));
            game.makeMove(pos.getX(), pos.getY(), p);
            moves = moves.substring(3);
        }
    }
    
    @Test
    public void testReachabilityWithGameOver() {
        System.out.println("TestGameTree.testReachabilityWithGameOver()");
        
        // the following game leads to a early game-over
        // +c5-c6+c7-b7+d6-c4+c3-d7+a8-a7+b8-b5+a6-e6+d8-e7+f7-a5+a4-e8+f5-b4
        // +f4-d3+f8-g4+h4-g8+h8-g6+f6-b6+h6-g5+h5-b3+c2

        Othello game = new Othello();
        play(game, "+c5-c6+c7-b7+d6-c4+c3-d7+a8-a7+b8-b5+a6-e6+d8-e7+f7-a5+a4-e8+f5-b4");
        play(game, "+f4-d3+f8-g4+h4-g8+h8-g6+f6-b6+h6-g5+h5");
        
        GameTree tree = new GameTree(game);
        GameTreeNode root = tree.getRoot();

        int mcount = 6;
        List<GameTreeNode> rnodes = root.reachableGames(mcount);
        System.out.println("Starting from : ");
        game.display();
        System.out.println(rnodes.size() + " possible game-states are reachable in " + mcount + " moves.");
        int tt_nodecount = tree.nodeCount();
        System.out.println("This represents a " + tt_nodecount + "-node tree.");
        
        play(game, "-b3+c2");
        game.acknowledgePass(BlackPlayer);
        boolean case_found = false;
        for(int i = 0; i < rnodes.size(); ++i) {
            if(game.equals(rnodes.get(i).getGame())) {
                case_found = true;
                break;
            }
        }
        assertTrue(case_found);
        

        System.out.println("--- test completed ---");
    }

    @Test
    public void testExplore() {
        System.out.println("TestGameTree.testExplore()");

        GameTree tree = new GameTree(new Othello());
        GameTreeNode root = tree.getRoot();

        GameTreeNode current = root;
        while(!current.isLeaf()) {
            current = current.getChild(0);
        }
        
        current = root;
        while(!current.isLeaf()) {
            current = current.getChild(current.childCount()-1);
            current.getGame(true);
        }
        Othello game = current.getGame();
        assertEquals(State.GameOver, game.getState());
        while(game.getMoves().size() > 0) {
            game.popLastMove();
            current = current.parent;
            assertEquals(current.getGame(false), game);
        }
        
        // 60 moves + root node
        assertEquals(60 + 1, tree.height());

        System.out.println("--- test completed ---");
    }
}
