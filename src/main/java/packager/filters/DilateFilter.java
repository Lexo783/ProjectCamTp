package com.packager.filters;

import com.packager.exception.FilterException;
import org.bytedeco.opencv.opencv_core.*;
import org.opencv.imgproc.Imgproc;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

/**
 * this class could be used to apply a Dilatation on an image opened as a matrix, using opencv's methods
 */
public class DilateFilter implements IFilter {

    /**
     * this method apply a dilating filter on a selected Matrix , and the effect's power given in parameters
     * @param img               Mat :  matrix from an image
     * @param size              int : it represent the effect's power: higher it is, more the dilatation will be important
     * @return                  Mat : matrix after modification
     * @throws FilterException  FilterException : throws if size is negative
     */
    @Override
    public Mat filter(Mat img, int size) throws FilterException {
        if(size>0) {
            Mat element = getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * size + 1, 2 * size + 1));
            dilate(img, img, element);
            return img;
        }
        else{
            throw new FilterException("you don't have enter a size value, please use a positive int");
        }

    }

    /**
     * this method return a description of this filter
     * @param value     int : the effect's power
     * @return          String : containing the description
     */
    @Override
    public String logDescription(int value) {
        return "applying dilate filter, \tvalue: " + value;
    }
}
