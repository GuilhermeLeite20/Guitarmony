package com.guitarmony.music;

/** Families share the same interval-based musical model. */
public enum Family {
    MODES("Modos gregos"), PENTATONICS("Pentatônicas"), BLUES("Pentablues"),
    ARPEGGIOS("Arpejos"), SCALES("Escalas");

    private final String label;
    Family(String label) { this.label = label; }
    @Override public String toString() { return label; }
}
