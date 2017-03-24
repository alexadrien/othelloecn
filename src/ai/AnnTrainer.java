package ai;

import com.googlecode.fannj.ActivationFunction;
import com.googlecode.fannj.Fann;
import com.googlecode.fannj.Fann.Connection;
import com.googlecode.fannj.Layer;
import com.googlecode.fannj.Trainer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import othello.Move;
import othello.Othello;
import othello.Player;
import static othello.Player.BlackPlayer;
import static othello.Player.WhitePlayer;
import othello.State;
import othello.TokenColor;
import static othello.TokenColor.BlackToken;
import static othello.TokenColor.WhiteToken;
import utils.Point2D;

/**
 *
 * @author Vincent
 */
public class AnnTrainer {
    
    public interface EvaluationFunctionF {
        public float evaluate(Othello game, Player p);

    }
    
    public static class EvaluationFunctionWrapper implements EvaluationFunction {
        private EvaluationFunctionF eval;
        
        public EvaluationFunctionWrapper(EvaluationFunctionF f) {
            eval = f;
        }
        
        public int evaluate(Othello game, Player p) {
            return (int) (eval.evaluate(game, p) * 100);
        }
    }
    
    public static class AnnEvaluationFunction implements EvaluationFunctionF {

        private Fann neural_network;

        public AnnEvaluationFunction(Fann ann) {
            neural_network = ann;
        }
        
        public static AnnEvaluationFunction construct(int[] layers) {
            List<Layer> list_layers = new ArrayList<Layer>();
            list_layers.add(Layer.create(64));
            for(int i = 0; i < layers.length; ++i) {
                list_layers.add(Layer.create(layers[i], ActivationFunction.FANN_SIGMOID_SYMMETRIC));
            }
            list_layers.add(Layer.create(128, ActivationFunction.FANN_SIGMOID_SYMMETRIC));
            list_layers.add(Layer.create(1, ActivationFunction.FANN_SIGMOID_SYMMETRIC));
            Fann ann = new Fann(list_layers);
            return new AnnEvaluationFunction(ann);
        }
        
        public void mutate(int count, float amount) {
            List<Connection> connections = neural_network.getConnections();
            Random rgen = new Random();
            while(count > 0) {
                Connection c = connections.get(rgen.nextInt(connections.size()));
                if(rgen.nextBoolean()) {
                    c.setWeight(c.getWeight() - amount);
                } else {
                    c.setWeight(c.getWeight() + amount);
                }
                count--;
            }
        }
        
        public void train(String trainingFile, int maxEpochs, int epochsBetweenReports, float desiredError) {
            Trainer trainer = new Trainer(neural_network);
            trainer.train(trainingFile, maxEpochs, epochsBetweenReports, desiredError);
        }
        
        public void train(String trainingFile) {
            train(trainingFile, 1000, 100, 0.0001f);
        }
      
        static protected void play(Othello game, String moves) {
            while (!moves.isEmpty()) {
                String m = moves.substring(0, 3);
                Player p = m.startsWith("+") ? WhitePlayer : BlackPlayer;
                Point2D pos = game.stringToPos(m.substring(1));
                game.makeMove(pos.getX(), pos.getY(), p);
                if (game.getState() == State.WhitePlayerPass) {
                    game.acknowledgePass(Player.WhitePlayer);
                } else if (game.getState() == State.BlackPlayerPass) {
                    game.acknowledgePass(Player.BlackPlayer);
                }
                moves = moves.substring(3);
            }
        }
        
        public float[] test(String testFile) throws FileNotFoundException, IOException {
            FileReader freader = new FileReader(testFile);
            BufferedReader reader = new BufferedReader(freader);
            
            List<String> games = new ArrayList<String>();
            String moves = reader.readLine();
            while(moves != null && !moves.isEmpty()) {
                games.add(moves);
                moves = reader.readLine();
            }
            
            reader.close();
            freader.close();
            
            
            float[] result = new float[games.size()];
            for(int i = 0; i < result.length; ++i) {
                Othello game = new Othello();
                play(game, games.get(i));
                result[i] = evaluate(game, BlackPlayer);
            }
            return result;
        } 
        
