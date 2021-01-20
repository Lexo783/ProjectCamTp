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
    public Tensor executeModelFromByteArray(Tensor input){
        byte[] graphDef = new byte[0];
        try {
            graphDef = Files.readAllBytes(this.fileTensorflow.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Tensor responseNeural = neuralNetwork.executeModelFromByteArray(graphDef,input);

        System.out.println(responseNeural.numElements());
        System.out.println(responseNeural.numDimensions());

        float[][] copy = new float[1][responseNeural.numElements()];
        responseNeural.copyTo(copy);

        System.out.println("copy success");
        List<Integer> maxIndexList = getIndexFromMaxMatrix(copy);
        HashMap<Integer,Float> bestProba =new HashMap<Integer,Float>();//Creating HashMap
        System.out.println("lists success");

        for (Integer integer : maxIndexList) {
            bestProba.put(integer, copy[0][integer]);
        }
        System.out.println(bestProba);

        return responseNeural;
    }

    public void printMatrix(int[][] matrix) {
        for (int row = 0; row < matrix.length; row++) {
            for (int col = 0; col < matrix[row].length; col++) {
                System.out.printf("%4d", matrix[row][col]);
            }
            System.out.println();
        }
    }
    public void printMatrix(float[][] matrix) {
        for (int row = 0; row < matrix.length; row++) {
            for (int col = 0; col < matrix[row].length; col++) {
                System.out.printf("%.2f", matrix[row][col]);
            }
            System.out.println();
        }
    }

    /**
     * Get element's index with max value in our matrix.
     * Supposed to be use for a matrix with 1 row and N columns.
     * Ex int[1][1000].
     * @param matrix    => a matrix as int [row][col]
     * @param elementNb => number of index with max value we want to get
     * @return list of index
     */
    public List<Integer> getIndexFromMaxMatrix(float[][] matrix, int elementNb) {
        List<Integer> listIndexMax = new ArrayList<Integer>();
        HashMap<Integer,Float> bestProba =new HashMap<Integer,Float>();//Creating HashMap

        for (float[] floats : matrix) {
            for (int col = 0; col < floats.length; col++) {
                Map.Entry<Integer,Float> minEntry = Collections.min(bestProba.entrySet(), Map.Entry.comparingByValue());
                int minFromList= (listIndexMax.size()>0 ? Collections.min(listIndexMax): 0);
                if (listIndexMax.size() <= elementNb || floats[col] > minFromList) {
                    listIndexMax.add(col);
                    System.out.println(floats[col]);
                    bestProba.put(col, floats[col]);
                    bestProba.remove(minEntry.getKey());

                }
                if (listIndexMax.size() > elementNb){
                    listIndexMax.remove(minFromList);
                }
            }
        }
        return listIndexMax;
    }
    public List<Integer> getIndexFromMaxMatrix(float[][] matrix) {
        return getIndexFromMaxMatrix(matrix, 5);
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
