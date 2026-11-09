package com.clinic.ui.components;

import com.clinic.ui.animation.Animator;
import com.clinic.ui.animation.ColorInterpolator;
import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Metric KPI Card featuring animated elevation and subtle drop shadow.
 */
public class StatCard extends JPanel {
    private final JLabel valueLabel;
    private final JLabel titleLabel;
    private final JLabel subtextLabel;
    private final Color accentColor;

    private float hoverProgress = 0.0f;
    private Timer hoverTimer;

    public StatCard(String title, String initialValue, String subtext, Color accentColor) {
        this.accentColor = accentColor;
        setOpaque(false);
        setLayout(new BorderLayout(8, 6));
        setBorder(new EmptyBorder(14, 18, 14, 18));

        titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(UIStyle.FONT_SMALL);
        titleLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        valueLabel = new JLabel(initialValue);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        subtextLabel = new JLabel(subtext);
        subtextLabel.setFont(UIStyle.FONT_SMALL);
        subtextLabel.setForeground(ThemeColors.TEXT_MUTED);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        content.add(titleLabel);
        content.add(Box.createVerticalStrut(4));
        content.add(valueLabel);
        content.add(Box.createVerticalStrut(2));
        content.add(subtextLabel);

        add(content, BorderLayout.CENTER);

        initHoverAnimation();
    }

    private void initHoverAnimation() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                animateHover(1.0f);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                animateHover(0.0f);
            }
        });
    }

    private void animateHover(float target) {
        if (hoverTimer != null && hoverTimer.isRunning()) {
            hoverTimer.stop();
        }
        final float start = hoverProgress;
        final float delta = target - start;

        hoverTimer = Animator.animate(150, Animator.Easing.EASE_OUT_QUAD, p -> {
            hoverProgress = start + delta * p;
            repaint();
        });
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setSubtext(String subtext) {
        subtextLabel.setText(subtext);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // 1. Dual-pass soft shadow
        int shadowAlpha = 14 + (int) (hoverProgress * 20);
        g2.setColor(new Color(15, 23, 42, shadowAlpha));
        g2.fillRoundRect(2, 3, w - 4, h - 3, 14, 14);

        // 2. White Surface Card
        g2.setColor(ThemeColors.SURFACE);
        g2.fillRoundRect(0, 0, w - 2, h - 2, 12, 12);

        // 3. Accent left pill
        g2.setColor(accentColor);
        g2.fillRoundRect(0, 0, 5, h - 2, 4, 4);

        // 4. Subtle border
        Color borderColor = ColorInterpolator.blend(ThemeColors.BORDER, accentColor, hoverProgress * 0.4f);
        g2.setColor(borderColor);
        g2.drawRoundRect(0, 0, w - 2, h - 2, 12, 12);

        g2.dispose();
        super.paintComponent(g);
    }
}
