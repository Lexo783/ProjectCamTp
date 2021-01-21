package com.packager.exception;

/**
 * this class is used to centralise Exception we could encounter
 */
public class FilterException extends Exception {

    /**
     * this method is used to print the Exception message
     * @param message   String : the Exception message
     */
    public FilterException(String message){
            super(message);
        }

    /**
     * this method is used to print the Exception message and its cause
     * @param message   String : the Exception message
     * @param cause     Throwable : the cause of the Exception encounter
     */
    public FilterException(String message, Throwable cause){
            super(message, cause);
        }

}