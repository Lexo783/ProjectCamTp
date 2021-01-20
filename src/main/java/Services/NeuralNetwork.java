package Services;

import org.tensorflow.Tensor;

public interface NeuralNetwork {

    public Tensor executeSavedModel(String modelFolderPath, Tensor input);

    public Tensor executeModelFromByteArray(byte[] graphDef, Tensor input);

    public Tensor byteBufferToTensor(byte[] imageBytes);
}
