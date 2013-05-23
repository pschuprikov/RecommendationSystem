import org.jblas.DoubleMatrix;
import org.jblas.Eigen;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class SpectralClustering {

    static class Config {
        boolean useCache = true;
        int numClusters = 10;

        Config setUseCache(boolean value) {
            useCache = value;
            return this;
        }

        Config setNumClusters(int numClusters) {
            this.numClusters = numClusters;
            return this;
        }
    }

    private static final File eigsFile = new File("eigs.dat");
    private final Config config;
    private final Kmeans kmeans = new Kmeans();

    SpectralClustering(Config config) {
        this.config = config;
    }

    int[][] cluster(DoubleMatrix w) {final long start = System.currentTimeMillis();
        System.err.println("start");

        convertToUnnormalizedLaplacian(w);

        final DoubleMatrix[] res = getEigenValues(w);
        final double[][] pts = new double[w.getColumns()][config.numClusters];

        for (int i = 1; i <= config.numClusters; i++) {
            for (int j = 0; j < w.getColumns(); j++)
                pts[j][i - 1] = res[0].get(i, j);
        }

        final int[][] result = kmeans.getClusters(pts, config.numClusters);

        System.err.println("finish");
        System.err.println(System.currentTimeMillis() - start + "ms");

        return result;
    }

    private DoubleMatrix[] getEigenValues(DoubleMatrix w) {
        if (config.useCache && eigsFile.exists()) {
            try (
                    InputStream fileStream = new BufferedInputStream(new FileInputStream(eigsFile));
                    ObjectInputStream objectStream = new ObjectInputStream(fileStream)
            ) {
                if (objectStream.readInt() == w.hashCode()) {
                    return (DoubleMatrix[]) objectStream.readObject();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        final DoubleMatrix[] result =  Eigen.symmetricEigenvectors(w);
        if (config.useCache) {
            try (
                    OutputStream fileStream = new BufferedOutputStream(new FileOutputStream(eigsFile));
                    ObjectOutputStream objectStream = new ObjectOutputStream(fileStream)
            ) {
                objectStream.writeInt(w.hashCode());
                objectStream.writeObject(result);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private void convertToNormalizedLaplacian(DoubleMatrix w) {
        final int n = w.getColumns();
        double[] d2 = getD(w, n);

        for (int i = 0; i < n; i++)
            if (d2[i] != 0)
                d2[i] = 1 / Math.sqrt(d2[i]);

        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++) {
                w.put(i, j, 1. - d2[i] * w.get(i, j) * d2[j]);
            }
    }

    private double[] getD(DoubleMatrix w, int n) {
        double[] d2 = new double[n];

        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                d2[i] += w.get(i, j);
        return d2;
    }

    private void convertToUnnormalizedLaplacian(DoubleMatrix w) {
        final int n = w.getColumns();
        double[] d = getD(w, n);

        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++) {
                w.put(i, j, (i == j ? d[i] : 0) - w.get(i, j));
            }
    }
}
