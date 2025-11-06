import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;

public class Main extends JFrame {
    private BufferedImage originalImage, resultImage;
    private JLabel originalLabel, resultLabel;
    private JComboBox<String> methodSelect;
    private JTextField lowField, highField;

    public Main() {
        super("ЛР2 — Гистограмма, контраст, выравнивание (Java)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLayout(new BorderLayout());

        JPanel controlPanel = new JPanel();
        JButton loadBtn = new JButton("Загрузить изображение");
        JButton applyBtn = new JButton("Применить");
        JButton resetBtn = new JButton("Сброс");
        JButton saveBtn = new JButton("Сохранить результат");

        methodSelect = new JComboBox<>(new String[]{
                "Линейное контрастирование",
                "Выравнивание по RGB",
                "Выравнивание по яркости (HSV)"
        });

        lowField = new JTextField("0", 4);
        highField = new JTextField("255", 4);

        controlPanel.add(loadBtn);
        controlPanel.add(resetBtn);
        controlPanel.add(saveBtn);
        controlPanel.add(methodSelect);
        controlPanel.add(new JLabel("low:"));
        controlPanel.add(lowField);
        controlPanel.add(new JLabel("high:"));
        controlPanel.add(highField);
        controlPanel.add(applyBtn);

        add(controlPanel, BorderLayout.NORTH);

        JPanel imagesPanel = new JPanel(new GridLayout(1, 2));
        originalLabel = new JLabel("Исходное изображение", SwingConstants.CENTER);
        resultLabel = new JLabel("Обработанное изображение", SwingConstants.CENTER);
        imagesPanel.add(originalLabel);
        imagesPanel.add(resultLabel);
        add(imagesPanel, BorderLayout.CENTER);

        loadBtn.addActionListener(e -> loadImage());
        resetBtn.addActionListener(e -> resetImage());
        saveBtn.addActionListener(e -> saveImage());
        applyBtn.addActionListener(e -> applyProcessing());

        setVisible(true);
    }

    private void loadImage() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                originalImage = ImageIO.read(chooser.getSelectedFile());
                resultImage = deepCopy(originalImage);
                originalLabel.setIcon(new ImageIcon(originalImage));
                resultLabel.setIcon(new ImageIcon(resultImage));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void resetImage() {
        if (originalImage != null) {
            resultImage = deepCopy(originalImage);
            resultLabel.setIcon(new ImageIcon(resultImage));
        }
    }

    private void saveImage() {
        if (resultImage == null) return;
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                ImageIO.write(resultImage, "png", chooser.getSelectedFile());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void applyProcessing() {
        if (originalImage == null) return;
        String method = (String) methodSelect.getSelectedItem();
        BufferedImage processed = null;

        if (method.contains("Линейное")) {
            int low = Integer.parseInt(lowField.getText());
            int high = Integer.parseInt(highField.getText());
            processed = linearStretch(originalImage, low, high);
        } else if (method.contains("RGB")) {
            processed = equalizePerChannel(originalImage);
        } else {
            processed = equalizeLuminanceHSV(originalImage);
        }

        resultImage = processed;
        resultLabel.setIcon(new ImageIcon(resultImage));
    }

    private BufferedImage linearStretch(BufferedImage img, int low, int high) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        double denom = (high - low == 0) ? 1 : (high - low);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color c = new Color(img.getRGB(x, y));
                int r = stretch(c.getRed(), low, high, denom);
                int g = stretch(c.getGreen(), low, high, denom);
                int b = stretch(c.getBlue(), low, high, denom);
                out.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }
        return out;
    }

    private int stretch(int v, int low, int high, double denom) {
        int nv = (int) Math.round((v - low) * 255 / denom);
        return Math.max(0, Math.min(255, nv));
    }

    private BufferedImage equalizePerChannel(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        int[][] hist = new int[3][256];

        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                Color c = new Color(img.getRGB(x, y));
                hist[0][c.getRed()]++;
                hist[1][c.getGreen()]++;
                hist[2][c.getBlue()]++;
            }

        int n = w * h;
        int[][] cdf = new int[3][256];
        for (int ch = 0; ch < 3; ch++) {
            int acc = 0;
            for (int i = 0; i < 256; i++) {
                acc += hist[ch][i];
                cdf[ch][i] = acc;
            }
        }

        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                Color c = new Color(img.getRGB(x, y));
                int r = mapCDF(cdf[0], c.getRed(), n);
                int g = mapCDF(cdf[1], c.getGreen(), n);
                int b = mapCDF(cdf[2], c.getBlue(), n);
                out.setRGB(x, y, new Color(r, g, b).getRGB());
            }

        return out;
    }

    private int mapCDF(int[] cdf, int val, int n) {
        return (int) Math.round(255.0 * (cdf[val] - cdf[0]) / (n - cdf[0] + 1e-6));
    }

    private BufferedImage equalizeLuminanceHSV(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        double[] hist = new double[256];
        double[][] hsvData = new double[w * h][3];

        int idx = 0;
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                Color c = new Color(img.getRGB(x, y));
                float[] hsv = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
                hsvData[idx++] = new double[]{hsv[0], hsv[1], hsv[2]};
                int L = (int) (hsv[2] * 255);
                hist[L]++;
            }

        for (int i = 1; i < 256; i++) hist[i] += hist[i - 1];
        int n = w * h;

        idx = 0;
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                double[] hsv = hsvData[idx++];
                int L = (int) (hsv[2] * 255);
                double newL = hist[L] / n;
                int rgb = Color.HSBtoRGB((float) hsv[0], (float) hsv[1], (float) newL);
                out.setRGB(x, y, rgb);
            }

        return out;
    }

    private BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}
