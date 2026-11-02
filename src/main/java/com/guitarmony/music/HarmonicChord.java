package com.guitarmony.music;

import java.util.List;
import java.util.Objects;

/** A chord obtained by stacking alternate degrees of a parent scale. */
public record HarmonicChord(int degree, String roman, String name, String root,
                            Formula formula, List<String> notes) {
    public HarmonicChord {
        if (degree < 1 || degree > 7) throw new IllegalArgumentException("Grau fora do campo harmônico.");
        Objects.requireNonNull(roman); Objects.requireNonNull(name); Objects.requireNonNull(root);
        Objects.requireNonNull(formula);
        notes = List.copyOf(notes);
    }

    @Override public String toString() { return roman + " · " + name; }
}
