package othellopgroup;

import java.util.List;
import java.util.Scanner;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hector
 */
public class TestClass {

    /**
     * @param args 
     */
    
    
    
    
    
    public static void main(String[] args) {
        Othello init=new Othello();
        init.InitPos();
        System.out.println("coucou");
        int i=0;
        while(i<4){
            i++;
            init.display();
            System.out.println("NbrToken"+" "+init.getTokenCount());
            System.out.println("List of moves");
            List<Move> Moves=init.possibleMoves(Player.BlackPlayer);
        for (int k=0;k<Moves.size();k++){
            Moves.get(k).display();
        }
            Scanner in = new Scanner(System.in);
            System.out.println("coup blanc");
            System.out.println("x=");
            int x = in.nextInt();
            System.out.println("y=");
            int y=in.nextInt();
      
            init.makeMove(x,y,Player.WhitePlayer);
        init.display();
        System.out.println("NbrToken"+" "+init.getTokenCount());
        System.out.println("List of moves");
        Moves=init.possibleMoves(Player.BlackPlayer);
        for (int k=0;k<Moves.size();k++){
            Moves.get(k).display();
        }
        
            System.out.println("coup noir");
            System.out.println("x=");
            int x2 = in.nextInt();
            System.out.println("y=");
            int y2=in.nextInt();
      
            init.makeMove(x2,y2,Player.BlackPlayer);

        }
        init.load("poney");
        init.display();
    }
}
