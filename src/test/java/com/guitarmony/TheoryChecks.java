package com.guitarmony;

import com.guitarmony.fretboard.Cell;
import com.guitarmony.fretboard.Position;
import com.guitarmony.fretboard.ShapeEngine;
import com.guitarmony.music.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Dependency-free regression suite. Every failed check exits with an error. */
public final class TheoryChecks {
    private static int assertions;
    private static int passed;
    private static final List<String> failures = new ArrayList<>();

    public static void main(String[] args) {
        run("fórmulas dos sete modos", TheoryChecks::modeFormulas);
        run("pentatônicas e blue notes", TheoryChecks::pentatonicAndBlues);
        run("grafia diatônica e enarmonias", TheoryChecks::spelling);
        run("catálogo consistente em todos os tons", TheoryChecks::catalogConsistency);
        run("afinação EADGBE e 24 casas", TheoryChecks::standardTuning);
        run("transposição e afinação individual", TheoryChecks::retuning);
        run("campo maior: tríades e tétrades", TheoryChecks::majorHarmony);
        run("campos menores natural, harmônico e melódico", TheoryChecks::minorHarmony);
        run("voicings completos, tocáveis e adaptados à afinação", TheoryChecks::voicings);
        run("mapa completo: presença e ausência de cada nota", TheoryChecks::allNotesMap);
        run("desenho Jônio em Mi: três notas por corda", TheoryChecks::ionianShape);
        run("sequência modal preserva campo e muda desenho", TheoryChecks::modalSequence);
        run("cinco desenhos pentatônicos distintos", TheoryChecks::pentatonicBoxes);
        run("pentablues: base pentatônica mais blue note", TheoryChecks::bluesBoxes);
        run("desenhos adaptam-se à afinação", TheoryChecks::alternateTuning);
        run("limites do braço, duas oitavas e posições altas", TheoryChecks::fretLimits);
        run("imutabilidade e entradas inválidas", TheoryChecks::invalidInputs);
        System.out.printf("%n%d grupos passaram; %d falharam; %d verificações executadas.%n",
                passed, failures.size(), assertions);
        if (!failures.isEmpty()) {
            failures.forEach(System.err::println);
            throw new AssertionError("A validação do Guitarmony falhou.");
        }
        if (assertions == 0 || passed == 0) throw new AssertionError("Nenhum teste executado.");
    }

    private static void modeFormulas() {
        int[][] expected = {
                {0, 2, 4, 5, 7, 9, 11}, {0, 2, 3, 5, 7, 9, 10},
                {0, 1, 3, 5, 7, 8, 10}, {0, 2, 4, 6, 7, 9, 11},
                {0, 2, 4, 5, 7, 9, 10}, {0, 2, 3, 5, 7, 8, 10},
                {0, 1, 3, 5, 6, 8, 10}
        };
        equal(7, Catalog.modes().size(), "sete modos");
        for (int i = 0; i < expected.length; i++) {
            List<Integer> actual = Catalog.modes().get(i).intervals();
            equal(ints(expected[i]), actual, "fórmula modal " + i);
            // Each mode is a rotation of one parent major scale.
            for (int degree = 0; degree < 7; degree++) {
                equal(expected[0][(degree + i) % 7] - expected[0][i] < 0
                        ? expected[0][(degree + i) % 7] - expected[0][i] + 12
                        : expected[0][(degree + i) % 7] - expected[0][i],
                        actual.get(degree), "rotação modal " + i + "/" + degree);
            }
        }
    }

    private static void pentatonicAndBlues() {
        equal(List.of(0, 2, 4, 7, 9), Catalog.byId("penta-major").intervals(), "penta maior");
        equal(List.of(0, 3, 5, 7, 10), Catalog.byId("penta-minor").intervals(), "penta menor");
        equal(List.of(0, 2, 3, 4, 7, 9), Catalog.byId("blues-major").intervals(), "blues maior");
        equal(List.of(0, 3, 5, 6, 7, 10), Catalog.byId("blues-minor").intervals(), "blues menor");
        check(Catalog.byId("blues-major").intervals().contains(3), "blue note b3 maior");
        check(Catalog.byId("blues-minor").intervals().contains(6), "blue note #4 menor");
        equal(List.of(0, 3, 5, 7, 9), Catalog.byId("penta-minor6").intervals(), "penta menor6");
        equal(List.of(0, 4, 5, 7, 10), Catalog.byId("penta-dominant").intervals(), "penta dominante");
        equal(List.of(0, 3, 5, 6, 10), Catalog.byId("penta-halfdim").intervals(), "penta meio-diminuta");
    }

