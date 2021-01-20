package Services;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Matrix {

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
    public Map<Integer, Float> getIndexFromMaxMatrix(float[][] matrix, int elementNb) {
        HashMap<Integer,Float> bestProba =new HashMap<Integer,Float>();//Creating HashMap
        for (float[] floats : matrix) {
            for (int col = 0; col < floats.length; col++) {
                Map.Entry<Integer,Float> minEntry = (bestProba.size()>0 ? Collections.min(bestProba.entrySet(), Map.Entry.comparingByValue()) : null);
                if (bestProba.size() < elementNb || (minEntry !=null && floats[col] > minEntry.getValue() )) {
                    //System.out.println("new max = " + floats[col] + " | old max = " + (minEntry !=null ? minEntry.getValue() : "none"));
                    bestProba.put(col, floats[col]);
                }
                if (minEntry !=null && bestProba.size() > elementNb){
                    bestProba.remove(minEntry.getKey());
                }
            }
        }
        return bestProba;
    }

    public Map<Integer, Float> getIndexFromMaxMatrix(float[][] matrix) {
        return getIndexFromMaxMatrix(matrix, 5);
    }


    /**
     * Get labels with max value in our matrix.
     * Supposed to be use for a matrix with 1 row and N columns.
     * Ex int[1][1000].
     * @param matrix    => a matrix as int [row][col]
     * @param elementNb => number of labels with max value we want to get
     * @return list of index
     */
    public Map<String, Float> getLabelsFromMaxMatrix(float[][] matrix,String[] labels,  int elementNb) {
        HashMap<String,Float> bestProba =new HashMap<String,Float>();//Creating HashMap
        for (float[] floats : matrix) {
            for (int col = 0; col < floats.length; col++) {
                Map.Entry<String,Float> minEntry = (bestProba.size()>0 ? Collections.min(bestProba.entrySet(), Map.Entry.comparingByValue()) : null);
                if (bestProba.size() < elementNb || (minEntry !=null && floats[col] > minEntry.getValue() )) {
                    //System.out.println("new max = " + floats[col] + " | old max = " + (minEntry !=null ? minEntry.getValue() : "none"));
                    bestProba.put(labels[col], floats[col]);
                }
                if (minEntry !=null && bestProba.size() > elementNb){
                    bestProba.remove(minEntry.getKey());
                }
            }
        }
        return bestProba;
    }

    public Map<String, Float> getLabelsFromMaxMatrix(float[][] matrix, String[] labels) {
        return getLabelsFromMaxMatrix(matrix, labels, 5);
    }
}
