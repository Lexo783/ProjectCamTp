package com.packager.filters;

import com.packager.exception.FilterException;
import org.bytedeco.opencv.opencv_core.Mat;

/**
 * this is an interface created for the various Filters we have
 */
public interface IFilter {

    /**
     * this method will apply a specific filter on a selected matrix
     * @param img               Mat : matrix from an image
     * @param size              int : it represent the effect's power, for methods that could use it
     * @return                  Mat : matrix after modification
     * @throws FilterException  FilterException : send a message if there is any Exception encounter
     */
    public  Mat filter(Mat img, int size) throws FilterException;

    /**
     * this method return a description of the filter
     * @param value     int : the effect's power
     * @return          String : containing the description
     */
    public String logDescription(int value);


}
