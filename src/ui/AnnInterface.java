/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui;

import ai.AbstractAI;
import ai.AnnAI;
import ai.AnnAI.EvaluationFunctionF;
import ai.GameTree;
import ai.GameTreeNode;
import com.googlecode.fannj.Fann;
import com.googlecode.fannj.Trainer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import omegathello.Main;
import othello.Move;
import othello.Othello;
import othello.Player;
import static othello.Player.BlackPlayer;
import static othello.Player.WhitePlayer;
import othello.State;
import static othello.State.BlackPlayerPass;
import static othello.State.GameOver;
import static othello.State.WhitePlayerPass;
import othello.TokenColor;
import utils.Point2D;
import utils.Timeout;

/**
 *
 * @author Vincent
 */
public class AnnInterface {
    String[] args;
    
    Othello test_game;
    EvaluationFunctionF test_eval;
    final int InterfaceAskUserInput = 1;
    final int InterfaceExit = 0;
    final int InterfaceContinue = 2;
    
    public AnnInterface(String[] args_)  {
        args = args_;
    }
    
    public int exec() throws IOException {
        if(args.length == 1) {
            System.err.println("[Error] Not enough input arguments");
            return 0;
        }
        
        if(args[1].equals("create")) {
            return create();
        } else if(args[1].equals("delete")) {
            return destroy();
        } else if(args[1].equals("copy")) { 
            return copy();
        } else if(args[1].equals("generate")) {
            return generate();
        } else if(args[1].equals("train")) {
            return train();
        } else if(args[1].equals("mutate")) {
            return mutate();
        } else if(args[1].equals("test")) {
            return test();
        } else {
            System.err.println("[Error] Unknown command '" + args[1] + "'");
        }
        
        return 0;
    }
    
    
    public int create()
    {
        if(args.length <= 2) {
            System.err.println("[Error] Not enough input arguments for 'ann create'");
        }
        
        String name = args[args.length - 1];
        
        int[] layers = new int[]{42};
        
        for(int i = 2; i < args.length - 1; ++i) {
            if(args[i].equals("--layers")) {
                if(i == args.length - 2) {
                    System.err.println("[Error] Missing parameter for option '--layers'");
                    return -1;
                }
                i+=1;
                String layer_string = args[i];
                String[] list_layer = layer_string.split(":");
                layers = new int[list_layer.length];
                for(int l = 0; l < list_layer.length; ++l) {
                    layers[l] = Integer.valueOf(list_layer[l]);
                }
            }
        }
        
        System.out.println("Creating neural network...");
        
        Fann ann = AnnAI.constructNeuralNetwork(layers);

        ensureAnnFolderExists();
        ann.save("ann/" + name + ".net");

        System.out.println("Done !");

        return 0;
    }
    
    public static void ensureAnnFolderExists() {
        File ann_folder = new File("ann");
        if (!ann_folder.exists()) {
            ann_folder.mkdir();
        }
    }

    public int destroy()
    {
        String name = args[2];
        File f = new File("ann/" + name + ".net");
        if(f.exists()) {
            System.out.println("Deleting ann '" + name + "'");
            if(f.delete()) {
                System.out.println("Success");
            } else {
                System.out.println("Failure");
            }
        } else {
            System.out.println("Not such ann '" + name + "'");
        }
        return 0;
    }
    
    public int copy()
    {
        if(args.length != 4) {
            System.err.println("[Error] Invalid argument count for command 'copy'");
            return -1;
        }
        String name = args[2];
        File f = new File("ann/" + name + ".net");
        if(!f.exists()) {
            System.err.println("Not such ann '" + name + "'");
            return -1;
        } 
        
        
        Fann ann = new Fann(f.getAbsolutePath());
        ann.save("ann/" + args[3] + ".net");
        
        return 0;
    }
    
