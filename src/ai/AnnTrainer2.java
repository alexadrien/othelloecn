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
import static othello.State.WhitePlayerTurn;
import othello.TokenColor;
import static othello.TokenColor.BlackToken;
import static othello.TokenColor.WhiteToken;
import utils.Point2D;

/**
 *
 * @author Vincent
 */
public class AnnTrainer2 {
    

    public static class AnnAi extends AbstractAI {

        private Fann neural_network;

        public AnnAi(Fann ann) {
            super("");
            neural_network = ann;
        }
        
        public static AnnAi construct(int[] layers) {
            List<Layer> list_layers = new ArrayList<Layer>();
            list_layers.add(Layer.create(128));
            for(int i = 0; i < layers.length; ++i) {
                list_layers.add(Layer.create(layers[i], ActivationFunction.FANN_SIGMOID_SYMMETRIC));
            }
            list_layers.add(Layer.create(64, ActivationFunction.FANN_SIGMOID_SYMMETRIC));
            Fann ann = new Fann(list_layers);
            return new AnnAi(ann);
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
        
        public static float getProbabilityForMove(float[] ann_output, Move m) {
            Point2D mp = m.getPos();
            return ann_output[mp.getY() * 8 + mp.getX()];
        }
        
        public void test(String testFile) throws FileNotFoundException, IOException {
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
            
            for(int i = 0; i < games.size(); ++i) {
                Othello game = new Othello();
                play(game, games.get(i));
                game.display();
                float[] result = neural_network.run(getBoardAsAnnInput(game, game.getState() == WhitePlayerTurn ? WhiteToken : BlackToken));
                for(int j = 0; j < result.length; ++j) {
                    System.out.print("" + result[j] + " | ");
                }
                System.out.println("");
                List<Move> pm = game.possibleMoves();
                for(Move m : pm) {
                    System.out.println(game.posString(m.getPos()) + " : " + getProbabilityForMove(result, m));
                }
            }
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
        
        public static AnnAi load(String fileName) {
            File f = new File(fileName);
            if(!f.exists()) {
                return null;
            }
            Fann ann = new Fann(fileName);
            return new AnnAi(ann);
        }
        
        public static float[] getOutputForAnnInput(List<Move> moves) {
            float[] ret = new float[64];
            for (int i = 0; i < 64; ++i) {
                ret[i] = 0;
            }
            for (Move m : moves) {
                Point2D mp = m.getPos();
                ret[8 * mp.getY() + mp.getX()] = (float) 1 / (float) moves.size();
            }
            return ret;
        }

        public static float[] getBoardAsAnnInput(Othello game, TokenColor positive) {
            float[] ret = new float[64+64];
            List<Move> moves = game.possibleMoves();
            for(int i = 0; i < 64; ++i) {
                ret[i] = 0;
            }
            for(Move m : moves) {
                Point2D mp = m.getPos();
                ret[8*mp.getY() + mp.getX()] = 1;
            }
            for (int y = 0; y < 8; ++y) {
                for (int x = 0; x < 8; ++x) {
                    if (game.getToken(x, y) == positive) {
                        ret[64+ 8 * y + x] = 1;
                    } else if (game.getToken(x, y) != TokenColor.NoToken) {
                        ret[64+ 8 * y + x] = -1;
                    } else {
                        ret[64+ 8 * y + x] = 0;
                    }
                }
            }
            return ret;
        }
        
        @Override
        public void notifyPass() {
            
        }
        
        @Override
        public void notifyRewind(int n) {
            
        }
        
        @Override
        public void notifyMove(Move m) {
            
        }
        
        @Override
        public void notifyLoad(Othello game) {
            
        }
        
        @Override
        public Move selectMoveWithTimeout(Othello game, List<Move> moves, int timeout) {
            TokenColor tc = game.getState() == WhitePlayerTurn ? WhiteToken : BlackToken;
            float[] result = neural_network.run(getBoardAsAnnInput(game, tc));
            //Random rgen = new Random();
            //float selected_move = rgen.nextFloat();
            Move best_move = moves.get(0);
            float best_prob = getProbabilityForMove(result, best_move);
            for(Move m : moves) {
                float prob = getProbabilityForMove(result, m);
                if(prob > best_prob) {
                    best_prob = prob;
                    best_move = m;
                }
            }
            
            return best_move;
        }
    }
    

    public static void generateTrainingData(String fileName, int n) throws IOException {        
        int[] sample_per_depth = new int[60];
        for(int i = 0; i < 60; ++i) {
            sample_per_depth[i] = n / 60;
        }
        generateTrainingData(fileName, sample_per_depth);
    }
    
    public static void generateTrainingData(String fileName, int[] samples) throws IOException {
        Random rgen = new Random();
        
        Othello game = new Othello();
        GameTree tree = new GameTree(game);
        
        FileWriter file = new FileWriter(fileName);
        BufferedWriter writer = new BufferedWriter(file);
        
        int total_sample = 0;
        for(int i = 0; i < samples.length; ++i) {
            total_sample += samples[i];
        }
        
        writer.write("" + total_sample + " 128 64\n");
        
        for(int d = 0; d < samples.length; ++d) {
            int sample_count = samples[d];
            for(int s = 0; s < sample_count; ++s) {
                GameTreeNode n = getRandomNode(tree, d+1);
                Othello g = n.getGame(true);
                writeAnnInput(writer, g);
                writeAnnOutput(writer, g);
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
    
    public static void writeAnnInput(BufferedWriter w, Othello game) throws IOException {
        float[] in = getBoardAsAnnInput(game, BlackToken);
        for(int i = 0; i < in.length; ++i) {
            w.write(""+in[i]+" ");
        }
        w.write("\n");
    }
    
    public static void writeAnnOutput(BufferedWriter w, Othello game) throws IOException {
        float[] out = getOutputForAnnInput(game.possibleMoves());
        for(int i = 0; i < out.length; ++i) {
            w.write(""+out[i]+" ");
        }
        w.write("\n");
    }
    
    public static float[] getOutputForAnnInput(List<Move> moves) {
        float[] ret = new float[64];
        for (int i = 0; i < 64; ++i) {
            ret[i] = 0;
        }
        for (Move m : moves) {
            Point2D mp = m.getPos();
            ret[8 * mp.getY() + mp.getX()] = (float) 1 / (float) moves.size();
        }
        return ret;
    }

    public static float[] getBoardAsAnnInput(Othello game, TokenColor positive) {
        float[] ret = new float[64 + 64];
        List<Move> moves = game.possibleMoves();
        for (int i = 0; i < 64; ++i) {
            ret[i] = 0;
        }
        for (Move m : moves) {
            Point2D mp = m.getPos();
            ret[8 * mp.getY() + mp.getX()] = 1;
        }
        for (int y = 0; y < 8; ++y) {
            for (int x = 0; x < 8; ++x) {
                if (game.getToken(x, y) == positive) {
                    ret[64 + 8 * y + x] = 1;
                } else if (game.getToken(x, y) != TokenColor.NoToken) {
                    ret[64 + 8 * y + x] = -1;
                } else {
                    ret[64 + 8 * y + x] = 0;
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
        
        for(int d = 0; d < samples.length; ++d) {
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

    public static void main(String[] args) throws IOException {
        Othello oth = new Othello();
        play(oth, "+d6-c4+d3-c2+b4-b5+b3-c6+e2-a4+e3");
        oth.display();
        
        
       File othello_test = new File("othello_play_test");
       if(!othello_test.exists()) {
           generateTestData("othello_play_test", new int[]{4, 8, 16, 32, 64, 128, 10, 10, 10, 10, 10});
       }

        File othello_training = new File("othello_play_training");
        if (!othello_training.exists()) {
            generateTrainingData("othello_play_training", new int[]{4, 8, 16, 32, 64, 128, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100});
        }

       File ann_file = new File("ann_play.net");
       if(!ann_file.exists()) {
           AnnAi ann = AnnAi.construct(new int[]{128});
           ann.train("othello_play_training", 10000, 500, 0.0001f);
           ann.save("ann_play.net");
       }

       AnnAi ann = AnnAi.load("ann_play.net");
       
       ann.test("othello_play_test");
       
       if(true) {
           return;
       }
        
    }
}
