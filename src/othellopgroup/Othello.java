package othellopgroup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author hector
 */
public class Othello {
    private int[][] board;
    State state;
    List<Move> moves;

    public Othello() {
        board = null;
        state = null;
        moves = null;
    }

    public Othello(int[][] boardin, State statein, List<Move> movesin) {
        board = new int[boardin.length][boardin[0].length];
        for (int i = 0; i < boardin.length; i++) {
            for (int j = 0; j < boardin[i].length; j++) {
                board[i][j] = boardin[i][j];
            }
        }
        state = statein;
        moves = new LinkedList();
        for (int k = 0; k < movesin.size(); k++) {
            moves.add(new Move(movesin.get(k)));
        }
    }
    
    public Othello(Othello game) {
        this(game.board,game.state,game.moves);
    }

    public int[][] getboard() {
        return board;
    }

    public void setboard(int[][] boardin) {
        board = boardin;
    }

    public State getState() {
        return state;
    }

    public void setState(State s) {
        state = s;
    }

    public List<Move> getMoves() {
        return moves;
    }

    public void setMoves(List<Move> M) {
        moves = M;
    }

    public void InitPos() {

        Othello init = new Othello();
        int[][] initboard = new int[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                initboard[i][j] = 0;
            }
        }
        initboard[3][3] = 1;
        initboard[4][4] = 1;
        initboard[4][3] = -1;
        initboard[3][4] = -1;

