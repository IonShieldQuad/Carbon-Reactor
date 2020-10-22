package ionshield.carbonreactor.math;

import java.util.function.Function;

public class GoldenRatioSolver extends Solver1D {
    private static final Double RATIO = (3 - Math.sqrt(5)) / 2;
    
    @Override
    public PointDouble solveInternal(double lower, double upper) {
        Function<Double, Double> f = getF();
        double a = lower;
        double b = upper;
        double x1;
        double x2;
        double y1;
        double y2;
        boolean upperCalculated;
        
        int lim = (int)Math.round(-Math.log((upper - lower) / EPSILON) / Math.log(1 - RATIO));
    
        //First iteration
        x1 = a + RATIO * (b - a);
        x2 = b - RATIO * (b - a);
        y1 = f.apply(x1);
        y2 = f.apply(x2);
    
        addToLog("0:");
        addToLog("a = " + a);
        addToLog("b = " + b);
        addToLog("x1 = " + x1);
        addToLog("x2 = " + x2);
        addToLog("y1 = " + y1);
        addToLog("y2 = " + y2);
        addPoint(new PointDouble(x1, y1));
        addPoint(new PointDouble(x2, y2));
    
        if (y1 > y2) {
            a = x1;
            x1 = x2;
            y1 = y2;
            upperCalculated = false;
        }
        else {
            b = x2;
            x2 = x1;
            y2 = y1;
            upperCalculated = true;
        }
        //Rest of the iterations
        for (int i = 1; i < lim ; i++) {
            if (upperCalculated) {
                x1 = a + RATIO * (b - a);
                y1 = f.apply(x1);
            }
            else {
                x2 = b - RATIO * (b - a);
                y2 = f.apply(x2);
            }
            
            addToLog(i + ":");
            addToLog("a = " + a);
            addToLog("b = " + b);
            addToLog("x1 = " + x1);
            addToLog("x2 = " + x2);
            addToLog("y1 = " + y1);
            addToLog("y2 = " + y2);
            addPoint(new PointDouble(x1, y1));
            addPoint(new PointDouble(x2, y2));
    
            if (y1 > y2) {
                a = x1;
                x1 = x2;
                y1 = y2;
                upperCalculated = false;
            }
            else {
                b = x2;
                x2 = x1;
                y2 = y1;
                upperCalculated = true;
            }
        }
    
        return new PointDouble((b + a) / 2, f.apply((b + a) / 2));
    }
    
    @Override
    protected int getLogBatchSize() {
        return 7;
    }
}
