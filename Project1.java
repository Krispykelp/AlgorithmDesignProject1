// Name: Kris Sy COMP482 Project 1

import java.io.*;
import java.util.*;

public class Project1 {
    public static void main(String[] args) {
        try {
            int[][] menRank;     // menRank[m][w] = rank (1..N), lower is better
            int[][] womenRank;   // womenRank[w][m] = rank (1..N), lower is better
            int N;

            // --- Read input.txt as preference LISTS and convert to rank matrices ---
            try (Scanner sc = new Scanner(new File("input.txt"))) {
                if (!sc.hasNextInt()) throw new RuntimeException("Missing N at top of input.txt");
                N = sc.nextInt();

                menRank = new int[N][N];
                womenRank = new int[N][N];

                // Men's preferences: N rows each with N IDs in order of preference (best to worst)
                for (int m = 0; m < N; m++) {
                    for (int pos = 0; pos < N; pos++) {
                        if (!sc.hasNextInt()) throw new RuntimeException("Not enough men's preferences: expected " + (N*N) + " integers");
                        int womanId = sc.nextInt(); // 1..N
                        if (womanId < 1 || womanId > N) throw new RuntimeException("Invalid woman id " + womanId + " on men's row " + (m+1));
                        menRank[m][womanId - 1] = pos + 1; // rank = 1..N (lower is better)
                    }
                }

                // Women's preferences: N rows each with N IDs in order of preference (best to worst)
                for (int w = 0; w < N; w++) {
                    for (int pos = 0; pos < N; pos++) {
                        if (!sc.hasNextInt()) throw new RuntimeException("Not enough women's preferences: expected " + (N*N) + " integers");
                        int manId = sc.nextInt(); 
                        if (manId < 1 || manId > N) throw new RuntimeException("Invalid man id " + manId + " on women's row " + (w+1));
                        womenRank[w][manId - 1] = pos + 1; // rank = 1..N (lower is better)
                    }
                }
            }

            int[] womanForMan = loweredExpectationsMatch(N, menRank, womenRank);
            boolean stable = isStable(N, womanForMan, menRank, womenRank);

            // --- Output per spec ---
            for (int m = 0; m < N; m++) {
                int w = womanForMan[m];
                System.out.println("M" + (m + 1) + "-W" + (w + 1));
            }
            System.out.println(stable ? "Stable" : "Unstable");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // LoweredExpectations algorithm
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

        // By round N, everyone should be matched; but in case any remain (shouldn't for valid inputs), pair greedily by index
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

    // Check stability: no blocking pair (m, w) who prefer each other over their current partners
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
                if (mPrefersW && wPrefersM) return false; // blocking pair
            }
        }
        return true;
    }
}