        public Player testAgainst(AnnEvaluationFunction other) {
            Othello game = new Othello();
            
            MinMaxTree ai1 = new MinMaxTree(game, new EvaluationFunctionWrapper(this));
            ai1.player = WhitePlayer;
            MinMaxTree ai2 = new MinMaxTree(game, new EvaluationFunctionWrapper(other));
            ai2.player = BlackPlayer;
            
            while(game.getState() != State.GameOver) {
                if(game.getState() == State.WhitePlayerPass) {
                    game.acknowledgePass(WhitePlayer);
                } else if(game.getState() == State.BlackPlayerPass) {
                    game.acknowledgePass(BlackPlayer);
                } else if(game.getState() == State.BlackPlayerTurn) {
                    List<Move> moves = ai2.minmax(ai2.current, 3, -1);
                    game.makeMove(moves.get(0));
                    ai1.advance(moves.get(0));
                    ai2.advance(moves.get(0));
                } else {
                    List<Move> moves = ai1.minmax(ai1.current, 3, -1);
                    game.makeMove(moves.get(0));
                    ai1.advance(moves.get(0));
                    ai2.advance(moves.get(0));
                }
            }
            
            return game.getWinner();
        }

        public int[] testAgainst(AnnEvaluationFunction other, int n) {
            int[] result = new int[]{0, 0, 0};
            
            for(int i = 0; i <n ; ++i) {
                Player winner = testAgainst(other);
                if(winner == WhitePlayer) {
                    result[0] += 1;
                } else if(winner == BlackPlayer) {
                    result[2] += 1;
                } else {
                    result[1] += 1;
                }
            }
            
            return result;
        }
        
        
        public static Player playAgainst(AbstractAI white, AbstractAI black) {
            Othello game = new Othello();
            
            while(game.getState() != State.GameOver) {
                if(game.getState() == State.WhitePlayerPass) {
                    game.acknowledgePass(WhitePlayer);
                    white.notifyPass();
                } else if(game.getState() == State.BlackPlayerPass) {
                    game.acknowledgePass(BlackPlayer);
                    black.notifyPass();
                } else if(game.getState() == State.BlackPlayerTurn) {
                    Move m = black.selectMove(game, game.possibleMoves());
                    game.makeMove(m);
                    white.notifyMove(m);
                    black.notifyMove(m);
                } else {
                    Move m = white.selectMove(game, game.possibleMoves());
                    game.makeMove(m);
                    white.notifyMove(m);
                    black.notifyMove(m);
                }
            }
            
            return game.getWinner();
        }

        public static int[] playAgainst(AbstractAI white, AbstractAI black, int n) {
            int[] result = new int[]{0, 0, 0};
            
            for(int i = 0; i <n ; ++i) {
                Othello game = new Othello();
                white.notifyLoad(game);
                black.notifyLoad(game);
                
                Player winner = playAgainst(white, black);
                if(winner == WhitePlayer) {
                    result[0] += 1;
                } else if(winner == BlackPlayer) {
                    result[2] += 1;
                } else {
                    result[1] += 1;
                }
            }
            
            return result;
        }
        
        public void save(String fileName) {
            neural_network.save(fileName);
        }
        
        public static AnnEvaluationFunction load(String fileName) {
            File f = new File(fileName);
            if(!f.exists()) {
                return null;
            }
            Fann ann = new Fann(fileName);
            return new AnnEvaluationFunction(ann);
        }

        public static float[] getBoardAsAnnInput(Othello game, TokenColor positive) {
            float[] ret = new float[64];
            for (int y = 0; y < 8; ++y) {
                for (int x = 0; x < 8; ++x) {
                    if (game.getToken(x, y) == positive) {
                        ret[8 * y + x] = 1;
                    } else if (game.getToken(x, y) != TokenColor.NoToken) {
                        ret[8 * y + x] = -1;
                    } else {
                        ret[8 * y + x] = 0;
                    }
                }
            }
            return ret;
        }
        
        @Override
        public float evaluate(Othello game, Player p) {
            float[] ret = neural_network.run(getBoardAsAnnInput(game, p == BlackPlayer ? BlackToken : WhiteToken));
            return ret[0];
        }
    }
    
    public static class TokenCountEvalF implements EvaluationFunctionF {
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
    
    public static void generateTrainingData(String fileName, EvaluationFunctionF f, int n) throws IOException {        
        int[] sample_per_depth = new int[60];
        for(int i = 0; i < 60; ++i) {
            sample_per_depth[i] = n / 60;
        }
        generateTrainingData(fileName, f, sample_per_depth);
    }
    