        init.board = initboard;
        init.setState(State.WhitePlayerTurn);
        board = init.board;
        state = init.state;
        moves = new LinkedList();

    }

    public void display() {
        Othello game2 = new Othello(this);
        int[][] board2 = game2.board;
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
        System.out.println(state);
        for (int i = 0; i < moves.size(); i++) {
            moves.get(i).display();
        }
    }

    public void acknowledgePass(Player p) {
        System.out.println(p + "doit passer");
        if (state == State.BlackPlayerTurn) {
            state = State.WhitePlayerTurn;
        } else {
            if (state == State.BlackPlayerTurn) {
                state = State.WhitePlayerTurn;
            }

        }
    }

    public int getTokenCount() {
        int NbToken = moves.size() + 4;
        return NbToken;
    }

    public int getTokenCount(TokenColor C) {
        int NbToken = 0;
        for (int i = 0; i <= 7; i++) {
            for (int j = 0; j <= 7; i++) {
                if (((board[i][j] == 1) && (C.equals(TokenColor.White) || C.equals(TokenColor.None)))
                        || ((board[i][j] == -1) && (C.equals(TokenColor.Black) || C.equals(TokenColor.None)))) {
                    NbToken++;
                }
            }
        }
        return NbToken;
    }

    public Player getWinner() throws NotGameOverException {
        int pionsBlancs = getTokenCount(TokenColor.White);
        int pionsNoirs = getTokenCount(TokenColor.Black);
        if (state.equals(State.GameOver)) {
            if (pionsBlancs > pionsNoirs) {
                return Player.WhitePlayer;
            } else {
                if (pionsBlancs == pionsNoirs) {
                    return Player.Noone;
                } else {
                    return Player.BlackPlayer;
                }
            }
        } else {
            throw new NotGameOverException();
        }
    }

    //TODO:A mettre ne private ou dans les spec
    public void reverseToken(int x, int y) {
        switch (board[x][y]) {
            case 0:
                break;
            case -1:
                board[x][y] = 1;
                break;
            case 1:
                board[x][y] = -1;
                break;
        }
    }

    public void reverseState() {
        if (state == State.BlackPlayerTurn) {
            state = State.WhitePlayerTurn;
        } else {
            if (state == State.WhitePlayerTurn) {
                state = State.BlackPlayerTurn;
            }
        }

    }

    public void makeMove(Move m) throws NotYourTurnException, InvalidMove {
        int indexX;
        int indexY;
        int nbrToken;
        boolean stop;
        int[][] dir = {{0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}};
        for (int i = 0; i < 8; i++) {
            nbrToken = 0;
            indexX = (int) m.pos.getX();
            indexY = (int) m.pos.getY();
            stop = false;
            while (!stop) {
                indexX += dir[i][0];
                indexY += dir[i][1];
                if ((indexX > 7) || (indexY > 7) || (indexX < 0) || (indexY < 0)
                        || board[indexX][indexY] == 0) {
                    stop = true;
                    nbrToken = 0;
                } else {

                    if ((board[indexX][indexY] == 1 && m.player == Player.WhitePlayer)
                            || (board[indexX][indexY] == -1 && m.player == Player.BlackPlayer)) {
                        stop = true;
                    } else {
                        nbrToken++;
                    }
                }
            }
            m.capture[i] = nbrToken;
            for (int k = 0; k < nbrToken; k++) {
                indexX = (int) m.pos.getX() + (k + 1) * (dir[i][0]);
                indexY = (int) m.pos.getY() + (k + 1) * (dir[i][1]);
                reverseToken(indexX, indexY);
            }
        }
        moves.add(m);
        reverseState();
        if (m.player == Player.WhitePlayer) {
            board[(int) m.pos.getX()][(int) m.pos.getY()] = 1;
        } else {
            board[(int) m.pos.getX()][(int) m.pos.getY()] = -1;
        }
    }

    public void makeMove(int x, int y, Player p) {
        Move m = new Move(x, y, p);
        try {
            makeMove(m);
        } catch (NotYourTurnException | InvalidMove ex) {
            Logger.getLogger(Othello.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public List<Move> possibleMoves(Player p) {
        List<Move> listMoves = new LinkedList();
        int indexX;
        int indexY;
        int dircpt;
        int[][] dir = {{0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}};
        boolean stop;
        boolean possible;
        boolean adversaryToken;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] == 0) {
                    dircpt = 0;
                    possible = false;
                    while (!possible && (dircpt < 8)) {
                        indexX = i;
                        indexY = j;
                        adversaryToken = false;
                        stop = false;
                        while (!stop) {
                            indexX += dir[dircpt][0];
                            indexY += dir[dircpt][1];
                            if ((indexX > 7) || (indexY > 7) || (indexX < 0) || (indexY < 0)
                                    || (board[indexX][indexY] == 0)) {
                                stop = true;
                            } else {
                                if (adversaryToken && ((board[indexX][indexY] == 1 && p == Player.WhitePlayer)
                                        || (board[indexX][indexY] == -1 && p == Player.BlackPlayer))) {
                                    stop = true;
                                    possible = true;
                                    listMoves.add(new Move(i, j, p));
                                } else {
                                    adversaryToken = true;
                                }
                            }
                        }
                        dircpt++;
                    }
                }
            }
        }
        if (listMoves.isEmpty()) {
            System.out.println("no movepossible found");
        }
        return listMoves;
    }

    public List<Move> possibleMoves() {
        if (state == State.WhitePlayerTurn) {
            return possibleMoves(Player.WhitePlayer);
        } else {
            return possibleMoves(Player.BlackPlayer);
        }
    }
    
    public void displayPossibleMoves(){
        List<Move> Moves=possibleMoves(Player.BlackPlayer);
        for (int k=0;k<Moves.size();k++){
            Moves.get(k).display();
        }
    }

    public void save(String filename) {
        BufferedWriter bufferedWriter = null;
        try {
            // Creation du BufferedWriter
            bufferedWriter = new BufferedWriter(new FileWriter(filename));
            // On ecrit dans le fichier
            bufferedWriter.write(state + "");
            for (int i = 0; i < moves.size(); i++) {
                Move m = moves.get(i);
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

    public void load(String source) {
        InitPos();
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
                makeMove(m);
                ligne = fichier.readLine();
            }
            state = State.valueOf(finalstate);

            fichier.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void rewind() throws NoMoveException{
        int[][] dir = {{0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}};
        if (moves.isEmpty()){
        throw new NoMoveException();
        }
        Move m = moves.get(moves.size() - 1);
        moves.remove(moves.size() - 1);
        
        int toReverse;
        int indexX;
        int indexY;
        for (int i = 0; i < 8; i++) {
            toReverse = m.capture[i];
            indexX = (int) m.pos.getX();
            indexY = (int) m.pos.getY();
            for (int k = 0; k < toReverse; k++) {
                indexX += dir[i][0];
                indexY += dir[i][1];
                reverseToken(indexX, indexY);
            }
        }
        indexX = (int) m.pos.getX();
        indexY = (int) m.pos.getY();
        board[indexX][indexY] = 0;
        reverseState();
    }
    
    public void rewind(int n) throws NoMoveException{
        for (int i=0;i<n;i++){
           rewind();
        }
    }
    
    public Move popLastMove(){
        Move m=moves.get(moves.size());
        try {
            rewind();
        } catch (NoMoveException ex) {
            Logger.getLogger(Othello.class.getName()).log(Level.SEVERE, null, ex);
        }
        return m;
        
        
    }
}
