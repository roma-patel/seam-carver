import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.Picture;

public class SeamCarver {

    private static final boolean horizontal_orient = false;
    private static final boolean vertical_orient = true;
    private Picture picture;
    private final Picture copy;
    private int width;
    private int height;
   
    private int width_orient;
    private int height_orient;
    private double[][] energy_matrix;
    private boolean orientation;
    
    public SeamCarver(Picture picture) {
        if (picture == null)
            throw new NullPointerException();
        
        this.picture = picture;
        this.width = picture.width();
        this.height = picture.height();
        set_orientation(vertical_orient);
        // deep copy of the picture
        copy = new Picture(picture);
    }
    
    public Picture picture() {
        return picture;
    }

    public int width() {
        return width;
    }
    
    public int height() {
        return height;
    }
    
    private void set_orientation(boolean orient) {
        orientation = orient;
        if (orientation == vertical_orient) {
            width_orient = width;
            height_orient = height;
        }
        else {
            width_orient = height;
            height_orient = width;
        }
    }
    
    // to update the width or height after a vertical or horizontal seam is removed and a new Picture object is created
    private void update_parameters() {
        width = picture.width();
        height = picture.height();
        set_orientation(vertical_orient);
    }
    
    // computes the required sum of squares value with the given parameters (wraps around the picture)
    private double compute_energy_pixel(int x1, int y1, int x2, int y2) {
        if (x1 > width-1) x1 = 0;
        if (y1 > height-1) y1 = 0;
        if (x2 < 0) x2 = width-1;
        if (y2 < 0) y2 = height-1;
        
        int r = picture.get(x2, y2).getRed() - picture.get(x1, y1).getRed();
        int b = picture.get(x2, y2).getBlue() - picture.get(x1, y1).getBlue();
        int g = picture.get(x2, y2).getGreen() - picture.get(x1, y1).getGreen();

        return r*r + b*b + g*g;
    }
    
    // energy of pixel at column x and row y
    public  double energy(int x, int y) {
        return Math.pow(compute_energy_pixel(x+1, y, x-1, y) + compute_energy_pixel(x, y+1, x, y-1), 0.5);
    }
    
    // orientation = true implies vertical seam, false for horizontal seam; we transpose the matrix if orientation equals false
    private void compute_energy_matrix(boolean orientation) {
        energy_matrix = new double[height_orient][width_orient];

        if (orientation == vertical_orient) {
            for (int i = 0; i < height; i++) 
                for (int j = 0; j < width; j++)
                    energy_matrix[i][j] = energy(j, i);
        }
        else {
            for (int i = 0; i < width; i++) 
                for (int j = 0; j < height; j++) 
                     energy_matrix[i][j] = energy(i, j);
        }
    }
    
    // computes the value i.e. chooses the shortest path length until that point
    private void compute_value(int i1, int j1, int i, int j, double[][] ans_matrix) {
        if (i < 0 || j < 0 || i > height_orient-1 || j > width_orient-1)
            return;
        
        if(ans_matrix[i][j] == 0 || ans_matrix[i][j] > energy_matrix[i][j] + ans_matrix[i1][j1])
            ans_matrix[i][j] = energy_matrix[i][j] + ans_matrix[i1][j1];
    }
    
    // computes the topological matrix by calculating for each pixel position the shortest path length until that pixel
    private double[][] compute_topological() {
        double[][] top_matrix = new double[height_orient][width_orient];
        for (int i = 0; i < width_orient; i++)
            top_matrix[0][i] = energy_matrix[0][i];
        
        // for pixel (i, j) consider (i+1, j) and (i+1, j-1) and (i+1, j+1) pixel positions i.e. the three pixels below it
        for (int i = 0; i < height_orient; i++) {
            for (int j = 0; j < width_orient; j++) {
                compute_value(i, j, i+1, j, top_matrix);
                compute_value(i, j, i+1, j-1, top_matrix);
                compute_value(i, j, i+1, j+1, top_matrix);
            }
        }
     
        return top_matrix;
    }
    
    // row indicates the row at which we are positioned, lowerbound and upperbound are the column values (inclusive) within which we find the minimum element
    // time taken: in the worst case (when lb = 0, ub = width/height) O(width or height), otherwise time taken = 3 (time taken to check three pixel positions)
    private int min_index(int lowerbound, int upperbound, int row, double[][] matrix) {
        if (lowerbound < 0) lowerbound = 0;
        if (upperbound >= matrix[0].length) upperbound = matrix[0].length-1;
        
        double min = matrix[row][lowerbound];
        int index = lowerbound;
        
        for (int i = lowerbound; i <= upperbound; i++) 
            if (matrix[row][i] < min) {
                min = matrix[row][i];
                index = i;
            }
        
        return index;
    }
    
    // sets the orientation to horizontal, therefore works with the transpose of the enrgy matrix and after the energy and topological matrix have been computed, sets orientation back to default i.e. vertical
    public int[] findHorizontalSeam() {
        int[] seam = new int[width_orient];
        set_orientation(horizontal_orient);
        seam = findVerticalSeam();
        set_orientation(vertical_orient);
        
        return seam;
    }
    
    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        // once you find the min in the bottom row, check for duplicates
        compute_energy_matrix(orientation);
        double[][] matrix = compute_topological();

        // finds the minimum weight element in the last row
        int index = min_index(0, width_orient-1, height_orient-1, matrix);
    
        int[] seam = new int[height_orient];
        seam[height_orient-1] = index;
        int new_index;
        for (int i = height_orient-2; i >= 0; i--) {
            new_index = min_index(index-1, index+1, i, matrix);
            index = new_index;
            seam[i] = index;
        }
        
        return seam;
    }    
        
    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        if (seam == null)
            throw new NullPointerException();
   
        Picture new_picture = new Picture(width, height-1);
        for (int i = 0; i < width; i++) {
            int index = 0;
            for (int j = 0; j < height; j++) {
                if (seam[i] == j) continue;
                else new_picture.set(i, index++, picture.get(i, j));
                }
        }
        picture = new_picture;
        update_parameters();
    }
    
    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        if (seam == null)
            throw new NullPointerException();

        Picture new_picture = new Picture(width-1, height);
        for (int i = 0; i < height; i++) {
            int index = 0;
            for (int j = 0; j < width; j++) {
                        if (seam[i] == j) continue;
                        else new_picture.set(index++, i, picture.get(j, i));
                    }
        }
        picture = new_picture;
        update_parameters();
    }
    
    // do unit testing of this class
    public static void main(String[] args) {
  
        Picture picture = new Picture("6x5.png");
        SeamCarver sc = new SeamCarver(picture);

        sc.compute_energy_matrix(true);
        sc.compute_topological();
       // double[][] a = sc.compute_topological();
        /*for (int i = 0; i < picture.height(); i++) {
            for (int j = 0; j < picture.width(); j++)
                System.out.println(Math.round(a[j][i]) + " ");
            System.out.println();
        }*/
            
        System.out.println("Vertical");
        int[] arr = sc.findVerticalSeam();
       // sc.removeVerticalSeam(arr);
        System.out.println("Horizontal");
        arr = sc.findHorizontalSeam();
        sc.removeHorizontalSeam(arr);
            
    }

}