    public static void generateTrainingData(String fileName, EvaluationFunctionF f, int[] samples) throws IOException {
        Random rgen = new Random();
        
        Othello game = new Othello();
        GameTree tree = new GameTree(game);
        
        FileWriter file = new FileWriter(fileName);
        BufferedWriter writer = new BufferedWriter(file);
        
        int total_sample = 0;
        for(int i = 0; i < samples.length; ++i) {
            total_sample += samples[i];
        }
        
        writer.write("" + total_sample + " 64 1\n");
        
        for(int d = 0; d < 60; ++d) {
            int sample_count = samples[d];
            for(int s = 0; s < sample_count; ++s) {
                GameTreeNode n = getRandomNode(tree, d+1);
                Othello g = n.getGame(true);
                float eval = f.evaluate(g, Player.BlackPlayer);
                writeBoard(writer, g);
                writer.write("" + eval + "\n");
            }
        }
        
        writer.close();
        file.close();
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
        for(int y = 0; y < 8; ++y) {
            for(int x = 0; x < 8; ++x) {
                if(game.getToken(x, y) == TokenColor.BlackToken) {
                    w.write(""+1+" ");
                } else if(game.getToken(x, y) == TokenColor.WhiteToken) {
                    w.write(""+(-1)+" ");
                } else {
                    w.write("0 ");
                }
            }
        }
        w.write("\n");
    }
    
    public static float[] getBoardAsAnnInput(Othello game) {
        float[] ret = new float[64];
        for(int y = 0; y < 8; ++y) {
            for(int x = 0; x < 8; ++x) {
                if(game.getToken(x, y) == TokenColor.BlackToken) {
                    ret[8*y+x] = 1;
                } else if(game.getToken(x, y) == TokenColor.WhiteToken) {
                    ret[8*y+x] = -1;
                } else {
                    ret[8*y+x] = 0;
                }
            }
        }
        return ret;
    }
    
    
    public static void generateTestData(String fileName, int[] samples) throws IOException {
        Random rgen = new Random();
        
        Othello game = new Othello();
        GameTree tree = new GameTree(game);
        
        FileWriter file = new FileWriter(fileName);
        BufferedWriter writer = new BufferedWriter(file);
        
        for(int d = 0; d < 60; ++d) {
            int sample_count = samples[d];
            for(int s = 0; s < sample_count; ++s) {
                GameTreeNode n = getRandomNode(tree, d+1);
                Othello g = n.getGame(true);
                writeMoves(writer, g);
            }
        }
        
        writer.close();
        file.close();
    }
    
    public static void writeMoves(BufferedWriter w, Othello game) throws IOException {
        w.write(getMoves(game));
        w.write("\n");
    }
    
    public static String getMoves(Othello game) {
        List<Move> moves = game.getMoves();
        String ret = "";
        for(Move m : moves) {
            if(m.getPlayer() == Player.WhitePlayer) {
                ret += "+";
            } else {
                ret += "-";
            }
            ret += game.posString(m.getPos());
        }
        return ret;
    }
    
    static public void play(Othello game, String moves) {
        while(!moves.isEmpty()) {
            String m = moves.substring(0, 3);
            Player p = m.startsWith("+") ? WhitePlayer : BlackPlayer;
            Point2D pos = game.stringToPos(m.substring(1));
            game.makeMove(pos.getX(), pos.getY(), p);
            if(game.getState() == State.WhitePlayerPass) {
                game.acknowledgePass(Player.WhitePlayer);
            } else if(game.getState() == State.BlackPlayerPass) {
                game.acknowledgePass(Player.BlackPlayer);
            }
            moves = moves.substring(3);
        }
    }
    
    public static void generateTestData(String fileName, int n) throws IOException {
        int[] sample_per_depth = new int[60];
        for(int i = 0; i < 60; ++i) {
            sample_per_depth[i] = n / 60;
        }
        generateTestData(fileName, sample_per_depth);
    }
    