    public int generate() throws IOException
    {
        String training_set_name = args[args.length - 1];
        
        EvaluationFunctionF eval = new TokenCountEvalF();
        GameTree tree = new GameTree(new Othello());
        Random rgen = new Random();
        
        ensureAnnFolderExists();
        FileWriter fwriter = new FileWriter("ann/" + training_set_name + ".train");
        BufferedWriter writer = new BufferedWriter(fwriter);
        
        int total = 0;
                
        for(int arg_index = 2; arg_index < args.length - 1; ++arg_index) {
            String arg = args[arg_index];
            if(arg.equals("--eval")) {
                // do nothing
            } else {
                int num;
                
                if(arg.contains(":")) {
                    num =  Integer.valueOf(arg.split(":")[1]);
                } else {
                    num = Integer.valueOf(arg);
                }
                
                total += num;
            }
        }
        
        writer.write("" + total + " 64 1");
        writer.write(System.lineSeparator());
        
        for(int arg_index = 2; arg_index < args.length - 1; ++arg_index) {
            String arg = args[arg_index];
            if(arg.equals("--eval")) {
                arg_index += 1;
                String eval_func_str = args[arg_index];
                if(eval_func_str.equals("token-count")) {
                    eval = new TokenCountEvalF();
                } else {
                    System.err.println("[Warning] Unknown evaluation function '" + eval_func_str + "', using default");
                }
            } else {
                int num;
                int depth = -1;
                
                if(arg.contains(":")) {
                    depth = Integer.valueOf(arg.split(":")[0]);
                    num =  Integer.valueOf(arg.split(":")[1]);
                } else {
                    num = Integer.valueOf(arg);
                }
                                
                System.out.println("Generating training data...");
                for(int s = 0; s < num; ++s) {
                    GameTreeNode node = getRandomNode(tree, depth == -1 ? rgen.nextInt(60-4) : depth);
                    writeBoard(writer, node.getGame(true));
                    writer.write("" + eval.evaluate(node.getGame(), BlackPlayer));
                    writer.write(System.lineSeparator());
                }
            }
        }
        
        writer.close();
        fwriter.close();
        
        System.out.println("Done !");
        
        return 0;
    }
    
    public int train()
    {
        String taining_data = args[args.length - 1];
        String ann = args[args.length - 2];
        int maxEpochs = 1000;
        int report = 100;
        float desiredError = 0.0001f;
        
        for(int arg_index = 2; arg_index < args.length - 2; ++arg_index) {
            String arg = args[arg_index];
            if(arg.equals("--max-epochs")) {
                arg_index += 1;
                maxEpochs = Integer.valueOf(args[arg_index]);
            } else if(arg.equals("--report")) {
                arg_index += 1;
                report = Integer.valueOf(args[arg_index]);
            } else if(arg.equals("--error")) {
                arg_index += 1;
                desiredError = Float.valueOf(args[arg_index]);
            } else {
                System.err.println("[Error] Unknown option '" + arg + "'");
                return -1;
            }
        }
        
        Fann neural_network = new Fann("ann/" + ann + ".net");
        Trainer trainer = new Trainer(neural_network);
        trainer.train("ann/" + taining_data + ".train", maxEpochs, report, desiredError);
        System.err.println("Saving network.");
        neural_network.save("ann/" + ann + ".net");
        
        return 0;
    }
    
