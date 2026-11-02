package com.guitarmony.music;

import java.util.List;
import java.util.Objects;

/** A grip: exactly one fret per string. -1 means muted, 0 means open. */
public record Voicing(String label, List<Integer> frets, List<String> notes) {
    public Voicing {
        Objects.requireNonNull(label);
        frets = List.copyOf(frets);
        notes = List.copyOf(notes);
        if (frets.size() != 6 || notes.size() != 6 || frets.stream().anyMatch(f -> f < -1 || f > 24)) {
            throw new IllegalArgumentException("Um voicing precisa de seis cordas, com -1 para corda abafada.");
        }
    }
    @Override public String toString() { return label; }
}
