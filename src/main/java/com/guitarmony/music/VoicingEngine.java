package com.guitarmony.music;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bounded fingering search for complete triads and tetrads. Shapes are calculated
 * from the active tuning; they are not labelled as reproductions of a photograph.
 */
public final class VoicingEngine {
    private static final int NODE_LIMIT = 45_000;
    private static final int CACHE_LIMIT = 64;
    private record Key(String root, Formula formula, Tuning tuning, int nearFret) {}
    private record Ranked(Voicing voicing, double score) {}
    private static final Map<Key, List<Voicing>> CACHE = new LinkedHashMap<>();
    private VoicingEngine() {}

    public static List<Voicing> find(String root, Formula chord, Tuning tuning, int nearFret) {
        if (chord.intervals().size() < 3 || chord.intervals().size() > 4) return List.of();
        int target = Math.max(0, Math.min(24, nearFret));
        Key key = new Key(root, chord, tuning, target);
        synchronized (CACHE) {
            List<Voicing> cached = CACHE.get(key);
            if (cached != null) return cached;
        }
        Search search = new Search(root, chord, tuning, target);
        List<Voicing> result = search.run();
        synchronized (CACHE) {
            if (CACHE.size() >= CACHE_LIMIT) CACHE.remove(CACHE.keySet().iterator().next());
            CACHE.put(key, result);
        }
        return result;
    }

    private static final class Search {
        private final int rootPitch;
        private final Formula formula;
        private final Tuning tuning;
        private final int target;
        private final int fullMask;
        private final List<String> spelled;
        private final Map<Integer, Ranked> found = new HashMap<>();
        private final int[][] toneMap = new int[6][25];
        private int nodes;

        Search(String root, Formula formula, Tuning tuning, int target) {
            this.rootPitch = Notes.pitch(root);
            this.formula = formula;
            this.tuning = tuning;
            this.target = target;
            this.fullMask = (1 << formula.intervals().size()) - 1;
            this.spelled = Notes.spell(root, formula);
            for (int string = 0; string < 6; string++) {
                for (int fret = 0; fret <= 24; fret++) {
                    toneMap[string][fret] = formula.intervals().indexOf(Math.floorMod(tuning.pitchAt(string, fret) - rootPitch, 12));
                }
            }
        }

        List<Voicing> run() {
            List<Integer> windows = new ArrayList<>();
            for (int lo = 1; lo <= 21; lo++) windows.add(lo);
            windows.sort(Comparator.comparingInt(lo -> Math.abs(lo - Math.max(1, target))));
            int minimumStrings = formula.intervals().size();
            for (int lo : windows) {
                for (int count = minimumStrings; count <= 6; count++) {
                    for (int first = 0; first + count <= 6; first++) {
                        int[][] candidates = new int[6][];
                        boolean possible = true;
                        for (int string = first; string < first + count; string++) {
                            List<Integer> available = new ArrayList<>();
                            if (toneIndex(string, 0) >= 0) available.add(0);
                            for (int fret = lo; fret <= Math.min(24, lo + 4); fret++) {
                                if (toneIndex(string, fret) >= 0) available.add(fret);
                            }
                            candidates[string] = available.stream().mapToInt(Integer::intValue).toArray();
                            if (available.isEmpty()) possible = false;
                        }
                        if (possible) {
                            int[] frets = new int[6];
                            Arrays.fill(frets, -1);
                            visit(first, first, first + count, candidates, frets, 0);
                        }
                        if (nodes >= NODE_LIMIT) return results();
                    }
                }
            }
            return results();
        }

        private List<Voicing> results() {
            return found.values().stream().sorted(Comparator.comparingDouble(Ranked::score)
                            .thenComparing(r -> r.voicing().frets().toString()))
                    .limit(8).map(Ranked::voicing).toList();
        }

        private void visit(int string, int first, int end, int[][] candidates, int[] frets, int mask) {
            if (++nodes >= NODE_LIMIT) return;
            if (string == end) {
                if (mask == fullMask) accept(first, end, frets);
                return;
            }
            int remaining = end - string;
            if (Integer.bitCount(fullMask & ~mask) > remaining) return;
            for (int fret : candidates[string]) {
                frets[string] = fret;
                visit(string + 1, first, end, candidates, frets, mask | (1 << toneIndex(string, fret)));
                if (nodes >= NODE_LIMIT) break;
            }
            frets[string] = -1;
        }

        private int toneIndex(int string, int fret) {
            return toneMap[string][fret];
        }

        private void accept(int first, int end, int[] frets) {
            int key = 0;
            for (int string = 0; string < 6; string++) key |= (frets[string] + 1) << (string * 5);
            if (found.containsKey(key)) return;
            int low = 25, high = 0, pressed = 0, open = 0;
            for (int fret : frets) {
                if (fret > 0) { low = Math.min(low, fret); high = Math.max(high, fret); pressed++; }
                if (fret == 0) open++;
            }
            int span = pressed == 0 ? 0 : high - low;
            int fingerCount = fingers(frets);
            if (span > 4 || fingerCount > 4) return;
            // A six-fret leap to an open string is possible, but short stretches rank higher.
            int bassString = first;
            for (int string = first + 1; string < end; string++) {
                if (tuning.midiAt(string, frets[string]) < tuning.midiAt(bassString, frets[bassString])) bassString = string;
            }
            boolean rootBass = tuning.pitchAt(bassString, frets[bassString]) == rootPitch;
            double centre = pressed == 0 ? 0 : (low + high) / 2.0;
            double score = (rootBass ? 0 : 5000) + Math.abs(centre - target) * 50
                    + span * 9 + fingerCount * 2 + Math.abs((end - first) - 4) * 2 - open;
            List<Integer> positions = Arrays.stream(frets).boxed().toList();
            List<String> names = new ArrayList<>(6);
            for (int string = 0; string < 6; string++) {
                names.add(frets[string] < 0 ? "—" : spelled.get(toneIndex(string, frets[string])));
            }
            String bass = spelled.get(toneIndex(bassString, frets[bassString]));
            String fretLabel = positions.stream().map(f -> f < 0 ? "x" : f.toString()).collect(java.util.stream.Collectors.joining(" "));
            Voicing voicing = new Voicing((rootBass ? "Tônica no baixo" : "Baixo " + bass) + " · " + fretLabel, positions, names);
            found.put(key, new Ranked(voicing, score));
        }

        /** A barre may cross a higher-fretted string, but not an open/lower string. */
        private int fingers(int[] frets) {
            int fingers = 0;
            int visited = 0;
            for (int fret : frets) {
                if (fret <= 0 || (visited & (1 << fret)) != 0) continue;
                visited |= 1 << fret;
                boolean connected = false;
                for (int value : frets) {
                    if (value >= 0 && value < fret) connected = false;
                    if (value == fret && !connected) { fingers++; connected = true; }
                }
            }
            return fingers;
        }
    }
}