    public int mutate() throws IOException
    {
        int round = 100;
        int epochs = 1000;
        int time = -1;
        Timeout timer = new Timeout();
        String ann_name = args[args.length - 1];
        File ann_file = new File("ann/" + ann_name + ".net");
        if(!ann_file.exists()) {
            System.err.println("[Error] No such ann '" + ann_name + "' found.");
            return -1;
        }
        
        String opponent_name = "random";
        String opponent_opts = "";
        int minmax_depth = 3;
        
        for(int arg_index = 2; arg_index < args.length - 1; ++arg_index) {
            String arg = args[arg_index];
            if(arg.equals("--epochs")) {
                arg_index += 1;
                epochs = Integer.valueOf(args[arg_index]);
            } else if(arg.equals("--time")) {
                arg_index += 1;
                time = Integer.valueOf(args[arg_index]);
            } else if(arg.equals("--round")) {
                arg_index += 1;
                round = Integer.valueOf(args[arg_index]);
            } else if(arg.equals("--ai")) {
                arg_index += 1;
                opponent_name = args[arg_index];
            } else if(arg.equals("--ai-opts")) {
                arg_index += 1;
                opponent_opts = args[arg_index];
            } else if(arg.equals("--depth")) {
                arg_index += 1;
                minmax_depth = Integer.valueOf(args[arg_index]);
            } else {
                System.err.println("[Error] Unknown option '" + arg + "'");
                return -1;
            }
        }
        
        Fann neural_network = new Fann("ann/" + ann_name + ".net");
        
        AbstractAI opponent = Main.instantiateAI(opponent_name, opponent_opts);
        AbstractAI self = new AnnAI(neural_network, minmax_depth);
        
        timer.start(time);
        
        for(int ep = 1; ep <= epochs; ++ep) {
            System.out.println("Epoch " + ep);
            
            Fann neural_network_mutated = neural_network.copy();
            mutateNetwork(neural_network_mutated, 1, 0.1f);
            AbstractAI self_mutated = new AnnAI(neural_network_mutated);
            
            System.out.println("Original individual ");
            int[] match_results = AbstractAI.comp(self, opponent, round / 2);
            System.out.println("" + match_results[0] + " | " + match_results[1] + " | " + match_results[2]);
            int[] rematch_results = AbstractAI.comp(opponent, self, round / 2);
            System.out.println("" + rematch_results[0] + " | " + rematch_results[1] + " | " + rematch_results[2]);
            
            System.out.println("Mutated individual ");
            int[] match_results_m = AbstractAI.comp(self, opponent, round / 2);
            System.out.println("" + match_results_m[0] + " | " + match_results_m[1] + " | " + match_results_m[2]);
            int[] rematch_results_m = AbstractAI.comp(opponent, self, round / 2);
            System.out.println("" + rematch_results_m[0] + " | " + rematch_results_m[1] + " | " + rematch_results_m[2]);

            int score = (match_results[0] + rematch_results[2]) * 2 + (match_results[1]+rematch_results[1]) - 1;
            int score_m = (match_results_m[0] + rematch_results_m[2]) * 2 + (match_results_m[1]+rematch_results_m[1]) - 1;

            if(score_m > score) {
                System.out.println("mutated individual performed better -> becomes original");
                neural_network = neural_network_mutated;
                self = self_mutated;
                
            } else {
                System.out.println("original individual performed better -> mutation discarded");
            }
            
            if(System.in.available() > 0 || timer.expired()) {
                break;
            }
        }
        
        System.out.println("Saving neural network");
        neural_network.save("ann/" + ann_name + ".net");
        
        return 0;
    }
    
    public int test()
    {
        String ann_name = args[args.length - 1];
        File ann_file = new File("ann/" + ann_name + ".net");
        if(!ann_file.exists()) {
            System.err.println("[Error] No such ann '" + ann_name + "' found.");
            return -1;
        }
        Fann ann = new Fann("ann/" + ann_name + ".net");
        test_eval = new AnnAI.AnnEvaluationFunction(ann);
        
        test_game = new Othello();
        Scanner scan = new Scanner(System.in);
        
        for(;;) {
            test_game.display();
            System.out.print("[In] : ");
            String in = scan.nextLine();
            int ret = handleUserInputTest(in);
            while(ret == InterfaceAskUserInput) {
                System.out.print("[In] : ");
                in = scan.nextLine();
                ret = handleUserInputTest(in);
            }
            
            if(ret == InterfaceExit) {
                break;
            }
        }
        
        return 0;
    }
    
    
    /****
     Used by generate()
     *****/
    

    public static class TokenCountEvalF implements AnnAI.EvaluationFunctionF {
        public float evaluate(Othello game, Player p) {
            float black = game.getTokenCount(TokenColor.BlackToken);
            float white = game.getTokenCount(TokenColor.WhiteToken);
            float total = black+white;
            /*
            if(p == Player.BlackPlayer) {
                return (black-white)/total;
            } 
            return (white-black)/total;
            */
            if(p == Player.BlackPlayer) {
                return (black-white)/Math.max(white, black);
            } else {
                return (white-black)/Math.max(white, black);
            }
        }
    }
    
    public static GameTreeNode getRandomNode(GameTree tree, int depth) {
        Random rgen = new Random();
        GameTreeNode current = tree.getRoot();
        while(depth > 0) {
            if(current.childCount() == 0) {
                return current;
            }
            current = current.getChild(rgen.nextInt(current.childCount()));
            depth -= 1;
        }
        return current;
    }
    
