package com.guitarmony.music;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/** Immutable pitch-class formula, independent of key, tuning or screen position. */
public record Formula(String id, String name, Family family, List<Integer> intervals,
                      List<String> degrees, String description) {
    public Formula {
        Objects.requireNonNull(id); Objects.requireNonNull(name); Objects.requireNonNull(family);
        Objects.requireNonNull(description);
        intervals = List.copyOf(intervals);
        degrees = List.copyOf(degrees);
        if (id.isBlank() || name.isBlank() || intervals.isEmpty() || intervals.get(0) != 0
                || intervals.size() != degrees.size()
                || new HashSet<>(intervals).size() != intervals.size()
                || intervals.stream().anyMatch(i -> i < 0 || i > 11)
                || degrees.stream().anyMatch(String::isBlank)) {
            throw new IllegalArgumentException("Fórmula inválida: tônica, graus e intervalos devem corresponder.");
        }
        for (int i = 0; i < intervals.size(); i++) {
            if (degreePitch(degrees.get(i)) != intervals.get(i)) {
                throw new IllegalArgumentException("O grau " + degrees.get(i) + " não corresponde ao intervalo " + intervals.get(i) + ".");
            }
            if (i > 0 && intervals.get(i) <= intervals.get(i - 1)) {
                throw new IllegalArgumentException("Os intervalos devem estar em ordem ascendente.");
            }
        }
    }

    private static int degreePitch(String degree) {
        String token = degree.replace('♯', '#').replace('♭', 'b');
        if (token.equals("T")) return 0;
        if (!token.matches("[b#]*[1-9][0-9]*")) throw new IllegalArgumentException("Grau inválido: " + degree);
        int accidentals = 0, index = 0;
        while (index < token.length() && (token.charAt(index) == 'b' || token.charAt(index) == '#')) {
            accidentals += token.charAt(index++) == '#' ? 1 : -1;
        }
        int number = Integer.parseInt(token.substring(index));
        int natural = new int[]{0, 2, 4, 5, 7, 9, 11}[(number - 1) % 7];
        return Math.floorMod(natural + accidentals, 12);
    }

    public boolean contains(int interval) { return intervals.contains(Math.floorMod(interval, 12)); }
    @Override public String toString() { return name; }
}
