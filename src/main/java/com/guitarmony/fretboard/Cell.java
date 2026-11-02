package com.guitarmony.fretboard;

/** A physical fretboard location. String 0 is the bass string; 5 is the treble string. */
public record Cell(int stringIndex, int fret) {
    public Cell {
        if (stringIndex < 0 || stringIndex > 5) {
            throw new IllegalArgumentException("A corda deve estar entre 0 e 5.");
        }
        if (fret < 0 || fret > 24) {
            throw new IllegalArgumentException("A casa deve estar entre 0 e 24.");
        }
    }
}
