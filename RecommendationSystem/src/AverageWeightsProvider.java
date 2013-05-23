public class AverageWeightsProvider implements WeightsProvider {
    private final DataSet ds;

    public AverageWeightsProvider(DataSet ds) {
        this.ds = ds;
    }


    @Override
    public int size() {
        return ds.getNumArtists();
    }

    @Override
    public double getWeight(int a, int b) {
        final DataSet.ArtistData ad = ds.getArtistData(a);
        final DataSet.ArtistData bd = ds.getArtistData(b);

        double res = 0;
        int numUsers = 0;

        for (int ai = 0, bi = 0; ai < ad.users.length && bi < bd.users.length;) {
            if (ad.users[ai] == bd.users[bi]) {
                numUsers++;
                res += Math.abs(ad.numListened[ai] - bd.numListened[bi]) / (double) (ad.numListened[ai] + bd.numListened[bi]);
                ai++; bi++;
            } else if (ad.users[ai] < bd.users[bi]) {
                ai++;
            } else {
                bi++;
            }
        }

        return numUsers == 0 ? Double.POSITIVE_INFINITY : res / numUsers;
    }
}
