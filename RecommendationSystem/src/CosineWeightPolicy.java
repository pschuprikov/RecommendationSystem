public class CosineWeightPolicy implements WeightPolicy {
    private final FastIntSet neighs;
    private final int[] artists;

    private double[] weights;
    private double[] norms;

    int currentArtist = -1;

    CosineWeightPolicy(int[] artists, DataSet ds) {
        neighs = new FastIntSet(artists.length);
        this.artists = artists;

        norms = new double[artists.length];
        weights = new double[artists.length];

        for (int i = 0; i < artists.length; i++) {
            for (int n : ds.getArtistData(artists[i]).numListened) {
                norms[i] += 1L * n * n;
                if (norms[i] < 0)
                    throw new AssertionError();
            }
            norms[i] = Math.sqrt(norms[i]);
        }
    }

    @Override
    public void startNewArtist(int artist) {
        currentArtist = artist;
        neighs.clear();
    }


    @Override
    public void addIntersection(int uid, int otherArtist, int numListened, int otherNumListened) {
        if (!neighs.contains(otherArtist)) {
            neighs.add(otherArtist);
            weights[otherArtist] = 0;
        }
        weights[otherArtist] += (1L * numListened * otherNumListened / norms[currentArtist]);
    }

    @Override
    public Edge[] getCurrentEdges() {
        Edge[] result = new Edge[neighs.size()];
        for (int i = 0; i < neighs.size(); i++) {
            final int otherArtist = neighs.get(i);
            if (Double.isNaN(weights[otherArtist]))
                throw new AssertionError();
            result[i] = new Edge(otherArtist, Math.max(0, 1. + weights[otherArtist] / norms[otherArtist]));
        }
        return result;
    }
}
