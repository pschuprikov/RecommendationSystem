import java.util.*;

public class KNNConstructor {
    private static class Edge implements Comparable<Edge> {
        final int neigh;
        final double weight;

        private Edge(int neigh, double weight) {
            this.neigh = neigh;
            this.weight = weight;
        }

        @Override
        public int compareTo(Edge o) {
            return Double.compare(weight, o.weight) != 0 ? Double.compare(weight, o.weight) : Integer.compare(neigh, o.neigh);
        }
    }

    static Graph construct(DataSet ds, int[] artists, int k) {
        Graph graph = new Graph(artists.length);

        final double[] weights = new double[artists.length];
        final int[] cnt = new int[artists.length];

        final int[] revArtists = new int[ds.getNumArtists()];
        Arrays.fill(revArtists, -1);
        for (int i = 0; i < artists.length; i++)
            revArtists[artists[i]] = i;

        FastIntSet neighs = new FastIntSet(artists.length);

        for (int u = 0; u < artists.length; u++) {
            System.err.println("inspecting user#" + u);

            ArrayList<Edge> adj = new ArrayList<>();
            neighs.clear();

            final DataSet.ArtistData adata = ds.getArtistData(artists[u]);

            for (int i = 0; i < adata.users.length; i++) {
                final int uid = adata.users[i];
                final int numListened = adata.numListened[i];

                final DataSet.UserData udata =  ds.getUserData(uid);
                for (int j = 0; j < udata.artists.length; j++) {
                    final int neighID = udata.artists[j];
                    final int neighNumListened = udata.numListened[j];
                    final int neighIdx = revArtists[neighID];

                    if (neighIdx == -1)
                        continue;

                    if (!neighs.contains(neighIdx)) {
                        neighs.add(neighIdx);
                        weights[neighIdx] = 0;
                        cnt[neighIdx] = 0;
                    }

                    weights[neighIdx] += Math.abs(neighNumListened - numListened) / (double) (neighNumListened + numListened);
                    cnt[neighIdx]++;
                }
            }

            for (int i = 0; i < neighs.size(); i++) {
                final int neighIdx = neighs.get(i);
                adj.add(new Edge(neighIdx, Math.exp(-weights[neighIdx] / cnt[neighIdx])));
            }


            Collections.sort(adj);
            for (int i = 0; i < k && i < adj.size(); i++) {
                graph.addEdge(u, adj.get(i).neigh, adj.get(i).weight);
            }
        }

        return graph;
    }
}
