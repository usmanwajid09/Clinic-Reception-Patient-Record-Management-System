package com.clinic.ui.components;

import com.clinic.ui.theme.ThemeColors;
import com.clinic.ui.theme.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Metric KPI Card showing title, large numeric value, and descriptive tag.
 */
public class StatCard extends JPanel {
    private final JLabel valueLabel;
    private final JLabel titleLabel;
    private final JLabel subtextLabel;

    public StatCard(String title, String initialValue, String subtext, Color accentColor) {
        setLayout(new BorderLayout(8, 6));
        setBackground(ThemeColors.SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ThemeColors.BORDER, 1, true),
                        new EmptyBorder(12, 16, 12, 16)
                )
        ));

        titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(UIStyle.FONT_SMALL);
        titleLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        valueLabel = new JLabel(initialValue);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
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
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setSubtext(String subtext) {
        subtextLabel.setText(subtext);
    }
}

