package com.guitarmony.ui;

import com.guitarmony.fretboard.*;
import com.guitarmony.music.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** One source of selection state. Changes replace the active study immediately. */
public final class WorkspacePanel extends JPanel {
    private final JComboBox<String> root = new JComboBox<>(Notes.roots().toArray(String[]::new));
    private final JComboBox<Formula> formula = new JComboBox<>();
    private final JComboBox<ViewMode> view = new JComboBox<>(ViewMode.values());
    private final JSpinner anchor = new JSpinner(new SpinnerNumberModel(0,0,12,1));
    private final JComboBox<String> box = new JComboBox<>();
    private final JComboBox<String> tuningPreset = new JComboBox<>(Tuning.presets().keySet().toArray(String[]::new));
    private final JCheckBox darkWood = check("Escala escura",false);
    private final JCheckBox intervals = check("Ver intervalos",false);
    private final JCheckBox allNotes = check("Notas de fundo",true);
    private final JCheckBox flats = check("Preferir bemóis",false);
    private final JCheckBox sevenths = check("Tétrades",true);
    private final JCheckBox wholeField = check("Mostrar os 7 acordes",false);
    private final JCheckBox grip = check("Voicing tocável",false);
    private final JComboBox<HarmonicChord> chord = new JComboBox<>();
    private final JComboBox<Voicing> voicing = new JComboBox<>();
    private final JComboBox<String> progressionPreset = new JComboBox<>(new String[]{"Progressões sugeridas…","I · IV · V","II · V · I","I · V · VI · IV"});
    private final FretboardPanel board = new FretboardPanel();
    private final JLabel studyTitle = Style.text("",25,true);
    private final JLabel studySubtitle = Style.muted("");
    private final JLabel tuningLabel = Style.muted("");
    private final JLabel status = Style.muted("Clique em uma nota para inspecionar sua posição e seus intervalos.");
    private final JTextArea explanation = new JTextArea();
    private final JPanel noteChips = Style.row(7);
    private final JPanel legend = Style.column();
    private final JPanel chordCards = Style.row(8);
    private final JPanel progressionRow = Style.row(6);
    private final JPanel harmonyControls = Style.column();
    private final JPanel layersPanel = Style.column();
    private final JPanel tabs = Style.row(6);
    private final List<JButton> tabButtons = new ArrayList<>();
    private final List<Study> pinned = new ArrayList<>();
    private final Deque<HarmonicChord> progression = new ArrayDeque<>();
    private List<HarmonicChord> harmonized = List.of();
    private List<Voicing> voicings = List.of();
    private List<Layer> currentLayers = List.of();
    private Tuning tuning = Tuning.standard();
    private Family family = Family.MODES;
    private boolean harmonyTab;
    private boolean updating;
    private boolean cleared;
    private boolean progressionDisplay;
    private final JButton pin = Style.button("+ Sobrepor seleção",false);
    private final JButton queue = Style.button("+ Na progressão",false);
    private final JButton sequenceChords = Style.button("Ver sequência",false);
    private JScrollPane page;

    private record Study(String root,Formula formula,ViewMode view,int anchor,int box) {}

