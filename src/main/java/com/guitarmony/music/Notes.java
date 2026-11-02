package com.guitarmony.music;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Pitch arithmetic and diatonic spelling are deliberately kept separate. */
public final class Notes {
    private static final String[] SHARPS = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    private static final String[] FLATS = {"C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B"};
    private static final String LETTERS = "CDEFGAB";
    private static final int[] NATURAL = {0, 2, 4, 5, 7, 9, 11};
    private static final List<String> ROOTS = List.of("C", "C#", "Db", "D", "D#", "Eb", "E", "F", "F#", "Gb", "G", "G#", "Ab", "A", "A#", "Bb", "B", "Cb");
    private Notes() {}

    public static int pitch(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Informe uma nota de C a B.");
        String n = name.trim().replace('♯', '#').replace('♭', 'b');
        int letter = LETTERS.indexOf(n.substring(0, 1).toUpperCase(Locale.ROOT));
        if (letter < 0) throw new IllegalArgumentException("Nota inválida: " + name);
        int value = NATURAL[letter];
        for (int i = 1; i < n.length(); i++) {
            value += switch (n.charAt(i)) {
                case '#' -> 1;
                case 'b' -> -1;
                case 'x' -> 2;
                default -> throw new IllegalArgumentException("Acidente inválido: " + name);
            };
        }
        return Math.floorMod(value, 12);
    }

    public static String chromatic(int pitch, boolean flats) {
        return (flats ? FLATS : SHARPS)[Math.floorMod(pitch, 12)];
    }

    /** Names use the degree's letter: F# major ends in E#, not F. */
    public static List<String> spell(String root, Formula formula) {
        int rootPitch = pitch(root);
        int rootLetter = LETTERS.indexOf(root.substring(0, 1).toUpperCase(Locale.ROOT));
        List<String> result = new ArrayList<>();
        for (int i = 0; i < formula.intervals().size(); i++) {
            String token = formula.degrees().get(i).replace('♯', '#').replace('♭', 'b');
            String digits = token.replaceAll("[^0-9]", "");
            int degree = digits.isEmpty() ? 1 : Integer.parseInt(digits);
            int letterIndex = Math.floorMod(rootLetter + degree - 1, 7);
            int desired = Math.floorMod(rootPitch + formula.intervals().get(i), 12);
            int delta = Math.floorMod(desired - NATURAL[letterIndex] + 6, 12) - 6;
            result.add(LETTERS.charAt(letterIndex) + (delta >= 0 ? "#".repeat(delta) : "b".repeat(-delta)));
        }
        return List.copyOf(result);
    }

    public static List<String> roots() { return ROOTS; }

    public static String intervalLabel(int semitones) {
        return new String[]{"T", "b2", "2", "b3", "3", "4", "#4 / b5", "5", "b6", "6", "b7", "7"}
                [Math.floorMod(semitones, 12)];
    }
}
