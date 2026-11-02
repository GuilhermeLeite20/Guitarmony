package com.guitarmony.music;

import java.util.ArrayList;
import java.util.List;

/** The musical vocabulary is data: every visualization uses these same formulas. */
public final class Catalog {
    private static final List<Formula> ALL;
    static {
        List<Formula> f = new ArrayList<>();
        add(f, "ionian", "Jônio", Family.MODES, "0 2 4 5 7 9 11", "T 2 3 4 5 6 7", "Modo maior: 3ª maior, 6ª maior e 7ª maior. Primeiro grau do campo maior.");
        add(f, "dorian", "Dórico", Family.MODES, "0 2 3 5 7 9 10", "T 2 b3 4 5 6 b7", "Modo menor com 6ª maior. Segundo grau do campo maior.");
        add(f, "phrygian", "Frígio", Family.MODES, "0 1 3 5 7 8 10", "T b2 b3 4 5 b6 b7", "Modo menor com 2ª menor. Terceiro grau do campo maior.");
        add(f, "lydian", "Lídio", Family.MODES, "0 2 4 6 7 9 11", "T 2 3 #4 5 6 7", "Modo maior com 4ª aumentada. Quarto grau do campo maior.");
        add(f, "mixolydian", "Mixolídio", Family.MODES, "0 2 4 5 7 9 10", "T 2 3 4 5 6 b7", "Modo maior com 7ª menor, associado ao acorde dominante. Quinto grau do campo maior.");
        add(f, "aeolian", "Eólio", Family.MODES, "0 2 3 5 7 8 10", "T 2 b3 4 5 b6 b7", "Escala menor natural. Sexto grau do campo maior.");
        add(f, "locrian", "Lócrio", Family.MODES, "0 1 3 5 6 8 10", "T b2 b3 4 b5 b6 b7", "Modo meio-diminuto com 2ª menor e 5ª diminuta. Sétimo grau do campo maior.");

        add(f, "penta-major", "Pentatônica maior", Family.PENTATONICS, "0 2 4 7 9", "T 2 3 5 6", "Cinco notas: tônica, 2ª, 3ª, 5ª e 6ª maiores. Relativa da pentatônica menor.");
        add(f, "penta-minor", "Pentatônica menor", Family.PENTATONICS, "0 3 5 7 10", "T b3 4 5 b7", "Cinco notas: tônica, 3ª menor, 4ª justa, 5ª justa e 7ª menor.");
        add(f, "penta-minor6", "Pentatônica menor 6", Family.PENTATONICS, "0 3 5 7 9", "T b3 4 5 6", "Pentatônica menor com 6ª maior em lugar da 7ª menor; cor dórica.");
        add(f, "penta-dominant", "Pentatônica dominante 7", Family.PENTATONICS, "0 4 5 7 10", "T 3 4 5 b7", "Pentatônica maior com 4ª e 7ª menor, como a fórmula dominante do material.");
        add(f, "penta-halfdim", "Pentatônica menor b5", Family.PENTATONICS, "0 3 5 6 10", "T b3 4 b5 b7", "Pentatônica meio-diminuta: a quinta justa é substituída pela quinta diminuta.");

        add(f, "blues-major", "Pentablues maior", Family.BLUES, "0 2 3 4 7 9", "T 2 b3 3 5 6", "Pentatônica maior mais a blue note b3. A 3ª menor é cromática, não substitui a 3ª maior.");
        add(f, "blues-minor", "Pentablues menor", Family.BLUES, "0 3 5 6 7 10", "T b3 4 #4 5 b7", "Pentatônica menor mais a blue note #4 (enarmônica de b5).");
        add(f, "blues-dominant", "Pentablues dominante 7", Family.BLUES, "0 4 5 6 7 10", "T 3 4 #4 5 b7", "Fórmula dominante do material: pentatônica dominante com a blue note #4.");
        add(f, "blues-halfdim", "Pentablues menor b5", Family.BLUES, "0 3 5 6 7 10", "T b3 4 b5 5 b7", "Pentatônica menor b5 mais a quinta justa cromática. Mesmas alturas do blues menor, função intervalar diferente.");

        add(f, "triad-major", "Tríade maior", Family.ARPEGGIOS, "0 4 7", "T 3 5", "Tônica, terça maior e quinta justa.");
        add(f, "triad-minor", "Tríade menor", Family.ARPEGGIOS, "0 3 7", "T b3 5", "Tônica, terça menor e quinta justa.");
        add(f, "triad-dim", "Tríade diminuta", Family.ARPEGGIOS, "0 3 6", "T b3 b5", "Tônica, terça menor e quinta diminuta.");
        add(f, "triad-aug", "Tríade aumentada", Family.ARPEGGIOS, "0 4 8", "T 3 #5", "Tônica, terça maior e quinta aumentada.");
        add(f, "triad-sus2", "Tríade sus2", Family.ARPEGGIOS, "0 2 7", "T 2 5", "A segunda maior substitui a terça.");
        add(f, "triad-sus4", "Tríade sus4", Family.ARPEGGIOS, "0 5 7", "T 4 5", "A quarta justa substitui a terça.");
        add(f, "maj7", "Tétrade maior 7M · maj7", Family.ARPEGGIOS, "0 4 7 11", "T 3 5 7", "Tônica, terça maior, quinta justa e sétima maior. Acorde de Jônio e Lídio.");
        add(f, "min7", "Tétrade menor 7 · m7", Family.ARPEGGIOS, "0 3 7 10", "T b3 5 b7", "Tônica, terça menor, quinta justa e sétima menor. Acorde de Dórico, Frígio e Eólio.");
        add(f, "dom7", "Tétrade dominante · 7", Family.ARPEGGIOS, "0 4 7 10", "T 3 5 b7", "Tríade maior com sétima menor; acorde do Mixolídio.");
        add(f, "halfdim7", "Tétrade meio-diminuta · m7(b5)", Family.ARPEGGIOS, "0 3 6 10", "T b3 b5 b7", "Tríade diminuta com sétima menor; acorde do Lócrio.");
        add(f, "dim7", "Tétrade diminuta · dim7", Family.ARPEGGIOS, "0 3 6 9", "T b3 b5 bb7", "Terças menores empilhadas: sétima diminuta (bb7), não sexta na grafia do acorde.");
        add(f, "minmaj7", "Tétrade menor 7M · m(maj7)", Family.ARPEGGIOS, "0 3 7 11", "T b3 5 7", "Tríade menor com sétima maior; primeiro grau da menor harmônica e melódica.");
        add(f, "augmaj7", "Tétrade aumentada 7M · +maj7", Family.ARPEGGIOS, "0 4 8 11", "T 3 #5 7", "Tríade aumentada com sétima maior.");
        add(f, "aug7", "Tétrade aumentada 7 · +7", Family.ARPEGGIOS, "0 4 8 10", "T 3 #5 b7", "Tríade aumentada com sétima menor.");
        add(f, "maj9", "Maior 9 · maj9", Family.ARPEGGIOS, "0 2 4 7 11", "T 9 3 5 7", "Tétrade maj7 com nona maior. A nona aparece na sua classe de altura no braço.");
        add(f, "min9", "Menor 9 · m9", Family.ARPEGGIOS, "0 2 3 7 10", "T 9 b3 5 b7", "Tétrade m7 com nona maior.");
        add(f, "dom9", "Dominante 9 · 9", Family.ARPEGGIOS, "0 2 4 7 10", "T 9 3 5 b7", "Tétrade dominante com nona maior.");
        add(f, "maj7sharp11", "Maior 7M com #11", Family.ARPEGGIOS, "0 4 6 7 11", "T 3 #11 5 7", "Tétrade maj7 com décima primeira aumentada; extensão característica do Lídio.");
        add(f, "min11", "Menor 11 · m11", Family.ARPEGGIOS, "0 2 3 5 7 10", "T 9 b3 11 5 b7", "Tétrade menor com nona e décima primeira justas.");
        add(f, "dom13", "Dominante 13 · 13", Family.ARPEGGIOS, "0 2 4 7 9 10", "T 9 3 5 13 b7", "Tétrade dominante com nona e décima terceira; a 11ª é omitida para evitar o choque com a terça.");
        add(f, "ionian-extensions", "Jônio · maj7 + 9, 11, 13", Family.ARPEGGIOS, "0 2 4 5 7 9 11", "T 9 3 11 5 13 7", "Tétrade e todas as extensões diatônicas do Jônio. A 11ª é uma tensão de passagem sobre a terça maior, não uma indicação de tocar tudo simultaneamente.");
        add(f, "dorian-extensions", "Dórico · m7 + 9, 11, 13", Family.ARPEGGIOS, "0 2 3 5 7 9 10", "T 9 b3 11 5 13 b7", "Tétrade menor e extensões diatônicas do Dórico; a 13ª maior é sua cor característica.");
        add(f, "phrygian-extensions", "Frígio · m7 + b9, 11, b13", Family.ARPEGGIOS, "0 1 3 5 7 8 10", "T b9 b3 11 5 b13 b7", "Tétrade menor e extensões diatônicas do Frígio; a nona menor exige atenção ao contexto harmônico.");
        add(f, "lydian-extensions", "Lídio · maj7 + 9, #11, 13", Family.ARPEGGIOS, "0 2 4 6 7 9 11", "T 9 3 #11 5 13 7", "Tétrade maior com as extensões diatônicas do Lídio; a #11 é sua cor característica.");
        add(f, "mixolydian-extensions", "Mixolídio · 7 + 9, 11, 13", Family.ARPEGGIOS, "0 2 4 5 7 9 10", "T 9 3 11 5 13 b7", "Tétrade dominante e extensões diatônicas do Mixolídio. A 11ª e a terça maior não são automaticamente uma disposição de acorde confortável.");
        add(f, "aeolian-extensions", "Eólio · m7 + 9, 11, b13", Family.ARPEGGIOS, "0 2 3 5 7 8 10", "T 9 b3 11 5 b13 b7", "Tétrade menor e extensões da menor natural; sexta menor equivale à b13.");
        add(f, "locrian-extensions", "Lócrio · m7(b5) + b9, 11, b13", Family.ARPEGGIOS, "0 1 3 5 6 8 10", "T b9 b3 11 b5 b13 b7", "Tétrade meio-diminuta e extensões diatônicas do Lócrio. As extensões são notas de estudo, não uma ordem para tocar todas ao mesmo tempo.");

        add(f, "major", "Maior", Family.SCALES, "0 2 4 5 7 9 11", "T 2 3 4 5 6 7", "Escala maior diatônica; mesma fórmula do modo Jônio.");
        add(f, "natural-minor", "Menor natural", Family.SCALES, "0 2 3 5 7 8 10", "T 2 b3 4 5 b6 b7", "Escala menor natural; mesma fórmula do modo Eólio.");
        add(f, "harmonic-minor", "Menor harmônica", Family.SCALES, "0 2 3 5 7 8 11", "T 2 b3 4 5 b6 7", "Menor natural com sétima maior. Gera dominante maior no quinto grau.");
        add(f, "melodic-minor", "Menor melódica (ascendente)", Family.SCALES, "0 2 3 5 7 9 11", "T 2 b3 4 5 6 7", "Forma ascendente/jazz: sexta e sétima maiores. A forma clássica descendente usa a menor natural.");
        ALL = List.copyOf(f);
    }
    private Catalog() {}

    private static void add(List<Formula> all, String id, String name, Family family, String intervals, String degrees, String description) {
        List<Integer> numbers = java.util.Arrays.stream(intervals.split(" ")).map(Integer::valueOf).toList();
        all.add(new Formula(id, name, family, numbers, List.of(degrees.split(" ")), description));
    }

    public static List<Formula> forFamily(Family family) {
        return ALL.stream().filter(f -> f.family() == family).toList();
    }

    public static List<Formula> modes() { return forFamily(Family.MODES); }

    public static Formula byId(String id) {
        return ALL.stream().filter(f -> f.id().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Fórmula desconhecida: " + id));
    }
}