    private static void spelling() {
        Formula major = Catalog.byId("ionian");
        equal(List.of("E", "F#", "G#", "A", "B", "C#", "D#"), Notes.spell("E", major), "Mi Jônio");
        equal(List.of("F#", "G#", "A#", "B", "C#", "D#", "E#"), Notes.spell("F#", major), "F# precisa de E#");
        equal(List.of("C#", "D#", "E#", "F#", "G#", "A#", "B#"), Notes.spell("C#", major), "C# precisa de B#");
        equal(List.of("Cb", "Db", "Eb", "Fb", "Gb", "Ab", "Bb"), Notes.spell("Cb", major), "Cb precisa de Fb");
        equal(List.of("F", "G", "A", "Bb", "C", "D", "E"), Notes.spell("F", major), "F usa Bb");
        equal(Notes.pitch("A#"), Notes.pitch("Bb"), "enarmonia A#/Bb");
        equal(0, Notes.pitch("B#"), "B# = C");
        equal(4, Notes.pitch("Fb"), "Fb = E");
        equal("Db", Notes.chromatic(1, true), "preferência bemol");
        equal("C#", Notes.chromatic(1, false), "preferência sustenido");
    }

    private static void catalogConsistency() {
        Set<String> ids = new HashSet<>();
        for (Family family : Family.values()) {
            check(!Catalog.forFamily(family).isEmpty(), "família preenchida " + family);
            for (Formula f : Catalog.forFamily(family)) {
                check(ids.add(f.id()), "identificador único " + f.id());
                equal(f.intervals().size(), f.degrees().size(), "graus correspondem a intervalos");
                equal(0, f.intervals().get(0), "tônica no início");
                equal(f.intervals().size(), new HashSet<>(f.intervals()).size(), "notas distintas");
                for (String root : Notes.roots()) {
                    List<String> names = Notes.spell(root, f);
                    equal(f.intervals().size(), names.size(), "número de notas " + f.id());
                    for (int i = 0; i < names.size(); i++) {
                        equal(Math.floorMod(Notes.pitch(root) + f.intervals().get(i), 12),
                                Notes.pitch(names.get(i)), "altura da grafia " + root + " " + f.id());
                    }
                }
            }
        }
    }

    private static void standardTuning() {
        int[] midi = {40, 45, 50, 55, 59, 64};
        Tuning tuning = Tuning.standard();
        for (int string = 0; string < 6; string++) {
            for (int fret = 0; fret <= 24; fret++) {
                equal(midi[string] + fret, tuning.midiAt(string, fret), "MIDI da casa");
                equal((midi[string] + fret) % 12, tuning.pitchAt(string, fret), "altura da casa");
            }
            equal(tuning.pitchAt(string, 0), tuning.pitchAt(string, 12), "primeira oitava");
            equal(tuning.pitchAt(string, 0), tuning.pitchAt(string, 24), "segunda oitava");
        }
        equal(7, tuning.pitchAt(3, 0), "terceira corda G");
        equal(11, tuning.pitchAt(4, 0), "segunda corda B: salto de terça");
    }

    private static void retuning() {
        Tuning standard = Tuning.standard();
        Tuning up = standard.shift(2), down = standard.shift(-2);
        for (int string = 0; string < 6; string++) {
            equal(standard.midiAt(string, 0) + 2, up.midiAt(string, 0), "+ um tom = dois semitons");
            equal(standard.midiAt(string, 0) - 2, down.midiAt(string, 0), "- um tom = dois semitons");
        }
        Tuning dropD = standard.shiftString(0, -2);
        equal(38, dropD.midiAt(0, 0), "Drop D baixo");
        for (int i = 1; i < 6; i++) equal(standard.midiAt(i, 0), dropD.midiAt(i, 0), "demais cordas preservadas");
        equal(40, standard.midiAt(0, 0), "afinação original imutável");
        equal(standard, up.shift(-2), "transposição reversível");
        check(Tuning.presets().size() >= 3, "presets de afinação disponíveis");
    }

