import Jama.Matrix;
import org.jblas.DoubleMatrix;
import org.jblas.Eigen;

public class SpectralClustering {
    static int[][] cluster(DoubleMatrix w) {

        final int n = w.getColumns();
        double[] d2 = new double[n];

        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                d2[i] += w.get(i, j);

        for (int i = 0; i < n; i++)
            if (d2[i] != 0)
                d2[i] = 1 / Math.sqrt(d2[i]);

        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                w.put(i, j, d2[i] * w.get(i, j) * d2[j]);

        long start = System.currentTimeMillis();
        System.err.println("start");
        DoubleMatrix[] res = Eigen.symmetricEigenvectors(w);
        System.err.println("finish");

        System.err.println(1.e-3 * (System.currentTimeMillis() - start) + "ms");
        return null;
    }
}
