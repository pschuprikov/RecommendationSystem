import org.jblas.DoubleMatrix;
import org.jblas.Eigen;

import java.io.*;

public class SpectralClustering {

    static class Config {
        boolean useCache = true;

        Config setUseCache(boolean value) {
            useCache = value;
            return this;
        }
    }

    private static final File eigsFile = new File("eigs.dat");
    private final Config config;

    SpectralClustering(Config config) {
        this.config = config;
    }

    int[][] cluster(DoubleMatrix w) {

        convertToNormalizedLaplacian(w);

        long start = System.currentTimeMillis();
        System.err.println("start");

        DoubleMatrix[] res = getEigenValues(w);

        System.err.println("finish");
        System.err.println(System.currentTimeMillis() - start + "ms");

        return null;
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
    }
}
