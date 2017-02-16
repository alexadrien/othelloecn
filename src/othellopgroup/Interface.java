/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package othellopgroup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author acube
 */
public class Interface {

    protected int joueurN;
    protected int joueurB;

    public Interface(int joueurN, int joueurB) {
        this.joueurN = joueurN;
        this.joueurB = joueurB;
    }

    public void displayBoard(Othello game) {
        Othello game2 = new Othello(game);
        int[][] board2 = game2.getboard();
        for (int j = 0; j < 8; j++) {
            for (int k = 0; k < 8; k++) {
                if (board2[k][j] == -1) {
                    board2[k][j] = 2;
                }
            }

            System.out.println(board2[0][j] + " "
                    + board2[1][j] + " "
                    + board2[2][j] + " "
                    + board2[3][j] + " "
                    + board2[4][j] + " "
                    + board2[5][j] + " "
                    + board2[6][j] + " "
                    + board2[7][j] + " ");
        }
        System.out.println(game.getState());
        for (int i = 0; i < game.getMoves().size(); i++) {
            game.getMoves().get(i).display();
        }

    }

    public void displayPossibilities(Othello game) {
        List<Move> Moves = game.possibleMoves();
        for (int k = 0; k < Moves.size(); k++) {
            Moves.get(k).display();
        }
    }

    public Move handleUserMove(Othello game) {
        System.out.println("Où voulez-vous poser un pion ? ('pass' pour passer)");
        this.displayPossibilities(game);

        Scanner in = new Scanner(System.in);
        String xstring;
        int x, y;
        Move leMove = null;
        System.out.println("x=");
        do {
            xstring = in.next();
            if (xstring.equals("pass")) {
                leMove = new Move(0, 0, Player.Noone);
                return leMove;
            }
            x = Integer.parseInt(xstring);
        } while (x > 8 || x < 1);
        System.out.println("y=");
        do {
            y = in.nextInt();
        } while (y > 8 || y < 1);

        if (game.getState() == State.BlackPlayerTurn) {
            leMove = new Move(x, y, Player.BlackPlayer);
        } else if (game.getState() == State.WhitePlayerTurn) {
            leMove = new Move(x, y, Player.WhitePlayer);
        }

        return leMove;
    }

    public void displayScores(Othello game) {
        if (game.getState() == State.GameOver) {
            int tokenCountBlack = game.getTokenCount(TokenColor.Black);
            int tokenCountWhite = game.getTokenCount(TokenColor.White);
            if (tokenCountBlack > tokenCountWhite) {
                System.out.println("Le joueur noir gagne ! Avec un total de " + tokenCountBlack);
            } else {
                System.out.println("Le joueur blanc gagne ! Avec un total de " + tokenCountWhite);

            }
        } else {
            System.out.println("Game is not over yet");
        }
    }

    public void play() {
    }

    public void saveGame(Othello game) {
        BufferedWriter bufferedWriter = null;
        Scanner in = new Scanner(System.in);
        System.out.println("Nom de la sauvegarde ?");
        String filename = in.next();
        try {
            // Creation du BufferedWriter
            bufferedWriter = new BufferedWriter(new FileWriter(filename));
            // On ecrit dans le fichier
            bufferedWriter.write(game.getState() + "");
            for (int i = 0; i < game.getMoves().size(); i++) {
                Move m = game.getMoves().get(i);
                bufferedWriter.newLine();
                bufferedWriter.write((int) m.pos.getX() + " " + (int) m.pos.getY() + " " + m.player);
            }

        } // on attrape l'exception si on a pas pu creer le fichier
        catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } // on attrape l'exception si il y a un probleme lors de l'ecriture dans le fichier
        catch (IOException ex) {
            ex.printStackTrace();
        } // on ferme le fichier
        finally {
            try {
                if (bufferedWriter != null) {
                    // je force l'Ã©criture dans le fichier
                    bufferedWriter.flush();
                    // puis je le ferme
                    bufferedWriter.close();
                }
            } // on attrape l'exception potentielle 
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public Othello loadGame(String path) {
        Othello game = new Othello();
        game.InitPos();
        Scanner in = new Scanner(System.in);
        System.out.println("Nom de la sauvegarde ?");
        String source = in.next();

        try {
            String ligne;
            BufferedReader fichier = new BufferedReader(new FileReader(source));
            ligne = fichier.readLine();
            String finalstate = ligne;
            ligne = fichier.readLine();

            while (ligne != null) {
                String[] reader = ligne.split(" ");
                int x = Integer.parseInt(reader[0]);
                int y = Integer.parseInt(reader[1]);
                Player p = Player.valueOf(reader[2]);
                Move m = new Move(x, y, p);
                game.makeMove(m);
                ligne = fichier.readLine();
            }
            game.setState(State.valueOf(finalstate));

            fichier.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return game;
    }

    public void replayGame(String path) {
        Othello game = loadGame(path);
        System.out.println("Voici le replay du jeu");
        for (int i = 0; i < game.getMoves().size(); i++) {
            System.out.println("Coup n°" + i);
            System.out.println("le joueur " + game.getMoves().get(i).player.toString() + " a joué en " + game.getMoves().get(i).pos.toString());
            displayBoard(game);
        }
        System.out.println("Fin du replay");
    }
}
