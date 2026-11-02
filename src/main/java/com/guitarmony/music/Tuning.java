package com.guitarmony.music;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/** Six open-string MIDI pitches, from lowest string to highest string. */
public final class Tuning {
    private final int[] open;

    public Tuning(int... openMidi) {
        if (openMidi.length != 6 || Arrays.stream(openMidi).anyMatch(n -> n < 12 || n > 96)) {
            throw new IllegalArgumentException("A afinação precisa de seis notas MIDI entre 12 e 96.");
        }
        open = openMidi.clone();
    }

    public static Tuning standard() { return new Tuning(40, 45, 50, 55, 59, 64); }

    public static Map<String, Tuning> presets() {
        Map<String, Tuning> values = new LinkedHashMap<>();
        values.put("Padrão · E A D G B E", standard());
        values.put("Drop D · D A D G B E", new Tuning(38, 45, 50, 55, 59, 64));
        values.put("D padrão · D G C F A D", new Tuning(38, 43, 48, 53, 57, 62));
        values.put("DADGAD · D A D G A D", new Tuning(38, 45, 50, 55, 57, 62));
        values.put("Open G · D G D G B D", new Tuning(38, 43, 50, 55, 59, 62));
        return Collections.unmodifiableMap(values);
    }

    public int midiAt(int stringIndex, int fret) {
        if (stringIndex < 0 || stringIndex >= 6 || fret < 0 || fret > 24) {
            throw new IllegalArgumentException("Corda (0–5) ou casa (0–24) inválida.");
        }
        return open[stringIndex] + fret;
    }

    public int pitchAt(int stringIndex, int fret) { return Math.floorMod(midiAt(stringIndex, fret), 12); }
    public String nameAt(int stringIndex, int fret, boolean flats) { return Notes.chromatic(pitchAt(stringIndex, fret), flats); }

    public Tuning shift(int semitones) {
        return new Tuning(Arrays.stream(open).map(n -> n + semitones).toArray());
    }

    public Tuning shiftString(int stringIndex, int semitones) {
        if (stringIndex < 0 || stringIndex >= 6) throw new IllegalArgumentException("Corda inválida.");
        int[] changed = open.clone();
        changed[stringIndex] += semitones;
        return new Tuning(changed);
    }

    public String summary() {
        return Arrays.stream(open).mapToObj(n -> Notes.chromatic(n, false)).collect(Collectors.joining(" · "));
    }

    @Override public boolean equals(Object other) { return other instanceof Tuning t && Arrays.equals(open, t.open); }
    @Override public int hashCode() { return Arrays.hashCode(open); }
    @Override public String toString() { return summary(); }
}
