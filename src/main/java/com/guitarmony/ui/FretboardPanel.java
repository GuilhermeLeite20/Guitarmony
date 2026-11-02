package com.guitarmony.ui;

import com.guitarmony.fretboard.Cell;
import com.guitarmony.music.Notes;
import com.guitarmony.music.Tuning;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A vector-drawn, silent guitar neck. The six rows have no string lines: notes,
 * fret boundaries and a quiet wood surface carry all information. Painting is
 * bounded to 150 cells; spelling and layer membership are computed on changes.
 */
public final class FretboardPanel extends JPanel {
    private static final Color INK = new Color(0x20253C);
    private static final Color MUTED = new Color(0x758095);
    private static final int[] MARKERS = {3, 5, 7, 9, 12, 15, 17, 19, 21, 24};
    private Tuning tuning = Tuning.standard();
    private List<Layer> layers = List.of();
    private Map<Cell, List<Layer>> memberships = Map.of();
    private Map<Layer, List<String>> spellings = Map.of();
    private boolean darkWood;
    private boolean intervalLabels;
    private boolean flats;
    private boolean showAllNotes = true;
    private Consumer<Cell> cellListener = cell -> {};
    private Cell hover;

    public FretboardPanel() {
        setOpaque(true);
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(1450, 350));
        setMinimumSize(new Dimension(1150, 320));
        getAccessibleContext().setAccessibleName("Braço da guitarra: seis cordas, casas zero a vinte e quatro");
        ToolTipManager.sharedInstance().registerComponent(this);
        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                Cell next = hit(e.getX(), e.getY());
                if (!Objects.equals(hover, next)) {
                    hover = next;
                    setCursor(next == null ? Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    repaint();
                }
            }
            @Override public void mouseExited(MouseEvent e) { hover = null; repaint(); }
            @Override public void mouseClicked(MouseEvent e) {
                Cell cell = hit(e.getX(), e.getY());
                if (cell != null) cellListener.accept(cell);
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    public void setData(Tuning tuning, List<Layer> layers, boolean darkWood,
                        boolean intervalLabels, boolean flats, boolean showAllNotes) {
        this.tuning = Objects.requireNonNull(tuning);
        this.layers = List.copyOf(layers);
        this.darkWood = darkWood;
        this.intervalLabels = intervalLabels;
        this.flats = flats;
        this.showAllNotes = showAllNotes;
        Map<Cell, List<Layer>> cells = new HashMap<>();
        Map<Layer, List<String>> names = new HashMap<>();
        for (Layer layer : layers) {
            names.put(layer, Notes.spell(layer.root(), layer.formula()));
            for (Cell cell : layer.cells()) cells.computeIfAbsent(cell, key -> new ArrayList<>()).add(layer);
        }
        Map<Cell, List<Layer>> immutable = new HashMap<>();
        cells.forEach((cell, list) -> immutable.put(cell, List.copyOf(list)));
        memberships = Map.copyOf(immutable);
        spellings = Map.copyOf(names);
        repaint();
    }

    public void setCellListener(Consumer<Cell> listener) { cellListener = Objects.requireNonNull(listener); }

    /** Export a high-resolution image using the same painter as the live neck. */
    public void exportPng(Path path) throws IOException {
        Objects.requireNonNull(path);
        Path parent = path.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);
        BufferedImage image = new BufferedImage(1800, 430, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
            paintNeck(g, image.getWidth(), image.getHeight(), true);
        } finally { g.dispose(); }
        if (!ImageIO.write(image, "png", path.toFile())) throw new IOException("Nenhum gravador PNG disponível.");
    }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        try { paintNeck(g, getWidth(), getHeight(), false); }
        finally { g.dispose(); }
    }

    private void paintNeck(Graphics2D g, int width, int height, boolean exporting) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Geometry geo = new Geometry(width, height);
        Color wood = darkWood ? new Color(0x343A46) : new Color(0xF2E6D0);
        Color frets = darkWood ? new Color(0x77808D) : new Color(0xC5B79F);
        RoundRectangle2D board = new RoundRectangle2D.Double(geo.left, geo.top, geo.boardWidth, geo.boardHeight, 20, 20);
        g.setColor(new Color(0xE6E8EE));
        g.fill(new RoundRectangle2D.Double(geo.left, geo.top + 3, geo.boardWidth, geo.boardHeight + 1, 20, 20));
        g.setColor(wood);
        g.fill(board);
        Shape previousClip = g.getClip();
        g.clip(board);
        // Quiet grain, not strings: unequal short translucent strokes in the wood.
        g.setStroke(new BasicStroke(1));
        g.setColor(darkWood ? new Color(255, 255, 255, 9) : new Color(129, 98, 49, 13));
        for (int y = (int) geo.top + 8; y < geo.top + geo.boardHeight; y += 9) {
            int inset = ((y * 17) % 79);
            g.drawLine((int) geo.left + inset, y, (int) (geo.left + geo.boardWidth - inset * .6), y);
        }
        g.setColor(darkWood ? new Color(0x282E39) : new Color(0xE9EAF2));
        g.fillRect((int) geo.left, (int) geo.top, (int) Math.ceil(geo.cellWidth), (int) geo.boardHeight);
        for (int fret = 1; fret <= 24; fret++) {
            double x = geo.left + geo.cellWidth * fret;
            g.setColor(frets);
            g.setStroke(new BasicStroke(fret == 1 ? 5f : (fret == 13 ? 2.3f : 1.4f)));
            g.drawLine((int) x, (int) geo.top, (int) x, (int) (geo.top + geo.boardHeight));
            if (fret > 1) {
                g.setColor(darkWood ? new Color(255, 255, 255, 20) : new Color(255, 255, 255, 110));
                g.drawLine((int) x + 1, (int) geo.top, (int) x + 1, (int) (geo.top + geo.boardHeight));
            }
        }
        g.setClip(previousClip);
        g.setStroke(new BasicStroke(1));
        g.setColor(darkWood ? new Color(0x5A6371) : new Color(0xD6C9B4));
        g.draw(board);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        for (int fret = 0; fret <= 24; fret++) {
            g.setColor(fret == 0 ? new Color(0x6863CF) : MUTED);
            center(g, fret == 0 ? "SOLTA" : String.valueOf(fret), geo.x(fret), geo.top - 16);
        }
        for (int string = 0; string < 6; string++) {
            double y = geo.y(string);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            g.setColor(MUTED);
            center(g, (6 - string) + "ª", 22, y + 4);
            for (int fret = 0; fret <= 24; fret++) drawNote(g, geo, new Cell(string, fret));
        }
        double markersY = geo.top + geo.boardHeight + 17;
        g.setColor(new Color(0xA6ACBD));
        for (int fret : MARKERS) {
            double x = geo.x(fret);
            if (fret == 12 || fret == 24) {
                g.fillOval((int) x - 6, (int) markersY - 2, 4, 4);
                g.fillOval((int) x + 2, (int) markersY - 2, 4, 4);
            } else g.fillOval((int) x - 2, (int) markersY - 2, 4, 4);
        }
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(MUTED);
        String caption = "6ª corda grave embaixo · 1ª corda aguda em cima · T = tônica · losango = blue note";
        if (exporting) caption = "GUITARMONY  /  " + caption;
        g.drawString(caption, (int) geo.left, height - 14);
        if (exporting && !layers.isEmpty()) {
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            String title = layers.size() == 1 ? layers.get(0).label() : layers.size() + " desenhos/camadas · " + layers.get(0).label();
            g.setColor(INK);
            g.drawString(title, (int) geo.left, 17);
        }
    }

    private void drawNote(Graphics2D g, Geometry geo, Cell cell) {
        List<Layer> matches = memberships.getOrDefault(cell, List.of());
        boolean active = !matches.isEmpty();
        if (!showAllNotes && !active) return;
        double x = geo.x(cell.fret());
        double y = geo.y(cell.stringIndex());
        double radius = geo.radius;
        int pitch = tuning.pitchAt(cell.stringIndex(), cell.fret());
        boolean tonic = matches.stream().anyMatch(layer -> pitch == Notes.pitch(layer.root()));
        boolean blue = matches.stream().anyMatch(layer -> isBlue(layer, pitch));
        String text = active ? label(matches.get(0), pitch) : tuning.nameAt(cell.stringIndex(), cell.fret(), flats);
        Color foreground = active || !darkWood ? INK : new Color(0xDDE3ED);
        Color fill = active ? lighten(matches.get(0).color(), .25) : (darkWood ? new Color(0x4A5361) : new Color(0xFAF5EA));
        g.setColor(new Color(0, 0, 0, darkWood ? 22 : 13));
        g.fillOval((int) (x - radius), (int) (y - radius + 2), (int) (radius * 2), (int) (radius * 2));
        g.setColor(fill);
        g.fillOval((int) (x - radius), (int) (y - radius), (int) (radius * 2), (int) (radius * 2));
        g.setColor(active ? darken(matches.get(0).color(), .15) : (darkWood ? new Color(0x687383) : new Color(0xD5C9B7)));
        g.setStroke(new BasicStroke(active ? 1.8f : 1f));
        g.drawOval((int) (x - radius), (int) (y - radius), (int) (radius * 2), (int) (radius * 2));
        if (matches.size() > 1) {
            double diameter = radius * 2 + 6;
            double sweep = 360.0 / matches.size();
            g.setStroke(new BasicStroke(3.3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < matches.size(); i++) {
                g.setColor(darken(matches.get(i).color(), .08));
                g.draw(new Arc2D.Double(x - diameter / 2, y - diameter / 2, diameter, diameter,
                        90 - (i + 1) * sweep + 2, sweep - 4, Arc2D.OPEN));
            }
        }
        if (tonic) {
            g.setColor(INK);
            g.setStroke(new BasicStroke(2));
            double r = radius - 3;
            g.drawOval((int) (x - r), (int) (y - r), (int) (r * 2), (int) (r * 2));
            g.setColor(INK);
            g.fillOval((int) (x + radius - 6), (int) (y - radius - 2), 9, 9);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 7));
            g.setColor(Color.WHITE);
            center(g, "T", x + radius - 1.5, y - radius + 5);
        }
        g.setFont(new Font(Font.SANS_SERIF, active ? Font.BOLD : Font.PLAIN,
                Math.max(10, (int) Math.min(13, radius * .79))));
        if (text.length() >= 4) g.setFont(g.getFont().deriveFont(10f));
        g.setColor(foreground);
        FontMetrics fm = g.getFontMetrics();
        center(g, text, x, y + (fm.getAscent() - fm.getDescent()) / 2.0);
        if (blue) {
            double dx = x + radius - 1, dy = y + radius - 2;
            Path2D diamond = new Path2D.Double();
            diamond.moveTo(dx, dy - 5); diamond.lineTo(dx + 5, dy);
            diamond.lineTo(dx, dy + 5); diamond.lineTo(dx - 5, dy); diamond.closePath();
            g.setColor(new Color(0x397894)); g.fill(diamond);
            g.setColor(Color.WHITE); g.setStroke(new BasicStroke(1)); g.draw(diamond);
        }
        if (cell.equals(hover)) {
            g.setColor(new Color(0x6863CF));
            g.setStroke(new BasicStroke(2));
            double r = radius + 6;
            g.drawOval((int) (x - r), (int) (y - r), (int) (r * 2), (int) (r * 2));
        }
    }

    private String label(Layer layer, int pitch) {
        int relative = Math.floorMod(pitch - Notes.pitch(layer.root()), 12);
        int index = layer.formula().intervals().indexOf(relative);
        if (index < 0) return Notes.chromatic(pitch, flats);
        return intervalLabels ? layer.formula().degrees().get(index) : spellings.get(layer).get(index);
    }

    private static boolean isBlue(Layer layer, int pitch) {
        if (!layer.formula().id().startsWith("blues-")) return false;
        int relative = Math.floorMod(pitch - Notes.pitch(layer.root()), 12);
        int blueInterval = switch (layer.formula().id()) {
            case "blues-major" -> 3;
            case "blues-halfdim" -> 7;
            default -> 6;
        };
        return relative == blueInterval;
    }

    @Override public String getToolTipText(MouseEvent e) {
        Cell cell = hit(e.getX(), e.getY());
        if (cell == null) return null;
        int pitch = tuning.pitchAt(cell.stringIndex(), cell.fret());
        StringBuilder html = new StringBuilder("<html><b>")
                .append(escape(tuning.nameAt(cell.stringIndex(), cell.fret(), flats)))
                .append("</b> · ").append(6 - cell.stringIndex()).append("ª corda · ")
                .append(cell.fret() == 0 ? "corda solta" : "casa " + cell.fret());
        for (Layer layer : memberships.getOrDefault(cell, List.of())) {
            int relative = Math.floorMod(pitch - Notes.pitch(layer.root()), 12);
            int index = layer.formula().intervals().indexOf(relative);
            String degree = index < 0 ? Notes.intervalLabel(relative) : layer.formula().degrees().get(index);
            String note = index < 0 ? Notes.chromatic(pitch, flats) : spellings.get(layer).get(index);
            html.append("<br>").append(escape(layer.label())).append(": ")
                    .append(escape(note)).append(" · grau ").append(escape(degree));
            if (isBlue(layer, pitch)) html.append(" · blue note");
        }
        return html.append("</html>").toString();
    }

    private Cell hit(int x, int y) {
        Geometry geo = new Geometry(getWidth(), getHeight());
        int fret = (int) Math.floor((x - geo.left) / geo.cellWidth);
        int row = (int) Math.floor((y - geo.top) / geo.rowHeight);
        if (fret < 0 || fret > 24 || row < 0 || row > 5) return null;
        int string = 5 - row;
        double dx = x - geo.x(fret), dy = y - geo.y(string);
        return dx * dx + dy * dy <= Math.pow(geo.radius + 7, 2) ? new Cell(string, fret) : null;
    }

    private static Color lighten(Color color, double amount) {
        return new Color((int) (color.getRed() * (1 - amount) + 255 * amount),
                (int) (color.getGreen() * (1 - amount) + 255 * amount),
                (int) (color.getBlue() * (1 - amount) + 255 * amount));
    }

    private static Color darken(Color color, double amount) {
        return new Color((int) (color.getRed() * (1 - amount)),
                (int) (color.getGreen() * (1 - amount)), (int) (color.getBlue() * (1 - amount)));
    }

    private static void center(Graphics2D g, String text, double x, double baseline) {
        g.drawString(text, (float) (x - g.getFontMetrics().stringWidth(text) / 2.0), (float) baseline);
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static final class Geometry {
        final double left = 46;
        final double top = 52;
        final double boardWidth;
        final double boardHeight;
        final double cellWidth;
        final double rowHeight;
        final double radius;
        Geometry(int width, int height) {
            boardWidth = Math.max(500, width - left - 18);
            boardHeight = Math.max(190, height - top - 58);
            cellWidth = boardWidth / 25.0;
            rowHeight = boardHeight / 6.0;
            radius = Math.min(17.0, Math.min(cellWidth * .32, rowHeight * .34));
        }
        double x(int fret) { return left + cellWidth * (fret + .5); }
        double y(int string) { return top + rowHeight * (5 - string + .5); }
    }
}
