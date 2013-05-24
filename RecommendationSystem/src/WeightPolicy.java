public interface WeightPolicy {
    static class Edge implements Comparable<Edge> {
        final int neigh;
        final double weight;

        Edge(int neigh, double weight) {
            this.neigh = neigh;
            this.weight = weight;
        }

        @Override
        public int compareTo(Edge o) {
            return Double.compare(weight, o.weight) != 0 ? Double.compare(weight, o.weight) : Integer.compare(neigh, o.neigh);
        }
    }

    void startNewArtist(int artist);
    void addIntersection(int user, int otherArtist, int numListened, int otherNumListened);
    Edge[] getCurrentEdges();
}
