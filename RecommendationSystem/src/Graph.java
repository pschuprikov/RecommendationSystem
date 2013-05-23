import java.util.ArrayList;
import java.util.List;

public class Graph {
    static class Edge {
        final int to;
        final double weight;

        Edge(int to, double weight) {
            this.to = to;
            this.weight = weight;
        }
    }

    private final ArrayList<Edge>[] edges;

    Graph(int n) {
        edges = new ArrayList[n];
        for (int i = 0; i < n; i++) {
            edges[i] = new ArrayList<>();
        }
    }

    void addEdge(int a, int b, double w) {
        edges[a].add(new Edge(b, w));
    }

    ArrayList<Edge> adj(int a) {
        return edges[a];
    }

    int size() {
        return edges.length;
    }
}
