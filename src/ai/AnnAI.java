/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import com.googlecode.fannj.ActivationFunction;
import com.googlecode.fannj.Fann;
import com.googlecode.fannj.Layer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import othello.Move;
import othello.Othello;
import othello.Player;
import static othello.Player.BlackPlayer;
import static othello.Player.NullPlayer;
import othello.TokenColor;
import static othello.TokenColor.BlackToken;
import static othello.TokenColor.WhiteToken;

/**
 *
 * @author Vincent
 */
public class AnnAI extends AbstractAI {
    public static final String NAME = "ann";
    
    private MinMaxAI impl;
    private Fann neural_network;

    public interface EvaluationFunctionF {
        public float evaluate(Othello game, Player p);
    }
    
    
    public static class AnnEvaluationFunction implements EvaluationFunctionF {

        private Fann neural_network;

        public AnnEvaluationFunction(Fann ann) {
            neural_network = ann;
        }
        
        @Override
        public float evaluate(Othello game, Player p) {
            float[] ret = neural_network.run(AnnAI.getBoardAsAnnInput(game, p == BlackPlayer ? BlackToken : WhiteToken));
            return ret[0];
        }
    }
    
    
    public static class EvaluationFunctionWrapper implements EvaluationFunction {
        private EvaluationFunctionF eval;
        
        public EvaluationFunctionWrapper(EvaluationFunctionF f) {
            eval = f;
        }
        
        public int evaluate(Othello game, Player p) {
            return (int) (eval.evaluate(game, p) * 1000);
        }
    }
    
    public static Fann constructNeuralNetwork(int[] layers) {
        List<Layer> list_layers = new ArrayList<Layer>();
        list_layers.add(Layer.create(64));
        for (int i = 0; i < layers.length; ++i) {
            list_layers.add(Layer.create(layers[i], ActivationFunction.FANN_SIGMOID_SYMMETRIC));
        }
        list_layers.add(Layer.create(1, ActivationFunction.FANN_SIGMOID_SYMMETRIC));
        Fann ann = new Fann(list_layers);
        return ann;
    }
    
    
    public static float[] getBoardAsAnnInput(Othello game, TokenColor positive) {
        float[] ret = new float[64];
        for(int y = 0; y < 8; ++y) {
            for(int x = 0; x < 8; ++x) {
                if(game.getToken(x, y) == positive) {
                    ret[8*y+x] = 1;
                } else if(game.getToken(x, y) == TokenColor.NoToken) {
                    ret[8*y+x] = 0;
                } else {
                    ret[8*y+x] = -1;
                }
            }
        }
        return ret;
    }
    
    
    public AnnAI(String arg_string) throws IOException
    {
        super(arg_string);
        File ann_file = copyRessourceToTempFile("ann_128.net");
        ann_file.deleteOnExit();
        
        int depth = 3;
        
        if (arg_string != null) {
            String[] args = arg_string.split(",");
            for (String a : args) {
                if (a.startsWith("depth=")) {
                    try {
                        depth = Integer.parseInt(a.substring("depth=".length()));
                    } catch (Exception e) {
                        System.err.println("[ann] Invalid format for 'depth' argument.");
                    }
                } else if (a.startsWith("ann=")) {
                    a = a.substring("ann=".length());
                    if (a.equals("ann_128")) {
                        // nothing to do
                    } else {
                        File provided_ann = new File("ann/" + a + ".net");
                        if(provided_ann.exists()) {
                            ann_file = provided_ann;
                        } else {
                            System.err.println("[ann] No such ann '" + a + "'.");
                        }
                    }
                } else {
                    System.err.println("[ann] Unknown option '" + a + "'.");
                }
            }
        }
        
        neural_network = new Fann(ann_file.getAbsolutePath());
        impl = new MinMaxAI(new EvaluationFunctionWrapper(new AnnEvaluationFunction(neural_network)), NullPlayer);
        impl.setDepth(depth);
        
    }
    
    public AnnAI(Fann ann)
    {
        super(null);
        neural_network = ann;
        impl = new MinMaxAI(new EvaluationFunctionWrapper(new AnnEvaluationFunction(neural_network)), NullPlayer);
    }
    
    public AnnAI(Fann ann, int depth)
    {
        super(null);
        neural_network = ann;
        impl = new MinMaxAI(new EvaluationFunctionWrapper(new AnnEvaluationFunction(neural_network)), NullPlayer);
        impl.setDepth(depth);
    }

    
    @Override
    public void notifyPass()
    {
        impl.notifyPass();
    }
    
    @Override
    public void notifyRewind(int n)
    {
        impl.notifyRewind(n);
    }
    
    @Override
    public void notifyMove(Move m)
    {
        impl.notifyMove(m);
    }
    
    @Override
    public void notifyLoad(Othello game)
    {
        impl.notifyLoad(game);
    }
    
    @Override
    public Move selectMoveWithTimeout(Othello game, List<Move> moves, int timeout) {
        if(moves.size() == 1) {
            return moves.get(0);
        }
        
        return impl.selectMoveWithTimeout(game, moves, timeout);
    }

    
    public File copyRessourceToTempFile(String ressourceName) throws IOException {
        File temp = File.createTempFile("ann_128", ".data");
        temp.deleteOnExit();
        InputStream res = this.getClass().getResourceAsStream(ressourceName);
        FileOutputStream out = new FileOutputStream(temp);
        byte[] buffer = new byte[1024];
        int read = res.read(buffer, 0, 1024);
        while(read > 0) {
            out.write(buffer, 0, read);
            read = res.read(buffer, 0, 1024);
        }
        return temp;
    }
    
}
