package packager.filters;

import packager.exception.FilterException;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;

/**
 * this class could be used to apply a magnificent filter on an image opened as a matrix, by printing a message on it (supposed to be our team's name)
 * thanks to opencv's methods. not usable for now
 */
public class FilterZeTeam implements IFilter {

    /**
     * this method print a message on a selected Matrix with a font size given in parameters
     * @param img               Mat : matrix from an image
     * @param size              int : it represent the font size
     * @return                  Mat : matrix after modification
     * @throws FilterException  : never for now
     */
    public Mat filter(Mat img, int size) throws FilterException {
        System.out.println("Filter the team not usable for now sorry, but maybe soon");
        /*
        Point org = null;
        Scalar scalar = new Scalar(6);
        org.x(0);
        org.y(30);
        opencv_imgproc.putText(img, "Francesco\nMarie\nHoussam\nGwenael", org, 6, size, scalar, 4, 8);
         */
        return img;
    }

    /**
     * this method return a description of this filter
     * @param value     int : the font size
     * @return          String : containing the description
     */
    @Override
    public String logDescription(int value) {
        return "applying a magnificent team filter, \tvalue: ";
    }
}