    private static void majorHarmony() {
        Formula major = Catalog.byId("major");
        List<HarmonicChord> triads = Harmony.harmonize("C", major, false);
        equal(List.of("C", "Dm", "Em", "F", "G", "Am", "Bdim"),
                triads.stream().map(HarmonicChord::name).toList(), "tríades de C");
        List<HarmonicChord> chords = Harmony.harmonize("C", major, true);
        equal(List.of("Cmaj7", "Dm7", "Em7", "Fmaj7", "G7", "Am7", "Bm7(b5)"),
                chords.stream().map(HarmonicChord::name).toList(), "tétrades de C");
        equal(List.of("C", "E", "G", "B"), chords.get(0).notes(), "Cmaj7 exato");
        equal(List.of("G", "B", "D", "F"), chords.get(4).notes(), "G7 exato");
        equal(List.of(0, 3, 6, 10), chords.get(6).formula().intervals(), "VII meio-diminuto");
        List<HarmonicChord> fSharp = Harmony.harmonize("F#", major, true);
        equal(List.of("F#", "A#", "C#", "E#"), fSharp.get(0).notes(), "F#maj7 com E#");
        for (int degree = 0; degree < chords.size(); degree++) {
            equal(degree + 1, chords.get(degree).degree(), "índice do grau");
        }
    }

    private static void minorHarmony() {
        List<HarmonicChord> natural = Harmony.harmonize("A", Catalog.byId("natural-minor"), true);
        equal(List.of("Am7", "Bm7(b5)", "Cmaj7", "Dm7", "Em7", "Fmaj7", "G7"),
                natural.stream().map(HarmonicChord::name).toList(), "menor natural em A");
        List<HarmonicChord> harmonic = Harmony.harmonize("A", Catalog.byId("harmonic-minor"), true);
        equal(List.of("E", "G#", "B", "D"), harmonic.get(4).notes(), "dominante maior na menor harmônica");
        equal(List.of(0, 3, 7, 11), harmonic.get(0).formula().intervals(), "menor com sétima maior");
        equal(List.of(0, 3, 6, 9), harmonic.get(6).formula().intervals(), "VII diminuto harmônico");
        List<HarmonicChord> melodic = Harmony.harmonize("A", Catalog.byId("melodic-minor"), true);
        equal(List.of("A", "C", "E", "G#"), melodic.get(0).notes(), "tônica menor melódica");
        equal(List.of(0, 4, 8, 11), melodic.get(2).formula().intervals(), "III aumentado melódico");
        equal(List.of(0, 3, 6, 10), melodic.get(5).formula().intervals(), "VI meio-diminuto melódico");
    }

    private static void allNotesMap() {
        Tuning tuning = Tuning.standard();
        for (Formula f : List.of(Catalog.byId("ionian"), Catalog.byId("penta-minor"), Catalog.byId("blues-major"), Catalog.byId("maj7"))) {
            for (int root = 0; root < 12; root++) {
                Set<Cell> actual = ShapeEngine.allNotes(f, root, tuning);
                int expectedCount = 0;
                for (int string = 0; string < 6; string++) {
                    for (int fret = 0; fret <= 24; fret++) {
                        boolean expected = f.intervals().contains(Math.floorMod(tuning.pitchAt(string, fret) - root, 12));
                        equal(expected, actual.contains(new Cell(string, fret)), "mapa exato, inclui e exclui");
                        if (expected) expectedCount++;
                    }
                }
                equal(expectedCount, actual.size(), "sem casas duplicadas ou externas");
            }
        }
    }

