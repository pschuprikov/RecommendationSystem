import java.io.*;
import java.util.*;

class DataSet {
    private final HashMap<String, Integer> uid2int = new HashMap<>();
    private final ArrayList<UserData> users = new ArrayList<>();

    private final HashMap<String, Integer> aid2int = new HashMap<>();
    private final ArrayList<ArtistData> artists = new ArrayList<>();

    class UserData {
        final String aid;
        final int totalListened;
        final int[] artists;
        final int[] numListened;

        UserData(String aid, int totalListened, int[] artists, int[] numListened) {
            this.aid = aid;
            this.totalListened = totalListened;
            this.numListened = numListened;
            this.artists = artists;
        }
    }

    class ArtistData {
        final String artistName;
        final String mbid;

        final int[] users;
        final int[] numListened;
        final int totalListened;

        ArtistData(String artistName, String mbid, int[] users, int[] numListened, int totalListened) {
            this.artistName = artistName;
            this.mbid = mbid;
            this.users = users;
            this.numListened = numListened;
            this.totalListened = totalListened;
        }
    }

    String[] parsetTSV(String line) {
        ArrayList<String> res = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == '\t') {
                res.add(sb.toString());
                sb.setLength(0);
            } else
                sb.append(line.charAt(i));
        }
        res.add(sb.toString());
        return res.toArray(new String[res.size()]);
    }

    DataSet(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            final ArrayList<String> userIDs = new ArrayList<>();
            final ArrayList<Integer> userTotalListened = new ArrayList<>();
            final ArrayList<ArrayList<Integer>> userArtists = new ArrayList<>();
            final ArrayList<ArrayList<Integer>> userNumListened = new ArrayList<>();
            final ArrayList<String> artistIDs = new ArrayList<>();
            final ArrayList<String> artistNames = new ArrayList<>();
            final ArrayList<ArrayList<Integer>> artistUsers = new ArrayList<>();
            final ArrayList<ArrayList<Integer>> artistNumListened = new ArrayList<>();

            int numLines = 0;
            while (true) {
                numLines++;
                if (numLines % 1000 == 0)
                    System.err.println("Line # " + numLines);
                final String line = br.readLine();
                if (line == null)
                    break;

                final String[] data = parsetTSV(line);

                for (int i = 0; i < data.length; i++) {
                    data[i] = data[i].trim();
                }

                if (data.length != 4 || data[1].isEmpty()) {
                    continue;
                }

                if (data[0].isEmpty() || data[3].isEmpty()) {
                    throw new AssertionError();
                }

                if (!uid2int.containsKey(data[0])) {
                    userIDs.add(data[0]);
                    userTotalListened.add(0);
                    userArtists.add(new ArrayList<Integer>());
                    userNumListened.add(new ArrayList<Integer>());
                    uid2int.put(data[0], userIDs.size() - 1);
                }
                final int userIdx = uid2int.get(data[0]);

                if (!aid2int.containsKey(data[1])) {
                    artistIDs.add(data[1]);
                    artistNames.add(data[2]);
                    artistUsers.add(new ArrayList<Integer>());
                    artistNumListened.add(new ArrayList<Integer>());
                    aid2int.put(data[1], artistNames.size() - 1);
                }
                final int artistIdx = aid2int.get(data[1]);

                artistUsers.get(artistIdx).add(userIdx);
                artistNumListened.get(artistIdx).add(Integer.parseInt(data[3]));

                userArtists.get(userIdx).add(artistIdx);
                userNumListened.get(userIdx).add(Integer.parseInt(data[3]));
            }

            for (int i = 0; i < artistIDs.size(); i++) {
                final ArrayList<Integer> curUsers = artistUsers.get(i);
                final ArrayList<Integer> curNumListened = artistNumListened.get(i);
                final Integer[] order = order(curUsers);

                int cnt = 1;
                for (int j = 1; j < order.length; j++)
                    if (curUsers.get(order[j]) != curUsers.get(order[j - 1]))
                        cnt++;

                int[] curUsersArray = new int[cnt];
                int[] curNumListenedArray = new int[cnt];

                int totalListened = 0;
                cnt = 0;
                for (int j = 0; j < order.length; j++)
                     if (j == 0 || curUsers.get(order[j]) != curUsers.get(order[j - 1])) {
                         final int user = curUsers.get(order[j]);
                         final int numListened = curNumListened.get(order[j]);

                         curUsersArray[cnt] = user;
                         curNumListenedArray[cnt++] = numListened;
                         userTotalListened.set(user, userTotalListened.get(user) + numListened);

                         totalListened += numListened;
                     }

                artists.add(new ArtistData(artistNames.get(i), artistNames.get(i), curUsersArray, curNumListenedArray, totalListened));
            }

            for (int i = 0; i < userIDs.size(); i++) {
                final ArrayList<Integer> curArtists = userArtists.get(i);
                final ArrayList<Integer> curNumListened = userNumListened.get(i);

                final Integer[] order = order(curNumListened);

                int cnt = 1;
                for (int j = 1; j < order.length; j++)
                    if (curArtists.get(order[j]) != curArtists.get(order[j - 1]))
                        cnt++;

                int[] curArtistsArray = new int[cnt];
                int[] curNumListenedArray = new int[cnt];

                cnt = 0;
                for (int j = 0; j < order.length; j++)
                    if (j == 0 || curArtists.get(order[j]) != curArtists.get(order[j - 1])) {
                        final int artist = curArtists.get(order[j]);
                        final int numListened = curNumListened.get(order[j]);

                        curArtistsArray[cnt] = artist;
                        curNumListenedArray[cnt++] = numListened;
                    }

                users.add(new UserData(userIDs.get(i), userTotalListened.get(i), curArtistsArray, curNumListenedArray));
            }
            System.err.println("Dataset: " + getNumArtists() + " artists; " + getNumUsers() + "users.");
        }
    }

    private Integer[] order(final ArrayList<Integer> curUsers) {
        final Integer[] order = new Integer[curUsers.size()];
        for (Integer j = 0; j < order.length; j++)
            order[j] = j;

        Arrays.sort(order, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return curUsers.get(o1).compareTo(curUsers.get(o2));
            }
        });
        return order;
    }

    int getNumArtists() {
        return artists.size();
    }

    int getNumUsers() {
        return uid2int.size();
    }

    ArtistData getArtistData(int artistIdx) {
        return artists.get(artistIdx);
    }

    ArtistData getArtistData(String artistHash) {
        return artists.get(aid2int.get(artistHash));
    }

    UserData getUserData(int userIdx) {
        return users.get(userIdx);
    }

    UserData getUserData(String userHash) {
        return users.get(uid2int.get(userHash));
    }
}
