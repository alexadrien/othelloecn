package othellopgroup;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import static othellopgroup.Player.*;
import static othellopgroup.TokenColor.*;
import static othellopgroup.State.*;


/**
 * @author Vincent
 */
public class TestOthello {


	public TestOthello() {
    }
	
	private static void printMoves(List<Move> moves)
	{
		for(Move m : moves)
		{
			m.display();
		}
	}
	
    
    @BeforeClass
    public static void setUpClass() {
        
    }
    
    @AfterClass
    public static void tearDownClass() 
	{
		
    }
    
    @Test
    public void testGame() 
    {
		System.out.println("TestOthello.testGame()");
		
		Othello game = new Othello();
		game.InitPos();
		
		assertTrue(game.getState() == WhitePlayerTurn);
		//assertTrue(game.getToken(3, 3) == White);
		//assertTrue(game.getToken(4, 4) == White);
		//assertTrue(game.getToken(3, 4) == Black);
		//assertTrue(game.getToken(4, 3) == Black);
		
		List<Move> moves = game.possibleMoves(WhitePlayer);
		//assertTrue(moves.contains(new Move(java.awt.Point(4, 2), WhitePlayer)));
		//assertTrue(moves.contains(new Move(java.awt.Point(5, 3), WhitePlayer)));
		//assertTrue(moves.contains(new Move(java.awt.Point(2, 4), WhitePlayer)));
		//assertTrue(moves.contains(new Move(java.awt.Point(3, 5), WhitePlayer)));
		printMoves(moves);
		
		game.makeMove(4, 2, WhitePlayer);
		
		assertTrue(game.getState() == BlackPlayerTurn);
		assertTrue(game.getTokenCount(White) == 4);
		assertTrue(game.getTokenCount(Black) == 1);
		assertTrue(game.getTokenCount() == 5);
		
		//assertTrue(game.getToken(4, 3) == White);
		
		System.out.println("--- test completed ---");
    }
	
	
	@Test
    public void testLoadSave() 
    {
		System.out.println("TestOthello.testLoadSave()");
		
		Othello game = new Othello();
		game.InitPos();
		
		// les 10 premiers coups de la partie valide
		// +d3-c3+c4-e3+c2-b3+d2-e1+d1-c1+f4-d6+e6-g4+b2-f6+d7-c8+e7-d8+f3-f5+g5-h4+h6-f2+f7-h5+g6-c6+c5-c7+b6-b5+b4-e8+h3-g3+h2-e2+h7-g2+b1-a1+h1-a2+a3-g7+f8-h8+g8-a4+g1-f1+a6-a5+a7-a8+b8-b7

		game.makeMove(3, 2, WhitePlayer); // d3
		game.makeMove(2, 2, BlackPlayer); // c3
		game.makeMove(2, 3, WhitePlayer); // c4
		game.makeMove(4, 2, BlackPlayer); // e3
		game.makeMove(2, 1, WhitePlayer); // c2
		game.makeMove(1, 2, BlackPlayer); // b3
		game.makeMove(3, 1, WhitePlayer); // d2
		game.makeMove(4, 0, BlackPlayer); // e1
		game.makeMove(3, 0, WhitePlayer); // d1
		game.makeMove(2, 0, BlackPlayer); // c1
		
		game.save("game-01");
		
		
		Othello lgame = new Othello();
		lgame.load("game-01");
		
		assertTrue(lgame.getState() == game.getState());
		for(int i = 0; i < 8; i++)
		{
			for(int j = 0; j < 8; j++)
			{
				assertEquals(lgame.getboard()[i][j], game.getboard()[i][j]);
			}
		}
		
		System.out.println("--- test completed ---");
    }

	@Test
    public void testRewind() 
    {
		System.out.println("TestOthello.testRewind()");
		
		Othello game = new Othello();
		game.InitPos();
		

		game.makeMove(3, 2, WhitePlayer); // d3
		
		Othello g1 = new Othello(game);
		
		game.makeMove(2, 2, BlackPlayer); // c3
		game.makeMove(2, 3, WhitePlayer); // c4
		game.makeMove(4, 2, BlackPlayer); // e3

		try { // ne devrait pas être nécessaire -> non respect des specs :(
			game.rewind(3);
		} catch (NoMoveException e) { fail(); }
		
		assertTrue(g1.getState() == game.getState());
		for(int i = 0; i < 8; i++)
		{
			for(int j = 0; j < 8; j++)
			{
				assertEquals(g1.getboard()[i][j], game.getboard()[i][j]);
			}
		}
		
		game.makeMove(2, 2, BlackPlayer); // c3
		Move m = game.popLastMove();
		//assertEquals(m, new Move(java.awt.Point(2, 2), BlackPlayer));
		m.display();
		
		System.out.println("--- test completed ---");
    }
	
	@Test
    public void testPassAndGameOver() 
    {
		/*
		* Effectue le test de la partie présentée dans les spécifications : 
		* le joueur blanc passe jsuqu'à la fin et c'est le joueur noir qui gagne.
		* Rem: Cet exemple montre que la strategie visant à avoir le plus de pions 
		* en cours de partie n'est pas forcément la meilleur.
		*/
		
		System.out.println("TestOthello.testPassAndGameOver()");
		
		final int InternalWhite = 1;
		final int InternalBlack = -1;
		final int InternalNoToken = -1;
		
		Othello game = new Othello();
		
		int[][] board = new int[8][8];
		for(int x = 0; x < 8; x++)
		{
			for(int y = 0; y < 8; y++)
			{
				board[x][y] = InternalWhite;
			}
		}
		board[0][0] = InternalNoToken;
		board[7][0] = InternalNoToken;
		board[0][7] = InternalNoToken;
		board[7][7] = InternalNoToken;
		board[3][3] = InternalBlack;

		game.setState(WhitePlayerPass); 
		game.setboard(board);
		
	
		assertEquals(game.getState(), WhitePlayerPass);
		
		game.acknowledgePass(WhitePlayer);
		
		assertEquals(game.getState(), BlackPlayerTurn);

		game.makeMove(0, 0, BlackPlayer);
		
		assertEquals(game.getState(), WhitePlayerPass);
		
		game.acknowledgePass(WhitePlayer);
		
		assertEquals(game.getState(), BlackPlayerTurn);
		
		game.makeMove(7, 7, BlackPlayer);
		
		assertEquals(game.getState(), WhitePlayerPass);
		
		game.acknowledgePass(WhitePlayer);
		
		assertEquals(game.getState(), BlackPlayerTurn);
		
		game.makeMove(7, 0, BlackPlayer);
		
		assertEquals(game.getState(), WhitePlayerPass);
		
		game.acknowledgePass(WhitePlayer);
		
		assertEquals(game.getState(), BlackPlayerTurn);
		
		game.makeMove(0, 7, BlackPlayer);
		
		assertEquals(game.getState(), GameOver);
		
		try { // ne devrait pas être nécessaire
			assertEquals(game.getWinner(), BlackPlayer);
		} catch(NotGameOverException e) { fail(); }
		
		System.out.println("--- test completed ---");
    }
	
}