    private static void voicings() {
        String[] roots = {"C", "E", "A", "B"};
        String[] formulas = {"maj7", "triad-major", "min7", "halfdim7"};
        for (Tuning tuning : List.of(Tuning.standard(), Tuning.standard().shiftString(0,-2))) {
            for (int example = 0; example < roots.length; example++) {
                String root = roots[example];
                Formula chord = Catalog.byId(formulas[example]);
                List<Voicing> grips = VoicingEngine.find(root,chord,tuning,3);
                check(!grips.isEmpty(), "voicing disponível " + root + chord.name());
                check(grips.size() <= 8, "busca limitada a oito sugestões");
                Set<List<Integer>> distinct = new HashSet<>();
                for (Voicing grip : grips) {
                    equal(6, grip.frets().size(), "seis cordas no voicing");
                    equal(6, grip.notes().size(), "seis nomes no voicing");
                    check(distinct.add(grip.frets()), "voicings sem duplicatas");
                    Set<Integer> actual = new HashSet<>();
                    int first = 6, last = -1, low = 25, high = 0, sounding = 0;
                    for (int string = 0; string < 6; string++) {
                        int fret = grip.frets().get(string);
                        check(fret >= -1 && fret <= 24, "casa válida no voicing");
                        if (fret >= 0) {
                            sounding++;
                            first = Math.min(first,string);
                            last = Math.max(last,string);
                            int interval = Math.floorMod(tuning.pitchAt(string,fret)-Notes.pitch(root),12);
                            actual.add(interval);
                            check(chord.intervals().contains(interval), "voicing só contém notas do acorde");
                            equal(tuning.pitchAt(string,fret), Notes.pitch(grip.notes().get(string)), "nota do voicing respeita afinação");
                            if (fret > 0) { low = Math.min(low,fret); high = Math.max(high,fret); }
                        } else equal("—", grip.notes().get(string), "corda abafada sem nota");
                    }
                    equal(new HashSet<>(chord.intervals()), actual, "todas as notas do acorde presentes");
                    equal(sounding,last-first+1,"cordas tocadas contíguas");
                    check(high == 0 || high-low <= 4,"abertura da mão limitada a quatro casas");
                }
            }
        }
        equal(List.of(),VoicingEngine.find("C",Catalog.byId("ionian"),Tuning.standard(),0),"escala de sete notas não é um voicing");
        List<Voicing> cached = VoicingEngine.find("C",Catalog.byId("maj7"),Tuning.standard(),3);
        equal(cached, VoicingEngine.find("C",Catalog.byId("maj7"),Tuning.standard(),3), "busca repetida preserva sugestões");
        rejects(() -> cached.get(0).frets().add(0),"voicings imutáveis");
    }

    private static void ionianShape() {
        Formula ionian = Catalog.byId("ionian");
        Position p = ShapeEngine.shape(ionian, 4, Tuning.standard(), 0);
        int[][] frets = {{0,2,4}, {0,2,4}, {1,2,4}, {1,2,4}, {2,4,5}, {2,4,5}};
        Set<Cell> expected = new HashSet<>();
        for (int string = 0; string < 6; string++) {
            for (int fret : frets[string]) expected.add(new Cell(string, fret));
        }
        equal(expected, p.cells(), "Jônio 3NPS em Mi, inclusive correção G/B");
        assertPositionNotes(p, Tuning.standard());
        equal(18, p.cells().size(), "18 notas do desenho modal");
    }

    private static void modalSequence() {
        Tuning tuning = Tuning.standard();
        List<Position> positions = ShapeEngine.sequence(Catalog.byId("ionian"), 4, tuning, 0);
        check(positions.size() >= 7, "ciclo modal completo");
        int[] roots = {4, 6, 8, 9, 11, 1, 3};
        Set<Integer> parent = Set.of(4, 6, 8, 9, 11, 1, 3);
        for (int i = 0; i < 7; i++) {
            Position p = positions.get(i);
            equal(roots[i], p.rootPitch(), "tônica modal relativa " + i);
            equal(Catalog.modes().get(i).id(), p.formula().id(), "modo relativo " + i);
            for (Cell cell : p.cells()) check(parent.contains(tuning.pitchAt(cell.stringIndex(), cell.fret())), "mesmo campo maior");
        }
        check(!positions.get(0).cells().equals(positions.get(1).cells()), "não repinta o braço inteiro sete vezes");
        List<Position> dorian = ShapeEngine.sequence(Catalog.byId("dorian"), 6, tuning, 2);
        equal("dorian", dorian.get(0).formula().id(), "sequência começa no modo escolhido");
        for (Position p : dorian) {
            for (Cell cell : p.cells()) check(parent.contains(tuning.pitchAt(cell.stringIndex(), cell.fret())), "F# dórico pertence a E maior");
        }
    }

    private static void pentatonicBoxes() {
        Tuning tuning = Tuning.standard();
        List<Position> boxes = ShapeEngine.sequence(Catalog.byId("penta-minor"), 9, tuning, 5);
        check(boxes.size() >= 5, "cinco boxes pentatônicos");
        Set<Set<Cell>> different = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            Position p = boxes.get(i);
            assertPositionNotes(p, tuning);
            check(different.add(p.cells()), "box distinto " + i);
            for (int string = 0; string < 6; string++) {
                int s = string;
                equal(2L, p.cells().stream().filter(c -> c.stringIndex() == s).count(), "duas notas por corda");
            }
        }
        Position first = boxes.get(0);
        equal(Set.of(new Cell(0,5), new Cell(0,8)), first.cells().stream().filter(c -> c.stringIndex() == 0).collect(Collectors.toSet()), "A menor box1 baixo");

