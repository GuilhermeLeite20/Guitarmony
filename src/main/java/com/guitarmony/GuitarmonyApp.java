package com.guitarmony;

import com.guitarmony.ui.WorkspacePanel;
import javax.swing.*;
import java.awt.*;

/** Native, offline desktop entry point. No audio threads or background services. */
public final class GuitarmonyApp {
    private GuitarmonyApp() {}

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("O Guitarmony precisa de um ambiente gráfico. Abra no IntelliJ e execute GuitarmonyApp, ou use java -jar dist/Guitarmony.jar no seu computador.");
            return;
        }
        SwingUtilities.invokeLater(() -> {
            installLookAndFeel();
            JFrame frame = new JFrame("Guitarmony · Seu laboratório de harmonia");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            java.awt.image.BufferedImage icon=new java.awt.image.BufferedImage(64,64,java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D g=icon.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(103,91,199));g.fillRoundRect(0,0,64,64,18,18);
            g.setColor(Color.WHITE);g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,45));g.drawString("G",14,49);g.dispose();
            frame.setIconImage(icon);
            frame.setContentPane(new WorkspacePanel());
            frame.setMinimumSize(new Dimension(1060, 740));
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            frame.setSize(Math.min(1540, screen.width - 60), Math.min(1020, screen.height - 60));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    public static void installLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) { /* Swing's bundled fallback remains usable. */ }
        UIManager.put("defaultFont", new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        UIManager.put("nimbusBase", new Color(105, 97, 206));
        UIManager.put("nimbusFocus", new Color(170, 163, 235));
        UIManager.put("control", new Color(247, 248, 252));
        UIManager.put("text", new Color(34, 40, 61));
        UIManager.put("ComboBox.contentMargins", new Insets(5, 9, 5, 9));
        ToolTipManager.sharedInstance().setInitialDelay(220);
        ToolTipManager.sharedInstance().setDismissDelay(12000);
    }
}
