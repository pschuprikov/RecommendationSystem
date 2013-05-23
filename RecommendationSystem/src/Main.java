import org.jblas.DoubleMatrix;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

public class Main {


    public static void main(String[] args) {
        try {
            final DataSet ds = new DataSet(new File("/home/pasha/Downloads/lastfm-dataset-360K/usersha1-artmbid-artname-plays.tsv"));
            System.err.println("artists red: " + ds.getNumArtists());

            int cnt = 0;
            for (int i = 0; i < ds.getNumUsers(); i++) {
                if (ds.getUserData(i).totalListened > 1000)
                    cnt++;
            }
            System.err.println("cnt:" + cnt);

            Integer[] order = new Integer[ds.getNumArtists()];
            for (int i = 0; i < order.length; i++)
                order[i] = i;

            Arrays.sort(order, new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return -Integer.compare(ds.getArtistData(o1).totalListened, ds.getArtistData(o2).totalListened);
                }
            });

            int[] artists = new int[Math.min(10000, order.length)];
            for (int i = 0; i < artists.length; i++) {
                artists[i] = order[i];
            }

            Graph knn = KNNConstructor.construct(ds, artists, 10);
            org.jblas.DoubleMatrix w = new DoubleMatrix(knn.size(), knn.size());
            for (int i = 0; i < knn.size(); i++) {
                for (Graph.Edge e : knn.adj(i)) {
                    w.put(i, e.to, e.weight);
                }
                w.put(i, i, 1);
            }

            for (int i = 0; i < knn.size(); i++)
                for (int j = 0; j < knn.size(); j++)
                    w.put(i, j, Math.max(w.get(i, j), w.get(j, i)));

            SpectralClustering.cluster(w);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
