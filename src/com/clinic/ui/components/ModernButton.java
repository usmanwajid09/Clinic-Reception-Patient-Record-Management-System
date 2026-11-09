package com.clinic.ui.components;

import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Modern flat rounded button with hover state transitions.
 */
public class ModernButton extends JButton {
    private Color normalColor;
    private Color hoverColor;
    private Color pressedColor;
    private Color textColor;
    private int cornerRadius = 8;

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
        setBackground(normalColor);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorder(new EmptyBorder(8, 16, 8, 16));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(hoverColor);
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(normalColor);
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                setBackground(pressedColor);
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setBackground(hoverColor);
                repaint();
            }
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

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
        g2.dispose();
        super.paintComponent(g);
    }
}
