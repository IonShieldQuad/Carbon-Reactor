package ionshield.carbonreactor.math;

import java.util.function.BiFunction;

public abstract class SolverUtils {
    public static final Double EPSILON = 0.001;
    
    public static PointDouble gradient(BiFunction<Double, Double, Double> f, PointDouble point) {
        return new PointDouble(fdx(f, point, 1), fdy(f, point, 1));
    }
    
    public static double fdx(BiFunction<Double, Double, Double> f, PointDouble point, int order) {
        if (order < 0) {
            throw new IllegalArgumentException("Derivative order has to be non-negative");
        }
        if (order == 0) {
            return f.apply(point.getX(), point.getY());
        }
        return (fdx(f, point.add(EPSILON, 0), order - 1) - fdx(f, point, order - 1)) / EPSILON;
    }
    
    public static double fdx(BiFunction<Double, Double, Double> f, PointDouble point) {
        return fdx(f, point, 1);
    }
    
    public static double fdy(BiFunction<Double, Double, Double> f, PointDouble point, int order) {
        if (order < 0) {
            throw new IllegalArgumentException("Derivative order has to be non-negative");
        }
        if (order == 0) {
            return f.apply(point.getX(), point.getY());
        }
        return (fdy(f, point.add(0, EPSILON), order - 1) - fdy(f, point, order - 1)) / EPSILON;
    }
    
    public static double fdy(BiFunction<Double, Double, Double> f, PointDouble point) {
        return fdy(f, point, 1);
    }
    
    public static PointDouble findMinOnAxis(BiFunction<Double, Double, Double> f, PointDouble axis, PointDouble startPoint) {
        
        int i = 0;
        double prevX = startPoint.getX();
        double prevY = startPoint.getY();
        double currX = startPoint.getX();
        double currY = startPoint.getY();
        double nextX = currX + axis.getX() * Math.pow(2, i);
        double nextY = currY + axis.getY() * Math.pow(2, i);
        i++;
        
        do {
            nextX = currX + axis.getX() * Math.pow(2, i);
            nextY = currY + axis.getY() * Math.pow(2, i);
            if (f.apply(currX, currY) >= f.apply(nextX, nextY)) {
                //Positive direction
                while (f.apply(currX, currY) > f.apply(nextX, nextY)) {
                    prevX = currX;
                    prevY = currY;
                    currX = nextX;
                    currY = nextY;
                    nextX = currX + axis.getX() * Math.pow(2, i);
                    nextY = currY + axis.getY() * Math.pow(2, i);
                    i++;
                }
                double minX = prevX;
                double maxX = nextX;
                double minY = prevY;
                double maxY = nextY;
                Solver1D solver = new GoldenRatioSolver();
                solver.setF(a -> f.apply(minX * (1 - a) + maxX * a, minY * (1 - a) + maxY * a));
                PointDouble res = solver.solve(0, 1);
                PointDouble point = new PointDouble(maxX * res.getX() + minX * (1 - res.getX()), maxY * res.getX() + minY * (1 - res.getX()));
                return point;
            }
            nextX = currX - axis.getX() * Math.pow(2, i - 1);
            nextY = currY - axis.getY() * Math.pow(2, i - 1);
            if (f.apply(currX, currY) > f.apply(nextX, nextY)) {
                //Negative direction
                while (f.apply(currX, currY) >= f.apply(nextX, nextY)) {
                    prevX = currX;
                    prevY = currY;
                    currX = nextX;
                    currY = nextY;
                    nextX = currX - axis.getX() * Math.pow(2, i);
                    nextY = currY - axis.getY() * Math.pow(2, i);
                    i++;
                }
                double minX = prevX;
                double maxX = nextX;
                double minY = prevY;
                double maxY = nextY;
                Solver1D solver = new GoldenRatioSolver();
                solver.setF(a -> f.apply(minX * (1 - a) + maxX * a, minY * (1 - a) + maxY * a));
                PointDouble res = solver.solve(0, 1);
                PointDouble point = new PointDouble(maxX * res.getX() + minX * (1 - res.getX()), maxY * res.getX() + minY * (1 - res.getX()));
                return point;
            }
            
            axis = axis.scale(0.5);
        } while (axis.length() > EPSILON);
        return startPoint;
    }
}
