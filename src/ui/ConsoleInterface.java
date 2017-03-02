package ui;

import othello.*;
import ai.AbstractAI;
import ai.RandomAI;
import java.util.List;
import java.util.Scanner;
import utils.Point2D;

public class ConsoleInterface {
    
    private Othello game;
    private int mode;
    private AbstractAI ai1;
    private AbstractAI ai2;
    
    private static final int HvM = 0;
    private static final int HvH = 1;
    private static final int MvM = 2;
    
    private static final int InterfaceExit = 0;
    private static final int InterfaceContinue = 1;
    private static final int InterfaceAskUserInput = 2;
    
    public ConsoleInterface(String mode_, String ai1_, String ai1_opts_, String ai2_, String ai2_opts_)
    {
        game = new Othello();
        
        mode = HvM;
        if(mode_.equals("MvM")) {
            mode = MvM;
        } else if(mode_.equals("HvH")) {
            mode = HvH;
        }
        
        if(ai1_.equals("random")) {
            ai1 = new RandomAI(null);
        }
        
        if(ai2_.equals("random")) {
            ai2 = new RandomAI(null);
        }
    }
    
    public void exec()
    {
        Scanner scan = new Scanner(System.in);
        int numPlayer = 1;
        
        for(;;) 
        {
            if(game.getState() == State.GameOver) 
            {
                printGameResult();
                break;
            }
            
            game.display();
            
            if(game.getState() == State.WhitePlayerTurn)
            {
                if(mode == HvM || mode == HvH)
                {
                    numPlayer = 1;
                    List<Move> pmoves = game.possibleMoves(Player.WhitePlayer);
                    String exPos = game.posString(pmoves.get(0).getPos());
                    System.out.println("White player turn, enter a command or a position (eg. " + exPos + ")");
                }
                else
                {
                    Move m = ai1.selectMove(game, game.possibleMoves(Player.WhitePlayer));
                    game.makeMove(m);
                    continue;
                }
            }
            else if(game.getState() == State.WhitePlayerPass)
            {
                if (mode == HvH || mode == HvM) {
                    numPlayer = 1;
                    System.out.println("White player must pass (-p command)");
                } else {
                    ai1.notifyPass();
                    game.acknowledgePass(Player.WhitePlayer);
                    continue;
                }
            }
            else if(game.getState() == State.BlackPlayerTurn)
            {
                if(mode == HvH)
                {
                    numPlayer = 2;
                    List<Move> pmoves = game.possibleMoves(Player.BlackPlayer);
                    String exPos = game.posString(pmoves.get(0).getPos());
                    System.out.println("Black player turn, enter a command or a position (eg. " + exPos + ")");
                }
                else if(mode == HvM)
                {
                    Move m = ai1.selectMove(game, game.possibleMoves(Player.BlackPlayer));
                    game.makeMove(m);
                    continue;
                }
                else if(mode == MvM)
                {
                    Move m = ai2.selectMove(game, game.possibleMoves(Player.BlackPlayer));
                    game.makeMove(m);
                    continue;
                }
            }
            else if(game.getState() == State.BlackPlayerPass)
            {
                if(mode == HvH)
                {
                    numPlayer = 2;
                    System.out.println("Black player must pass (-p command)");
                }
                else if(mode == HvM)
                {
                    ai1.notifyPass();
                    game.acknowledgePass(Player.BlackPlayer);
                    continue;
                }
                else if(mode == MvM)
                {
                    ai2.notifyPass();
                    game.acknowledgePass(Player.BlackPlayer);
                    continue;
                }
            }
            
            System.out.print("[" + numPlayer + "]:");
            int action = handleUserInput(scan.nextLine());
            while(action == InterfaceAskUserInput) {
                System.out.print("[" + numPlayer + "]:");
                action = handleUserInput(scan.nextLine());
            }
            
            if(action == InterfaceExit)
            {
                break;
            }
        }
        
        
    }
    
    private int handleUserInput(String in)
    {
        if(in.startsWith("-")) 
        {
            if(in.equals("-h"))
            {
                printHelp();
                return InterfaceAskUserInput;
            }
            else if(in.startsWith("-s "))
            {
                String savename = in.substring("-s ".length());
                boolean ok = game.save(savename);
                if(ok) {
                    System.out.println("[Info] Game successfully saved");
                } else {
                    System.out.println("[Info] Error while saving game");
                }
                return InterfaceAskUserInput;
            }
            else if(in.startsWith("-l "))
            {
                String savename = in.substring("-l ".length());
                Othello newGame = new Othello();
                boolean ok = newGame.load(savename);
                if(ok) {
                    game = newGame;
                    System.out.println("[Info] Game successfully loaded");
                } else {
                    System.out.println("[Info] Error while loading game");
                }
                return InterfaceContinue;
            }
            else if(in.equals("-p")) 
            {
                if(game.getState() == State.WhitePlayerPass) {
                    game.acknowledgePass(Player.WhitePlayer);
                    return InterfaceContinue;
                } else if(game.getState() == State.BlackPlayerPass) {
                    game.acknowledgePass(Player.BlackPlayer);
                    return InterfaceContinue;
                }
                System.out.println("[Error] Current player cannot pass");
                return InterfaceAskUserInput;
            } 
            else if(in.equals("-exit"))
            {
                return InterfaceExit;
            }
            else if(in.equals("-m"))
            {
                List<Move> pmoves = game.possibleMoves();
                for(Move m : pmoves)
                {
                    System.out.print(game.posString(m.getPos()) + " ");
                }
                System.out.println("");
                return InterfaceAskUserInput;
            }
            else if(in.equals("-d"))
            {
                game.display();
                return InterfaceAskUserInput;
            }
        }
        else
        {
            Point2D pos = game.stringToPos(in);
            List<Move> pmoves = game.possibleMoves();
            Move m = null;
            for(int i = 0; i < pmoves.size(); ++i)
            {
                if(pmoves.get(i).getPos().equals(pos)) {
                    m = pmoves.get(i);
                    break;
                }
            }
            
            if(m == null)
            {
                System.out.println("[Error] Provided move is not valid");
                return InterfaceAskUserInput;
            }
            
            if(game.getState() == State.WhitePlayerTurn) {
                game.makeMove(m);
                return InterfaceContinue;
            } else if(game.getState() == State.BlackPlayerTurn) {
                game.makeMove(m);
                return InterfaceContinue;
            } else {
                System.out.println("[Error] Current player cannot make a move");
                return InterfaceAskUserInput;
            }
        }
        
        System.out.println("Unknown command");
        
        return InterfaceAskUserInput;
    }
    
    private void printGameResult()
    {
        System.out.println("White token count : " + game.getTokenCount(TokenColor.WhiteToken));
        System.out.println("Black token count : " + game.getTokenCount(TokenColor.BlackToken));
        Player winner = game.getWinner();
        if(winner == Player.BlackPlayer) {
            System.out.println("Black player wins !");
        } else if(winner == Player.WhitePlayer) {
            System.out.println("White player wins !");
        } else {
            System.out.println("Draw !");
        }
    }
    
    private void printHelp()
    {
        System.out.println("[Info] Command help");
        System.out.println("[Info] -h : shows this help");
        System.out.println("[Info] -p : pass");
        System.out.println("[Info] -m : displays a list of possible moves");
        System.out.println("[Info] -exit : exits the game");
        System.out.println("[Info] -s <savename> : saves the game");
        System.out.println("[Info] -l <savename> : loads a previously saved game");
    }
    
}
