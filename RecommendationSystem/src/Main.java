import org.jblas.DoubleMatrix;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            final DataSet ds = new DataSet(new File("/home/pasha/Downloads/lastfm-dataset-360K/usersha1-artmbid-artname-plays.tsv"));
            SpectralClustering clustering = new SpectralClustering(new SpectralClustering.Config().setNumClusters(80));

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
            for (int i = 0; i < mostPopularArtists.length; i++)
                mostPopularArtists[i] = mostPopularOrder[i];

            final int KNEAREST = 10;
            WeightPolicy weights = new PearsonWeightPolicy(mostPopularArtists, ds);
            //WeightPolicy weights = new CosineWeightPolicy(mostPopularArtists, ds);
            final Graph knn = KNNConstructor.construct(ds, mostPopularArtists, KNEAREST, weights);
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

            RecommendationSystem rs = new RecommendationSystem(ds, mostPopularArtists, result);

            testExpectedListens(ds, mostPopularArtists, rs);
            //testRecommendationPlace(ds, rs);
            //runInteractive(ds, rs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void testExpectedListens(DataSet ds, int[] mostPopularArtists, RecommendationSystem rs) {
        Random rng = new Random();
        int count = 0;
        double sum = 0;
        while(count < 1000) {
            int artistIdx = mostPopularArtists[rng.nextInt(mostPopularArtists.length)];
            final DataSet.ArtistData data = ds.getArtistData(artistIdx);
            final int artistUserIdx = rng.nextInt(data.users.length);
            final double real = data.numListened[artistUserIdx];
            int userIdx = data.users[artistUserIdx];
            if (ds.getUserData(userIdx).totalListened < 10000)
                continue;
            final RecommendationSystem.Stat result = rs.expectNumListenedExcluded(userIdx, artistIdx);
            if (result.found && result.intersection >= 5) {
                count++;
                sum += Math.abs(real - result.expected) / Math.max(real, result.expected);
                System.err.println("intersection: " + result.intersection);
            }
        }

        if (count == 0)
            throw new AssertionError();

        System.err.println("Result: " + sum / count + "(" + count + ")");
    }

    private static void testRecommendationPlace(DataSet ds, RecommendationSystem rs) {
        final Random rng = new Random();
        int totalHits = 0;
        int totalMisses = 0;
        double sumBest = 0;
        double sumBestPercent = 0;
        System.err.println("Starting testing...");
        for (int i = 0; i < 10000; i++) {
            final int uid = rng.nextInt(ds.getNumUsers());
            final DataSet.UserData data = ds.getUserData(uid);

            int best = -1;
            double bestPercent = 0;
            for (int aid : rs.recommend(data)) {
                for (int artIdx = 0; artIdx < data.artists.length; artIdx++)
                    if (data.artists[artIdx] == aid) {
                        final int place = (data.artists.length - 1 - artIdx);
                        final double percent = data.numListened[artIdx] / (double) data.totalListened;

                        if (best == -1 || place < best) {
                            best = place;
                            bestPercent = percent;
                        }
                    }
            }
            if (best == -1)
                totalMisses++;
            else {
                totalHits++;
                sumBest += best;
                sumBestPercent += bestPercent;
            }
        }

        System.err.println("totalHits: " + totalHits + "; totalMisses: " + totalMisses + "; avgBest: " +
                (sumBest / totalHits) + "; avgBestPercent: " + (sumBestPercent / totalHits));
    }

    private static void runInteractive(DataSet ds, RecommendationSystem rs) {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.err.println("==========================");
            final String userhash = sc.next();
            final DataSet.UserData data = ds.getUserData(userhash);
            for (int aid : rs.recommend(data)) {
                System.err.print(ds.getArtistData(aid).artistName + " ");
                for (int i = 0; i < data.artists.length; i++)
                    if (data.artists[i] == aid)
                        System.err.print((data.artists.length - 1 - i) + " " + (data.numListened[i] / (double) data.totalListened));
                System.err.println("");
            }
        }
    }
}
