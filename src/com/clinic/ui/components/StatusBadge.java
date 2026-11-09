package com.clinic.ui.components;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Modern pill badge renderer for JTable status columns.
 * Displays soft rounded colored pills with status indicator dots.
 */
public class StatusBadge extends DefaultTableCellRenderer {
    private static class BadgeStyle {
        final Color background;
        final Color text;
        final Color dot;

        BadgeStyle(Color bg, Color text, Color dot) {
            this.background = bg;
            this.text = text;
            this.dot = dot;
        }
    }

    private static final BadgeStyle STYLE_AVAILABLE = new BadgeStyle(new Color(236, 253, 245), new Color(6, 95, 70), new Color(16, 185, 129));
    private static final BadgeStyle STYLE_UNAVAILABLE = new BadgeStyle(new Color(254, 242, 242), new Color(153, 27, 27), new Color(239, 68, 68));
    private static final BadgeStyle STYLE_SCHEDULED = new BadgeStyle(new Color(239, 246, 255), new Color(30, 64, 175), new Color(59, 130, 246));
    private static final BadgeStyle STYLE_COMPLETED = new BadgeStyle(new Color(240, 253, 250), new Color(15, 118, 110), new Color(13, 148, 136));
    private static final BadgeStyle STYLE_PAID = new BadgeStyle(new Color(220, 252, 231), new Color(21, 128, 61), new Color(34, 197, 94));
    private static final BadgeStyle STYLE_UNPAID = new BadgeStyle(new Color(254, 243, 199), new Color(146, 64, 14), new Color(245, 158, 11));
    private static final BadgeStyle STYLE_NO_SHOW = new BadgeStyle(new Color(255, 237, 213), new Color(154, 52, 18), new Color(249, 115, 22));
    private static final BadgeStyle STYLE_CANCELLED = new BadgeStyle(new Color(241, 245, 249), new Color(71, 85, 105), new Color(148, 163, 184));
    private static final BadgeStyle STYLE_DEFAULT = new BadgeStyle(new Color(241, 245, 249), new Color(51, 65, 85), new Color(100, 116, 139));

    private String text = "";
    private BadgeStyle currentStyle = STYLE_DEFAULT;

    public StatusBadge() {
        setOpaque(false);
        setHorizontalAlignment(SwingConstants.CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        this.text = value != null ? value.toString().trim() : "";
        this.currentStyle = resolveStyle(text);
        return this;
    }

    private BadgeStyle resolveStyle(String val) {
        String upper = val.toUpperCase().replace(" ", "_");
        if (upper.contains("AVAILABLE") && !upper.contains("UNAVAILABLE")) return STYLE_AVAILABLE;
        if (upper.contains("UNAVAILABLE")) return STYLE_UNAVAILABLE;
        if (upper.contains("SCHEDULED")) return STYLE_SCHEDULED;
        if (upper.contains("COMPLETED")) return STYLE_COMPLETED;
        if (upper.equals("PAID")) return STYLE_PAID;
        if (upper.equals("UNPAID")) return STYLE_UNPAID;
        if (upper.contains("NO_SHOW")) return STYLE_NO_SHOW;
        if (upper.contains("CANCEL")) return STYLE_CANCELLED;
        return STYLE_DEFAULT;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (text.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int h = getHeight() - 10;
        int y = 5;

        Font font = new Font("Segoe UI", Font.BOLD, 11);
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();

        int dotSize = 6;
        int paddingX = 10;
        int textWidth = fm.stringWidth(text);
        int pillWidth = paddingX + dotSize + 6 + textWidth + paddingX;
        int x = (getWidth() - pillWidth) / 2;

        // Pill background
        g2.setColor(currentStyle.background);
        g2.fillRoundRect(x, y, pillWidth, h, h, h);

        // Dot
        g2.setColor(currentStyle.dot);
        int dotY = y + (h - dotSize) / 2;
        g2.fillOval(x + paddingX, dotY, dotSize, dotSize);

        // Text
        g2.setColor(currentStyle.text);
        int textX = x + paddingX + dotSize + 6;
        int textY = y + ((h - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(text, textX, textY);

        g2.dispose();
    }
}
