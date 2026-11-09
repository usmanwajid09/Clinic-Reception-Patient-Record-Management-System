package com.clinic.ui.theme;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * High-resolution icon management system.
 * Loads optimized PNG assets from classpath and provides procedural anti-aliased vector fallbacks.
 */
public final class IconFactory {
    private static final Map<String, ImageIcon> cache = new HashMap<>();

    private IconFactory() {}

    /**
     * Retrieves an icon by identifier and desired pixel size (24 or 32).
     */
    public static ImageIcon getIcon(String name, int size) {
        String key = name + "_" + size;
        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        ImageIcon icon = loadFromClasspath(name, size);
        if (icon == null) {
            icon = loadFromFileSystem(name, size);
        }
        if (icon == null) {
            icon = generateProceduralFallback(name, size);
        }

        cache.put(key, icon);
        return icon;
    }

    private static ImageIcon loadFromClasspath(String name, int size) {
        String path = "/com/clinic/ui/icons/" + name + "_" + size + ".png";
        try (InputStream is = IconFactory.class.getResourceAsStream(path)) {
            if (is != null) {
                BufferedImage img = ImageIO.read(is);
                if (img != null) {
                    return new ImageIcon(img);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static ImageIcon loadFromFileSystem(String name, int size) {
        String[] possiblePaths = {
                "src/com/clinic/ui/icons/" + name + "_" + size + ".png",
                "bin/com/clinic/ui/icons/" + name + "_" + size + ".png"
        };
        for (String p : possiblePaths) {
            File f = new File(p);
            if (f.exists()) {
                try {
                    BufferedImage img = ImageIO.read(f);
                    if (img != null) {
                        return new ImageIcon(img);
                    }
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    /**
     * Procedural anti-aliased vector icon fallback if image file is not present.
     */
    private static ImageIcon generateProceduralFallback(String name, int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color c = switch (name.toLowerCase()) {
            case "workstation" -> ThemeColors.PRIMARY;
            case "patients" -> ThemeColors.ACCENT;
            case "doctors" -> ThemeColors.SUCCESS;
            case "appointments" -> new Color(79, 70, 229);
            case "billing" -> new Color(245, 158, 11);
            case "reports" -> new Color(124, 58, 237);
            default -> ThemeColors.PRIMARY_DARK;
        };

        g2.setColor(c);
        g2.fillRoundRect(2, 2, size - 4, size - 4, 6, 6);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, Math.max(10, size / 2)));
        String initial = name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();
        FontMetrics fm = g2.getFontMetrics();
        int x = (size - fm.stringWidth(initial)) / 2;
        int y = ((size - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(initial, x, y);
        g2.dispose();

        return new ImageIcon(img);
    }

    // Specific accessors
    public static ImageIcon getWorkstationIcon(int size) {
        return getIcon("workstation", size);
    }

    public static ImageIcon getPatientsIcon(int size) {
        return getIcon("patients", size);
    }

    public static ImageIcon getDoctorsIcon(int size) {
        return getIcon("doctors", size);
    }

    public static ImageIcon getAppointmentsIcon(int size) {
        return getIcon("appointments", size);
    }

    public static ImageIcon getBillingIcon(int size) {
        return getIcon("billing", size);
    }

    public static ImageIcon getReportsIcon(int size) {
        return getIcon("reports", size);
    }

    public static ImageIcon getClinicLogo(int size) {
        return getIcon("clinic_logo", size);
    }
}
