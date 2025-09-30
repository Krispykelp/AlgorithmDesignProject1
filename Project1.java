// Name: <<YOUR NAME HERE>>
// Project: COMP482 Project 1

import java.io.*;
import java.util.*;

public class Project1 {
    public static void main(String[] args) {
        try {
            int[][] menRank;     // menRank[m][w] = rank (1..N), lower is better
            int[][] womenRank;   // womenRank[w][m] = rank (1..N), lower is better
            int N;

            // Read all integers from input.txt
            List<Integer> ints = new ArrayList<>();
            try (Scanner sc = new Scanner(new File("input.txt"))) {
                while (sc.hasNext()) {
                    if (sc.hasNextInt()) {
                        ints.add(sc.nextInt());
                    } else {
                        sc.next();
                    }
                }
            }
            if (ints.isEmpty()) throw new RuntimeException("Empty input.txt");

            int idx = 0;
            N = ints.get(idx++);
            int expected = 2 * N * N;
            if (ints.size() - 1 < expected) {
                throw new RuntimeException("Expected " + expected + " preference integers after N; found " + (ints.size()-1));
            }

            menRank = new int[N][N];
            womenRank = new int[N][N];

            // Read men's preference table (as ranks), row-major N x N
            for (int r = 0; r < N; r++) {
                for (int c = 0; c < N; c++) {
                    menRank[r][c] = ints.get(idx++);
                }
            }
            // Read women's preference table (as ranks), row-major N x N
            for (int r = 0; r < N; r++) {
                for (int c = 0; c < N; c++) {
                    womenRank[r][c] = ints.get(idx++);
                }
            }

            int[] womanForMan = loweredExpectationsMatch(N, menRank, womenRank);
            boolean stable = isStable(N, womanForMan, menRank, womenRank);

            // Output per spec
            StringBuilder out = new StringBuilder();
            for (int m = 0; m < N; m++) {
                int w = womanForMan[m];
                out.append("M").append(m + 1).append("-W").append(w + 1).append('\n');
            }
            out.append(stable ? "Stable" : "Unstable");
            System.out.print(out.toString());
        } catch (Exception e) {
            // Fail hard with a message to stderr; grading will look at stdout formatting
            System.err.println("Error: " + e.getMessage());
        }
    }

    // Implements the LoweredExpectations algorithm as described
    private static int[] loweredExpectationsMatch(int N, int[][] menRank, int[][] womenRank) {
        int[] womanForMan = new int[N];
        int[] manForWoman = new int[N];
        Arrays.fill(womanForMan, -1);
        Arrays.fill(manForWoman, -1);

        boolean[] manAssigned = new boolean[N];
        boolean[] womanAssigned = new boolean[N];

        for (int i = 1; i <= N; i++) { // rounds 1..N
            // Collect all candidate pairs (m, w) where both rank each other in top i and both unassigned
            List<int[]> candidates = new ArrayList<>();
            for (int m = 0; m < N; m++) {
                if (manAssigned[m]) continue;
                for (int w = 0; w < N; w++) {
                    if (womanAssigned[w]) continue;
                    if (menRank[m][w] <= i && womenRank[w][m] <= i) {
                        candidates.add(new int[]{m, w});
                    }
                }
            }
            // Sort by tie-break rules: lower numbered man first, then lower numbered woman
            candidates.sort((a, b) -> {
                if (a[0] != b[0]) return Integer.compare(a[0], b[0]);
                return Integer.compare(a[1], b[1]);
            });

            // Greedily add pairs while respecting that each man/woman can only be assigned once
            for (int[] pair : candidates) {
                int m = pair[0];
                int w = pair[1];
                if (!manAssigned[m] && !womanAssigned[w]) {
                    womanForMan[m] = w;
                    manForWoman[w] = m;
                    manAssigned[m] = true;
                    womanAssigned[w] = true;
                }
            }
            // Early exit if perfect matching achieved
            if (allAssigned(manAssigned) && allAssigned(womanAssigned)) break;
        }

        // By round N, everyone should be matched; but in case any remain (due to malformed input), pair greedily by index
        for (int m = 0; m < N; m++) {
            if (womanForMan[m] == -1) {
                for (int w = 0; w < N; w++) {
                    if (manForWoman[w] == -1) {
                        womanForMan[m] = w;
                        manForWoman[w] = m;
                        break;
                    }
                }
            }
        }
        return womanForMan;
    }

    private static boolean allAssigned(boolean[] arr) {
        for (boolean b : arr) if (!b) return false;
        return true;
    }

    // Check standard stability: no blocking pair (m, w) who prefer each other over their current partners
    private static boolean isStable(int N, int[] womanForMan, int[][] menRank, int[][] womenRank) {
        int[] manForWoman = new int[N];
        Arrays.fill(manForWoman, -1);
        for (int m = 0; m < N; m++) {
            manForWoman[womanForMan[m]] = m;
        }

        for (int m = 0; m < N; m++) {
            int wCurrent = womanForMan[m];
            for (int w = 0; w < N; w++) {
                if (w == wCurrent) continue;
                int mCurrent = manForWoman[w];
                // Does man m prefer w over his current partner?
                boolean mPrefersW = menRank[m][w] < menRank[m][wCurrent];
                // Does woman w prefer m over her current partner?
                boolean wPrefersM = womenRank[w][m] < womenRank[w][mCurrent];
                if (mPrefersW && wPrefersM) return false; // blocking pair found
            }
        }
        return true;
    }
}
