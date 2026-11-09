package com.clinic.ui.theme;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Aesthetic UI styling helpers and typography for the desktop Swing interface.
 */
public final class UIStyle {
    private UIStyle() {}

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);

    public static Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
                new LineBorder(ThemeColors.BORDER, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        );
    }

    public static Border createInputBorder() {
        return BorderFactory.createCompoundBorder(
                new LineBorder(ThemeColors.BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        );
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(38);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(ThemeColors.BORDER_LIGHT);
        table.setSelectionBackground(ThemeColors.PRIMARY_LIGHT);
        table.setSelectionForeground(ThemeColors.TEXT_PRIMARY);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(ThemeColors.TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ThemeColors.BORDER));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
    }

    public static JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(ThemeColors.SURFACE);
        card.setBorder(createCardBorder());
        return card;
    }

    public static JTextField createStyledTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(FONT_REGULAR);
        tf.setBorder(createInputBorder());
        return tf;
    }
}
