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

    double getCosine(DataSet.ArtistData a, DataSet.ArtistData b) {
        double sumSqrA = 0;
        for (int n : a.numListened) {
            sumSqrA += 1L * n * n;
        }

        double sumSqrB = 0;
        for (int n : b.numListened) {
            sumSqrB += 1L * n * n;
        }

        double dot = 0;
        for (int ai = 0, bi = 0; ai < a.numListened.length && bi < b.numListened.length;) {
            if (a.users[ai] == b.users[bi]) {
                dot += 1L * a.numListened[ai] * b.numListened[bi];
                ai++; bi++;
            } else if (a.users[ai] < b.users[bi]) {
                ai++;
            } else {
                bi++;
            }
        }
        return Math.abs(dot) / (Math.sqrt(sumSqrA) * Math.sqrt(sumSqrB));
    }

    double getPearson(DataSet.ArtistData a, DataSet.ArtistData b) {
        double sumA = 0;
        double sumSqrA = 0;
        for (int n : a.numListened) {
            sumSqrA += 1L * n * n;
            sumA += n;
        }

        double sumB = 0;
        double sumSqrB = 0;
        for (int n : b.numListened) {
            sumSqrB += 1L * n * n;
            sumB += n;
        }

        double dot = 0;
        for (int ai = 0, bi = 0; ai < a.numListened.length && bi < b.numListened.length;) {
            if (a.users[ai] == b.users[bi]) {
                dot += 1L * a.numListened[ai] * b.numListened[bi];
                ai++; bi++;
            } else if (a.users[ai] < b.users[bi]) {
                ai++;
            } else {
                bi++;
            }
        }
        return (dot - sumA * sumB / ds.getNumUsers()) / (Math.sqrt(sumSqrA - sumA * sumA / ds.getNumUsers()) * Math.sqrt(sumSqrB - sumB * sumB / ds.getNumUsers()));
    }

    double expectNumListenedExcluded(int userIdx, int artistIdx) {
        if (artistToCluster[artistIdx] == -1)
            throw new AssertionError();
        final DataSet.UserData data = ds.getUserData(userIdx);
        int[] cluster = clusters[artistToCluster[artistIdx]];
        double totalSimilarity = 0;
        double weightedSum = 0;
        for (int i = 0; i < cluster.length; i++) {
            final int curArtist = artists[cluster[i]];
            if (curArtist == artistIdx)
                continue;
            //final double weight = getCosine(ds.getArtistData(artistIdx), ds.getArtistData(curArtist));
            final double weight = getPearson(ds.getArtistData(artistIdx), ds.getArtistData(curArtist));
            for (int j = 0; j < data.artists.length; j++) {
                if (data.artists[j] == curArtist) {
                    totalSimilarity += Math.abs(weight);
                    weightedSum += weight * data.numListened[j];
                }
            }
        }
        if (totalSimilarity == 0)
            return -1;
        return weightedSum / totalSimilarity;
    }
}
