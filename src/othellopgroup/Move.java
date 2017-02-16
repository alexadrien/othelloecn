package othellopgroup;


import java.awt.Point;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hector
 */
public class Move {
    Point pos;
    Player player;
    int[] capture;
    
    public Move(){
        pos=new Point();
        player=Player.WhitePlayer;
        capture= new int[8];      
    }
    
    public Move(Point P,Player P1, int n, int ne, int e, int se,int s,int sw, int w, int nw){
        pos=P;
        player=P1;
        int[] tableau={n,ne,e,se,s,sw,w,nw};
        capture=tableau;
        
    }
    
    public Move(Move m){
        pos=new Point(m.pos);
        player=m.player;
        capture=m.capture.clone();
    }
    
    public Move(int x,int y,Player p){
        pos=new Point(x,y);
        player=p;
        int[] tableau={-1,-1,-1,-1,-1,-1,-1,-1};
        capture= tableau;
            
    }
    
    public Point getPos(){return pos;}
    public Player getplayer(){return player;}
    public int[] getcapture(){return capture;}
    
    public void setPos(Point p){pos=p;}
    public void setplayer(Player p){player=p;}
    public void setcapture(int[] c){capture=c;}
    
    public void display(){
        System.out.println("("+(int)pos.getX()+","+(int)pos.getY()+", joueur"+player+")");
    }
    
    
}
