package othello;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import static othello.Player.*;
import static othello.TokenColor.*;
import static othello.State.*;
import utils.Point2D;

public class Othello {

    private int[][] board;
    private State state;
    private List<Move> moves;
    
    private int nbrTokens;
    private List<Move> possibleMovesCache;
    
    public Othello() {
        board = new int[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = 0;
            }
        }
        board[3][3] = 1;
        board[4][4] = 1;
        board[4][3] = -1;
        board[3][4] = -1;

        nbrTokens = 4;
        state = WhitePlayerTurn;
        moves = new ArrayList<Move>();
    }

    public Othello(int[][] boardin, State statein, List<Move> movesin) {
        board = new int[boardin.length][boardin[0].length];
        for (int i = 0; i < boardin.length; i++) {
            for (int j = 0; j < boardin[i].length; j++) {
                board[i][j] = boardin[i][j];
            }
        }
        state = statein;
        moves = new ArrayList<Move>();
        for (int k = 0; k < movesin.size(); k++) {
            moves.add(new Move(movesin.get(k)));
        }
    }

    public Othello(Othello game) {
        this(game.board, game.state, game.moves);
    }

    public int[][] getBoard() {
        return board;
    }
    
    public State getState() {
        return state;
    }
    
    public TokenColor getToken(int x, int y)
    {
        if(board[x][y] == 1) return WhiteToken;
        if(board[x][y] == -1) return BlackToken;
        return NoToken;
    }
    
    
    /**
     * Sets the state, board and list of played move to the given arguments.
     * 
     * This function should only be used for testing purpose.
     * 
     * Calling popLastMove() or rewind() after using this function may produce 
     * invalid results if the list of moves m is not consistent with the current 
     * game state.
     * Additionnaly, saving the game with save() and loading it with load() 
     * will work only if the list of moves is consitent with the game state.
     * It is recommended to pass m as null and not to use any of the 
     * aforementioned functions.
     * @param b
     * @param s
     * @param m list of moves, can be null
     */
    public void setGame(int[][] b, State s, List<Move> m) {
        board = b;
        state = s;
        moves = m;
        if(moves == null) {
            moves = new ArrayList<Move>();
        }
        nbrTokens = 0;
        for(int x = 0; x < 8; ++x) {
            for(int y = 0; y < 8; ++y) {
                if(!isEmpty(x, y)) {
                    nbrTokens += 1;
                }
            }
        }
    }

    public List<Move> getMoves() {
        return moves;
    }
    
    public String posString(Point2D pt) {
        assert inBoard(pt.getX(), pt.getY());
        String[] chars = {"a", "b", "c", "d", "e", "f", "g", "h"};
        return chars[pt.getX()] + (pt.getY()+1);
    }
    
    public Point2D stringToPos(String pos) {
        String[] chars = {"a", "b", "c", "d", "e", "f", "g", "h"};
        String c = pos.substring(0, 1);
        int x = -1;
        for(int i = 0; i < chars.length; ++i) {
            if(c.equals(chars[i])) {
                x = i;
            }
        }
        int y = Integer.valueOf(pos.substring(1, 2));
        return new Point2D(x, y-1);
    }


    public void display() {
        String[] chars = {"a", "b", "c", "d", "e", "f", "g", "h"};
        System.out.print("  ");
        for (int i = 0; i < 8; ++i) {
            System.out.print(chars[i] + " ");
        }
        System.out.println("");
        
        for(int y = 0; y < 8; ++y) {
            
            System.out.print((y+1) + " ");
            
            for(int x = 0; x < 8; ++x) {
                if(board[x][y] == 1) {
                    System.out.print("B");
                } else if(board[x][y] == -1) {
                    System.out.print("N");
                } else {
                    System.out.print(".");
                }
                System.out.print(" ");
            }
            System.out.println("");
        }
    }

    /**
     * Note : cette fonction n'est pas tout-à-fait correct car lorsqu'elle 
     * détecte que les deux joueurs passe elle met directement l'état du jeu 
     * à GameOver sans demander au deuxième joueur de passer.
     * Il faudrait pour être totalement correct introduire un compteur qui 
     * indiquerai le nombre de passe consécutive.
     * @param p 
     */
    public void acknowledgePass(Player p) {
        if(state != BlackPlayerPass && state != WhitePlayerPass) {
            throw new InvalidMoveException("Othello.acknowledgePass() requires getState() == (BlackPlayerPass | WhitePlayerPass]");
        }
        if (state == BlackPlayerPass) 
        {
            if(p != BlackPlayer) {
                throw new InvalidMoveException("Othello.acknowledgePass() requires p == BlackPlayer when getState() == BlackPlayerPass");
            }
            
            List<Move> moves = possibleMoves(WhitePlayer);
            if(moves.isEmpty()) {
                state = GameOver;
            } else {
                state = State.WhitePlayerTurn;
            }
        } 
        else 
        {
            if (p != WhitePlayer) {
                throw new InvalidMoveException("Othello.acknowledgePass() requires p == WhitePlayer when getState() == WhitePlayerPass");
            }

            List<Move> moves = possibleMoves(BlackPlayer);
            if (moves.isEmpty()) {
                state = GameOver;
            } else {
                state = BlackPlayerTurn;
            }
        }
    }

    public int getTokenCount() {
        return nbrTokens;
    }

    public int getTokenCount(TokenColor c) {
        if(c == NoToken) {
            return 64 - nbrTokens;
        }
        
        int NbToken = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (isWhite(i, j) && c == WhiteToken
                        || isBlack(i, j) && c == BlackToken) {
                    NbToken++;
                }
            }
        }
        return NbToken;
    }

    public Player getWinner() throws NotGameOverException {
        if (!state.equals(State.GameOver)) {
            throw new NotGameOverException("Othello.getWinner() requires Othello.getState() == State.GameOver");
        } 
        int pionsBlancs = getTokenCount(WhiteToken);
        int pionsNoirs = getTokenCount(BlackToken);
        if(pionsBlancs > pionsNoirs) {
            return WhitePlayer;
        } else if(pionsNoirs > pionsBlancs) {
            return BlackPlayer;
        }
        
        return NullPlayer;
    }
    
    /**
     * Toggles n tokens next to (x,y) in the given direction.
     * @param n
     * @param x
     * @param y
     * @param dx
     * @param dy 
     */
    private void toggleTokens(int n, int x, int y, int dx, int dy)
    {
        while(n > 0) {
            board[x+n*dx][y+n*dy] = -board[x+n*dx][y+n*dy];
            n -= 1;
        }
    }

    public void makeMove(Move m)
    {
        assert m.getPlayer() != NullPlayer;

        int x = m.getX();
        int y = m.getY();
        
        if(!isEmpty(x, y)) {
            throw new InvalidMoveException("Othello.makeMove() requires board to be empty at given move position.");
        }
        
        TokenColor tok = m.getPlayer() == WhitePlayer ? WhiteToken : BlackToken;
        int[] cap = m.getCapture();
        boolean needUpdate = false;
        int[][] dir = {{0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}};
        for (int i = 0; i < dir.length; i++) {
            if(cap[i] == -1) {
                needUpdate = true;
                cap[i] = getCaptureInDirection(tok, x, y, dir[i][0], dir[i][1]);
            }
            toggleTokens(cap[i], x, y, dir[i][0], dir[i][1]);
        }
        
        board[x][y] = TokenColor2Int(tok);
        nbrTokens += 1;
        
        if(needUpdate) {
            m.setCapture(cap);
        }
        moves.add(m);
        
        if(nbrTokens == 64) {
            state = GameOver;
            return;
        }
       
        Player adv = (m.getPlayer() == WhitePlayer ? BlackPlayer : WhitePlayer);
        List<Move> pmoves = possibleMoves(adv);
        if (pmoves.isEmpty()) {
            state = (adv == WhitePlayer ? WhitePlayerPass : BlackPlayerPass);
        } else {
            state = (adv == WhitePlayer ? WhitePlayerTurn : BlackPlayerTurn);
        }
    }

    public void makeMove(int x, int y, Player p) {
        Move m = new Move(x, y, p);
        makeMove(m);
    }
    
    /**
     * Computes the number of captures in each direction for the given token 
     * color and starting position.
     * @param t
     * @param x
     * @param y
     * @param captures array used to store the result
     * @return true if any capture is actually done ; false otherwise
     */
    private boolean getCapture(TokenColor t, int x, int y, int[] captures) {
        assert isEmpty(x, y);
        
        boolean doCapture = false;
        int[][] dir = {{0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}};
        for(int i = 0; i < dir.length; i++) {
            captures[i] = getCaptureInDirection(t, x, y, dir[i][0], dir[i][1]);
            doCapture = doCapture || captures[i] != 0;
        }
        return doCapture;
    }
    
    private boolean inBoard(int x, int y) {
        return x >= 0 && x < 8 && y >= 0 && y < 8;
    }
    
    private int getCaptureInDirection(TokenColor t, int x, int y, int dx, int dy) {
        int adv_color = (t == WhiteToken ? TokenColor2Int(BlackToken) : TokenColor2Int(WhiteToken));
        int n = 0;
        while(inBoard(x+(n+1)*dx, y+(n+1)*dy) && board[x+(n+1)*dx][y+(n+1)*dy] == adv_color) {
            n += 1;
        }
        
        if(!inBoard(x+(n+1)*dx, y+(n+1)*dy)) {
            return 0;
        }
        
        if(isEmpty(x+(n+1)*dx, y+(n+1)*dy)) {
            return 0;
        }
        
        assert board[x+(n+1)*dx][y+(n+1)*dy] == TokenColor2Int(t);
        return n;
    }

    public List<Move> possibleMoves(Player p) {
        if(possibleMovesCache != null) {
            if(possibleMovesCache.isEmpty()) {
                possibleMovesCache = null;
            } else if(possibleMovesCache.get(0).getPlayer() == p) {
                return possibleMovesCache;
            } else {
                possibleMovesCache = null;
            }
        }
        
        List<Move> listMoves = new ArrayList<Move>();
        int cap[] = new int[8];
        TokenColor token = p == WhitePlayer ? WhiteToken : BlackToken;
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                if(isEmpty(x, y)) {
                    boolean keep = getCapture(token, x, y, cap);
                    if(keep) {
                        listMoves.add(new Move(new Point2D(x, y), p, cap[0],
                        cap[1], cap[2], cap[3], cap[4], cap[5], cap[6], cap[7]));
                    }
                }
            }
        }

        possibleMovesCache = listMoves;
        
        return listMoves;
    }

    public List<Move> possibleMoves() {
        if (state == State.WhitePlayerTurn) {
            return possibleMoves(Player.WhitePlayer);
        } else {
            return possibleMoves(Player.BlackPlayer);
        }
    }

    public boolean save(String filename) {
        String save = "";
        
        save += state.toString() + ":";
        for(Move m : moves) {
            if(m.getPlayer() == WhitePlayer) {
                save += "+";
            } else {
                save += "-";
            }
            save += posString(m.getPos());
        }
        
        FileWriter writer = null;
        try {
            writer = new FileWriter(filename);
        } 
        catch (IOException ex) {
            ex.printStackTrace();
            writer = null;
        } 
        
        if(writer == null) {
            return false;
        }
        
        boolean success = true;
        
        try {
            writer.write(save);
        } catch(IOException ex) {
            ex.printStackTrace();
            success = false;
        }
        
        try {
            writer.close();
        } catch(IOException ex) {
            ex.printStackTrace();
            success = false;
        }
        
        return success;
    }

    public boolean load(String filename) {
        FileReader reader = null;
        String save = "";
        try {
            reader = new FileReader(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            reader = null;
        }
        
        if(reader == null) {
            return false;
        }
        
        boolean success = true;
        
        try {
            char[] buf = new char[1024];
            int read = reader.read(buf, 0, 1024);
            save = new String(buf, 0, read);
        } catch(IOException e) {
            e.printStackTrace();
            success = false;
        }
       
        try {
            reader.close();
        } catch(IOException e) {
            e.printStackTrace();
            success = false;
        }
        
        if(save.equals("") || !success) {
            return false;
        }
        
        String state_str = save.substring(0, save.indexOf(":"));
        State sstate = State.valueOf(state_str);
        
        save = save.substring(state_str.length() + 1);
        
        while(save.length() > 0) {
            String move_str = save.substring(0, 3);
            Point2D pos = stringToPos(move_str.substring(1));
            if(move_str.substring(0, 1).equals("+")) {
                makeMove(pos.getX(), pos.getY(), WhitePlayer);
            } else {
                makeMove(pos.getX(), pos.getY(), BlackPlayer);
            }
            
            save = save.substring(move_str.length());
        }
        
        state = sstate;
        
        return true;
    }

    public void rewind() {
        popLastMove();
    }

    public void rewind(int n) {
        for (int i = 0; i < n; i++) {
            rewind();
        }
    }

    public Move popLastMove() {
        if (moves.isEmpty()) {
            throw new NoMoveException("Othello.popLastMove() requires that at least one move be played");
        }
        Move m = moves.get(moves.size() - 1);
        moves.remove(moves.size() - 1);

        int x = m.getX();
        int y = m.getY();
        TokenColor tok = m.getPlayer() == WhitePlayer ? WhiteToken : BlackToken;
        int[] cap = m.getCapture();
        
      
        int[][] dir = {{0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}};
        for(int i = 0; i < dir.length; i++) {
            toggleTokens(cap[i], x, y, dir[i][0], dir[i][1]);
        }
        
        board[x][y] = TokenColor2Int(NoToken);
        nbrTokens -= 1;
        
        state = (m.getPlayer() == WhitePlayer ? WhitePlayerTurn : BlackPlayerTurn);
        
        return m;
    }
    
    private int TokenColor2Int(TokenColor c) {
        if(c == BlackToken) {
            return -1;
        } else if(c == WhiteToken) {
            return 1;
        }
        return 0;
    }
    
    private TokenColor int2TokenColor(int c) {
        if(c == 1) {
            return WhiteToken;
        } else if(c == -1) {
            return BlackToken;
        }
        return NoToken;
    }
    
    private boolean isWhite(int x, int y) {
        return board[x][y] == 1;
    }
    
    private boolean isBlack(int x, int y) {
        return board[x][y] == -1;
    }
    
    private boolean isEmpty(int x, int y) {
        return board[x][y] == 0;
    }
}