    public static float[] test(EvaluationFunctionF eval, String testFile) throws FileNotFoundException, IOException {
        FileReader freader = new FileReader(testFile);
        BufferedReader reader = new BufferedReader(freader);

        List<String> games = new ArrayList<String>();
        String moves = reader.readLine();
        while (moves != null && !moves.isEmpty()) {
            games.add(moves);
            moves = reader.readLine();
        }

        reader.close();
        freader.close();

        float[] result = new float[games.size()];
        for (int i = 0; i < result.length; ++i) {
            Othello game = new Othello();
            play(game, games.get(i));
            result[i] = eval.evaluate(game, BlackPlayer);
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        Othello oth = new Othello();
        play(oth, "+d6-c4+d3-c2+b4-b5+b3-c6+e2-a4+e3");
        oth.display();
        
        
       File othello_test = new File("othello_test");
       if(!othello_test.exists()) {
           generateTestData("othello_test", 2*60);
       }

        File othello_training = new File("othello_training");
        if (!othello_training.exists()) {
            generateTrainingData("othello_training", new TokenCountEvalF(), 1000);
        }

       File ann = new File("ann_42.net");
       if(!ann.exists()) {
           AnnEvaluationFunction ann_eval= AnnEvaluationFunction.construct(new int[]{42});
           ann_eval.train("othello_training");
           ann_eval.save("ann_42.net");
       }
       ann = new File("ann_128.net");
       if(!ann.exists()) {
           AnnEvaluationFunction ann_eval= AnnEvaluationFunction.construct(new int[]{128});
           ann_eval.train("othello_training");
           ann_eval.save("ann_128.net");
       }
       
       AnnEvaluationFunction ann_42 = AnnEvaluationFunction.load("ann_42.net");
       AnnEvaluationFunction ann_128 = AnnEvaluationFunction.load("ann_128.net");
       
       
       float[] set1 = ann_42.test("othello_test");
       float[] set2 = ann_128.test("othello_test");
       float[] set3 = test(new TokenCountEvalF(), "othello_test");
       for(int i = 0; i < set1.length; ++i) {
           System.out.println("" + set1[i] + " | " + set2[i] + " | " + set3[i]);
       }
       
       if(true) {
           return;
       }
       
       int[] res = null;
       
       /*
       res = ann_128.testAgainst(ann_42, 20);
       System.out.println("" + res[0] + " | " + res[1] + " | " + res[2]);
       res = ann_42.testAgainst(ann_128, 20);
       System.out.println("" + res[0] + " | " + res[1] + " | " + res[2]);
       */
       
       System.out.println("------------");
       
        res = AnnEvaluationFunction.playAgainst(new MinMaxAI(new EvaluationFunctionWrapper(ann_42), WhitePlayer), new MinMaxAI(new EvaluationFunctionWrapper(ann_128), BlackPlayer), 1);
        System.out.println("" + res[0] + " | " + res[1] + " | " + res[2]);
        res = AnnEvaluationFunction.playAgainst(new MinMaxAI(new EvaluationFunctionWrapper(ann_128), WhitePlayer), new MinMaxAI(new EvaluationFunctionWrapper(ann_42), BlackPlayer), 1);
        System.out.println("" + res[0] + " | " + res[1] + " | " + res[2]);
        
        
        System.out.println("------------");
       
        res = AnnEvaluationFunction.playAgainst(new RandomAI(""), new MinMaxAI(new EvaluationFunctionWrapper(ann_128), BlackPlayer), 20);
        System.out.println("" + res[0] + " | " + res[1] + " | " + res[2]);
        res = AnnEvaluationFunction.playAgainst(new MinMaxAI(new EvaluationFunctionWrapper(ann_128), WhitePlayer), new RandomAI(""), 20);
        System.out.println("" + res[0] + " | " + res[1] + " | " + res[2]);
        
        System.out.println("------------");
       
        /*
        res = AnnEvaluationFunction.playAgainst(new MinMaxAI(""), new MinMaxAI(new EvaluationFunctionWrapper(ann_128), BlackPlayer), 1);
        System.out.println("" + res[0] + " | " + res[1] + " | " + res[2]);
        res = AnnEvaluationFunction.playAgainst(new MinMaxAI(new EvaluationFunctionWrapper(ann_128), WhitePlayer), new MinMaxAI(""), 1);
        System.out.println("" + res[0] + " | " + res[1] + " | " + res[2]);
        */
        
        
        
        System.out.println("------------");
        
        AnnEvaluationFunction ann_128_m = AnnEvaluationFunction.load("ann_128.net");
        ann_128_m.mutate(1, 0.1f);
       
        res = AnnEvaluationFunction.playAgainst(new MinMaxAI(new EvaluationFunctionWrapper(ann_128_m), WhitePlayer), new MinMaxAI(new EvaluationFunctionWrapper(ann_128), BlackPlayer), 1);
        System.out.println("" + res[0] + " | " + res[1] + " | " + res[2]);
        res = AnnEvaluationFunction.playAgainst(new MinMaxAI(new EvaluationFunctionWrapper(ann_128), WhitePlayer), new MinMaxAI(new EvaluationFunctionWrapper(ann_128_m), BlackPlayer), 1);
        System.out.println("" + res[0] + " | " + res[1] + " | " + res[2]);
        
        /*
        res = AnnEvaluationFunction.playAgainst(new RandomAI(""), new MinMaxAI(new EvaluationFunctionWrapper(ann_128), BlackPlayer), 30);
        System.out.println("" + res[0] + " | " + res[1] + " | " + res[2]);
        res = AnnEvaluationFunction.playAgainst(new MinMaxAI(new EvaluationFunctionWrapper(ann_128), WhitePlayer), new RandomAI(""), 30);
        System.out.println("" + res[0] + " | " + res[1] + " | " + res[2]);
        res = AnnEvaluationFunction.playAgainst(new RandomAI(""), new MinMaxAI(new EvaluationFunctionWrapper(ann_128_m), BlackPlayer), 30);
        System.out.println("" + res[0] + " | " + res[1] + " | " + res[2]);
        res = AnnEvaluationFunction.playAgainst(new MinMaxAI(new EvaluationFunctionWrapper(ann_128_m), WhitePlayer), new RandomAI(""), 30);
        System.out.println("" + res[0] + " | " + res[1] + " | " + res[2]);
        */

        /***********************************************************************/
        
        System.out.println("Starting mutation process...");
        
        final int mut_count = 100;
        for(int i = 0; i < mut_count; ++i) {
            System.out.println("Epoch " + (i+1));
            System.out.println("original individual against random");
            int[] res1 = AnnEvaluationFunction.playAgainst(new MinMaxAI(new EvaluationFunctionWrapper(ann_128), WhitePlayer), new RandomAI(""), 100);
            System.out.println("" + res1[0] + " | " + res1[1] + " | " + res1[2]);
            int[] res2 = AnnEvaluationFunction.playAgainst(new RandomAI(""), new MinMaxAI(new EvaluationFunctionWrapper(ann_128), BlackPlayer), 100);
            System.out.println("" + res2[0] + " | " + res2[1] + " | " + res2[2]);
            int score_original = (res1[0]+res2[2]) * 2 + res1[1] + res2[1];
            System.out.println("original individual scored " + score_original);

            System.out.println("mutated individual against random");
            int[] res3 = AnnEvaluationFunction.playAgainst(new MinMaxAI(new EvaluationFunctionWrapper(ann_128_m), WhitePlayer), new RandomAI(""), 100);
            System.out.println("" + res3[0] + " | " + res3[1] + " | " + res3[2]);
            int[] res4 = AnnEvaluationFunction.playAgainst(new RandomAI(""), new MinMaxAI(new EvaluationFunctionWrapper(ann_128_m), BlackPlayer), 100);
            System.out.println("" + res4[0] + " | " + res4[1] + " | " + res4[2]);
            int score_mutated = (res3[0]+res4[2]) * 2 + res3[1] + res4[1];
            System.out.println("mutated individual scored " + score_mutated);
            
            if(score_mutated > score_original) {
                System.out.println("mutated individual performed better -> becomes original");
                ann_128 = ann_128_m;
            } else {
                System.out.println("original individual performed better -> mutation discarded");
            }

            ann_128_m = new AnnEvaluationFunction(ann_128.neural_network.copy());
            ann_128_m.mutate(1, 0.1f);
            
            if(System.in.available() > 0) {
                break;
            }
        }
        
        ann_128.save("ann_128.net");
        
        /*
        File othello_ai = new File("othello_ai.net");
        if (othello_ai.exists()) {
            Fann ann = new Fann("othello_ai.net");
            
            File othello_test = new File("othello_test");
            if(!othello_test.exists()) {
                generateTestData("othello_test", 60);
            }
            
            EvaluationFunctionF eval = new TokenCountEvalF();
            
            FileReader freader = new FileReader("othello_test");
            BufferedReader reader = new BufferedReader(freader);
            
            String moves = reader.readLine();
            while(moves != null && !moves.isEmpty()) {
                
                Othello game = new Othello();
                play(game, moves);

                float[] ret = ann.run(getBoardAsAnnInput(game));
                game.display();
                System.out.println("Ranked " + ret[0] + " by the network (against " + eval.evaluate(game, BlackPlayer) + ")");
                
                moves = reader.readLine();
            }
            
            reader.close();
            freader.close();
            
        } else {
            File othello_training = new File("othello_training");
            if(!othello_training.exists()) {
                generateTrainingData("othello_training", new TokenCountEvalF(), 10000);                
            }

            List<Layer> layers = new ArrayList<Layer>();
            layers.add(Layer.create(64));
            layers.add(Layer.create(128, ActivationFunction.FANN_SIGMOID_SYMMETRIC));
            layers.add(Layer.create(1, ActivationFunction.FANN_SIGMOID_SYMMETRIC));
            Fann ann = new Fann(layers);

            Trainer trainer = new Trainer(ann);
            trainer.train("othello_training", 1000, 100, 0.001f);
            ann.save("othello_ai.net");
        }
        */
    }
}
