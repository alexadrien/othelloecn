/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package othellopgroup;

import java.util.List;

/**
 *
 * @author hector
 */
public class TestClass2 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws NotYourTurnException, InvalidMove {
        // TODO code application logic here
        
        Othello game1=new Othello();
        game1.InitPos();
        game1.load("poney");
        List<Move> l1=game1.possibleMoves();
        game1.makeMove(l1.get(0));
        l1=game1.possibleMoves();
        game1.makeMove(l1.get(0));
        l1=game1.possibleMoves();
        game1.makeMove(l1.get(0));
        l1=game1.possibleMoves();
        game1.makeMove(l1.get(0));
        game1.display();
        game1.save("poney2");
        
        Othello game2=new Othello();
        game2.load("poney2");
        game2.display();
        
        
        
        
        
        
    }
    
}