        // A high anchor must preserve the requested box, even when its first
        // bass note has to move to the preceding octave to fit on the neck.
        Position highBox = ShapeEngine.boxShape(Catalog.byId("penta-major"),0,tuning,12,4);
        int[][] expectedFrets = {{17,20},{17,19},{17,19},{17,19},{17,20},{17,20}};
        Set<Cell> expected = new HashSet<>();
        for (int string=0; string<6; string++) {
            for (int fret:expectedFrets[string]) expected.add(new Cell(string,fret));
        }
        equal(expected,highBox.cells(),"C maior desenho5 em região alta preserva o box");
        check(highBox.label().contains("desenho 5") && highBox.label().contains("oitava anterior"),"fallback da oitava explicado no rótulo");
        equal(9,tuning.pitchAt(0,17),"C maior box5 começa no sexto grau A");

        List<Formula> numbered = new ArrayList<>(Catalog.forFamily(Family.PENTATONICS));
        numbered.addAll(Catalog.forFamily(Family.BLUES));
        for (Formula selected:numbered) {
            Formula base = selected.family()!=Family.BLUES ? selected : Catalog.byId(switch(selected.id()) {
                case "blues-major" -> "penta-major";
                case "blues-dominant" -> "penta-dominant";
                case "blues-halfdim" -> "penta-halfdim";
                default -> "penta-minor";
            });
            for (int root=0;root<12;root++) {
                for (int anchor=0;anchor<=12;anchor++) {
                    // Enumerate the physical root positions independently of
                    // the shape engine, then check each requested degree.
                    List<Integer> physicalRoots = new ArrayList<>();
                    for(int fret=0;fret<=24;fret++) if(tuning.pitchAt(0,fret)==root) physicalRoots.add(fret);
                    final int reference=anchor;
                    int rootFret=physicalRoots.stream().filter(fret->fret>=reference).findFirst()
                            .orElse(physicalRoots.get(physicalRoots.size()-1));
                    Set<Set<Cell>> distinctBoxes=new HashSet<>();
                    for(int box=0;box<5;box++) {
                        int requestedBass=rootFret+base.intervals().get(box);
                        int expectedBass=requestedBass>24?requestedBass-12:requestedBass;
                        Position selectedBox=ShapeEngine.boxShape(selected,root,tuning,anchor,box);
                        int actualBass=selectedBox.cells().stream().filter(c->c.stringIndex()==0)
                                .mapToInt(Cell::fret).min().orElseThrow();
                        equal(expectedBass,actualBass,"desenho preserva primeiro grau e oitava físicos");
                        equal(Math.floorMod(root+base.intervals().get(box),12),tuning.pitchAt(0,actualBass),"primeiro baixo corresponde ao grau do box escolhido");
                        check(distinctBoxes.add(selectedBox.cells()),"cinco boxes continuam distintos em todos os tons e regiões");
                        equal(requestedBass>24,selectedBox.label().contains("oitava anterior"),"rótulo de oitava corresponde ao deslocamento real");
                        assertPositionNotes(selectedBox,tuning);
                    }
                }
            }
        }
        rejects(()->ShapeEngine.boxShape(Catalog.byId("penta-major"),0,tuning,12,5),"não existe sexto box pentatônico");
        rejects(()->ShapeEngine.boxShape(Catalog.byId("ionian"),0,tuning,0,0),"modo não usa seleção de box pentatônico");
    }

    private static void bluesBoxes() {
        Tuning tuning = Tuning.standard();
        List<Position> blues = ShapeEngine.sequence(Catalog.byId("blues-minor"), 9, tuning, 5);
        List<Position> penta = ShapeEngine.sequence(Catalog.byId("penta-minor"), 9, tuning, 5);
        check(blues.size() >= 5, "cinco pentablues");
        for (int i = 0; i < 5; i++) {
            Position b = blues.get(i);
            check(b.cells().containsAll(penta.get(i).cells()), "mantém base penta " + i);
            check(b.cells().size() > penta.get(i).cells().size(), "adiciona blue note " + i);
            for (Cell c : b.cells()) {
                int relative = Math.floorMod(tuning.pitchAt(c.stringIndex(), c.fret()) - 9, 12);
                check(Set.of(0,3,5,6,7,10).contains(relative), "sem nota inventada no blues");
            }
        }
    }

    private static void alternateTuning() {
        Tuning drop = Tuning.standard().shiftString(0, -2);
        Formula f = Catalog.byId("ionian");
        Position shape = ShapeEngine.shape(f, 4, drop, 0);
        assertPositionNotes(shape, drop);
        check(shape.cells().contains(new Cell(0,2)), "Mi fundamental em Drop D fica na casa2");
        check(!shape.cells().equals(ShapeEngine.shape(f,4,Tuning.standard(),0).cells()), "desenho recalculado ao afinar");
        for (Tuning tuning : Tuning.presets().values()) {
            for (Formula formula : List.of(f,Catalog.byId("penta-major"),Catalog.byId("blues-minor"),Catalog.byId("min7"))) {
                assertPositionNotes(ShapeEngine.shape(formula,2,tuning,0), tuning);
            }
        }
    }

    private static void fretLimits() {
        Tuning tuning = Tuning.standard();
        for (Formula f : List.of(Catalog.byId("ionian"),Catalog.byId("penta-major"),Catalog.byId("blues-minor"),Catalog.byId("maj7"),Catalog.byId("harmonic-minor"))) {
            for (int root = 0; root < 12; root++) {
                for (int anchor : new int[]{0, 12, 22, 24}) {
                    Position p = ShapeEngine.shape(f, root, tuning, anchor);
                    assertPositionNotes(p, tuning);
                    for (Position step : ShapeEngine.sequence(f,root,tuning,anchor)) assertPositionNotes(step,tuning);
                }
            }
        }
        Set<Cell> all = ShapeEngine.allNotes(Catalog.byId("ionian"),4,tuning);
        check(all.contains(new Cell(0,0)) && all.contains(new Cell(0,12)) && all.contains(new Cell(0,24)), "tônicas nas duas oitavas");
    }

    private static void invalidInputs() {
        rejects(() -> new Cell(-1,0), "corda negativa");
        rejects(() -> new Cell(0,25), "casa acima de24");
        rejects(() -> Tuning.standard().midiAt(6,0), "corda inexistente");
        rejects(() -> new Tuning(40,45), "afinação com número incorreto de cordas");
        rejects(() -> Harmony.harmonize("C",Catalog.byId("penta-major"),true), "penta não é campo diatônico de sete graus");
        rejects(() -> Catalog.byId("unknown"), "id desconhecido");
        rejects(() -> Catalog.byId("ionian").intervals().add(1), "fórmulas imutáveis");
    }

    private static void assertPositionNotes(Position p, Tuning tuning) {
        check(!p.cells().isEmpty(), "posição com notas " + p.label());
        for (Cell c : p.cells()) {
            check(c.stringIndex() >= 0 && c.stringIndex() < 6 && c.fret() >= 0 && c.fret() <= 24, "limite da posição");
            int relative = Math.floorMod(tuning.pitchAt(c.stringIndex(),c.fret()) - p.rootPitch(),12);
            check(p.formula().intervals().contains(relative), "nota pertence à fórmula " + p.label());
        }
    }

    private static List<Integer> ints(int[] input) {
        return java.util.Arrays.stream(input).boxed().toList();
    }

    private static void run(String name, Runnable action) {
        try { action.run(); passed++; System.out.println("PASSOU  " + name); }
        catch (Throwable error) {
            String message = "FALHOU  " + name + ": " + error;
            failures.add(message);
            System.err.println(message);
        }
    }

    private static void check(boolean value, String message) {
        assertions++;
        if (!value) throw new AssertionError(message);
    }

    private static void equal(Object expected, Object actual, String message) {
        check(java.util.Objects.equals(expected,actual), message + ": esperado=" + expected + "; obtido=" + actual);
    }

    private static void rejects(Runnable action, String message) {
        assertions++;
        try { action.run(); }
        catch (IllegalArgumentException | UnsupportedOperationException expected) { return; }
        throw new AssertionError("Entrada inválida foi aceita: " + message);
    }
}
