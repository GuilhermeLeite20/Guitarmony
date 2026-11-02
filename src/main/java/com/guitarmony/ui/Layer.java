package com.guitarmony.ui;

import com.guitarmony.fretboard.Cell;
import com.guitarmony.music.Formula;
import java.awt.Color;
import java.util.Objects;
import java.util.Set;

/** One visible study layer. Positions belong to a tuning, never to the painter. */
public record Layer(String label, String root, Formula formula, Set<Cell> cells, Color color) {
    public Layer {
        Objects.requireNonNull(label);
        Objects.requireNonNull(root);
        Objects.requireNonNull(formula);
        Objects.requireNonNull(color);
        cells = Set.copyOf(cells);
    }
}
