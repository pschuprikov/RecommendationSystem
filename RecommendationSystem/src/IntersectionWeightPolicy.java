
public class IntersectionWeightPolicy implements WeightPolicy {
    private final int[] personalCount;
    private final int[] intersectionCount;
    private final FastIntSet neighs;
    private int currentUser = - 1;

    IntersectionWeightPolicy(int[] artists, DataSet ds) {
        neighs = new FastIntSet(artists.length);
        personalCount = new int[artists.length];
        for (int i = 0; i < personalCount.length; i++)
            personalCount[i] = ds.getArtistData(artists[i]).users.length;
        intersectionCount = new int[artists.length];
    }


    @Override
    public void startNewArtist(int artist) {
        currentUser = artist;
        neighs.clear();
    }

    @Override
    public void addIntersection(int user, int otherArtist, int numListened, int otherNumListened) {
        if (!neighs.contains(otherArtist)) {
            neighs.add(otherArtist);
            intersectionCount[otherArtist] = 0;
        }
        intersectionCount[otherArtist]++;
    }

    @Override
    public Edge[] getCurrentEdges() {
        final Edge[] result = new Edge[neighs.size()];
        for (int i = 0; i < neighs.size(); i++) {
            final int otherArtist = neighs.get(i);
            result[i] = new Edge(otherArtist,
                    intersectionCount[otherArtist] / (double)(personalCount[otherArtist] + personalCount[currentUser] - intersectionCount[otherArtist]));
        }
        return result;
    }
}