    public WorkspacePanel() {
        super(new BorderLayout());
        setBackground(Style.BG);
        Style.name(this,"Área de estudo Guitarmony");
        setBorder(new EmptyBorder(0,24,12,24));
        add(buildHeader(),BorderLayout.NORTH);
        JPanel content = new StudyPage();
        content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));content.setOpaque(false);
        content.add(buildTabs()); content.add(Box.createVerticalStrut(14));
        content.add(buildSelection()); content.add(Box.createVerticalStrut(14));
        content.add(buildBoard()); content.add(Box.createVerticalStrut(14));
        content.add(buildBottom());
        page = new JScrollPane(content);
        page.setBorder(null); page.setOpaque(false);page.getViewport().setOpaque(false);
        page.getVerticalScrollBar().setUnitIncrement(22);
        page.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(page,BorderLayout.CENTER);
        status.setBorder(new EmptyBorder(10,4,0,0)); add(status,BorderLayout.SOUTH);
        configureListeners();
        root.setSelectedItem("E");
        changeTab(Family.MODES,false);
        bindKeys();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(14,0)); header.setOpaque(false);
        header.setBorder(new EmptyBorder(22,0,22,0));
        JPanel brand=Style.row(12);
        JLabel logo=Style.text("G",32,true);logo.setForeground(Style.ACCENT);
        JPanel titles=Style.column(); titles.add(Style.text("Guitarmony",29,true));
        titles.add(Style.muted("ENXERGUE AS NOTAS. CONECTE A HARMONIA."));
        brand.add(logo);brand.add(titles);header.add(brand,BorderLayout.WEST);
        JPanel right=Style.row(12);
        JLabel badge=Style.text("●  OFFLINE · SEM ÁUDIO",11,true);badge.setForeground(new Color(69,130,111));
        JButton help=Style.button("Como estudar",false);Style.name(help,"Ajuda");
        help.addActionListener(e->showHelp());right.add(badge);right.add(help);
        header.add(right,BorderLayout.EAST);return header;
    }

    private JPanel buildTabs() {
        for (Family f: new Family[]{Family.MODES,Family.PENTATONICS,Family.BLUES,Family.ARPEGGIOS,Family.SCALES}) {
            JButton button=Style.button(f.toString(),false);Style.name(button,"Aba "+f);
            button.addActionListener(e->changeTab(f,false));tabs.add(button);tabButtons.add(button);
        }
        JButton chords=Style.button("Acordes & campos",false);Style.name(chords,"Aba Acordes");
        chords.addActionListener(e->changeTab(Family.SCALES,true));tabs.add(chords);tabButtons.add(chords);
        tabs.setAlignmentX(LEFT_ALIGNMENT);return tabs;
    }

    private JPanel buildSelection() {
        JPanel card=Style.card(new BorderLayout(10,12)); card.setAlignmentX(LEFT_ALIGNMENT);
        JPanel controls=Style.row(12);
        controls.add(field("TÔNICA / TOM",root,95));
        controls.add(field("O QUE ESTUDAR",formula,260));
        controls.add(field("VISUALIZAÇÃO",view,195));
        JPanel region=Style.row(6);anchor.setPreferredSize(new Dimension(58,34));
        anchor.setToolTipText("Primeira casa de referência. A tônica é posicionada na 6ª corda, a partir daqui.");
        region.add(anchor);box.setPreferredSize(new Dimension(145,34));region.add(box);
        controls.add(field("REGIÃO INICIAL / DESENHO",region,210));
        JPanel actions=Style.row(7);actions.setBorder(new EmptyBorder(19,0,0,0));
        actions.add(pin);JButton clear=Style.button("Limpar",false);Style.name(clear,"Limpar");
        clear.addActionListener(e->clearStudy());actions.add(clear);controls.add(actions);
        card.add(controls,BorderLayout.NORTH);
        harmonyControls.add(buildHarmonyControls());
        card.add(harmonyControls,BorderLayout.CENTER);
        Style.name(root,"Tônica");Style.name(formula,"Fórmula");Style.name(view,"Visualização");
        Style.name(anchor,"Casa inicial");Style.name(box,"Desenho");Style.name(pin,"Sobrepor seleção");
        return card;
    }

    private JPanel buildHarmonyControls() {
        JPanel p=Style.column();JPanel row=Style.row(10);
        chord.setRenderer(new DefaultListCellRenderer(){
            @Override public Component getListCellRendererComponent(JList<?> list,Object value,int index,boolean selected,boolean focus) {
                super.getListCellRendererComponent(list,value,index,selected,focus);
                if(value instanceof HarmonicChord c)setText(c.roman()+"   ·   "+c.name()); return this;
            }
        });
        voicing.setRenderer(new DefaultListCellRenderer(){
            @Override public Component getListCellRendererComponent(JList<?> list,Object value,int index,boolean selected,boolean focus) {
                super.getListCellRendererComponent(list,value,index,selected,focus);
                if(value instanceof Voicing v)setText(v.label()); return this;
            }
        });
        chord.setPreferredSize(new Dimension(180,34));voicing.setPreferredSize(new Dimension(240,34));
        row.add(sevenths);row.add(chord);row.add(wholeField);row.add(grip);row.add(voicing);row.add(queue);
        p.add(row);p.add(Box.createVerticalStrut(10));
        p.add(chordCards);p.add(Box.createVerticalStrut(8));
        JPanel progressionControls=Style.row(8);
        progressionPreset.setPreferredSize(new Dimension(195,32));
        progressionControls.add(Style.muted("SUA PROGRESSÃO"));progressionControls.add(progressionPreset);
        progressionControls.add(sequenceChords);
        JButton undo=Style.button("Remover último",false);undo.addActionListener(e->{
            progression.pollLast();if(progression.isEmpty())progressionDisplay=false;
            render();refreshProgression();
        });progressionControls.add(undo);
        p.add(progressionControls);p.add(Box.createVerticalStrut(8));p.add(progressionRow);
        Style.name(chord,"Acorde do campo");Style.name(wholeField,"Mostrar campo completo");
        Style.name(grip,"Voicing tocável");Style.name(voicing,"Forma do acorde");
        Style.name(progressionPreset,"Progressão sugerida");Style.name(queue,"Adicionar acorde à progressão");
        Style.name(sequenceChords,"Visualizar progressão");
        return p;
    }

    private JPanel buildBoard() {
        JPanel card=Style.card(new BorderLayout(0,10));card.setAlignmentX(LEFT_ALIGNMENT);
        JPanel heading=Style.row(28);
        JPanel title=Style.column();title.add(studyTitle);title.add(Box.createVerticalStrut(4));title.add(studySubtitle);
        heading.add(title);
        JPanel actions=Style.row(8);actions.add(darkWood);actions.add(intervals);actions.add(allNotes);
        JButton export=Style.button("Exportar PNG",false);export.addActionListener(e->export());
        Style.name(export,"Exportar PNG");actions.add(export);heading.add(actions);
        card.add(heading,BorderLayout.NORTH);
        JScrollPane neck=new JScrollPane(board);neck.setBorder(null);neck.getViewport().setBackground(Color.WHITE);
        neck.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        neck.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        neck.setPreferredSize(new Dimension(1300,356));neck.getHorizontalScrollBar().setUnitIncrement(55);
        card.add(neck,BorderLayout.CENTER);
        JPanel footer=Style.column();footer.add(noteChips);footer.add(Box.createVerticalStrut(12));
        JPanel tuningRow=Style.row(8);
        tuningPreset.setPreferredSize(new Dimension(230,32));Style.name(tuningPreset,"Afinação");
        tuningRow.add(Style.muted("AFINAÇÃO"));tuningRow.add(tuningPreset);
        JButton minus=Style.button("− 1 tom",false),plus=Style.button("+ 1 tom",false);
        minus.addActionListener(e->shiftTuning(-2));plus.addActionListener(e->shiftTuning(2));
        Style.name(minus,"Descer afinação um tom");Style.name(plus,"Subir afinação um tom");
        tuningRow.add(minus);tuningRow.add(plus);
        JButton individual=Style.button("Por corda…",false);individual.addActionListener(e->editTuning());
        tuningRow.add(individual);tuningRow.add(tuningLabel);tuningRow.add(flats);
        footer.add(tuningRow);card.add(footer,BorderLayout.SOUTH);
        board.setCellListener(cell->{
            String note=tuning.nameAt(cell.stringIndex(),cell.fret(),flats.isSelected());
            StringBuilder s=new StringBuilder("Corda ").append(6-cell.stringIndex()).append(" · casa ").append(cell.fret()).append(" · ").append(note);
            for(Layer layer:currentLayers) if(layer.cells().contains(cell)) {
                int idx=layer.formula().intervals().indexOf(Math.floorMod(tuning.pitchAt(cell.stringIndex(),cell.fret())-Notes.pitch(layer.root()),12));
                s.append("   |   ").append(layer.label()).append(": ").append(layer.formula().degrees().get(idx));
            }
            status.setText(s.toString());
        });
        return card;
    }

    private JPanel buildBottom() {
        JPanel bottom=new JPanel(new GridLayout(1,2,14,0));bottom.setOpaque(false);bottom.setAlignmentX(LEFT_ALIGNMENT);
        JPanel left=Style.card(new BorderLayout(0,12));
        left.add(Style.text("Camadas & desenhos",16,true),BorderLayout.NORTH);
        JPanel layerContent=Style.column();layerContent.add(layersPanel);layerContent.add(legend);
        JScrollPane scroll=new JScrollPane(layerContent);scroll.setBorder(null);scroll.setOpaque(false);scroll.getViewport().setOpaque(false);
        scroll.setPreferredSize(new Dimension(460,140));scroll.getVerticalScrollBar().setUnitIncrement(18);
        left.add(scroll,BorderLayout.CENTER);bottom.add(left);
        JPanel right=Style.card(new BorderLayout(0,12));right.add(Style.text("Entenda o que está vendo",16,true),BorderLayout.NORTH);
        explanation.setEditable(false);explanation.setLineWrap(true);explanation.setWrapStyleWord(true);
        explanation.setFocusable(false);
        ((javax.swing.text.DefaultCaret)explanation.getCaret()).setUpdatePolicy(javax.swing.text.DefaultCaret.NEVER_UPDATE);
        explanation.setOpaque(false);explanation.setForeground(Style.MUTED);explanation.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,13));
        explanation.setRows(6);right.add(explanation,BorderLayout.CENTER);bottom.add(right);
        return bottom;
    }

    private static JPanel field(String title,JComponent input,int width) {
        JPanel p=Style.column();p.add(Style.muted(title));p.add(Box.createVerticalStrut(5));
        input.setPreferredSize(new Dimension(width,34));input.setMaximumSize(new Dimension(width,34));
        input.setAlignmentX(LEFT_ALIGNMENT);p.add(input);return p;
    }
    private static JCheckBox check(String title,boolean selected) {
        JCheckBox c=new JCheckBox(title,selected);c.setOpaque(false);c.setForeground(Style.MUTED);
        c.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,12));return c;
    }

    private void configureListeners() {
        root.addActionListener(e->selectionChanged(true));
        formula.addActionListener(e->{if(!updating){configureBoxes();selectionChanged(true);}});
        view.addActionListener(e->selectionChanged(false));
        anchor.addChangeListener(e->selectionChanged(false));box.addActionListener(e->selectionChanged(false));
        chord.addActionListener(e->{if(!updating){cleared=false;progressionDisplay=false;updateVoicings();render();}});
        voicing.addActionListener(e->{if(!updating)render();});
        sevenths.addActionListener(e->selectionChanged(true));
        wholeField.addActionListener(e->{chordCards.setVisible(wholeField.isSelected());refreshChordCards();});
        grip.addActionListener(e->{updateVoicings();render();});
        for(JCheckBox toggle:new JCheckBox[]{darkWood,intervals,allNotes,flats})toggle.addActionListener(e->render());
        tuningPreset.addActionListener(e->{if(!updating){
            Tuning preset=Tuning.presets().get(tuningPreset.getSelectedItem());if(preset!=null){tuning=preset;tuningChanged();}
        }});
        pin.addActionListener(e->{
            if(cleared)return;
            if(pinned.size()>=6){status.setText("Até 6 seleções fixadas. Remova uma camada antes de adicionar outra.");return;}
            Study selection=study(); if(!pinned.contains(selection))pinned.add(selection);
            render();status.setText("Seleção fixada. Agora escolha outro modo, tom ou aba para comparar.");
        });
        queue.addActionListener(e->{HarmonicChord c=(HarmonicChord)chord.getSelectedItem();if(c!=null){
            if(progression.size()==16)progression.removeFirst();progression.addLast(c);refreshProgression();
            if(progressionDisplay)render();
        }});
        sequenceChords.addActionListener(e->{cleared=false;progressionDisplay=!progressionDisplay;render();});
        progressionPreset.addActionListener(e->{if(!updating&&progressionPreset.getSelectedIndex()>0){
            int[] indices=switch(progressionPreset.getSelectedIndex()){case 1->new int[]{0,3,4};case 2->new int[]{1,4,0};default->new int[]{0,4,5,3};};
            progression.clear();for(int i:indices)progression.addLast(harmonized.get(i));
            progressionDisplay=true;cleared=false;refreshProgression();render();
        }});
    }

    private void changeTab(Family next,boolean chordsTab) {
        updating=true; family=next;harmonyTab=chordsTab;progressionDisplay=false;cleared=false;
        formula.setModel(new DefaultComboBoxModel<>(Catalog.forFamily(next).toArray(Formula[]::new)));
        harmonyControls.setVisible(chordsTab);view.setEnabled(!chordsTab);box.setEnabled(!chordsTab);
        pin.setEnabled(!chordsTab);
        int selected=chordsTab?5:switch(next){case MODES->0;case PENTATONICS->1;case BLUES->2;case ARPEGGIOS->3;case SCALES->4;};
        for(int i=0;i<tabButtons.size();i++){
            tabButtons.get(i).setBackground(i==selected?new Color(232,228,252):Color.WHITE);
            tabButtons.get(i).setForeground(i==selected?Style.ACCENT:Style.INK);
        }
        updating=false;configureBoxes();selectionChanged(true);revalidate();
        if(page!=null)SwingUtilities.invokeLater(()->page.getViewport().setViewPosition(new Point(0,0)));
    }

    private void configureBoxes() {
        boolean previous=updating;updating=true;
        Formula f=(Formula)formula.getSelectedItem();
        if(f!=null&&(f.family()==Family.PENTATONICS||f.family()==Family.BLUES))
            box.setModel(new DefaultComboBoxModel<>(new String[]{"Desenho 1","Desenho 2","Desenho 3","Desenho 4","Desenho 5"}));
        else box.setModel(new DefaultComboBoxModel<>(new String[]{"Tônica na 6ª"}));
        updating=previous;
    }

    private void selectionChanged(boolean rebuildHarmony) {
        if(updating||formula.getSelectedItem()==null)return;
        cleared=false;progressionDisplay=false;
        if(harmonyTab&&rebuildHarmony) {
            updating=true;int selected=Math.max(0,chord.getSelectedIndex());
            harmonized=Harmony.harmonize((String)root.getSelectedItem(),(Formula)formula.getSelectedItem(),sevenths.isSelected());
            chord.setModel(new DefaultComboBoxModel<>(harmonized.toArray(HarmonicChord[]::new)));
            chord.setSelectedIndex(Math.min(selected,6));
            // A new key/scale replaces every chord from the old harmonic context.
            progression.clear();progressionPreset.setSelectedIndex(0);
            updating=false;refreshChordCards();refreshProgression();
        }
        if(harmonyTab)updateVoicings();render();
    }

    private Study study() {
        return new Study((String)root.getSelectedItem(),(Formula)formula.getSelectedItem(),
                (ViewMode)view.getSelectedItem(),(Integer)anchor.getValue(),Math.max(0,box.getSelectedIndex()));
    }

    private List<Layer> layersFor(Study study,int colorIndex) {
        List<Layer> result=new ArrayList<>();int pitch=Notes.pitch(study.root());
        if(study.view()==ViewMode.ALL_NOTES) {
            result.add(new Layer(study.root()+" "+study.formula().name(),study.root(),study.formula(),
                    ShapeEngine.allNotes(study.formula(),pitch,tuning),Style.COLORS[colorIndex%7]));
        } else if(study.view()==ViewMode.ONE_SHAPE) {
            Position position;
            if(study.formula().family()==Family.PENTATONICS||study.formula().family()==Family.BLUES) {
                position=ShapeEngine.boxShape(study.formula(),pitch,tuning,study.anchor(),study.box());
            } else position=ShapeEngine.shape(study.formula(),pitch,tuning,study.anchor());
            String boxName=study.formula().family()==Family.PENTATONICS||study.formula().family()==Family.BLUES?
                    " · desenho "+(study.box()+1)+(position.label().contains("oitava anterior")?" · oitava anterior":""):"";
            result.add(new Layer(study.root()+" "+study.formula().name()+boxName+" · casas "+position.startFret()+"–"+position.endFret(),
                    study.root(),study.formula(),position.cells(),Style.COLORS[colorIndex%7]));
        } else {
            List<Position> positions=ShapeEngine.sequence(study.formula(),pitch,tuning,study.anchor());
            for(int i=0;i<positions.size();i++){
                Position p=positions.get(i);
                String layerRoot=study.root();
                if(study.formula().family()==Family.MODES){
                    List<String> modeNames=Notes.spell(study.root(),study.formula());
                    layerRoot=modeNames.get(i%7);
                }
                String label=study.formula().family()==Family.MODES?
                        layerRoot+" "+p.formula().name()+(i>=7?" · oitava":""):p.label();
                result.add(new Layer(label,layerRoot,p.formula(),p.cells(),Style.COLORS[(colorIndex+i)%7]));
            }
        }
        return result;
    }

    private void render() {
        if(updating||formula.getSelectedItem()==null)return;
        Study current=study();List<Layer> result=new ArrayList<>();
        for(int i=0;i<pinned.size();i++)result.addAll(layersFor(pinned.get(i),i));
        List<Layer> active=new ArrayList<>();
        if(!cleared) {
            if(harmonyTab) {
                if(progressionDisplay) {
                    int i=0;for(HarmonicChord c:progression)active.add(chordLayer(c,i++));
                } else {
                    HarmonicChord c=(HarmonicChord)chord.getSelectedItem();if(c!=null){
                        if(grip.isSelected()&&voicing.getSelectedItem() instanceof Voicing v) {
                            Set<Cell> cells=new LinkedHashSet<>();for(int s=0;s<6;s++)if(v.frets().get(s)>=0)cells.add(new Cell(s,v.frets().get(s)));
                            active.add(new Layer(c.name()+" · "+v.label(),c.root(),c.formula(),cells,Style.COLORS[0]));
                        } else active.add(chordLayer(c,0));
                    }
                }
            } else active.addAll(layersFor(current,pinned.size()));
            for(Layer layer:active)if(result.stream().noneMatch(p->p.cells().equals(layer.cells())&&p.root().equals(layer.root())&&p.formula().equals(layer.formula())))result.add(layer);
        }
        currentLayers=List.copyOf(result);
        board.setData(tuning,currentLayers,darkWood.isSelected(),intervals.isSelected(),flats.isSelected(),allNotes.isSelected());
        tuningLabel.setText(tuning.summary()+"  ·  6ª → 1ª");
        studyTitle.setText(cleared?"Braço livre para explorar":harmonyTab?
                (progressionDisplay?"Sua progressão em "+current.root():chord.getSelectedItem() instanceof HarmonicChord c?c.name():"Campo harmônico"):
                current.root()+" "+current.formula().name());
        String subtitle=harmonyTab?(grip.isSelected()&&!progressionDisplay?"Voicing calculado · X = corda abafada · números = casas":"Notas do acorde · não é uma posição para tocar todas as bolinhas juntas"):
                current.view()==ViewMode.ALL_NOTES?"Todas as ocorrências das notas escolhidas · casas 0 a 24":
                current.view()==ViewMode.ONE_SHAPE?"Um desenho de referência · calculado para a afinação e a região escolhidas":
                current.formula().family()==Family.MODES?"Modos relativos do mesmo campo · desenhos diferentes · repetição na oitava":"Desenhos da mesma escala · cores por posição · repetição na oitava";
        if(cleared)subtitle="Escolha um tom ou uma fórmula para recomeçar. Afinação e preferências foram mantidas.";
        studySubtitle.setText(subtitle);updateChips(current,active);updateLayersLegend();updateExplanation(current);
        boolean positional=!harmonyTab&&current.view()!=ViewMode.ALL_NOTES;
        anchor.setEnabled(positional||(harmonyTab&&grip.isSelected()));
        box.setEnabled(positional&&current.view()==ViewMode.ONE_SHAPE&&box.getItemCount()>1);
        voicing.setEnabled(harmonyTab&&grip.isSelected()&&!progressionDisplay);
        sequenceChords.setText(progressionDisplay?"Ver acorde isolado":"Ver sequência");
        sequenceChords.setEnabled(!progression.isEmpty());queue.setEnabled(!cleared);
        revalidate();repaint();
    }

    private Layer chordLayer(HarmonicChord c,int index) {
        return new Layer(c.name(),c.root(),c.formula(),ShapeEngine.allNotes(c.formula(),Notes.pitch(c.root()),tuning),Style.COLORS[index%7]);
    }

    private void updateChips(Study current,List<Layer> active) {
        noteChips.removeAll();if(cleared){noteChips.add(Style.muted("Nenhuma seleção ativa."));return;}
        Formula f=current.formula();String r=current.root();
        if(harmonyTab&&chord.getSelectedItem() instanceof HarmonicChord c){f=c.formula();r=c.root();}
        List<String> names=Notes.spell(r,f);
        for(int i=0;i<names.size();i++) {
            JLabel chip=Style.text(" "+names.get(i)+"   "+f.degrees().get(i)+" ",12,i==0);
            chip.setOpaque(true);chip.setBackground(i==0?Style.COLORS[0]:new Color(242,242,249));
            chip.setBorder(new EmptyBorder(5,7,5,7));noteChips.add(chip);
        }
        noteChips.add(Style.muted("  T = tônica   ◇ = blue note   anéis = sobreposição"));
    }

    private void updateLayersLegend() {
        layersPanel.removeAll();legend.removeAll();
        for(int i=0;i<pinned.size();i++) {
            final int index=i;Study s=pinned.get(i);JPanel row=Style.row(8);
            JLabel title=Style.text("●  "+s.root()+" "+s.formula().name(),12,true);title.setForeground(Style.ACCENT);
            row.add(title);JButton remove=Style.button("Remover",false);remove.addActionListener(e->{pinned.remove(index);render();});row.add(remove);layersPanel.add(row);
        }
        if(currentLayers.isEmpty()){legend.add(Style.muted("As seleções aparecem aqui, com sua cor e região."));return;}
        for(Layer layer:currentLayers) {
            JPanel row=Style.row(8);JLabel dot=Style.text("●",18,true);dot.setForeground(layer.color().darker());row.add(dot);
            row.add(Style.text(layer.label()+"   ·   "+layer.cells().size()+" posições",12,false));legend.add(row);
        }
    }

    private void updateExplanation(Study current) {
        String extra;
        if(harmonyTab){
            extra="O campo é calculado empilhando terças da escala escolhida. Selecione um grau para ver só aquele acorde; abra os sete quando precisar. Adicione acordes à fila para montar sua progressão.\n\n"+
                    (grip.isSelected()?"Voicings mostram uma nota por corda, com até quatro casas de extensão (cordas soltas não contam). A cifra X indica corda abafada. São sugestões calculadas; confira sua ergonomia.":"No mapa, as bolinhas são alternativas para localizar os sons do acorde. Ative ‘Voicing tocável’ para ver uma posição de mão.");
            if(grip.isSelected()&&voicings.isEmpty())extra+="\nNenhum voicing completo encontrado nesta afinação/região. O mapa de notas continua disponível.";
        } else {
            extra=current.formula().description()+"\n\n"+switch(current.view()) {
                case ALL_NOTES->"Braço inteiro destaca todas as ocorrências. Mudar a tônica ou a fórmula substitui a seleção na hora. ‘Sobrepor seleção’ fixa o estudo atual para compará-lo com outra escolha.";
                case ONE_SHAPE->"Modos usam três notas por corda; pentas usam dois sons por corda, com cinco desenhos. No blues, a nota cromática é acrescentada ao box. Arpejos usam um mapa local de cinco casas, não um voicing.";
                case SEQUENCE->"Cada cor representa um desenho localizado. Nos modos, a tônica avança pelos graus do mesmo campo (E Jônio → F# Dórico → G# Frígio…). As cores se repetem na oitava. Os anéis mostram regiões que se cruzam.";
            };
            if(current.view()!=ViewMode.ALL_NOTES)extra+="\nDesenhos são recalculados pela afinação; partes abaixo da casa 0 ou acima da 24 são cortadas. Um box que começaria após a casa 24 retorna à oitava anterior. Afinações muito abertas podem exigir alongamentos.";
        }
        explanation.setText(extra);
    }

    private void updateVoicings() {
        if(!harmonyTab)return;
        boolean previous=updating;updating=true;int selected=Math.max(0,voicing.getSelectedIndex());
        HarmonicChord c=(HarmonicChord)chord.getSelectedItem();
        voicings=c==null||!grip.isSelected()?List.of():VoicingEngine.find(c.root(),c.formula(),tuning,(Integer)anchor.getValue());
        voicing.setModel(new DefaultComboBoxModel<>(voicings.toArray(Voicing[]::new)));
        if(!voicings.isEmpty())voicing.setSelectedIndex(Math.min(selected,voicings.size()-1));
        updating=previous;
    }

    private void refreshChordCards() {
        chordCards.removeAll();chordCards.setVisible(wholeField.isSelected());
        for(int i=0;i<harmonized.size();i++){
            int index=i;HarmonicChord c=harmonized.get(i);
            JButton button=Style.button(c.roman()+" · "+c.name(),false);
            button.setToolTipText(String.join(" · ",c.notes()));button.addActionListener(e->chord.setSelectedIndex(index));chordCards.add(button);
        }
        revalidate();repaint();
    }

    private void refreshProgression() {
        progressionRow.removeAll();int i=0;
        for(HarmonicChord c:progression){
            JLabel label=Style.text((++i)+". "+c.name()+"  "+(i<progression.size()?"→":""),13,true);progressionRow.add(label);
        }
        if(progression.isEmpty())progressionRow.add(Style.muted("Adicione acordes ou escolha uma sugestão. Até 16 passos, na ordem em que você escolheu."));
        sequenceChords.setEnabled(!progression.isEmpty());revalidate();repaint();
    }

    private void tuningChanged() {
        cleared=false;updateVoicings();render();status.setText("Afinação atualizada. Todas as posições foram recalculadas.");
    }
    private void shiftTuning(int semitones) {
        try{tuning=tuning.shift(semitones);markCustomTuning();tuningChanged();}
        catch(IllegalArgumentException e){status.setText("Limite de afinação atingido. Escolha outra afinação no menu.");}
    }
    private void markCustomTuning(){boolean previous=updating;updating=true;tuningPreset.setSelectedIndex(-1);updating=previous;}

    private void editTuning() {
        JPanel form=Style.column();form.add(Style.muted("Cada clique altera 1 semitom. A ordem abaixo vai da 6ª para a 1ª corda."));
        for(int s=0;s<6;s++) {
            final int string=s;JPanel row=Style.row(10);JLabel note=Style.text(tuning.nameAt(s,0,false),17,true);
            row.add(Style.text("Corda "+(6-s),13,false));
            JButton down=Style.button("−",false),up=Style.button("+",false);
            down.addActionListener(e->{try{tuning=tuning.shiftString(string,-1);note.setText(tuning.nameAt(string,0,false));markCustomTuning();tuningChanged();}catch(IllegalArgumentException ex){status.setText("Limite de afinação atingido para esta corda.");}});
            up.addActionListener(e->{try{tuning=tuning.shiftString(string,1);note.setText(tuning.nameAt(string,0,false));markCustomTuning();tuningChanged();}catch(IllegalArgumentException ex){status.setText("Limite de afinação atingido para esta corda.");}});
            row.add(down);row.add(note);row.add(up);form.add(row);form.add(Box.createVerticalStrut(6));
        }
        JOptionPane.showMessageDialog(this,form,"Afinação por corda",JOptionPane.PLAIN_MESSAGE);
    }

    private void clearStudy() {
        pinned.clear();progression.clear();progressionDisplay=false;cleared=true;
        boolean previous=updating;updating=true;progressionPreset.setSelectedIndex(0);updating=previous;
        refreshProgression();render();status.setText("Estudo limpo. Escolha outra nota, fórmula ou aba para começar de novo.");
    }

    private void bindKeys() {
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_L,KeyEvent.CTRL_DOWN_MASK),"clear");
        getActionMap().put("clear",new AbstractAction(){@Override public void actionPerformed(java.awt.event.ActionEvent e){clearStudy();}});
    }
    private void export() {
        JFileChooser chooser=new JFileChooser();chooser.setDialogTitle("Salvar seu estudo como imagem");
        chooser.setSelectedFile(new java.io.File("Guitarmony-estudo.png"));
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Imagem PNG","png"));
        if(chooser.showSaveDialog(this)==JFileChooser.APPROVE_OPTION){
            Path path=chooser.getSelectedFile().toPath();if(!path.toString().toLowerCase().endsWith(".png"))path=Path.of(path+".png");
            if(java.nio.file.Files.exists(path)&&JOptionPane.showConfirmDialog(this,"Este arquivo já existe. Substituir?","Exportar",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
            try{board.exportPng(path);status.setText("Imagem exportada: "+path.getFileName());}
            catch(IOException e){JOptionPane.showMessageDialog(this,"Não foi possível salvar: "+e.getMessage(),"Exportar PNG",JOptionPane.ERROR_MESSAGE);}
        }
    }
    private void showHelp() {
        JTextArea text=new JTextArea("1. Escolha a aba, a tônica e a fórmula. O braço muda imediatamente.\n\n2. Braço inteiro: todas as notas daquela estrutura. Um desenho: uma região de referência. Sequência: desenhos que percorrem o braço; os modos permanecem no mesmo campo harmônico.\n\n3. Para comparar seleções, clique em Sobrepor seleção antes de escolher a próxima. Remova camadas individualmente ou use Limpar (Ctrl+L).\n\n4. T identifica a tônica, o losango marca a blue note. Anéis coloridos indicam sobreposição. Passe o mouse para ler todas as camadas de uma posição.\n\n5. Na aba Acordes, selecione um grau. ‘Mostrar os 7 acordes’ abre o campo. ‘Voicing tocável’ mostra uma nota por corda; X significa corda abafada. Sem essa opção, você vê todas as alternativas de localização.\n\n6. −/+ 1 tom transpõe a afinação em dois semitons, sem mudar o tom do estudo. ‘Por corda’ ajusta semitons. O desenho é recalculado.\n\n7. A grafia das notas selecionadas respeita o tom: em F# maior, a sensível é E#, que soa como F. Preferir bemóis afeta as notas de fundo.\n\nGuitarmony funciona offline, não emite som e não precisa de login.");
        text.setWrapStyleWord(true);text.setLineWrap(true);text.setEditable(false);text.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,14));text.setColumns(52);
        JScrollPane scroll=new JScrollPane(text);scroll.setPreferredSize(new Dimension(620,540));
        JOptionPane.showMessageDialog(this,scroll,"Como estudar com o Guitarmony",JOptionPane.PLAIN_MESSAGE);
    }

    /** Keep the page at viewport width; the neck alone may scroll horizontally. */
    private static final class StudyPage extends JPanel implements Scrollable {
        @Override public Dimension getPreferredScrollableViewportSize(){return getPreferredSize();}
        @Override public int getScrollableUnitIncrement(Rectangle r,int orientation,int direction){return 22;}
        @Override public int getScrollableBlockIncrement(Rectangle r,int orientation,int direction){return Math.max(22,r.height-30);}
        @Override public boolean getScrollableTracksViewportWidth(){return true;}
        @Override public boolean getScrollableTracksViewportHeight(){return false;}
    }
}
