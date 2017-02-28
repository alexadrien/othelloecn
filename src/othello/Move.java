package othello;


import utils.Point2D;

public class Move {
    private final Point2D pos;
    private final Player player;
    private int[] capture;
    
    
    public Move(Point2D P,Player P1, int n, int ne, int e, int se,int s,int sw, int w, int nw){
        pos = P;
        player = P1;
        int[] tableau = {n,ne,e,se,s,sw,w,nw};
        capture = tableau;
    }
    
    public Move(Move m){
        pos = new Point2D(m.pos);
        player = m.player;
        capture = m.capture.clone();
    }
    
    public Move(int x,int y,Player p){
        pos = new Point2D(x,y);
        player = p;
        int[] tableau = {-1,-1,-1,-1,-1,-1,-1,-1};
        capture = tableau;
    }
    
    public Point2D getPos(){return pos;}
    public int getX(){return pos.getX();}
    public int getY(){return pos.getY();}
    public Player getPlayer(){return player;}
    public int[] getCapture(){return capture;}
    
    public void setCapture(int[] c){capture=c;}
    
    public void display(){
        System.out.println("("+pos.getX()+","+pos.getY()+", joueur"+player+")");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        Move m = (Move) obj;
        if(m == null) return false;
        return m.getPlayer() == player && m.getPos().equals(pos);
    }
    
    
}
