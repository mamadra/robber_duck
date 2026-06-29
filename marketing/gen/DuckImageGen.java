import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class DuckImageGen {
    enum Mood { IDLE, HAPPY, PANIC, NOD }

    static final Color BODY       = new Color(0xFF, 0xD5, 0x4A);
    static final Color BODY_SHADE = new Color(0xF2, 0xB8, 0x1E);
    static final Color BEAK       = new Color(0xFF, 0x9E, 0x2C);
    static final Color EYE        = new Color(0x2B, 0x2B, 0x2B);
    static final Color PANIC_TINT = new Color(0xFF, 0x6B, 0x5B, 90);
    static final Color SWEAT      = new Color(0x6F, 0xC6, 0xFF);
    static final Color BLUSH      = new Color(0xFF, 0x8A, 0x8A, 120);

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        File out = new File(args.length > 0 ? args[0] : "marketing");
        out.mkdirs();

        writePng(avatar(512), new File(out, "pluginIcon-512.png"));
        writePng(moodsStrip(1400, 460), new File(out, "preview-moods.png"));
        writePng(editorMockup(1280, 800), new File(out, "preview-editor.png"));
        writePng(tellMockup(900, 560), new File(out, "preview-tell-the-duck.png"));
        System.out.println("Done → " + out.getAbsolutePath());
    }

    static BufferedImage avatar(int s) {
        BufferedImage img = canvas(s, s);
        Graphics2D g = g2(img);

        GradientPaint bg = new GradientPaint(0, 0, new Color(0x2E, 0x3A, 0x59),
                0, s, new Color(0x1B, 0x22, 0x33));
        g.setPaint(bg);
        g.fill(new RoundRectangle2D.Float(0, 0, s, s, s * 0.22f, s * 0.22f));

        int d = (int) (s * 0.74);
        paintDuck(g, (s - d) / 2, (int) (s * 0.16), d, Mood.HAPPY, 0.75);
        g.dispose();
        return img;
    }

    static BufferedImage moodsStrip(int w, int h) {
        BufferedImage img = canvas(w, h);
        Graphics2D g = g2(img);
        g.setPaint(new GradientPaint(0, 0, new Color(0xF7, 0xF8, 0xFA), 0, h, new Color(0xE7, 0xEC, 0xF3)));
        g.fillRect(0, 0, w, h);

        g.setColor(new Color(0x24, 0x2A, 0x33));
        g.setFont(new Font("SansSerif", Font.BOLD, 34));
        drawCentered(g, "One duck, four moods — it reacts to your build.", w / 2, 56);

        Mood[] moods = Mood.values();
        String[] caps = {"idle — breathing", "happy — clean build", "panic — build failed", "nod — tell the duck"};
        double[] phase = {0.75, 0.75, 0.08, 0.156};
        int cellW = w / moods.length;
        int duck = (int) (Math.min(cellW, h) * 0.62);
        for (int i = 0; i < moods.length; i++) {
            int cx = cellW * i + cellW / 2;
            paintDuck(g, cx - duck / 2, 110, duck, moods[i], phase[i]);
            g.setColor(new Color(0x3A, 0x42, 0x4F));
            g.setFont(new Font("SansSerif", Font.PLAIN, 22));
            drawCentered(g, caps[i], cx, 110 + duck + 44);
        }
        g.dispose();
        return img;
    }

    static BufferedImage editorMockup(int w, int h) {
        BufferedImage img = canvas(w, h);
        Graphics2D g = g2(img);

        g.setColor(new Color(0x1E, 0x1F, 0x22));
        g.fillRect(0, 0, w, h);

        g.setColor(new Color(0x2B, 0x2D, 0x30));
        g.fillRect(0, 0, w, 38);
        for (int i = 0; i < 3; i++) {
            g.setColor(new Color[]{new Color(0xFF, 0x5F, 0x57), new Color(0xFE, 0xBC, 0x2E), new Color(0x28, 0xC8, 0x40)}[i]);
            g.fill(new Ellipse2D.Float(18 + i * 22, 13, 12, 12));
        }
        g.setColor(new Color(0x9A, 0xA0, 0xA6));
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.drawString("Main.kt", 120, 25);

        g.setColor(new Color(0x26, 0x28, 0x2B));
        g.fillRect(0, 38, 56, h - 38);

        int y = 78, lh = 30;
        java.util.Random rnd = new java.util.Random(7);
        Color kw = new Color(0xCF, 0x8E, 0x6D), id = new Color(0xBC, 0xBE, 0xC4),
              str = new Color(0x6A, 0xAB, 0x73), com = new Color(0x7A, 0x7E, 0x85);
        for (int line = 0; line < 18; line++) {
            int x = 80 + (line % 4) * 18;
            g.setColor(new Color(0x4B, 0x50, 0x58));
            g.setFont(new Font("Monospaced", Font.PLAIN, 14));
            g.drawString(String.valueOf(line + 1), 26, y + 12);
            int tokens = 2 + rnd.nextInt(4);
            int tx = x;
            boolean errorLine = (line == 9);
            for (int t = 0; t < tokens; t++) {
                int tw = 40 + rnd.nextInt(120);
                Color c = new Color[]{kw, id, id, str, com}[rnd.nextInt(5)];
                g.setColor(c);
                g.fillRoundRect(tx, y, tw, 12, 6, 6);
                if (errorLine && t == 1) {
                    drawSquiggle(g, tx, y + 17, tw);
                }
                tx += tw + 12;
                if (tx > w - 360) break;
            }
            y += lh;
        }

        int d = 150;
        paintDuck(g, w - d - 28, h - d - 28, d, Mood.PANIC, 0.08);

        g.dispose();
        return img;
    }

    static BufferedImage tellMockup(int w, int h) {
        BufferedImage img = canvas(w, h);
        Graphics2D g = g2(img);
        g.setColor(new Color(0x1E, 0x1F, 0x22));
        g.fillRect(0, 0, w, h);

        int pw = (int) (w * 0.8), ph = (int) (h * 0.74);
        int px = (w - pw) / 2, py = (h - ph) / 2;
        g.setColor(new Color(0x2B, 0x2D, 0x30));
        g.fill(new RoundRectangle2D.Float(px, py, pw, ph, 24, 24));
        g.setColor(new Color(0x3C, 0x3F, 0x43));
        g.draw(new RoundRectangle2D.Float(px, py, pw, ph, 24, 24));

        int d = (int) (ph * 0.62);
        paintDuck(g, px + 28, py + (ph - d) / 2, d, Mood.NOD, 0.156);

        int tx = px + 28 + d + 28;
        g.setColor(new Color(0xE6, 0xE8, 0xEB));
        g.setFont(new Font("SansSerif", Font.BOLD, 26));
        g.drawString("Tell me what's wrong. I'm listening.", tx, py + 60);
        g.setColor(new Color(0x9A, 0xA0, 0xA6));
        g.setFont(new Font("SansSerif", Font.PLAIN, 17));
        g.drawString("(I won't answer — just say it out loud.)", tx, py + 90);

        g.setColor(new Color(0x1E, 0x1F, 0x22));
        int boxW = pw - (tx - px) - 28, boxH = (int) (ph * 0.42);
        g.fill(new RoundRectangle2D.Float(tx, py + 112, boxW, boxH, 10, 10));
        g.setColor(new Color(0x45, 0x49, 0x4F));
        g.draw(new RoundRectangle2D.Float(tx, py + 112, boxW, boxH, 10, 10));
        g.setColor(new Color(0xC8, 0xCB, 0xD0));
        g.setFont(new Font("Monospaced", Font.PLAIN, 16));
        g.drawString("...it just doesn't work and I don't know why", tx + 14, py + 142);

        g.setColor(new Color(0x6F, 0xC6, 0xFF));
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.drawString("Quack.", tx, py + ph - 28);

        g.dispose();
        return img;
    }

    static void paintDuck(Graphics2D base, int x, int y, int size, Mood mood, double phase) {
        Graphics2D g = (Graphics2D) base.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.translate(x, y);
        g.scale(size, size);
        g.setStroke(new BasicStroke((float) Math.max(1.6f / size, 0.012f)));

        double breath = Math.sin(phase * 2 * Math.PI / 3.0) * 0.5 + 0.5;
        switch (mood) {
            case IDLE  -> drawDuck(g, breath, 0.0, false, false, 0.0);
            case HAPPY -> drawDuck(g, breath * 1.4, -0.04, true, false, 0.0);
            case PANIC -> drawDuck(g, breath, 0.0, false, true, Math.sin(phase * 2 * Math.PI * 9) * 0.012);
            case NOD   -> drawDuck(g, breath, (Math.sin(phase * 2 * Math.PI * 1.6) * 0.5 + 0.5) * 0.22, false, false, 0.0);
        }
        g.dispose();
    }

    static void drawDuck(Graphics2D g, double breath, double headTilt, boolean happy, boolean panic, double shake) {
        double grow = 0.02 * breath;
        double cx = 0.5 + shake;
        double bodyW = 0.62 + grow, bodyH = 0.46 + grow, bodyCy = 0.66;

        fill(g, BODY, new Ellipse2D.Double(cx - bodyW / 2, bodyCy - bodyH / 2, bodyW, bodyH));
        fill(g, BODY_SHADE, new Arc2D.Double(cx - bodyW / 2, bodyCy - bodyH / 2, bodyW, bodyH, 200, 140, Arc2D.PIE));

        Path2D tail = new Path2D.Double();
        tail.moveTo(cx - bodyW / 2 + 0.02, bodyCy - 0.04);
        tail.lineTo(cx - bodyW / 2 - 0.10, bodyCy - 0.12);
        tail.lineTo(cx - bodyW / 2 + 0.04, bodyCy + 0.06);
        tail.closePath();
        fill(g, BODY, tail);

        double headR = 0.30, headCx = cx + 0.14, headCy = 0.38 + headTilt;
        fill(g, BODY, new Ellipse2D.Double(headCx - headR / 2, headCy - headR / 2, headR, headR));

        double beakY = headCy + 0.02 + headTilt * 0.4;
        Path2D beak = new Path2D.Double();
        beak.moveTo(headCx + headR / 2 - 0.02, beakY - 0.05);
        beak.lineTo(headCx + headR / 2 + 0.20, beakY);
        beak.lineTo(headCx + headR / 2 - 0.02, beakY + 0.05);
        beak.closePath();
        fill(g, BEAK, beak);

        double eyeX = headCx + 0.06, eyeY = headCy - 0.05 + headTilt * 0.5;
        if (happy) {
            stroke(g, EYE, new Arc2D.Double(eyeX - 0.04, eyeY - 0.02, 0.08, 0.06, 200, 140, Arc2D.OPEN));
            fill(g, BLUSH, new Ellipse2D.Double(eyeX - 0.10, eyeY + 0.06, 0.07, 0.045));
        } else if (panic) {
            fill(g, Color.WHITE, new Ellipse2D.Double(eyeX - 0.05, eyeY - 0.05, 0.10, 0.11));
            fill(g, EYE, new Ellipse2D.Double(eyeX - 0.018, eyeY - 0.005, 0.036, 0.04));
            double dx = headCx - 0.12, dy = headCy - 0.14;
            Path2D drop = new Path2D.Double();
            drop.moveTo(dx, dy);
            drop.curveTo(dx + 0.05, dy + 0.06, dx + 0.045, dy + 0.11, dx, dy + 0.11);
            drop.curveTo(dx - 0.045, dy + 0.11, dx - 0.05, dy + 0.06, dx, dy);
            drop.closePath();
            fill(g, SWEAT, drop);
        } else {
            fill(g, EYE, new Ellipse2D.Double(eyeX - 0.028, eyeY - 0.028, 0.056, 0.056));
            fill(g, Color.WHITE, new Ellipse2D.Double(eyeX - 0.008, eyeY - 0.018, 0.018, 0.018));
        }

        if (panic) {
            fill(g, PANIC_TINT, new Ellipse2D.Double(cx - bodyW / 2, bodyCy - bodyH / 2, bodyW, bodyH));
        }
    }

    static void fill(Graphics2D g, Color c, Shape s) { g.setColor(c); g.fill(s); }
    static void stroke(Graphics2D g, Color c, Shape s) { g.setColor(c); g.draw(s); }

    static void drawSquiggle(Graphics2D g, int x, int y, int w) {
        g.setColor(new Color(0xE0, 0x5A, 0x4F));
        g.setStroke(new BasicStroke(1.6f));
        Path2D p = new Path2D.Float();
        p.moveTo(x, y);
        for (int i = 0; i <= w; i += 4) {
            p.lineTo(x + i, y + ((i / 4) % 2 == 0 ? -2 : 2));
        }
        g.draw(p);
    }

    static void drawCentered(Graphics2D g, String text, int cx, int baseY) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, cx - fm.stringWidth(text) / 2, baseY);
    }

    static BufferedImage canvas(int w, int h) {
        return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    }

    static Graphics2D g2(BufferedImage img) {
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        return g;
    }

    static void writePng(BufferedImage img, File f) throws Exception {
        ImageIO.write(img, "png", f);
        System.out.println("wrote " + f.getName() + " (" + img.getWidth() + "x" + img.getHeight() + ")");
    }
}
