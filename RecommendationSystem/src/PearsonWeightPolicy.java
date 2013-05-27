public class PearsonWeightPolicy implements WeightPolicy {
    private final FastIntSet neighs;
    private final int[] artists;

    private double[] dot;
    private double[] sum;
    private double[] disp;

    private final int numUsers;

    int currentArtist = -1;

    PearsonWeightPolicy(int[] artists, DataSet ds) {
        neighs = new FastIntSet(artists.length);
        this.artists = artists;

        sum = new double[artists.length];
        dot = new double[artists.length];
        disp = new double[artists.length];

        numUsers = ds.getNumUsers();

        for (int i = 0; i < artists.length; i++) {
            double square = 0;
            for (int n : ds.getArtistData(artists[i]).numListened) {
                sum[i] += n;
                square += 1L * n * n;
            }
            disp[i] = Math.sqrt(square - sum[i] * sum[i] / numUsers);
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
            dot[otherArtist] = 0;
        }
        dot[otherArtist] += 1L * numListened * otherNumListened / (disp[currentArtist] * disp[otherArtist]);
    }

    @Override
    public Edge[] getCurrentEdges() {
        Edge[] result = new Edge[neighs.size()];
        for (int i = 0; i < neighs.size(); i++) {
            final int otherArtist = neighs.get(i);
            double weight = dot[otherArtist] - sum[otherArtist] * sum[currentArtist] / (disp[currentArtist] * disp[otherArtist] * numUsers);
            result[i] = new Edge(otherArtist, weight + 1);
        }
        return result;
    }
}