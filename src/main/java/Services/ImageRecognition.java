package Services;

import TFUtils.TFUtils;
import org.tensorflow.Tensor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class ImageRecognition {

    /**
     * file             : image upload
     * fileTensorflow   : le tensorflow_inception pour faire marcher l'IA
     * NeuralNetwork    : L'IA
     * byteFile         : les Byte de l'image actuelle
     */
    private File file;
    private File fileTensorflow;
    private NeuralNetwork neuralNetwork;
    private byte[] byteFile = null;

    public ImageRecognition(){
        this.fileTensorflow = new File(getClass().getClassLoader().getResource("inception5h/tensorflow_inception_graph.pb").getFile());
        this.neuralNetwork = new TFUtils();
    }

    public Tensor ConvertByteToTensor(File file){
        try {

            this.byteFile = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        setFile(file);
        return this.neuralNetwork.byteBufferToTensor(byteFile);
    }

    /**
     * graphDef : Prendre le fichier tensorflow_inception_graph.pb
     * @param input Envoie le Tensor de l'image byte[] graphDef
     * @return tensor qui contient une liste de valeur qu'il faut convertir array float
     * https://www.tensorflow.org/api_docs/java/org/tensorflow/Tensor#copyTo(U)
     */
    public float[][] executeModelFromByteArray(Tensor input){
        byte[] graphDef = new byte[0];
        try {
            graphDef = Files.readAllBytes(this.fileTensorflow.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Tensor responseNeural = neuralNetwork.executeModelFromByteArray(graphDef,input);

        float[][] copy = new float[1][responseNeural.numElements()];
        responseNeural.copyTo(copy);
        return copy;
    }

    public String[] getLabels(){
        try {
            String fileContents = new Scanner(new File(getClass().getClassLoader().getResource("inception5h/labels.txt").getFile())).useDelimiter("\\Z").next();
            return fileContents.split("\n");
        }
        catch (Exception e){
            System.out.println(e);
        }
        return null;
    }

    public String getImagePotentialLabel(Map<Integer, Float> map){
        Map.Entry<Integer,Float> maxEntry = Collections.max(map.entrySet(), Map.Entry.comparingByValue());
        return getImagePotentialLabel(map, maxEntry.getKey());
    }
    public String getImagePotentialLabel(Map<Integer, Float> map, int index){
        return this.getLabels()[index];
    }

    /*
    public void copyTo()
    {
        int matrix[2][2] = { {1,2,{3,4} };
            try(Tensor t = Tensor.create(matrix))
            {
                // Succeeds and prints "3"
                int[][] copy = new int[2][2];
                System.out.println(t.copyTo(copy)[1][0]);

                // Throws IllegalArgumentException since the shape of dst does not match the shape of t.
                int[][] dst = new int[4][1];
                t.copyTo(dst);
            }
        }
    }*/

    public File getFile() {
        return file;
    }

    private void setFile(File file) {
        this.file = file;
    }
}
