package com.guitarmony.fretboard;

public enum ViewMode {
    ALL_NOTES("Braço inteiro"),
    ONE_SHAPE("Um desenho"),
    SEQUENCE("Sequência de desenhos");

    private final String label;

    ViewMode(String label) {
        this.label = label;
    }

    @Override public String toString() {
        return label;
    }
}
