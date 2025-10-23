import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.util.*;

public class Main extends JFrame {
    private JPanel colorPreview;
    private JTextField hexField;

    private JSlider rSlider, gSlider, bSlider;
    private JTextField rField, gField, bField;

    private JSlider cSlider, mSlider, ySlider, kSlider;
    private JTextField cField, mField, yField, kField;

    private JSlider hSlider, lSlider, sSlider;
    private JTextField hField, lField, sField;

    private boolean updating = false;

    public Main() {
        super("Цветовые модели - CMYK, RGB, HLS");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(700, 600);
        setLayout(new BorderLayout());

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Цветовые модели: CMYK, RGB, HLS", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setAlignmentX(CENTER_ALIGNMENT);
        container.add(title);

        JPanel colorPanel = new JPanel();
        colorPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        colorPreview = new JPanel();
        colorPreview.setPreferredSize(new Dimension(80, 80));
        colorPreview.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        colorPreview.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                Color initialColor = colorPreview.getBackground();
                Color chosenColor = JColorChooser.showDialog(Main.this, "Выбор цвета", initialColor);
                if (chosenColor != null) {
                    updating = true;
                    int r = chosenColor.getRed();
                    int g = chosenColor.getGreen();
                    int b = chosenColor.getBlue();
                    updateAll(r, g, b);
                    updating = false;
                }
            }
        });

        hexField = new JTextField("#FFFFFF", 7);
        colorPanel.add(colorPreview);
        colorPanel.add(hexField);
        container.add(colorPanel);

        JPanel rgbPanel = createSliderPanel("RGB", new String[]{"R", "G", "B"}, new int[]{255, 255, 255});// Red, Green, Blue
        rSlider = (JSlider) rgbPanel.getClientProperty("RSlider");
        gSlider = (JSlider) rgbPanel.getClientProperty("GSlider");
        bSlider = (JSlider) rgbPanel.getClientProperty("BSlider");
        rField = (JTextField) rgbPanel.getClientProperty("RField");
        gField = (JTextField) rgbPanel.getClientProperty("GField");
        bField = (JTextField) rgbPanel.getClientProperty("BField");
        container.add(rgbPanel);

        JPanel cmykPanel = createSliderPanel("CMYK", new String[]{"C", "M", "Y", "K"}, new int[]{0, 0, 0, 0});// Cyan, Magenta, Yellow, Key/Black
        cSlider = (JSlider) cmykPanel.getClientProperty("CSlider");
        mSlider = (JSlider) cmykPanel.getClientProperty("MSlider");
        ySlider = (JSlider) cmykPanel.getClientProperty("YSlider");
        kSlider = (JSlider) cmykPanel.getClientProperty("KSlider");
        cField = (JTextField) cmykPanel.getClientProperty("CField");
        mField = (JTextField) cmykPanel.getClientProperty("MField");
        yField = (JTextField) cmykPanel.getClientProperty("YField");
        kField = (JTextField) cmykPanel.getClientProperty("KField");
        container.add(cmykPanel);

        JPanel hlsPanel = createSliderPanel("HLS", new String[]{"H", "L", "S"}, new int[]{0, 50, 100}); // Hue, Lightness, Saturation
        hSlider = (JSlider) hlsPanel.getClientProperty("HSlider");
        lSlider = (JSlider) hlsPanel.getClientProperty("LSlider");
        sSlider = (JSlider) hlsPanel.getClientProperty("SSlider");
        hField = (JTextField) hlsPanel.getClientProperty("HField");
        lField = (JTextField) hlsPanel.getClientProperty("LField");
        sField = (JTextField) hlsPanel.getClientProperty("SField");
        container.add(hlsPanel);

        add(container, BorderLayout.CENTER);

        ChangeListener rgbChange = e -> {
            if (!updating) updateFromRgb();
        };
        rSlider.addChangeListener(rgbChange);
        gSlider.addChangeListener(rgbChange);
        bSlider.addChangeListener(rgbChange);

        cSlider.addChangeListener(e -> { if(!updating) updateFromCmyk(); });
        mSlider.addChangeListener(e -> { if(!updating) updateFromCmyk(); });
        ySlider.addChangeListener(e -> { if(!updating) updateFromCmyk(); });
        kSlider.addChangeListener(e -> { if(!updating) updateFromCmyk(); });

        hSlider.addChangeListener(e -> { if(!updating) updateFromHls(); });
        lSlider.addChangeListener(e -> { if(!updating) updateFromHls(); });
        sSlider.addChangeListener(e -> { if(!updating) updateFromHls(); });

        updateAll(255,255,255);

        setVisible(true);
    }

    private JPanel createSliderPanel(String title, String[] labels, int[] initialValues) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(labels.length, 1));
        panel.setBorder(BorderFactory.createTitledBorder(title));

        for (int i = 0; i < labels.length; i++) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel label = new JLabel(labels[i]+": ");
            JSlider slider = new JSlider();
            if (title.equals("RGB")) slider.setMaximum(255);
            else if (title.equals("CMYK") || labels[i].equals("L") || labels[i].equals("S")) slider.setMaximum(100);
            else if (labels[i].equals("H")) slider.setMaximum(360);
            slider.setValue(initialValues[i]);
            JTextField field = new JTextField(String.valueOf(initialValues[i]), 3);

            row.add(label);
            row.add(slider);
            row.add(field);
            panel.add(row);

            panel.putClientProperty(labels[i]+"Slider", slider);
            panel.putClientProperty(labels[i]+"Field", field);
        }
        return panel;
    }

    private void updateFromRgb() {
        updating = true;
        int r = rSlider.getValue();
        int g = gSlider.getValue();
        int b = bSlider.getValue();
        updateAll(r,g,b);
        updating = false;
    }

    private void updateFromCmyk() {
        updating = true;
        int c = cSlider.getValue();
        int m = mSlider.getValue();
        int y = ySlider.getValue();
        int k = kSlider.getValue();
        int[] rgb = cmykToRgb(c,m,y,k);
        updateAll(rgb[0], rgb[1], rgb[2]);
        updating = false;
    }

    private void updateFromHls() {
        updating = true;
        int h = hSlider.getValue();
        int l = lSlider.getValue();
        int s = sSlider.getValue();
        int[] rgb = hlsToRgb(h, l, s);
        updateAll(rgb[0], rgb[1], rgb[2]);
        updating = false;
    }

    private void updateAll(int r, int g, int b) {
        rSlider.setValue(r); rField.setText(""+r);
        gSlider.setValue(g); gField.setText(""+g);
        bSlider.setValue(b); bField.setText(""+b);

        String hex = String.format("#%02X%02X%02X", r, g, b);
        hexField.setText(hex);
        colorPreview.setBackground(new Color(r,g,b));

        int[] cmyk = rgbToCmyk(r,g,b);
        cSlider.setValue(cmyk[0]); cField.setText(""+cmyk[0]);
        mSlider.setValue(cmyk[1]); mField.setText(""+cmyk[1]);
        ySlider.setValue(cmyk[2]); yField.setText(""+cmyk[2]);
        kSlider.setValue(cmyk[3]); kField.setText(""+cmyk[3]);

        int[] hls = rgbToHls(r,g,b);
        hSlider.setValue(hls[0]); hField.setText(""+hls[0]);
        lSlider.setValue(hls[1]); lField.setText(""+hls[1]);
        sSlider.setValue(hls[2]); sField.setText(""+hls[2]);
    }

    private int[] rgbToCmyk(int r,int g,int b){
        double rd = r/255.0, gd = g/255.0, bd = b/255.0;
        double k = 1 - Math.max(rd, Math.max(gd, bd));
        if (k == 1) return new int[]{0,0,0,100};
        int c = (int)Math.round((1-rd-k)/(1-k)*100);
        int m = (int)Math.round((1-gd-k)/(1-k)*100);
        int y = (int)Math.round((1-bd-k)/(1-k)*100);
        int kk = (int)Math.round(k*100);
        return new int[]{c,m,y,kk};
    }

    private int[] cmykToRgb(int c,int m,int y,int k){
        int r = (int)Math.round(255*(1-c/100.0)*(1-k/100.0));
        int g = (int)Math.round(255*(1-m/100.0)*(1-k/100.0));
        int b = (int)Math.round(255*(1-y/100.0)*(1-k/100.0));
        return new int[]{r,g,b};
    }

    private int[] rgbToHls(int r, int g, int b) {
        double rd = r / 255.0, gd = g / 255.0, bd = b / 255.0;
        double max = Math.max(rd, Math.max(gd, bd));
        double min = Math.min(rd, Math.min(gd, bd));
        double h = 0, l = (max + min) / 2;
        double s;

        double delta = max - min;
        if (delta == 0) {
            h = 0;
            s = 0;
        } else {
            s = delta / (1 - Math.abs(2 * l - 1));
            if (max == rd)
                h = 60 * (((gd - bd) / delta) % 6);
            else if (max == gd)
                h = 60 * (((bd - rd) / delta) + 2);
            else
                h = 60 * (((rd - gd) / delta) + 4);
        }
        if (h < 0) h += 360;

        return new int[]{(int)Math.round(h), (int)Math.round(l * 100), (int)Math.round(s * 100)};
    }

    private int[] hlsToRgb(int h, int l, int s) {
        double H = h / 360.0, L = l / 100.0, S = s / 100.0;
        double r, g, b;

        if (S == 0) {
            r = g = b = L;
        } else {
            double q = (L < 0.5) ? (L * (1 + S)) : (L + S - L * S);
            double p = 2 * L - q;
            r = hueToRgb(p, q, H + 1.0/3.0);
            g = hueToRgb(p, q, H);
            b = hueToRgb(p, q, H - 1.0/3.0);
        }

        return new int[]{
                (int)Math.round(r * 255),
                (int)Math.round(g * 255),
                (int)Math.round(b * 255)
        };
    }

    private double hueToRgb(double p, double q, double t) {
        if (t < 0) t += 1;
        if (t > 1) t -= 1;
        if (t < 1.0/6.0) return p + (q - p) * 6 * t;
        if (t < 1.0/2.0) return q;
        if (t < 2.0/3.0) return p + (q - p) * (2.0/3.0 - t) * 6;
        return p;
    }

    private void checkColorConversions() {
        int step = 20;
        int tolerance = 1;

        for (int r = 0; r <= 255; r += step) {
            for (int g = 0; g <= 255; g += step) {
                for (int b = 0; b <= 255; b += step) {
                    int[] cmyk = rgbToCmyk(r, g, b);
                    int[] rgbBack = cmykToRgb(cmyk[0], cmyk[1], cmyk[2], cmyk[3]);
                    if (Math.abs(r - rgbBack[0]) > tolerance ||
                            Math.abs(g - rgbBack[1]) > tolerance ||
                            Math.abs(b - rgbBack[2]) > tolerance) {
                        System.out.println("RGB->CMYK->RGB ошибка: " + r + "," + g + "," + b +
                                " -> " + Arrays.toString(rgbBack));
                    }

                    int[] hls = rgbToHls(r, g, b);
                    int[] rgbBackHls = hlsToRgb(hls[0], hls[1], hls[2]);
                    if (Math.abs(r - rgbBackHls[0]) > tolerance ||
                            Math.abs(g - rgbBackHls[1]) > tolerance ||
                            Math.abs(b - rgbBackHls[2]) > tolerance) {
                        System.out.println("RGB->HLS->RGB ошибка: " + r + "," + g + "," + b +
                                " -> " + Arrays.toString(rgbBackHls));
                    }
                }
            }
        }
        System.out.println("Проверка преобразований завершена!");
    }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}
