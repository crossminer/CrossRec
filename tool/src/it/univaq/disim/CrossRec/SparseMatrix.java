package it.univaq.disim.CrossRec;

/*************************************************************************
 *  Compilation:  javac SparseMatrix.java
 *  Execution:    java SparseMatrix
 *  
 *  A sparse, square matrix, implementing using two arrays of sparse
 *  vectors, one representation for the cols and one for the columns.
 *
 *  For matrix-matrix product, we might also want to store the
 *  column representation.
 *
 *************************************************************************/

public class SparseMatrix {
    private final int N;           // N-by-N matrix
    private SparseVector[] cols;   // the cols, each row is a sparse vector

    // initialize an N-by-N matrix of all 0s
    public SparseMatrix(int N) {
        this.N  = N;
        cols = new SparseVector[N];
        for (int i = 0; i < N; i++) cols[i] = new SparseVector(N);
    }

    // put A[i][j] = value
    public void put(int i, int j, double value) {
        if (i < 0 || i >= N) throw new RuntimeException("Illegal index");
        if (j < 0 || j >= N) throw new RuntimeException("Illegal index");
        cols[i].put(j, value);
    }

    // return A[i][j]
    public double get(int i, int j) {
        if (i < 0 || i >= N) throw new RuntimeException("Illegal index");
        if (j < 0 || j >= N) throw new RuntimeException("Illegal index");
        return cols[i].get(j);
    }

    // return the number of nonzero entries (not the most efficient implementation)
    public int nnz() { 
        int sum = 0;
        for (int i = 0; i < N; i++)
            sum += cols[i].nnz();
        return sum;
    }

    // return the matrix-vector product b = xA
    public SparseVector times(SparseVector x) {
        SparseMatrix A = this;
        if (N != x.size()) throw new RuntimeException("Dimensions disagree");
        SparseVector b = new SparseVector(N);
        for (int i = 0; i < N; i++)
            b.put(i, A.cols[i].dot(x));
        return b;
    }

    // return C = A + B
    public SparseMatrix plus(SparseMatrix B) {
        SparseMatrix A = this;
        if (A.N != B.N) throw new RuntimeException("Dimensions disagree");
        SparseMatrix C = new SparseMatrix(N);
        for (int i = 0; i < N; i++)
            C.cols[i] = A.cols[i].plus(B.cols[i]);
        return C;
    }


    // return a string representation
    public String toString() {
        String s = "N = " + N + ", nonzeros = " + nnz() + "\n";
        for (int i = 0; i < N; i++) {
            s += i + ": " + cols[i] + "\n";
        }
        return s;
    }


    // test client
    public void Test() {
        SparseMatrix A = new SparseMatrix(5);
        SparseVector x = new SparseVector(5);
        A.put(0, 0, 1.0);
        A.put(1, 1, 1.0);
        A.put(2, 2, 1.0);
        A.put(3, 3, 1.0);
        A.put(4, 4, 1.0);
        A.put(2, 4, 0.3);
        x.put(0, 0.75);
        x.put(2, 0.11);
        /*
        System.out.println("x     : " + x);
        System.out.println("A     : " + A);
        System.out.println("Ax    : " + A.times(x));
        System.out.println("A + A : " + A.plus(A));
        */
        
        System.out.println("x     : " + A.get(3, 4));
    }

}
