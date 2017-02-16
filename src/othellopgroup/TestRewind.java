/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package othellopgroup;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hector
 */
public class TestRewind {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        Othello game=new Othello();
        game.InitPos();
        game.load("poney");
        game.display();
        System.out.println("rewind");
        try {
            game.rewind();
        } catch (NoMoveException ex) {
            Logger.getLogger(TestRewind.class.getName()).log(Level.SEVERE, null, ex);
        }
        game.display();
        List<Move> Moves=game.possibleMoves(Player.WhitePlayer);
        System.out.println("possiblemoves");
        for (int k=0;k<Moves.size();k++){
            Moves.get(k).display();
        }
        System.out.println(game.getTokenCount()-4);
        try {
            game.rewind(7);
        } catch (NoMoveException ex) {
            Logger.getLogger(TestRewind.class.getName()).log(Level.SEVERE, null, ex);
        }
        game.display();

        
        
        
    }
    
}
