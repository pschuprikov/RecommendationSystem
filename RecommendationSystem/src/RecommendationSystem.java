import java.util.Arrays;
import java.util.Comparator;

public class RecommendationSystem {
    private final DataSet ds;
    private final int[][] clusters;
    private final int[] artists;
    private final int[] artistToCluster;

    RecommendationSystem(DataSet ds, int[] artists, int[][] clusters) {
        this.ds = ds;
        this.clusters = clusters;
        this.artists = artists;

        artistToCluster = new int[ds.getNumArtists()];
        Arrays.fill(artistToCluster, -1);
        for (int i = 0; i < clusters.length; i++)
            for (int u : clusters[i])
                artistToCluster[artists[u]] = i;
    }

    int[] recommend(DataSet.UserData data) {
        final double[] clusterCount = new double[clusters.length];

        for (int i = 0; i < data.artists.length; i++)
            if (artistToCluster[data.artists[i]] != -1)
                clusterCount[artistToCluster[data.artists[i]]] += data.numListened[i];

        for (int i = 0; i < clusterCount.length; i++)
            clusterCount[i] /= data.totalListened;

        double maxScore = 0;
        int maxClusterIdx = 0;
        for (int i = 0; i < clusterCount.length; i++)
            if (clusterCount[i] > maxScore) {
                maxScore = clusterCount[i];
                maxClusterIdx = i;
            }

        final int[] maxCluster = clusters[maxClusterIdx];
        final Integer[] order = new Integer[maxCluster.length];
        for (int i = 0; i < order.length; i++)
            order[i] = i;

        Arrays.sort(order, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return -Integer.compare(ds.getArtistData(artists[maxCluster[o1]]).totalListened, ds.getArtistData(artists[maxCluster[o2]]).totalListened);
            }
        });

        final int[] result = new int[Math.min(maxCluster.length, 10)];
        for (int i = 0; i < result.length; i++)
            result[i] = artists[maxCluster[order[i]]];

        return result;
    }
}
