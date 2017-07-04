import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.ST;
import edu.princeton.cs.algs4.Picture;
import java.util.*;
import java.awt.Color;
public class SeamCarver {
    
    private Picture picture;
    private final Picture copy;
    private int width;
    private int height;
    private ST<Integer, List<Integer>> st;
    private double[][] energy_matrix;
    private double[][] tr_energy_matrix;
    
    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null)
            throw new NullPointerException();
        this.picture = picture;
        this.width = picture.width();
        this.height = picture.height();
       
        copy = picture;
        //copy = picture.clone();
    }
    
    public Picture clone() {
        try {
            return ((Picture)super.clone());
        }
        catch (CloneNotSupportedException e) {
            throw new RuntimeException();
        }
    }
    
    // current picture
    public Picture picture() {
        return picture;
    }

    // width of current picture
    public     int width() {
        return width;
    }
    
    // height of current picture
    public     int height() {
        return height;
    }
    // energy of pixel at column x and row y
    public  double energy(int x, int y) {
        return Math.pow(compute(x+1, y, x-1, y) + compute(x, y+1, x, y-1), 0.5);
    }
    
    private void update() {
        width = picture.width();
        height = picture.height();
    }
    
    private double compute(int x1, int y1, int x2, int y2) {
        if (x1 > width-1) x1 = 0;
        if (y1 > height-1) y1 = 0;
        if (x2 < 0) x2 = width-1;
        if (y2 < 0) y2 = height-1;
      
        int r = picture.get(x2, y2).getRed() - picture.get(x1, y1).getRed();
        int b = picture.get(x2, y2).getBlue() - picture.get(x1, y1).getBlue();
        int g = picture.get(x2, y2).getGreen() - picture.get(x1, y1).getGreen();

        return r*r + b*b + g*g;
    }
    
    private void compute_energy_matrix() {
        energy_matrix = new double[picture.height()][picture.width()];
        for (int i = 0; i < picture.height(); i++) 
            for (int j = 0; j < picture.width(); j++) 
                energy_matrix[i][j] = energy(j, i);
        
        System.out.println("Energy matrix: ");
        for (int i = 0; i < picture.height(); i++) {
            for (int j = 0; j < picture.width(); j++)
                System.out.print(Math.round(energy_matrix[i][j]) + " ");
            System.out.println();
        }
        System.out.println();
    }
    // make private
    public double[][] compute_topological() {
        double[][] ans_matrix = new double[picture.height()][picture.width()];
        // #columns = width, #rows = height
        for (int i = 0; i < picture.width(); i++)
            ans_matrix[0][i] = energy_matrix[0][i];
        
        // for i, j fill in i+1, j and i+1, j-1 and i+1, j-1
        for (int i = 0; i < picture.height(); i++) {
            for (int j = 0; j < picture.width(); j++) {
                fill_value(i, j, i+1, j, ans_matrix);
                fill_value(i, j, i+1, j-1, ans_matrix);
                fill_value(i, j, i+1, j+1, ans_matrix);
            }
        }
        
        System.out.println("Topological matrix: ");
        for (int i = 0; i < picture.height(); i++) {
            for (int j = 0; j < picture.width(); j++)
                System.out.print(Math.round(ans_matrix[i][j]) + " ");
            System.out.println();
        }
     
        return ans_matrix;
    }
    
    private void fill_value(int i1, int j1, int i, int j, double[][] ans_matrix) {
        if (i < 0 || j < 0 || i > height-1 || j > width-1)
            return;
        
        if(ans_matrix[i][j] == 0 || ans_matrix[i][j] > energy_matrix[i][j] + ans_matrix[i1][j1])
            ans_matrix[i][j] = energy_matrix[i][j] + ans_matrix[i1][j1];
    }
    
    
    private int min_index(int lowerbound, int upperbound, int layer, double[][] matrix, boolean orientation) {
        if (lowerbound < 0)
            lowerbound = 0;
        if (layer == 
        double min = matrix[layer][lowerbound];
        int index = lowerbound;
        for (int i = lowerbound; i <= upperbound; i++) {
            if (matrix[layer][i] < min) {
                min = matrix[layer][i];
                index = i;
            }
        }
        return index;
    }
    // sequence of indices for horizontal seam
    public   int[] findHorizontalSeam() {
        int[] seam = new int[width];
        // transpose the energy matrix - make instance variable?
        tr_energy_matrix = new double[width][height];
        
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                tr_energy_matrix[i][j] = energy_matrix[j][i];
            }
        }
        
        seam = findVerticalSeam();
        return seam;
    }
    
    // sequence of indices for vertical seam
    public   int[] findVerticalSeam() {
        int[] seam = new int[height];
        // once you find the min in the bottom row, check for duplicates
        compute_energy_matrix();
        double[][] matrix = compute_topological();
        int index = min_index(0, width-1, height-1, matrix, true);
    
        // check for duplicates
        seam[height-1] = index;
        int new_index;
        for (int i = height-2; i >= 0; i--) {
            new_index = min_index(index-1, index+1, i, matrix, true);
            index = new_index;
            seam[i] = index;
        }
        
        //for (int i = 0; i < seam.length; i++)
           // System.out.println(seam[i]);
        return seam;
    }
        
    // remove horizontal seam from current picture
    public    void removeHorizontalSeam(int[] seam) {
        if (seam == null)
            throw new NullPointerException();
        
        Picture new_picture = new Picture(width, height-1);
        for (int i = 0; i < height; i++) {
            int index = 0;
            for (int j = 0; j < width; j++) {
                        if (seam[i] == j) continue;
                        else new_picture.set(i, index++, picture.get(j, i));
                    }
        }
    }
    
    // remove vertical seam from current picture
    public    void removeVerticalSeam(int[] seam) {
        if (seam == null)
            throw new NullPointerException();
        // if a index equals to the seam value
        // array index = row and array value = column`
        Picture new_picture = new Picture(width-1, height);
        // for row = array index, column = array value skip
        for (int i = 0; i < height; i++) {
            int index = 0;
            for (int j = 0; j < width; j++) {
                        if (seam[i] == j) continue;
                        else new_picture.set(index++, i, picture.get(j, i));
                    }
        }

        picture = new_picture;
        update();
        //System.out.println(new_picture.width() + " " + new_picture.height());
        //System.out.println(picture.width() + " " + picture.height());
        //compute_energy_matrix(); compute_topological();
            
    }
    
    // do unit testing of this class
    public static void main(String[] args) {
  
        Picture picture = new Picture("6x5.png");
        SeamCarver sc = new SeamCarver(picture);
        System.out.println(picture.height() + " " + picture.width());
        // 6 columns (height = 6), 5 rows (width = 5)
    
        System.out.println();
        sc.compute_energy_matrix();
        sc.compute_topological();
       // double[][] a = sc.compute_topological();
        /*for (int i = 0; i < picture.height(); i++) {
            for (int j = 0; j < picture.width(); j++)
                System.out.println(Math.round(a[j][i]) + " ");
            System.out.println();
        }*/
            
        int[] arr = sc.findVerticalSeam();
        sc.removeVerticalSeam(arr);
        arr = sc.findHorizontalSeam();
        sc.removeHorizontalSeam(arr);
            
    }

}
