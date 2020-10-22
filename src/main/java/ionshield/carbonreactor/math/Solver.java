package ionshield.carbonreactor.math;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public abstract class Solver {
    public static final Double EPSILON = 0.001;
    public static int PRECISION = 3;
    private BiFunction<Double, Double, Double> f;
    private List<String> log = new ArrayList<>();
    private List<PointDouble> points = new ArrayList<>();
    private List<LineDouble> lines = new ArrayList<>();
    
    public Solver(){}
    public Solver(BiFunction<Double, Double, Double> f) {
        this.f = f;
    }

    public BiFunction<Double, Double, Double> getF() {
        return this.f;
    }

    public void setF(BiFunction<Double, Double, Double> f) {
        this.f = f;
    }
    
    public PointDouble solve(PointDouble... data) {
        points.clear();
        log.clear();
        lines.clear();
        return solveInternal(data);
    }
    
    protected abstract PointDouble solveInternal(PointDouble... data);
    protected abstract int getLogBatchSize();

    protected void addToLog(String value) {
        log.add(value);
    }
    protected void addPoint(PointDouble point) {
        points.add(point);
    }
    protected void addLine(LineDouble line) {
        lines.add(line);
    }
    protected void addLine(PointDouble a, PointDouble b) {
        lines.add(new LineDouble(a, b));
    }

    /**@return Returns log as a list of string, each representing a solution step*/
    public List<String> getSolutionLog() {
        List<String> strings = new ArrayList<>();
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < this.log.size(); ++i) {
            if (i % getLogBatchSize() == 0 && string.length() != 0) {
                strings.add(string.toString());
                string = new StringBuilder();
            }
            string.append(this.log.get(i)).append(" ");
        }
        if (string.length() != 0) {
            strings.add(string.toString());
        }
        return strings;
    }
    
    public List<PointDouble> getPoints() {
        return points;
    }
    public List<LineDouble> getLines() {
        return lines;
    }
    
    public PointDouble gradient(PointDouble point) {
        return new PointDouble(fdx(point, 1), fdy(point, 1));
    }
    
    public double fdx(PointDouble point, int order) {
        if (order < 0) {
            throw new IllegalArgumentException("Derivative order has to be non-negative");
        }
        if (order == 0) {
            return f.apply(point.getX(), point.getY());
        }
        return (fdx(point.add(EPSILON, 0), order - 1) - fdx(point, order - 1)) / EPSILON;
    }
    
    public double fdx(PointDouble point) {
        return fdx(point, 1);
    }
    
    public double fdy(PointDouble point, int order) {
        if (order < 0) {
            throw new IllegalArgumentException("Derivative order has to be non-negative");
        }
        if (order == 0) {
            return f.apply(point.getX(), point.getY());
        }
        return (fdy(point.add(0, EPSILON), order - 1) - fdy(point, order - 1)) / EPSILON;
    }
    
    public double fdy(PointDouble point) {
        return fdy(point, 1);
    }
    
    protected PointDouble findMinOnAxis(PointDouble axis, PointDouble startPoint) {
        axis = new PointDouble(Math.min(Math.max(axis.getX(), -Double.MAX_VALUE), Double.MAX_VALUE), Math.min(Math.max(axis.getY(), -Double.MAX_VALUE), Double.MAX_VALUE));
        if (axis.getX().isNaN() || axis.getY().isNaN()) {
            return startPoint;
        }
        
        int i = 0;
        double prevX = startPoint.getX();
        double prevY = startPoint.getY();
        double currX = startPoint.getX();
        double currY = startPoint.getY();
        double nextX = currX + axis.getX() * Math.pow(2, i);
        double nextY = currY + axis.getY() * Math.pow(2, i);
        i++;
        addPoint(new PointDouble(currX, currY));
    
        do {
            nextX = currX + axis.getX() * Math.pow(2, i);
            nextY = currY + axis.getY() * Math.pow(2, i);
            addPoint(new PointDouble(nextX, nextY));
            if (getF().apply(currX, currY) > getF().apply(nextX, nextY)) {
                //Positive direction
                while (getF().apply(currX, currY) > getF().apply(nextX, nextY)) {
                    prevX = currX;
                    prevY = currY;
                    currX = nextX;
                    currY = nextY;
                    nextX = currX + axis.getX() * Math.pow(2, i);
                    nextY = currY + axis.getY() * Math.pow(2, i);
                    i++;
                    addPoint(new PointDouble(nextX, nextY));
                }
                double minX = prevX;
                double maxX = nextX;
                double minY = prevY;
                double maxY = nextY;
                Solver1D solver = new GoldenRatioSolver();
                solver.setF(a -> getF().apply(minX * (1 - a) + maxX * a, minY * (1 - a) + maxY * a));
                PointDouble res = solver.solve(0, 1);
                PointDouble point = new PointDouble(maxX * res.getX() + minX * (1 - res.getX()), maxY * res.getX() + minY * (1 - res.getX()));
                addPoint(point);
                return point;
            }
            nextX = currX - axis.getX() * Math.pow(2, i - 1);
            nextY = currY - axis.getY() * Math.pow(2, i - 1);
            addPoint(new PointDouble(nextX, nextY));
            if (getF().apply(currX, currY) > getF().apply(nextX, nextY)) {
                //Negative direction
                while (getF().apply(currX, currY) > getF().apply(nextX, nextY)) {
                    prevX = currX;
                    prevY = currY;
                    currX = nextX;
                    currY = nextY;
                    nextX = currX - axis.getX() * Math.pow(2, i);
                    nextY = currY - axis.getY() * Math.pow(2, i);
                    i++;
                    addPoint(new PointDouble(nextX, nextY));
                }
                double minX = prevX;
                double maxX = nextX;
                double minY = prevY;
                double maxY = nextY;
                Solver1D solver = new GoldenRatioSolver();
                solver.setF(a -> getF().apply(minX * (1 - a) + maxX * a, minY * (1 - a) + maxY * a));
                PointDouble res = solver.solve(0, 1);
                PointDouble point = new PointDouble(maxX * res.getX() + minX * (1 - res.getX()), maxY * res.getX() + minY * (1 - res.getX()));
                addPoint(point);
                return point;
            }
    
            axis = axis.scale(0.5);
        } while (axis.length() > EPSILON && i < 32);
        return startPoint;
    }
}
