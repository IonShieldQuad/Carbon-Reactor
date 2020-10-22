package ionshield.carbonreactor.math;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class Solver1D {
    public static final Double EPSILON = 0.0001;
    private Function<Double, Double> f;
    private List<String> log = new ArrayList<>();
    private List<PointDouble> points = new ArrayList<>();
    
    public Solver1D(){}
    public Solver1D(Function<Double, Double> f) {
        this.f = f;
    }

    public Function<Double, Double> getF() {
        return this.f;
    }

    public void setF(Function<Double, Double> f) {
        this.f = f;
    }

    /**
     * Solve the equation
     * @param lower Lower bound of search
     * @param upper Upper bound of search
     * @return Root of the equation, null if none found
     * */
    public PointDouble solve(double lower, double upper) {
        points.clear();
        log.clear();
        return solveInternal(lower, upper);
    }
    
    protected abstract PointDouble solveInternal(double lower, double upper);
    protected abstract int getLogBatchSize();

    protected void addToLog(String value) {
        log.add(value);
    }
    protected void addPoint(PointDouble point) {
        points.add(point);
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
        return strings;
    }
    
    public List<PointDouble> getPoints() {
        return points;
    }
}
