package ionshield.carbonreactor.core;

import com.bulenkov.darcula.DarculaLaf;
import ionshield.carbonreactor.graphics.GraphDisplay;
import ionshield.carbonreactor.math.*;

import javax.swing.*;
import javax.swing.plaf.basic.BasicLookAndFeel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainWindow {
    private JPanel rootPanel;
    private JTextArea log;
    private JTextField cCH4Field;
    private JButton calculateButton;
    private GraphDisplay graph0;
    private JTextField p1Field;
    private JTextField vField;
    private JTextField tInField;
    private JTextField deltaField;
    private JTextField deltaCField;
    private JTextField deltaMField;
    private JTextField deltaTfield;
    private JTextField timeField;
    private JTextField deltaTimeField;
    private JTextField cC3H4Field;
    private GraphDisplay graph2;
    private GraphDisplay graph1;
    private GraphDisplay graph3;
    private GraphDisplay graph4;

    public static final String TITLE = "Carbon-Reactor";
    
    private MainWindow() {
        initComponents();
    }
    
    private void initComponents() {
        calculateButton.addActionListener(e -> calculate());
    }
    
    
    
    private void calculate() {
        try {
            log.setText("");
            
            double cCH4Fraction = Double.parseDouble(cCH4Field.getText());
            double cC3H4Fraction = Double.parseDouble(cC3H4Field.getText());
            double cO2Fraction = 1 - cC3H4Fraction - cCH4Fraction;

            double v = Double.parseDouble(vField.getText());
            double tIn = Double.parseDouble(tInField.getText());
    
            double time = Double.parseDouble(timeField.getText());
            double deltaTime = Double.parseDouble(deltaTimeField.getText());
            
            int steps = Math.min((int)Math.round(time / deltaTime), 1000000);
            
            CarbonReactor reactor = new CarbonReactor();
            Interpolator[] result = new Interpolator[5];
            
            List<PointDouble> points0 = new ArrayList<>();
            List<PointDouble> points1 = new ArrayList<>();
            List<PointDouble> points2 = new ArrayList<>();
            List<PointDouble> points3 = new ArrayList<>();
            List<PointDouble> points4 = new ArrayList<>();

            double cCH4 = reactor.concentrationInMolesPerCubicMeter(cCH4Fraction, reactor.getmCH4());
            double cC3H4 = reactor.concentrationInMolesPerCubicMeter(cC3H4Fraction, reactor.getmC3H4());
            double cO2 = reactor.concentrationInMolesPerCubicMeter(cO2Fraction, reactor.getmO2());
    
            reactor.init(cCH4, cC3H4, cO2, v, tIn);
            
            points0.add(new PointDouble(0, reactor.getcC()));
            points1.add(new PointDouble(0, reactor.getQ() / (reactor.getVolume() * reactor.getDensity() * reactor.getCt())));
            points2.add(new PointDouble(0, reactor.getcCH4()));
            points3.add(new PointDouble(0, reactor.getcC3H4()));
            points4.add(new PointDouble(0, reactor.getcO2()));
            
            for (int i = 0; i <= steps; i++) {
                reactor.tick(deltaTime, cCH4, cC3H4, cO2, tIn);
                
                double currTime = reactor.getTime();
    
                PointDouble point0 = new PointDouble(currTime, reactor.getcC());
                PointDouble point1 = new PointDouble(currTime, reactor.getQ() / (reactor.getVolume() * reactor.getDensity() * reactor.getCt()));
                PointDouble point2 = new PointDouble(currTime, reactor.getcCH4());
                PointDouble point3 = new PointDouble(currTime, reactor.getcC3H4());
                PointDouble point4 = new PointDouble(currTime, reactor.getcO2());
                
                points0.add(point0);
                points1.add(point1);
                points2.add(point2);
                points3.add(point3);
                points4.add(point4);
                
                log.append("\n" + point0.toString(6));
            }
                    
            result[0] = new LinearInterpolator(points0);
            result[1] = new LinearInterpolator(points1);
            result[2] = new LinearInterpolator(points2);
            result[3] = new LinearInterpolator(points3);
            result[4] = new LinearInterpolator(points4);
            
            updateGraphs(result);
        }
        catch (NumberFormatException e) {
            log.append("\nInvalid input format");
        }
    }
    
    private void updateGraphs(Interpolator[] result) {
        try {
            if (result == null || result.length < 5) {

                graph0.setInterpolators(new ArrayList<>());
                graph0.repaint();
                graph1.setInterpolators(new ArrayList<>());
                graph1.repaint();
                graph2.setInterpolators(new ArrayList<>());
                graph2.repaint();
                graph3.setInterpolators(new ArrayList<>());
                graph3.repaint();
                graph4.setInterpolators(new ArrayList<>());
                graph4.repaint();
                return;
            }
            for (int i = 0; i < result.length; i++) {
                if (result[i] == null) return;
            }
            /*graphCout.setMinX(result[0].lower());
            graphCout.setMaxX(result[0].upper());
            graphCout.setMinY(result[0].lowerVal());
            graphCout.setMaxY(result[0].upperVal());*/
    
            /*graphCin.setMinX(result[1].lower());
            graphCin.setMaxX(result[1].upper());
            graphCin.setMinY(result[1].lowerVal());
            graphCin.setMaxY(result[1].upperVal());
    
            graphMin.setMinX(result[2].lower());
            graphMin.setMaxX(result[2].upper());
            graphMin.setMinY(result[2].lowerVal());
            graphMin.setMaxY(result[2].upperVal());
    
            graphTp.setMinX(result[3].lower());
            graphTp.setMaxX(result[3].upper());
            graphTp.setMinY(result[3].lowerVal());
            graphTp.setMaxY(result[3].upperVal());*/
            
            graph0.setInterpolators(Collections.singletonList(result[0]));
            graph1.setInterpolators(Collections.singletonList(result[1]));
            graph2.setInterpolators(Collections.singletonList(result[2]));
            graph3.setInterpolators(Collections.singletonList(result[3]));
            graph4.setInterpolators(Collections.singletonList(result[4]));
            /*graphCin.setInterpolators(Collections.singletonList(result[1]));
            graphMin.setInterpolators(Collections.singletonList(result[2]));
            graphTp.setInterpolators(Collections.singletonList(result[3]));*/
            
            //graph.setInterpolatorsHighligthed(Collections.singletonList(results.get(results.size() - 1)));
    
            graph0.repaint();
            graph1.repaint();
            graph2.repaint();
            graph3.repaint();
            graph4.repaint();
            /*graphCin.repaint();
            graphMin.repaint();
            graphTp.repaint();*/
        }
        catch (NumberFormatException e) {
            log.append("\nInvalid input format");
        }
    }
    
    
    public static void main(String[] args) {
        BasicLookAndFeel darcula = new DarculaLaf();
        try {
            UIManager.setLookAndFeel(darcula);
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame(TITLE);
        MainWindow gui = new MainWindow();
        frame.setContentPane(gui.rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
