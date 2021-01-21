package com.packager.filters;

import com.packager.exception.FilterException;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;

import static org.bytedeco.opencv.global.opencv_imgproc.*;

/**
 * this class could be used to apply a blur filter on an image opened as a matrix, using opencv's methods
 */
public class BlurFilter implements IFilter {

    /**
     * this method apply a Gaussian blur on a selected Matrix with a power effect's given in parameters
     * @param img               Mat : matrix from an image
     * @param size              int : need to be impair to use this method. it represent the effect's power: higher it is, hazier image will be
     * @return                  Mat : matrix after modification
     * @throws FilterException  FilterException : throws when size isn't impair or negative
     */
    public Mat filter(Mat img, int size) throws FilterException {

        if(size%2==1) {
            GaussianBlur(img, img, new Size(size, size), 0);
        }
        else{
            throw new FilterException("you don't have enter a good size value, please use a positive int ");
        }
        return img;
    }

    /**
     * this method return a description of this filter
     * @param value     int : the effect's power
     * @return          String : containing the description
     */
    @Override
    public String logDescription(int value) {
        return "applying Blur filter, \tvalue: " + value;
    }
}

