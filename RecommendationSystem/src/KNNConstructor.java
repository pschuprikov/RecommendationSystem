import java.util.*;

public class KNNConstructor {

    static Graph construct(DataSet ds, int[] artists, int k, WeightPolicy weight) {
        Graph graph = new Graph(artists.length);

        final int[] revArtists = new int[ds.getNumArtists()];
        Arrays.fill(revArtists, -1);
        for (int i = 0; i < artists.length; i++)
            revArtists[artists[i]] = i;

        for (int u = 0; u < artists.length; u++) {
            System.err.println("inspecting user#" + u);
            weight.startNewArtist(u);

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

                    weight.addIntersection(uid, neighIdx, numListened, neighNumListened);
                }
            }

            final WeightPolicy.Edge[] edges = weight.getCurrentEdges();
            Arrays.sort(edges);
            for (int i = 0; i < k && i < edges.length; i++) {
                graph.addEdge(u, edges[i].neigh, edges[i].weight);
            }
        }

        return graph;
    }
}
