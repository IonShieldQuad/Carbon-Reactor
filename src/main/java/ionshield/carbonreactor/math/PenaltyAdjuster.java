package ionshield.carbonreactor.math;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class PenaltyAdjuster extends Solver {
    private Solver solver;
    private BiFunction<Double, Double, Double> function;
    private BiFunction<List<Double>, Double, Double> penaltyFunction;
    private BiFunction<List<Double>, Double, Double> constraintPenaltyFunction = QUADRATIC_BIDIRECTIONAL_PENALTY_FUNCTION;
    private List<BiFunction<Double, Double, Double>> bounds;
    private List<BiFunction<Double, Double, Double>> constraints;
    private boolean enabled;
    
    private double kStart = 1;
    private double kMulti = 10;
    private double k = kStart;
    private int  displayIteration = -1;
    
    public static final BiFunction<List<Double>, Double, Double> INVERSE_PENALTY_FUNCTION = (l, k) -> {
        double sum = 0;
        for (double val : l) {
            if (Double.isNaN(val)) {
                val = 0.0;
            }
            if (val <= 0) {
                return Double.MAX_VALUE;
            }
            sum += 1 / val;
        }
        return Math.min(sum / k, Double.MAX_VALUE);
    };
    
    public static final BiFunction<List<Double>, Double, Double> LOGARITHMIC_PENALTY_FUNCTION = (l, k) -> {
        double sum = 0;
        for (double val : l) {
            if (Double.isNaN(val)) {
                val = 0.0;
            }
            if (val <= 0) {
                return Double.MAX_VALUE;
            }
            sum += Math.log(val);
        }
        return Math.min(-sum / k, Double.MAX_VALUE);
    };
    
    public static final BiFunction<List<Double>, Double, Double> QUADRATIC_PENALTY_FUNCTION = (l, k) -> {
        double sum = 0;
        for (double val : l) {
            if (Double.isNaN(val)) {
                val = 0.0;
            }
            sum += Math.pow(Math.min(0, val), 2);
        }
        return Math.min(sum * k, Double.MAX_VALUE);
    };
    
    public static final BiFunction<List<Double>, Double, Double> QUADRATIC_BIDIRECTIONAL_PENALTY_FUNCTION = (l, k) -> {
        double sum = 0;
        for (double val : l) {
            if (Double.isNaN(val)) {
                val = 0.0;
            }
            sum += Math.pow(val, 2);
        }
        return Math.min(sum * k, Double.MAX_VALUE);
    };
    
    public PenaltyAdjuster(Solver solver, BiFunction<List<Double>, Double, Double> penaltyFunction, List<BiFunction<Double, Double, Double>> bounds, List<BiFunction<Double, Double, Double>> constraints, boolean enabled) {
        this.solver = solver;
        this.penaltyFunction = penaltyFunction;
        this.bounds = bounds;
        this.constraints = constraints;
        this.enabled = enabled;
        if (bounds == null) {
            bounds = new ArrayList<>();
        }
        if (penaltyFunction == null) {
            penaltyFunction = (l, k) -> 0.0;
        }
    }
    
    @Override
    protected PointDouble solveInternal(PointDouble... data) {
        if (!enabled) {
            k = kStart;
            PointDouble res = solver.solve(data);
            solver.getSolutionLog().forEach(this::addToLog);
            solver.getPoints().forEach(this::addPoint);
            solver.getLines().forEach(this::addLine);
            
            return res;
        }
        
        function = solver.getF();
        k = kStart;
        PointDouble curr;
        PointDouble prev;
        int i = 0;
    
        for (int j = 0; j < data.length; j++) {
            PointDouble returned = returnToBounds(data[i]);
            addPoint(returned);
            addPoint(data[i]);
            addLine(returned, data[i]);
            data[i] = returned;
        }
        
        solver.setF(getCombinedPenaltyFunction(function, bounds, penaltyFunction, constraints, constraintPenaltyFunction, k));
        
        curr = returnToBounds(solver.solve(data));
        addPoint(curr);
        addToLog("i = " + i + "; k = " + k + "; Solution: " + curr.toString(PRECISION));
    
        if (displayIteration == i) {
            addToLog("Log of the iteration " + i + ":");
            solver.getSolutionLog().forEach(this::addToLog);
            solver.getPoints().forEach(this::addPoint);
            solver.getLines().forEach(this::addLine);
        }
        
        do {
            i++;
            prev = curr;
            k *= kMulti;
    
            solver.setF(getCombinedPenaltyFunction(function, bounds, penaltyFunction, constraints, constraintPenaltyFunction, k));
            
            curr = returnToBounds(solver.solve(data));
    
            addPoint(curr);
            addToLog("i = " + i + "; k = " + k + "; Solution: " + curr.toString(PRECISION));
    
            if (displayIteration == i) {
                addToLog("Log of the iteration " + i + ":");
                solver.getSolutionLog().forEach(this::addToLog);
                solver.getPoints().forEach(this::addPoint);
                solver.getLines().forEach(this::addLine);
            }
            
        } while (i < 16 && (curr.add(prev.scale(-1)).length() > EPSILON));
    
        if (displayIteration < 0 || displayIteration >= i) {
            addToLog("Log of the iteration " + i + ":");
            solver.getSolutionLog().forEach(this::addToLog);
            solver.getPoints().forEach(this::addPoint);
            solver.getLines().forEach(this::addLine);
        }
        
        solver.setF(function);
        return curr;
    }
    
    @Override
    protected int getLogBatchSize() {
        return 1;
    }
    
    private PointDouble returnToBounds(PointDouble point) {
        PointDouble totalGradient = new PointDouble(0, 0);
        for (BiFunction<Double, Double, Double> bound : bounds) {
            totalGradient = totalGradient.add(SolverUtils.gradient((x, y) -> Math.min(0, bound.apply(x, y)), point));
        }
        if (totalGradient.length() < EPSILON) {
            return point;
        }
        
        BiFunction<Double, Double, Double> totalF = (x, y) -> {
            double sum = 0;
            for (int i = 0; i < bounds.size(); i++) {
                sum += -Math.min(0, bounds.get(i).apply(x, y));
            }
            return sum;
        };
        
        PointDouble res =  SolverUtils.findMinOnAxis(totalF/*getCombinedPenaltyFunction(function, bounds, penaltyFunction, k)*/, totalGradient, point);
        
        int i = 0;
        PointDouble prevGradient = totalGradient;
        while (i < 16 && totalGradient.length() > EPSILON) {
            i++;
            prevGradient = totalGradient;
    
            totalGradient = new PointDouble(0, 0);
            for (BiFunction<Double, Double, Double> bound : bounds) {
                totalGradient = totalGradient.add(SolverUtils.gradient((x, y) -> Math.min(0, bound.apply(x, y)), point));
            }
            
            res =  SolverUtils.findMinOnAxis(totalF, totalGradient, res);
        }
        
        if (prevGradient.length() > EPSILON) {
            res = res.add(prevGradient.normalize().scale(1 / k));
        }
        
        return res;
    }
    
    public static BiFunction<Double, Double, Double> getCombinedPenaltyFunction(BiFunction<Double, Double, Double> function, List<BiFunction<Double, Double, Double>> bounds, BiFunction<List<Double>, Double, Double> penaltyFunction, List<BiFunction<Double, Double, Double>> constraints, BiFunction<List<Double>, Double, Double> constraintPenaltyFunction, double k) {
        return (x, y) -> {
            List<Double> values = new ArrayList<>();
            List<Double> values2 = new ArrayList<>();
            for (BiFunction<Double, Double, Double> bound : bounds) {
                values.add(bound.apply(x, y));
            }
            for (BiFunction<Double, Double, Double> constraint : constraints) {
                values2.add(constraint.apply(x, y));
            }
            return function.apply(x, y) + penaltyFunction.apply(values, k) + constraintPenaltyFunction.apply(values2, k);
        };
    }
    
    public Solver getSolver() {
        return solver;
    }
    
    public void setSolver(Solver solver) {
        this.solver = solver;
    }
    
    public BiFunction<List<Double>, Double, Double> getPenaltyFunction() {
        return penaltyFunction;
    }
    
    public void setPenaltyFunction(BiFunction<List<Double>, Double, Double> penaltyFunction) {
        this.penaltyFunction = penaltyFunction;
        if (this.penaltyFunction == null) {
            penaltyFunction = (l, k) -> 0.0;
        }
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public double getkStart() {
        return kStart;
    }
    
    public void setkStart(double kStart) {
        this.kStart = kStart;
    }
    
    public double getkMulti() {
        return kMulti;
    }
    
    public void setkMulti(double kMulti) {
        this.kMulti = kMulti;
    }
    
    public double getK() {
        return k;
    }
    
    public int getDisplayIteration() {
        return displayIteration;
    }
    
    public void setDisplayIteration(int displayIteration) {
        this.displayIteration = displayIteration;
    }
    
    public double getKOfIteration(int i) {
        return kStart * Math.pow(kMulti, i);
    }
}
