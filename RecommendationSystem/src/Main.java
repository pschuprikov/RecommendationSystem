import org.jblas.DoubleMatrix;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

public class Main {


    public static void main(String[] args) {
        try {
            final DataSet ds = new DataSet(new File("/home/pasha/Downloads/lastfm-dataset-360K/usersha1-artmbid-artname-plays.tsv"));
            SpectralClustering clustering = new SpectralClustering(new SpectralClustering.Config().setNumClusters(20));

            final Integer[] mostPopularOrder = new Integer[ds.getNumArtists()];
            for (int i = 0; i < mostPopularOrder.length; i++)
                mostPopularOrder[i] = i;

            Arrays.sort(mostPopularOrder, new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return -Integer.compare(ds.getArtistData(o1).totalListened, ds.getArtistData(o2).totalListened);
                }
            });

            final int[] mostPopularArtists = new int[Math.min(1000, mostPopularOrder.length)];
            for (int i = 0; i < mostPopularArtists.length; i++) {
                mostPopularArtists[i] = mostPopularOrder[i];
            }

            final Graph knn = KNNConstructor.construct(ds, mostPopularArtists, 5);
            final DoubleMatrix w = new DoubleMatrix(knn.size(), knn.size());
            for (int i = 0; i < knn.size(); i++) {
                for (Graph.Edge e : knn.adj(i))
                    w.put(i, e.to, e.weight);
                w.put(i, i, 1);
            }

            for (int i = 0; i < knn.size(); i++)
                for (int j = 0; j < knn.size(); j++)
                    w.put(i, j, Math.max(w.get(i, j), w.get(j, i)));

            int[][] result = clustering.cluster(w);

            for (int i = 0; i < result.length; i++) {
                for (int a : result[i])
                    System.err.println(ds.getArtistData(mostPopularArtists[a]).artistName);
                System.err.println("==========================");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
