package com.clinic.ui.animation;

import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Animated floating toast notification component.
 * Mounts onto the window's JLayeredPane, slides into view with easing,
 * and automatically dismisses with a fade-out animation.
 */
public class ToastNotification extends JPanel {
    public enum Type {
        SUCCESS(new Color(16, 185, 129), new Color(236, 253, 245), "✓"),
        INFO(new Color(59, 130, 246), new Color(239, 246, 255), "ℹ"),
        WARNING(new Color(245, 158, 11), new Color(254, 243, 199), "⚠"),
        ERROR(new Color(239, 68, 68), new Color(254, 242, 242), "✕");

        final Color primaryColor;
        final Color bgColor;
        final String iconText;

        Type(Color primaryColor, Color bgColor, String iconText) {
            this.primaryColor = primaryColor;
            this.bgColor = bgColor;
            this.iconText = iconText;
        }
    }

    private static final int TOAST_WIDTH = 340;
    private static final int TOAST_HEIGHT = 68;
    private static final int DISPLAY_DURATION_MS = 3200;

    private final Type type;
    private final String title;
    private final String message;
    private float opacity = 0.0f;
    private int currentY = -TOAST_HEIGHT;
    private int targetY = 24;
    private Timer autoDismissTimer;

    private ToastNotification(Type type, String title, String message) {
        this.type = type;
        this.title = title;
        this.message = message;

        setOpaque(false);
        setLayout(new BorderLayout(12, 0));
        setBorder(new EmptyBorder(10, 14, 10, 14));
        setSize(TOAST_WIDTH, TOAST_HEIGHT);

        initComponents();
    }

    private void initComponents() {
        // Icon Badge
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(type.primaryColor);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(type.iconText)) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(type.iconText, x, y);
                g2.dispose();
            }
        };
        iconPanel.setPreferredSize(new Dimension(32, 32));
        iconPanel.setOpaque(false);

        // Text Panel
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UIStyle.FONT_BOLD);
        titleLbl.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel msgLbl = new JLabel("<html><body style='width: 220px;'>" + message + "</body></html>");
        msgLbl.setFont(UIStyle.FONT_SMALL);
        msgLbl.setForeground(ThemeColors.TEXT_SECONDARY);

        textPanel.add(titleLbl);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(msgLbl);

        // Close Button
        JLabel closeBtn = new JLabel("×");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        closeBtn.setForeground(ThemeColors.TEXT_MUTED);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dismiss();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                closeBtn.setForeground(ThemeColors.TEXT_PRIMARY);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                closeBtn.setForeground(ThemeColors.TEXT_MUTED);
            }
        });

        add(iconPanel, BorderLayout.WEST);
        add(textPanel, BorderLayout.CENTER);
        add(closeBtn, BorderLayout.EAST);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

        // Soft drop shadow
        g2.setColor(new Color(15, 23, 42, 28));
        g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, 14, 14);

        // Background card
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 12, 12);

        // Left accent stripe
        g2.setColor(type.primaryColor);
        g2.fillRoundRect(0, 0, 5, getHeight() - 4, 4, 4);

        // Soft outer border
        g2.setColor(new Color(226, 232, 240));
        g2.drawRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 12, 12);

        g2.dispose();
        super.paintComponent(g);
    }

    private void slideIn(JLayeredPane layeredPane) {
        int targetX = layeredPane.getWidth() - TOAST_WIDTH - 24;
        setLocation(targetX, currentY);
        layeredPane.add(this, JLayeredPane.POPUP_LAYER);
        layeredPane.moveToFront(this);

        Animator.animate(280, Animator.Easing.EASE_OUT_CUBIC, progress -> {
            opacity = progress;
            currentY = (int) (-TOAST_HEIGHT + progress * (targetY + TOAST_HEIGHT));
            setLocation(targetX, currentY);
            repaint();
        }, () -> {
            autoDismissTimer = new Timer(DISPLAY_DURATION_MS, e -> dismiss());
            autoDismissTimer.setRepeats(false);
            autoDismissTimer.start();
        });
    }

    public void dismiss() {
        if (autoDismissTimer != null && autoDismissTimer.isRunning()) {
            autoDismissTimer.stop();
        }

        Container parent = getParent();
        if (parent == null) return;

        int startY = getY();
        int targetExitY = -TOAST_HEIGHT;
        int currentX = getX();

        Animator.animate(220, Animator.Easing.EASE_OUT_QUAD, progress -> {
            opacity = 1.0f - progress;
            int newY = (int) (startY + progress * (targetExitY - startY));
            setLocation(currentX, newY);
            repaint();
        }, () -> {
            parent.remove(this);
            parent.repaint();
        });
    }

    // Static dispatch helpers
    public static void show(Component parent, Type type, String title, String message) {
        if (parent == null) return;
        SwingUtilities.invokeLater(() -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(parent);
            if (frame != null && frame.getLayeredPane() != null) {
                ToastNotification toast = new ToastNotification(type, title, message);
                toast.slideIn(frame.getLayeredPane());
            } else {
                // Fallback to JOptionPane if no layered pane available
                int msgType = (type == Type.ERROR) ? JOptionPane.ERROR_MESSAGE :
                        (type == Type.WARNING) ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE;
                JOptionPane.showMessageDialog(parent, message, title, msgType);
            }
        });
    }

    public static void showSuccess(Component parent, String title, String message) {
        show(parent, Type.SUCCESS, title, message);
    }

    public static void showError(Component parent, String title, String message) {
        show(parent, Type.ERROR, title, message);
    }

    public static void showInfo(Component parent, String title, String message) {
        show(parent, Type.INFO, title, message);
    }

    public static void showWarning(Component parent, String title, String message) {
        show(parent, Type.WARNING, title, message);
    }
}

