import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.Picture;

public class SeamCarver {
    // the only data structures I used are multi dimensional arrays, I was considering using a symbol table or a ternary tree structure (composed of Nodes that i would define) but I think this way is faster, since I only have to find the minimum element from three positions above, tehrefore finding a minimum element takes O(width + 3(height-1)) to find the minimum seam
    private static final boolean horizontal_orient = false;
    private static final boolean vertical_orient = true;
    private Picture picture;
    private final Picture copy;
    private int width;
    private int height;
    // instance variables to keep track of the width or height when the matrix is transposed, instead of checking the orientation each time. the value is updated as soon as the orientation is changed and then used for subsequent purposes
    private int width_orient;
    private int height_orient;
    // the energy matrix is an instance variable because most functions require it. the matrix with path lengths (topological_matrix) is computed based on the energy matrix
    private double[][] energy_matrix;
    // default orientation is vertical; when horizontal orientation is required, we change it to false and then back to true once the operation has been performed
    private boolean orientation;
    
    // create a seam carver object based on the given picture
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
    
    // current picture
    // time taken: constant
    public Picture picture() {
        return picture;
    }

    // width of current picture
    // time taken: constant
    public int width() {
        return width;
    }
    
    // height of current picture
    // time taken: constant
    public int height() {
        return height;
    }
    
    // sets orientation and changes the width_orient and height_orient values
    // time taken: constant
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
    // time taken: constant
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
    // time taken: two calls to compute_energy_pixel(), therefore constant
    public  double energy(int x, int y) {
        return Math.pow(compute_energy_pixel(x+1, y, x-1, y) + compute_energy_pixel(x, y+1, x, y-1), 0.5);
    }
    
    // computes the energy matrix, by calling the energy function for each pixel
    // orientation = true implies vertical seam, false for horizontal seam; we transpose the matrix if orientation equals false
    // time taken: O(width*height)
    private void compute_energy_matrix(boolean orientation) {
        energy_matrix = new double[height_orient][width_orient];
        // instead of reducing the code and using one loop, i figured this would take less time rather than using an if statement to check orientation for every pixel
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
    // time taken: constant
    private void compute_value(int i1, int j1, int i, int j, double[][] ans_matrix) {
        if (i < 0 || j < 0 || i > height_orient-1 || j > width_orient-1)
            return;
        
        if(ans_matrix[i][j] == 0 || ans_matrix[i][j] > energy_matrix[i][j] + ans_matrix[i1][j1])
            ans_matrix[i][j] = energy_matrix[i][j] + ans_matrix[i1][j1];
    }
    
    // computes the topological matrix by calculating for each pixel position the shortest path length until that pixel
    // time taken: three calls to compute_value for each pixel, therefore for width*height number of pixels O(width*height)
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
    
    // sequence of indices for horizontal seam
    // sets the orientation to horizontal, therefore works with the transpose of the enrgy matrix and after the energy and topological matrix have been computed, sets orientation back to default i.e. vertical
    // time taken: time taken by findVerticalSeam = O(width*height)
    public int[] findHorizontalSeam() {
        int[] seam = new int[width_orient];
        set_orientation(horizontal_orient);
        seam = findVerticalSeam();
        set_orientation(vertical_orient);
        
        return seam;
    }
    
    // sequence of indices for vertical seam
    // time taken: time to compute energy and topological matrix is O(width*height), for the last row O(width or height) to find the minimum element, for subsequent rows, checks three positions above to find the minimum, therefore O(width + 3(height-1)) therefore the total is O(width*height)
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
    // time taken: O(width*height)
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
    // time taken: O(width*height)
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