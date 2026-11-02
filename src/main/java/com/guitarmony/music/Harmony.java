package com.guitarmony.music;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Diatonic chords are derived from intervals, never hardcoded per key. */
public final class Harmony {
    private static final String[] ROMANS = {"I", "II", "III", "IV", "V", "VI", "VII"};
    private Harmony() {}

    public static List<HarmonicChord> harmonize(String root, Formula sevenNoteScale, boolean sevenths) {
        if (sevenNoteScale.intervals().size() != 7) {
            throw new IllegalArgumentException("O campo harmônico precisa de uma escala de sete notas.");
        }
        List<Integer> scale = sevenNoteScale.intervals();
        for (int i = 1; i < scale.size(); i++) {
            if (scale.get(i) <= scale.get(i - 1)) throw new IllegalArgumentException("A escala precisa estar em ordem ascendente.");
        }
        List<String> scaleNames = Notes.spell(root, sevenNoteScale);
        List<HarmonicChord> result = new ArrayList<>();
        int toneCount = sevenths ? 4 : 3;
        for (int degree = 0; degree < 7; degree++) {
            List<Integer> intervals = new ArrayList<>();
            List<String> notes = new ArrayList<>();
            List<String> degrees = new ArrayList<>();
            for (int tone = 0; tone < toneCount; tone++) {
                int scaleIndex = (degree + 2 * tone) % 7;
                int interval = Math.floorMod(scale.get(scaleIndex) - scale.get(degree), 12);
                intervals.add(interval);
                notes.add(scaleNames.get(scaleIndex));
                degrees.add(degreeToken(tone * 2 + 1, interval));
            }
            Formula formula = Catalog.forFamily(Family.ARPEGGIOS).stream()
                    .filter(f -> f.intervals().equals(intervals)).findFirst()
                    .orElseGet(() -> new Formula("harmonized-" + intervals.toString().replaceAll("[^0-9]", "-"),
                            "Acorde diatônico", Family.ARPEGGIOS, intervals, degrees, "Empilhamento de terças da escala selecionada."));
            String suffix = suffix(intervals);
            String numeral = ROMANS[degree];
            if (intervals.get(1) == 3) numeral = numeral.toLowerCase(Locale.ROOT);
            if (intervals.get(2) == 6) numeral += sevenths && intervals.get(3) == 10 ? "ø" : "°";
            if (intervals.get(2) == 8) numeral += "+";
            if (sevenths) numeral += intervals.get(3) == 11 ? "maj7" : "7";
            String chordRoot = scaleNames.get(degree);
            result.add(new HarmonicChord(degree + 1, numeral, chordRoot + suffix, chordRoot, formula, notes));
        }
        return List.copyOf(result);
    }

    private static String degreeToken(int degree, int interval) {
        if (degree == 1) return "T";
        int natural = switch (degree) { case 3 -> 4; case 5 -> 7; case 7 -> 11; default -> throw new IllegalArgumentException(); };
        int difference = interval - natural;
        return (difference < 0 ? "b".repeat(-difference) : "#".repeat(difference)) + degree;
    }

    private static String suffix(List<Integer> tones) {
        int third = tones.get(1), fifth = tones.get(2);
        String triad = third == 3 ? (fifth == 6 ? "dim" : "m") : (fifth == 8 ? "+" : "");
        if (tones.size() == 3) return triad;
        int seventh = tones.get(3);
        if (third == 3 && fifth == 6 && seventh == 10) return "m7(b5)";
        if (third == 3 && fifth == 6 && seventh == 9) return "dim7";
        if (seventh == 11) return triad.isEmpty() ? "maj7" : triad + "(maj7)";
        if (seventh == 10) return triad + "7";
        return triad + "(bb7)";
    }
}