    public static void writeBoard(BufferedWriter w, Othello game) throws IOException {
        String str = "";
        for(int y = 0; y < 8; ++y) {
            for(int x = 0; x < 8; ++x) {
                if(game.getToken(x, y) == TokenColor.BlackToken) {
                    str += ""+1+" ";
                } else if(game.getToken(x, y) == TokenColor.WhiteToken) {
                    str += ""+(-1)+" ";
                } else {
                    str += "0 ";
                }
            }
        }
        w.write(str.substring(0, str.length()-1));
        w.write(System.lineSeparator());
    }
    
    
    
        
    /****
     Used by mutate()
     *****/
    
    public void mutateNetwork(Fann neural_network, int count, float amount) {
        List<Fann.Connection> connections = neural_network.getConnections();
        Random rgen = new Random();
        while (count > 0) {
            Fann.Connection c = connections.get(rgen.nextInt(connections.size()));
            if (rgen.nextBoolean()) {
                c.setWeight(c.getWeight() - amount);
            } else {
                c.setWeight(c.getWeight() + amount);
            }
            count--;
        }
    }
    
    
    /****
     Used by test()
     *****/
    
    private int handleUserInputTest(String in) {
        in = in.trim();

        if(in.length() == 0) {
            return InterfaceAskUserInput;
        }
        
        if (in.startsWith("-")) {
            if (in.equals("-exit")) {
                return InterfaceExit;
            } else if (in.equals("-m")) {
                List<Move> pmoves = test_game.possibleMoves();
                for (Move m : pmoves) {
                    System.out.print(test_game.posString(m.getPos()) + " ");
                }
                System.out.println("");
                return InterfaceAskUserInput;
            } else if (in.equals("-d")) {
                test_game.display();
                return InterfaceAskUserInput;
                
            } else if (in.equals("-e")) {
                System.out.println("" + test_eval.evaluate(test_game, BlackPlayer));
                return InterfaceAskUserInput;
            } else if (in.startsWith("-a")) {
                String arg = in.substring("-a".length()).trim();
                int n = Integer.valueOf(arg);
                Random rgen = new Random();
                while(n > 0 && test_game.getState() != GameOver) {
                    List<Move> pmoves = test_game.possibleMoves();
                    test_game.makeMove(pmoves.get(rgen.nextInt(pmoves.size())));
                    autoPass(test_game);
                    n--;
                }
                return InterfaceContinue;
            } else if (in.startsWith("-r")) {
                int n = 1;
                String arg = in.substring("-r".length()).trim();
                if (!arg.isEmpty()) {
                    try {
                        n = Integer.parseInt(arg);
                    } catch (NumberFormatException e) {
                        System.out.print("[Info] Invalid input '" + arg + "' for -r command.");
                        return InterfaceAskUserInput;
                    }
                }
                if (test_game.getMoves().size() < n) {
                    System.out.print("[Info] Not enough move played.");
                    return InterfaceAskUserInput;
                }
                test_game.rewind(n);

                return InterfaceContinue;
            }
        } else {
            Point2D pos = test_game.stringToPos(in);
            List<Move> pmoves = test_game.possibleMoves();
            Move m = null;
            for (int i = 0; i < pmoves.size(); ++i) {
                if (pmoves.get(i).getPos().equals(pos)) {
                    m = pmoves.get(i);
                    break;
                }
            }

            if (m == null) {
                System.out.println("[Error] Provided move is not valid");
                return InterfaceAskUserInput;
            }

            if (test_game.getState() == State.WhitePlayerTurn) {
                test_game.makeMove(m);
                autoPass(test_game);
                return InterfaceContinue;
            } else if (test_game.getState() == State.BlackPlayerTurn) {
                test_game.makeMove(m);
                autoPass(test_game);
                return InterfaceContinue;
            } else {
                System.out.println("[Error] Current player cannot make a move");
                return InterfaceAskUserInput;
            }
            
        }

        System.out.println("Unknown command");

        return InterfaceAskUserInput;
    }
    
    private void autoPass(Othello game) {
        while(game.getState() == WhitePlayerPass || game.getState() == BlackPlayerPass) {
            if(game.getState() == BlackPlayerPass) {
                game.acknowledgePass(WhitePlayer);
            }
            else if(game.getState() == WhitePlayerPass) {
                game.acknowledgePass(BlackPlayer);
            }
        } 
    }


}
