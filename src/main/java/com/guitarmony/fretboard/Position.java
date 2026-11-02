package com.guitarmony.fretboard;

import com.guitarmony.music.Formula;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/** An immutable, concrete fingering region, not a second copy of the full scale. */
public record Position(String label, int rootPitch, Formula formula, Set<Cell> cells,
                       int startFret, int endFret) {
    public Position {
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(formula, "formula");
        Objects.requireNonNull(cells, "cells");
        rootPitch = Math.floorMod(rootPitch, 12);
        cells = Collections.unmodifiableSet(new LinkedHashSet<>(cells));
        if (startFret < 0 || endFret > 24 || startFret > endFret) {
            throw new IllegalArgumentException("Região do braço inválida.");
        }
    }
}
