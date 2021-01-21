package com.packager.filters;

import com.packager.exception.FilterException;
import org.bytedeco.opencv.opencv_core.Mat;
import org.opencv.core.CvType;
import org.opencv.imgproc.Imgproc;

import static org.bytedeco.opencv.global.opencv_imgproc.*;

/**
 * this class could be used to apply a grayscale (black n'white) filter on an image opened as a matrix, using opencv's methods
 */
public class BnWFilter implements IFilter {

    /**
     * this method apply a black and white filter on a selected Matrix
     * @param img                   Mat :  matrix from an image
     * @param size                  int : not used for now
     * @return                      Mat : matrix after modification
     * @throws FilterException      FilterException : throws if img is null
     */
    @Override
    public Mat filter(Mat img, int size) throws FilterException{
        if(img!=null) {
            Mat result = new Mat(img.rows(), img.cols(), CvType.CV_8SC3);
            cvtColor(img, result, Imgproc.COLOR_RGB2GRAY);
            return result;
        }
        else{
            throw new FilterException("the format is not accept, try another one");
        }

    }

    /**
     * this method return a description of this filter
     * @param value     int : not used for now
     * @return          String : containing the description
     */
    @Override
    public String logDescription(int value) {
        return "applying black n'white filter";
    }
}
