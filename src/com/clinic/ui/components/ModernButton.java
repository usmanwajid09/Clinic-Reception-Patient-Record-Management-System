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
 * Enhanced interactive button with 60 FPS smooth hover transitions and elevation shadows.
 */
public class ModernButton extends JButton {
    private final Color normalColor;
    private final Color hoverColor;
    private final Color pressedColor;
    private final Color textColor;
    private int cornerRadius = 10;

    private float hoverProgress = 0.0f;
    private boolean isPressed = false;
    private Timer hoverAnimTimer;

    public ModernButton(String text) {
        this(text, ThemeColors.PRIMARY, ThemeColors.PRIMARY_DARK, Color.WHITE);
    }

    public ModernButton(String text, Color normalColor, Color hoverColor, Color textColor) {
        super(text);
        this.normalColor = normalColor;
        this.hoverColor = hoverColor;
        this.pressedColor = hoverColor.darker();
        this.textColor = textColor;

        setFont(UIStyle.FONT_BOLD);
        setForeground(textColor);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorder(new EmptyBorder(10, 18, 10, 18));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        initInteractions();
    }

    private void initInteractions() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isEnabled()) return;
                startHoverAnimation(1.0f);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!isEnabled()) return;
                startHoverAnimation(0.0f);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (!isEnabled()) return;
                isPressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!isEnabled()) return;
                isPressed = false;
                repaint();
            }
        });
    }

    private void startHoverAnimation(float target) {
        if (hoverAnimTimer != null && hoverAnimTimer.isRunning()) {
            hoverAnimTimer.stop();
        }

        final float startVal = hoverProgress;
        final float delta = target - startVal;

        hoverAnimTimer = Animator.animate(140, Animator.Easing.EASE_OUT_QUAD, progress -> {
            hoverProgress = startVal + delta * progress;
            repaint();
        });
    }

    public static ModernButton secondary(String text) {
        return new ModernButton(text, new Color(241, 245, 249), new Color(226, 232, 240), ThemeColors.TEXT_PRIMARY);
    }

    public static ModernButton danger(String text) {
        return new ModernButton(text, ThemeColors.DANGER, new Color(220, 38, 38), Color.WHITE);
    }

    public static ModernButton success(String text) {
        return new ModernButton(text, ThemeColors.SUCCESS, new Color(5, 150, 105), Color.WHITE);
    }

    public static ModernButton outline(String text, Color accent) {
        ModernButton btn = new ModernButton(text, Color.WHITE, new Color(248, 250, 252), accent);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent, 1, true),
                new EmptyBorder(9, 17, 9, 17)
        ));
        return btn;
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // 1. Soft shadow on hover
        if (hoverProgress > 0.05f && !isPressed) {
            int shadowAlpha = (int) (hoverProgress * 30);
            g2.setColor(new Color(15, 23, 42, shadowAlpha));
            g2.fillRoundRect(1, 2, w - 2, h - 1, cornerRadius, cornerRadius);
        }

        // 2. Animated Background color interpolation
        Color currentColor;
        if (isPressed) {
            currentColor = pressedColor;
        } else {
            currentColor = ColorInterpolator.blend(normalColor, hoverColor, hoverProgress);
        }

        g2.setColor(currentColor);
        int yOffset = isPressed ? 1 : 0;
        g2.fillRoundRect(0, yOffset, w, h - yOffset, cornerRadius, cornerRadius);

        g2.dispose();
        super.paintComponent(g);
    }
}
