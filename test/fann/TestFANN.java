/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fann;

import com.googlecode.fannj.ActivationFunction;
import com.googlecode.fannj.Fann;
import com.googlecode.fannj.Layer;
import com.googlecode.fannj.Trainer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.junit.Test;

/**
 *
 * @author Vincent
 */
public class TestFANN extends TestCase {
    
    public TestFANN(String testName) {
        super(testName);
    }
    
    public File copyRessourceToTempFile(String ressourceName) throws IOException {
        File temp = File.createTempFile("fann_test_", ".data");
        temp.deleteOnExit();
        InputStream res = this.getClass().getResourceAsStream("xor.data");
        FileOutputStream out = new FileOutputStream(temp);
        byte[] buffer = new byte[1024];
        int read = res.read(buffer, 0, 1024);
        while(read > 0) {
            out.write(buffer, 0, read);
            read = res.read(buffer, 0, 1024);
        }
        return temp;
    }

    //@Test
    public void testFannWorking() throws FileNotFoundException, IOException {
        File data = copyRessourceToTempFile("xor.data");
        
        List<Layer> layers = new ArrayList<Layer>();
        layers.add(Layer.create(2));
        layers.add(Layer.create(3, ActivationFunction.FANN_SIGMOID_SYMMETRIC));
        layers.add(Layer.create(1, ActivationFunction.FANN_SIGMOID_SYMMETRIC));
        Fann fann = new Fann(layers);
        Trainer trainer = new Trainer(fann);
        float desiredError = .001f;
        float mse = trainer.train(data.getAbsolutePath(), 500000, 1000, desiredError);        
        fann.save("fann_test.net");
    }
    
    @Test
    public void testFann() throws FileNotFoundException, IOException {
        
        List<Layer> layers = new ArrayList<Layer>();
        layers.add(Layer.create(2));
        layers.add(Layer.create(3, ActivationFunction.FANN_SIGMOID_SYMMETRIC));
        layers.add(Layer.create(1, ActivationFunction.FANN_SIGMOID_SYMMETRIC));
        Fann fann = new Fann(layers);
              
        System.out.println(fann.getNumInputNeurons());
        System.out.println(fann.getNumOutputNeurons());
        System.out.println(fann.getNumLayers());
        System.out.println(fann.getNumNeurons(0));
        System.out.println(fann.getNumNeurons(1));
        System.out.println(fann.getNumNeurons(2));
        
        fann.printConnections();
        
        List<Fann.Connection> connections = fann.getConnections();
        System.out.println("Connection count : " + connections.size());
        
        float[] ret = fann.run(new float[]{1, 1});
        
        System.out.println("Out-1 : " + ret[0]);
        
        connections.get(0).setWeight(0.9f);
        
        connections = fann.getConnections();
        
        ret = fann.run(new float[]{1, 1});
        
        System.out.println("Out-2 : " + ret[0]);
        
        fann.printConnections();
    }
}
