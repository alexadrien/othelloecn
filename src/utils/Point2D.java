/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.util.ArrayList;
import java.util.List;

/**
 * objet représentant la position d'une pierre sur le goban
 * @author thibault
 */
public class Point2D {
    /**
     * x position selon l'axe horizontal du point 2D
     */
    private int x;
    /**
     * y position selon l'axe vertical du point 2D
     */
    private int y;
    
    /**
     * Constructeur de Point2D
     * @param _x position selon l'axe horizontal du point 2D
     * @param _y position selon l'axe vertical du point 2D
     */
    public Point2D(int _x,int _y){
        this.x = _x;
        this.y = _y;
    }

    /**
     * constructeur de copie
     * @param P Point2D à copier
     */
    public Point2D( Point2D P){
        this.x = P.x;
        this.y = P.y;
    }

    /**
     * constructeur par défaut
     * place le Point2D en (0,0)
     */
    public Point2D(){
        this.x = 0;
        this.y = 0;
    }
    
    // getters,setters

    /**
     * setter abscisse
     * @param _x abscisse
     */
    public void setX(int _x){
        this.x = _x;
    }

    /**
     * getter abscisse
     * @return abscisse
     */
    public int getX(){
        return this.x;
    }

    /**
     * setter ordonnée
     * @param _y ordonnée
     */
    public void setY(int _y){
        this.y = _y;
    }

    /**
     * getter ordonnée
     * @return ordonnée
     */
    public int getY(){
        return this.y;
    }

    /**
     * déplace le Point2D sur une autre position
     * @param _x abscisse de la nouvelle position
     * @param _y ordonnée de la nouvelle position
     */
    public void setPosition(int _x,int _y){
        this.x = _x;
        this.y = _y;
    }

    /**
     * déplace le Point2D en ajoutant un déplacement
     * @param _x déplacement en abscisse
     * @param _y déplacement en ordonnée
     */
    public void translate(int _x,int _y){
        this.x += _x;
        this.y += _y;
    }

    /**
     * affiche la position du Point2D
     */
    public void affiche(){
        System.out.println("["+ this.x +";"+ this.y+"]");
    }
    
    @Override
    public String toString()
    {
        return "(" + x + "," + y + ")";
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) {
            return false;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        Point2D pt = (Point2D) obj;
        if(pt == null) {
            return false;
        }
        
        return pt.getX() == x && pt.getY() == y;
    }
    
    /**
     * Renvoie la liste des 4 postions adjancentes au point
     * @return 
     */
    public List<Point2D> getPosAdjacentes()
    {
        List<Point2D> ret = new ArrayList<Point2D>();
        ret.add(new Point2D(x-1, y));
        ret.add(new Point2D(x+1, y));
        ret.add(new Point2D(x, y-1));
        ret.add(new Point2D(x, y+1));
        return ret;
    }
}
