import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main extends JFrame {
    private DrawingPanel drawingPanel;
    private JComboBox<String> algorithmSelector;
    private JButton drawButton;

    public Main() {
        setTitle("Raster Algorithms Demo");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        drawingPanel = new DrawingPanel();
        algorithmSelector = new JComboBox<>(new String[]{
                "Пошаговый алгоритм",
                "Алгоритм ЦДА",
                "Алгоритм Брезенхема (линия)",
                "Алгоритм Брезенхема (окружность)"
        });

        drawButton = new JButton("Нарисовать");
        drawButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                drawingPanel.setAlgorithm(algorithmSelector.getSelectedIndex());
                drawingPanel.repaint();
            }
        });

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Выберите алгоритм:"));
        topPanel.add(algorithmSelector);
        topPanel.add(drawButton);

        add(topPanel, BorderLayout.NORTH);
        add(drawingPanel, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main frame = new Main();
            frame.setVisible(true);
        });
    }
}

class DrawingPanel extends JPanel {
    private int algorithm = 0;
    private final int scale = 20;

    public void setAlgorithm(int algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawGrid(g);
        g.setColor(Color.RED);

        switch (algorithm) {
            case 0:
                drawStepByStepLine(g, 2, 2, 15, 10);
                break;
            case 1:
                drawDDALine(g, 2, 2, 15, 10);
                break;
            case 2:
                drawBresenhamLine(g, 2, 2, 15, 10);
                break;
            case 3:
                drawBresenhamCircle(g, 10, 10, 7);
                break;
        }
    }

    private void drawGrid(Graphics g) {
        g.setColor(Color.LIGHT_GRAY);
        int width = getWidth();
        int height = getHeight();
        for (int i = 0; i < width; i += scale) {
            g.drawLine(i, 0, i, height);
        }
        for (int i = 0; i < height; i += scale) {
            g.drawLine(0, i, width, i);
        }

        g.setColor(Color.BLACK);
        // оси
        g.drawLine(0, height / 2, width, height / 2);
        g.drawLine(width / 2, 0, width / 2, height);
    }

    private void drawPixel(Graphics g, int x, int y) {
        int px = x * scale;
        int py = y * scale;
        g.fillRect(px, py, scale, scale);
    }

    private void drawStepByStepLine(Graphics g, int x0, int y0, int x1, int y1) {
        int dx = x1 - x0;
        int dy = y1 - y0;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));

        for (int i = 0; i <= steps; i++) {
            int x = x0 + i * dx / steps;
            int y = y0 + i * dy / steps;
            drawPixel(g, x, y);
        }
    }

    private void drawDDALine(Graphics g, int x0, int y0, int x1, int y1) {
        int dx = x1 - x0;
        int dy = y1 - y0;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));
        double x = x0;
        double y = y0;
        double xInc = dx / (double) steps;
        double yInc = dy / (double) steps;

        for (int i = 0; i <= steps; i++) {
            drawPixel(g, (int)Math.round(x), (int)Math.round(y));
            x += xInc;
            y += yInc;
        }
    }

    private void drawBresenhamLine(Graphics g, int x0, int y0, int x1, int y1) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            drawPixel(g, x0, y0);
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x0 += sx; }
            if (e2 < dx) { err += dx; y0 += sy; }
        }
    }

    private void drawBresenhamCircle(Graphics g, int xc, int yc, int r) {
        int x = 0;
        int y = r;
        int d = 3 - 2 * r;
        drawCirclePoints(g, xc, yc, x, y);
        while (y >= x) {
            x++;
            if (d > 0) {
                y--;
                d = d + 4 * (x - y) + 10;
            } else {
                d = d + 4 * x + 6;
            }
            drawCirclePoints(g, xc, yc, x, y);
        }
    }

    private void drawCirclePoints(Graphics g, int xc, int yc, int x, int y) {
        drawPixel(g, xc + x, yc + y);
        drawPixel(g, xc - x, yc + y);
        drawPixel(g, xc + x, yc - y);
        drawPixel(g, xc - x, yc - y);
        drawPixel(g, xc + y, yc + x);
        drawPixel(g, xc - y, yc + x);
        drawPixel(g, xc + y, yc - x);
        drawPixel(g, xc - y, yc - x);
    }
}
