package com.guitarmony.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

final class Style {
    static final Color BG = new Color(245,246,250);
    static final Color INK = new Color(32,38,59);
    static final Color MUTED = new Color(105,114,135);
    static final Color ACCENT = new Color(103,91,199);
    static final Color LINE = new Color(228,231,240);
    static final Color[] COLORS = {
        new Color(255,178,177), new Color(172,215,248), new Color(185,226,194),
        new Color(255,219,152), new Color(204,187,244), new Color(158,225,221),
        new Color(246,189,220)
    };
    private Style() {}
    static JLabel text(String value,int size,boolean bold) {
        JLabel label = new JLabel(value);
        label.setFont(new Font(Font.SANS_SERIF,bold?Font.BOLD:Font.PLAIN,size));
        label.setForeground(INK);
        return label;
    }
    static JPanel row(int gap) {
        JPanel p = new JPanel(new WrapLayout(gap));
        p.setOpaque(false);
        return p;
    }
    static JPanel column() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        p.setOpaque(false);
        return p;
    }
    static JPanel card(LayoutManager layout) {
        JPanel p = new JPanel(layout) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE); g2.fillRoundRect(0,0,getWidth(),getHeight(),22,22);
                g2.setColor(LINE); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,22,22);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(18,20,18,20));
        return p;
    }
    static JButton button(String title,boolean primary) {
        JButton b=new JButton(title) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill=primary?ACCENT:getBackground();
                if (getModel().isRollover()) fill=primary?ACCENT.darker():new Color(236,233,250);
                g2.setColor(fill); g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setColor(primary?ACCENT:LINE); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);
                if (hasFocus()) {g2.setColor(ACCENT);g2.drawRoundRect(2,2,getWidth()-5,getHeight()-5,10,10);}
                g2.dispose(); super.paintComponent(g);
            }
        };
        b.setFont(new Font(Font.SANS_SERIF,Font.BOLD,12));
        b.setForeground(primary?Color.WHITE:INK);
        b.setBackground(Color.WHITE); b.setOpaque(false); b.setContentAreaFilled(false);
        b.setBorder(new EmptyBorder(9,14,9,14)); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
    static void name(JComponent c,String name) { c.setName(name); c.getAccessibleContext().setAccessibleName(name); }
    static JLabel muted(String value) {JLabel l=text(value,12,false);l.setForeground(MUTED);return l;}
}
