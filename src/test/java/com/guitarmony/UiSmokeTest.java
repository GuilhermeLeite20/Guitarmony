package com.guitarmony;

import com.guitarmony.fretboard.Cell;
import com.guitarmony.fretboard.ViewMode;
import com.guitarmony.music.Catalog;
import com.guitarmony.music.Family;
import com.guitarmony.music.Formula;
import com.guitarmony.music.HarmonicChord;
import com.guitarmony.music.Notes;
import com.guitarmony.music.Tuning;
import com.guitarmony.ui.Layer;
import com.guitarmony.ui.WorkspacePanel;
import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/** Exercises real Swing listeners on the EDT; no window server or third-party test library needed. */
public final class UiSmokeTest {
    private static int assertions;
    private static int groups;
    private UiSmokeTest() { }

    public static void main(String[] args) throws Exception {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            try {
                GuitarmonyApp.installLookAndFeel();
                WorkspacePanel ui = new WorkspacePanel();
                run(ui);
            } catch (Throwable error) {
                failure.set(error);
            }
        });
        if (failure.get() != null) throw new AssertionError("Falha na interação da interface", failure.get());
        System.out.println("Interface: " + groups + " grupos passaram; " + assertions + " verificações.");
    }

    private static void run(WorkspacePanel ui) {
        check(SwingUtilities.isEventDispatchThread(), "A interação precisa ocorrer na EDT");
        JComboBox<String> root = combo(ui, "Tônica");
        JComboBox<Formula> formula = combo(ui, "Fórmula");
        JComboBox<ViewMode> view = combo(ui, "Visualização");

        check(root.getSelectedItem().equals("E"), "O estudo inicial deve partir de E");
        check(formula.getSelectedItem().equals(Catalog.byId("ionian")), "Estudo inicial: Jônio");
        check(layers(ui).size() == 1, "Só uma seleção deve estar ativa inicialmente");
        assertPitchSet(layers(ui).get(0), tuning(ui), Set.of(4, 6, 8, 9, 11, 1, 3));
        check(!layers(ui).get(0).cells().contains(new Cell(0, 1)), "F não pertence a E Jônio");
        check(layers(ui).get(0).cells().contains(new Cell(0, 2)), "F# pertence a E Jônio");
        passed("Seleção inicial e ausência de notas estranhas");

        root.setSelectedItem("F");
        check(layers(ui).size() == 1, "Alterar o tom deve substituir, sem acumular estudos");
        check(layers(ui).get(0).root().equals("F"), "Tom atualizado na mesma interação");
        check(!layers(ui).get(0).cells().contains(new Cell(0, 2)), "F# do estudo anterior deve sair");
        check(layers(ui).get(0).cells().contains(new Cell(0, 1)), "F tônica precisa aparecer");
        assertPitchSet(layers(ui).get(0), tuning(ui), Set.of(5, 7, 9, 10, 0, 2, 4));
        formula.setSelectedItem(Catalog.byId("mixolydian"));
        assertPitchSet(layers(ui).get(0), tuning(ui), Set.of(5, 7, 9, 10, 0, 2, 3));
        passed("Troca imediata de tom e de modo");

        root.setSelectedItem("E");
        formula.setSelectedItem(Catalog.byId("ionian"));
        view.setSelectedItem(ViewMode.ONE_SHAPE);
        Layer oneShape = layers(ui).get(0);
        check(oneShape.cells().size() == 18, "Jônio padrão precisa de três notas nas seis cordas");
        for (int string = 0; string < 6; string++) {
            final int index = string;
            check(oneShape.cells().stream().filter(cell -> cell.stringIndex() == index).count() == 3,
                    "Cada corda deve mostrar três notas no desenho modal");
        }
        check(oneShape.cells().contains(new Cell(0, 0)), "Desenho parte da tônica na sexta corda");
        view.setSelectedItem(ViewMode.SEQUENCE);
        List<Layer> sequence = layers(ui);
        check(sequence.size() >= 7, "Sequência precisa conter os sete modos relativos");
        check(sequence.get(0).root().equals("E") && sequence.get(1).root().equals("F#"),
                "E Jônio é seguido por F# Dórico, mantendo o campo");
        Set<Set<Cell>> physicalPatterns = new HashSet<>();
        for (int index = 0; index < 7; index++) {
            Layer layer = sequence.get(index);
            assertPitchSet(layer, tuning(ui), Set.of(4, 6, 8, 9, 11, 1, 3));
            physicalPatterns.add(layer.cells());
        }
        check(physicalPatterns.size() == 7, "Os modos precisam de desenhos físicos distintos");
        passed("Desenho único e sequência modal relativa");

        view.setSelectedItem(ViewMode.ALL_NOTES);
        click(ui, "Sobrepor seleção");
        root.setSelectedItem("F");
        check(layers(ui).size() == 2, "Sobreposição explícita precisa preservar as duas escolhas");
        check(layers(ui).stream().map(Layer::root).collect(java.util.stream.Collectors.toSet()).equals(Set.of("E", "F")),
                "Camadas E e F devem coexistir somente depois de sobrepor");
        click(ui, "Limpar");
        check(layers(ui).isEmpty(), "Limpar deve remover todas as marcações");
        check(((List<?>) state(ui, "pinned")).isEmpty(), "Limpar precisa remover seleções fixadas");
        check(progression(ui).isEmpty(), "Limpar precisa remover a progressão");
        root.setSelectedItem("E");
        check(layers(ui).size() == 1, "Uma nova escolha deve reativar o estudo após limpar");
        passed("Sobreposição explícita e limpeza completa");

        Tuning original = tuning(ui);
        click(ui, "Subir afinação um tom");
        for (int string = 0; string < 6; string++) {
            check(tuning(ui).midiAt(string, 0) == original.midiAt(string, 0) + 2,
                    "+1 tom deve representar exatamente dois semitons em cada corda");
        }
        check(layers(ui).get(0).root().equals("E"), "Afinação não altera o tom escolhido");
        assertPitchSet(layers(ui).get(0), tuning(ui), Set.of(4, 6, 8, 9, 11, 1, 3));
        click(ui, "Descer afinação um tom");
        for (int string = 0; string < 6; string++) {
            check(tuning(ui).midiAt(string, 0) == original.midiAt(string, 0), "Transposição reversível");
        }
        JComboBox<String> preset = combo(ui, "Afinação");
        String alternative = Tuning.presets().keySet().stream().skip(1).findFirst().orElseThrow();
        preset.setSelectedItem(alternative);
        Tuning alternateTuning = Tuning.presets().get(alternative);
        for (int string = 0; string < 6; string++) {
            check(tuning(ui).midiAt(string, 0) == alternateTuning.midiAt(string, 0), "Preset deve aplicar sua afinação real");
        }
        preset.setSelectedIndex(0);
        passed("Transposição em tons e presets de afinação");

        Set<Cell> beforePreferences = layers(ui).get(0).cells();
        clickText(ui, "Escala escura");
        clickText(ui, "Ver intervalos");
        clickText(ui, "Notas de fundo");
        clickText(ui, "Preferir bemóis");
        check(layers(ui).get(0).cells().equals(beforePreferences), "Preferências visuais não mudam as notas do estudo");
        clickText(ui, "Escala escura");
        clickText(ui, "Ver intervalos");
        clickText(ui, "Notas de fundo");
        clickText(ui, "Preferir bemóis");
        passed("Preferências visuais preservam a teoria");

        click(ui, "Aba " + Family.PENTATONICS);
        formula.setSelectedItem(Catalog.byId("penta-minor"));
        view.setSelectedItem(ViewMode.ONE_SHAPE);
        check(layers(ui).get(0).cells().size() == 12, "Penta menor deve apresentar duas notas por corda");
        Set<Cell> firstPenta = layers(ui).get(0).cells();
        JComboBox<String> box = combo(ui, "Desenho");
        check(box.isEnabled() && box.getItemCount() == 5, "Cinco desenhos devem estar disponíveis");
        box.setSelectedIndex(1);
        check(!layers(ui).get(0).cells().equals(firstPenta), "Mudar de desenho deve alterar o mapa físico");
        assertPitchSet(layers(ui).get(0), tuning(ui), Set.of(4, 7, 9, 11, 2));
        root.setSelectedItem("C");
        formula.setSelectedItem(Catalog.byId("penta-major"));
        JSpinner anchor=(JSpinner)named(ui,"Casa inicial");
        anchor.setValue(12);
        box.setSelectedIndex(4);
        Layer highBox=layers(ui).get(0);
        int firstBass=highBox.cells().stream().filter(c->c.stringIndex()==0).mapToInt(Cell::fret).min().orElseThrow();
        check(firstBass==17,"C maior box5 em região alta começa em A na casa17, sem trocar por outro box");
        check(tuning(ui).pitchAt(0,firstBass)==9,"Primeira nota do box5 de C maior é o sexto grau A");
        check(highBox.label().contains("desenho 5") && highBox.label().contains("oitava anterior"),"Interface explica deslocamento do box escolhido para oitava anterior");
        assertPitchSet(highBox,tuning(ui),Set.of(0,2,4,7,9));
        anchor.setValue(0);
        root.setSelectedItem("E");
        click(ui, "Aba " + Family.BLUES);
        formula.setSelectedItem(Catalog.byId("blues-minor"));
        view.setSelectedItem(ViewMode.ALL_NOTES);
        assertPitchSet(layers(ui).get(0), tuning(ui), Set.of(4, 7, 9, 10, 11, 2));
        check(layers(ui).get(0).cells().contains(new Cell(0, 6)), "Blue note Bb deve estar visível em E blues menor");
        passed("Abas pentatônicas, boxes e blue note");

        click(ui, "Aba Acordes");
        JComboBox<HarmonicChord> chord = combo(ui, "Acorde do campo");
        check(chord.getItemCount() == 7, "Campo precisa calcular sete acordes");
        check(layers(ui).size() == 1, "Acordes inicia mostrando só um acorde");
        check(!((JPanel) state(ui, "chordCards")).isVisible(), "Lista completa deve iniciar recolhida");
        root.setSelectedItem("F#");
        check(((HarmonicChord) chord.getSelectedItem()).name().startsWith("F#"), "Tom deve atualizar acorde imediatamente");
        check(layers(ui).get(0).root().equals("F#"), "Camada precisa acompanhar a cifra");
        assertPitchSet(layers(ui).get(0), tuning(ui), Set.of(6, 10, 1, 5));
        click(ui, "Mostrar campo completo");
        JPanel chordCards = (JPanel) state(ui, "chordCards");
        check(chordCards.isVisible() && chordCards.getComponentCount() == 7, "Exibir campo revela os sete graus");
        check(layers(ui).size() == 1, "Abrir a lista não polui o braço com sete acordes");
        click(ui, "Mostrar campo completo");
        passed("Campo harmônico recolhido e tom F#");

        click(ui, "Voicing tocável");
        check(!((List<?>) state(ui, "voicings")).isEmpty(), "Uma posição completa de F#maj7 deve ser encontrada");
        Layer gripLayer = layers(ui).get(0);
        check(gripLayer.cells().size() >= 4 && gripLayer.cells().size() <= 6, "Voicing precisa de quatro a seis cordas");
        check(gripLayer.cells().stream().map(Cell::stringIndex).distinct().count() == gripLayer.cells().size(),
                "Um voicing pode indicar só uma casa por corda");
        assertPitchSet(gripLayer, tuning(ui), Set.of(6, 10, 1, 5));
        check(pitches(gripLayer, tuning(ui)).equals(Set.of(6, 10, 1, 5)), "Voicing precisa conter todos os sons da tétrade");
        click(ui, "Voicing tocável");
        check(layers(ui).get(0).cells().size() > 6, "Desativar voicing retorna ao mapa de alternativas");
        passed("Voicing completo e retorno ao mapa de notas");

        click(ui, "Adicionar acorde à progressão");
        chord.setSelectedIndex(3);
        click(ui, "Adicionar acorde à progressão");
        List<HarmonicChord> queue = List.copyOf(progression(ui));
        check(queue.size() == 2 && queue.get(0).degree() == 1 && queue.get(1).degree() == 4,
                "Fila deve preservar a ordem dos acordes escolhidos");
        click(ui, "Visualizar progressão");
        check(layers(ui).size() == 2, "Visualizar fila mostra os dois acordes selecionados");
        JComboBox<String> suggested = combo(ui, "Progressão sugerida");
        suggested.setSelectedIndex(2);
        check(progression(ui).stream().map(HarmonicChord::degree).toList().equals(List.of(2, 5, 1)),
                "Sugestão II-V-I precisa preservar graus e ordem");
        root.setSelectedItem("G");
        check(progression(ui).isEmpty(), "Tom novo precisa remover acordes do campo anterior");
        check(layers(ui).size() == 1 && layers(ui).get(0).root().equals("C"),
                "Ao mudar o tom, o grau selecionado IV deve acompanhar o novo campo");
        check(!button(ui, "Visualizar progressão").isEnabled(), "Sequência vazia deve ficar indisponível");
        passed("Fila ordenada, sugestões e substituição de contexto");

        chord.setSelectedIndex(0);
        for (int i = 0; i < 17; i++) {
            chord.setSelectedIndex(i % 7);
            click(ui, "Adicionar acorde à progressão");
        }
        check(progression(ui).size() == 16, "Fila deve ser limitada a dezesseis passos");
        check(progression(ui).getFirst().degree() == 2, "FIFO descarta só o acorde mais antigo");
        click(ui, "Limpar");
        check(layers(ui).isEmpty() && progression(ui).isEmpty(), "Limpar também funciona em acordes");
        passed("Limite FIFO e limpeza na aba de acordes");

        click(ui, "Aba " + Family.MODES);
        view.setSelectedItem(ViewMode.ALL_NOTES);
        for (int i = 0; i < 8; i++) root.setSelectedItem(i % 2 == 0 ? "E" : "F");
        long worst = 0;
        long start = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            String selected = i % 2 == 0 ? "E" : "F";
            long interaction = System.nanoTime();
            root.setSelectedItem(selected);
            worst = Math.max(worst, System.nanoTime() - interaction);
            check(layers(ui).size() == 1 && layers(ui).get(0).root().equals(selected),
                    "Cada troca rápida deve concluir sincronicamente, sem seleção antiga");
        }
        System.out.printf("100 trocas reais de tom: %.1f ms no total; pior interação %.1f ms.%n",
                (System.nanoTime() - start) / 1_000_000.0, worst / 1_000_000.0);
        passed("Cem interações consecutivas sem estado atrasado");

        ui.setSize(1540, 1020);
        layout(ui);
        BufferedImage image = new BufferedImage(1540, 1020, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try { ui.printAll(g); } finally { g.dispose(); }
        check(image.getRGB(10, 10) != 0, "A interface precisa pintar um conteúdo real");
        passed("Renderização completa em memória");
    }

    private static void assertPitchSet(Layer layer, Tuning tuning, Set<Integer> allowed) {
        check(!layer.cells().isEmpty(), "Estudo precisa marcar notas");
        for (Cell cell : layer.cells()) {
            check(allowed.contains(tuning.pitchAt(cell.stringIndex(), cell.fret())),
                    "Nota fora do contexto em " + cell + " da camada " + layer.label());
        }
        check(pitches(layer, tuning).equals(allowed), "O mapa deve conter todos os sons da estrutura");
    }

    private static Set<Integer> pitches(Layer layer, Tuning tuning) {
        Set<Integer> result = new HashSet<>();
        for (Cell cell : layer.cells()) result.add(tuning.pitchAt(cell.stringIndex(), cell.fret()));
        return result;
    }

    @SuppressWarnings("unchecked")
    private static List<Layer> layers(WorkspacePanel ui) { return (List<Layer>) state(ui, "currentLayers"); }
    private static Tuning tuning(WorkspacePanel ui) { return (Tuning) state(ui, "tuning"); }
    @SuppressWarnings("unchecked")
    private static Deque<HarmonicChord> progression(WorkspacePanel ui) {
        return (Deque<HarmonicChord>) state(ui, "progression");
    }
    private static Object state(Object instance, String name) {
        try {
            Field field = instance.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(instance);
        } catch (ReflectiveOperationException e) { throw new AssertionError("Estado indisponível: " + name, e); }
    }

    @SuppressWarnings("unchecked")
    private static <T> JComboBox<T> combo(Container ui, String name) { return (JComboBox<T>) named(ui, name); }
    private static AbstractButton button(Container ui, String name) { return (AbstractButton) named(ui, name); }
    private static void click(Container ui, String name) {
        AbstractButton button = button(ui, name);
        check(button.isEnabled(), "Controle deve estar disponível: " + name);
        button.doClick(0);
    }
    private static void clickText(Container ui, String text) {
        for (Component component : descendants(ui)) {
            if (component instanceof AbstractButton button && text.equals(button.getText())) {
                check(button.isEnabled(), "Preferência deve estar habilitada: " + text);
                button.doClick(0);
                return;
            }
        }
        throw new AssertionError("Controle não encontrado: " + text);
    }
    private static JComponent named(Container root, String name) {
        for (Component component : descendants(root)) {
            if (component instanceof JComponent candidate && name.equals(candidate.getName())) return candidate;
        }
        throw new AssertionError("Componente não encontrado: " + name);
    }
    private static List<Component> descendants(Container root) {
        List<Component> result = new ArrayList<>();
        for (Component child : root.getComponents()) {
            result.add(child);
            if (child instanceof Container container) result.addAll(descendants(container));
        }
        return result;
    }
    private static void layout(Container component) {
        component.doLayout();
        for (Component child : component.getComponents()) if (child instanceof Container c) layout(c);
    }
    private static void check(boolean condition, String message) {
        assertions++;
        if (!condition) throw new AssertionError(message);
    }
    private static void passed(String name) { groups++; System.out.println("  ✓ " + name); }
}
