/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package othellopgroup;

/**
 *
 * @author hector
 */
public class TestConstructor {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        
//         TODO code application logic here
        
        Othello game1=new Othello();
        game1.InitPos();
        int x=game1.getboard()[2][5];
        System.out.println(x+"");
           game1.display();
        Othello game2=new Othello(game1);
         game2.getboard()[4][2]=1;
         x=game1.getboard()[4][2];
         System.out.println(x+"");
         game2.display();

//        int[][] test={{1,2},{3,4}};
//        int[][] test2=test.clone();
//        test2[0][0]=8;
//        System.out.println(test[0][0]);
//          

        
    }
    
}
