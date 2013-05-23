
public class FastIntSet {
    private final int[] version;
    private final int[] currentSet;
    private int lastID = 0;
    private int currentVersion = 0;

    FastIntSet(int n) {
        version = new int[n];
        currentSet = new int[n];
    }

    void clear() {
        currentVersion++;
        lastID = 0;
    }

    boolean contains(int a) {
        return version[a] == currentVersion;
    }

    void add(int a) {
        if (!contains(a)) {
            version[a] = currentVersion;
            currentSet[lastID++] = a;
        }
    }

    int size() {
        return lastID;
    }

    int get(int idx) {
        return currentSet[idx];
    }
}
