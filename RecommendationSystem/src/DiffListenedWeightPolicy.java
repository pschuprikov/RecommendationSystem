public class DiffListenedWeightPolicy implements WeightPolicy {
    final FastIntSet neighs;
    final double[] weights;
    final int[] count;

    DiffListenedWeightPolicy(int n) {
        neighs = new FastIntSet(n);
        weights = new double[n];
        count = new int[n];
    }

    @Override
    public void startNewArtist(int artist) {
        neighs.clear();
    }

    @Override
    public void addIntersection(int user, int otherArtist, int numListened, int otherNumListened) {
        if (!neighs.contains(otherArtist)) {
            neighs.add(otherArtist);
            weights[otherArtist] = 0;
            count[otherArtist] = 0;
        }
        count[otherArtist]++;
        weights[otherArtist] += Math.abs(numListened - otherNumListened) / (double) (numListened + otherNumListened);
    }

    @Override
    public Edge[] getCurrentEdges() {
        final Edge[] result = new Edge[neighs.size()];
        for (int i = 0; i < neighs.size(); i++) {
            final int otherArtist = neighs.get(i);
            result[i] = new Edge(otherArtist, 1. - weights[otherArtist] / count[otherArtist]);
        }
        return result;
    }
}
