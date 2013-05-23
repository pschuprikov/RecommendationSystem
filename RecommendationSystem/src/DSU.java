import java.util.Arrays;

public class DSU {
    private final int[] p;
    private final int[] s;
    private final int[] r;
    private int numDSUS;

    DSU(int n) {
        numDSUS = n;

        p = new int[n];
        s = new int[n];
        r = new int[n];

        for (int i = 0; i < n; i++)
            p[i] = i;
        Arrays.fill(s, 1);
    }

    int size() {
        return p.length;
    }

    int get(int a) {
        return p[a] == a ? a : (p[a] = get(p[a]));
    }

    int getSize(int a) {
        return s[get(a)];
    }

    boolean unite(int a, int b) {
        a = get(a);
        b = get(b);
        if (a == b)
            return false;
        if (r[a] < r[b]) {
            p[a] = b;
            s[b] += s[a];
        } else if (r[a] > r[b]) {
            p[b] = a;
            s[a] += s[b];
        } else {
            p[b] = a;
            s[a] += s[b];
            r[a]++;
        }
        numDSUS--;
        return true;
    }

    int numDSs() {
        return numDSUS;
    }
}
