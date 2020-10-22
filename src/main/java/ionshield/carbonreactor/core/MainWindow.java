package ionshield.carbonreactor.core;

import com.bulenkov.darcula.DarculaLaf;
import ionshield.carbonreactor.graphics.ContourGraphDisplay;
import ionshield.carbonreactor.graphics.GraphDisplay;
import ionshield.carbonreactor.graphics.GraphUtils;
import ionshield.carbonreactor.math.*;

import javax.swing.*;
import javax.swing.plaf.basic.BasicLookAndFeel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

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
    private ContourGraphDisplay contourGraphDisplay1;
    private JCheckBox optimizeCheckBox;
    private JTextField minVField;
    private JTextField maxVField;
    private JTextField minTField;
    private JTextField maxTField;
    private JTextField seedField;
    private JTextField multiField;
    private JTextField modField;
    private JTextField m0Field;
    private JTextField s0Field;
    private JTextField a0Field;
    private JCheckBox randomizeCheckBox;

    private GraphDisplay[] graphs = new GraphDisplay[] {};

    public static final String TITLE = "Carbon-Reactor";
    
    private MainWindow() {
        initComponents();
    }
    
    private void initComponents() {
        calculateButton.addActionListener(e -> calculate());
        graphs = new GraphDisplay[] {graph0, graph1, graph2, graph3, graph4};
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
            
            int steps = Math.min((int)Math.round(time / deltaTime), 10000000);
            
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
            
            for (int i = 0; i < steps; i++) {
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
            
            updateGraphs(result, 0, time);
            if (optimizeCheckBox.isSelected()) {
                double minV = Double.parseDouble(minVField.getText());
                double maxV = Double.parseDouble(maxVField.getText());
                double minT = Double.parseDouble(minTField.getText());
                double maxT = Double.parseDouble(maxTField.getText());

                BiFunction<Double, Double, Double> function = (vol, temp) -> {
                    CarbonReactor rc = new CarbonReactor();
                    rc.init(cCH4, cC3H4, cO2, vol, temp);
                    for (int i = 0; i < steps; i++) {
                        rc.tick(deltaTime, cCH4, cC3H4, cO2, temp);
                    }
                    return -rc.getcC();
                };

                Solver solver = new HookeJeevesSolver();
                solver.setF(function);

                List<BiFunction<Double, Double, Double>> limits = new ArrayList<>();
                limits.add((x, y) -> x - minV);
                limits.add((x, y) -> maxV - x);
                limits.add((x, y) -> y - minT);
                limits.add((x, y) -> maxT - y);

                PenaltyAdjuster pa = new PenaltyAdjuster(solver, PenaltyAdjuster.QUADRATIC_PENALTY_FUNCTION, limits, new ArrayList<>(), true);

                PointDouble res = pa.solve(new PointDouble(v, tIn), new PointDouble(0.5, 40));

                log.append("\nOptimized params V and tIn: " + res.toString(6) + "\n");
                log.append("Result: " + GraphUtils.roundDouble(-function.apply(res.getX(), res.getY()), 6, 10, true) + " mol/m^3");

                contourGraphDisplay1.setPoints(pa.getPoints());
                contourGraphDisplay1.setLines(pa.getLines());

                contourGraphDisplay1.setLowerX(minV);
                contourGraphDisplay1.setUpperX(maxV);
                contourGraphDisplay1.setLowerY(minT);
                contourGraphDisplay1.setUpperY(maxT);
                contourGraphDisplay1.setUpperZ(+Double.MAX_VALUE);
                contourGraphDisplay1.setLowerZ(-Double.MAX_VALUE);
                contourGraphDisplay1.setResolution(30);
                contourGraphDisplay1.setFunction(function);
                contourGraphDisplay1.repaint();
            }
        }
        catch (NumberFormatException e) {
            log.append("\nInvalid input format");
        }
    }
    
    private void updateGraphs(Interpolator[] result, double minX, double maxX) {
        try {
            if (result == null || result.length < graphs.length) {

                for (GraphDisplay graph : graphs) {
                    graph.setInterpolators(new ArrayList<>());
                    graph.repaint();
                }

                return;
            }
            for (Interpolator interpolator : result) {
                if (interpolator == null) return;
            }

            for (int i = 0; i < graphs.length; i++) {
                graphs[i].setInterpolators(Collections.singletonList(result[i]));
                graphs[i].setMinX(minX);
                graphs[i].setMaxX(maxX);
                graphs[i].repaint();
            }


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
