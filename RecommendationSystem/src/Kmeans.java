import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class Kmeans {
    private double centroinds[][];
    private List<Integer> clusters[];
    private int inside[];

    private void recalcCentroinds(double vec[][], int k) {
        for (int i = 0; i < k; ++i) {
            double sum[] = new double [vec[0].length];
            for (int j = 0; j < sum.length; ++j) {
                sum[j] = 0;
            }
            double n = clusters[i].size();

            for (int t : clusters[i])
                for (int j = 0; j < vec[t].length; ++j)
                    sum[j] += vec[t][j];

            for (int j = 0; j < sum.length; ++j)
                sum[j] /= n;

            centroinds[i] = sum;
        }

        for (int i = 0; i < clusters.length; ++i)
            clusters[i].clear();
    }

    private int getNearest (double a[]) {
        double min = sub(a, centroinds[0]);
        int res = 0;
        for (int i = 1; i < centroinds.length; ++i) {
            double t = sub(a, centroinds[i]);
            if (t < min) {
                min = t;
                res = i;
            }
        }
        return res;
    }

    private double sub(double a[], double b[]) {
        if (a.length != b.length)
            return -1;
        double res = 0;
        for (int i = 0; i < a.length; ++i)
            res += (a[i]  - b[i]) * (a[i]  - b[i]);

        return res;
    }

    private boolean rebuildclusters(double vec[][], int k) {
        boolean flag = true;
        for (int i = 0; i < vec.length; ++i) {
            int t = getNearest(vec[i]);
            clusters[t].add(i);
            if (inside[i] != t) {
                flag = false;
                inside[i] = t;
            }
        }

        if (flag)
            return true;

        return false;
    }

    public int[][] getClusters(double vec[][], int k) {
        centroinds = new double [k][vec[0].length];
        inside = new int [vec.length];
        for (int i = 0; i < vec.length; ++i) {
            inside[i] = 0;
        }
        clusters = new List[k];
        for (int i = 0; i < k; ++i) {
            clusters[i] = new ArrayList<Integer>();
        }
        if (vec.length < k) {
            return null;
        }
        for (int i = 0; i < k; ++i) {
            centroinds[i] = vec[i];
        }
        int numIterations = 0;
        while(!rebuildclusters(vec, k) && numIterations < 1000) {
            recalcCentroinds(vec, k);
            numIterations++;
            System.err.println(numIterations);
        }
        System.err.println("numIterations: " + numIterations);

        final int[][] result = new int[k][];
        for (int i = 0; i < result.length; i++) {
            final List<Integer> currentCluster = clusters[i];
            result[i] = new int[currentCluster.size()];
            for (int j = 0; j < currentCluster.size(); j++)
                result[i][j] = currentCluster.get(j);
        }

        return result;
    }
}