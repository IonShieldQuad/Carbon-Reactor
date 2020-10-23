package ionshield.carbonreactor.math;

import java.util.ArrayList;
import java.util.List;

public class RandomProcessGenerator {
    private double m0;
    private double s0;
    private double a0;
    private double a1 = 1;
    private double a2 = 1;
    private double nS = 10;

    private double avg;
    private double dev;

    private boolean valid = false;
    //private List<Double> cache = new ArrayList<>();
    
    public List<Double> row;
    
    public RandomProcessGenerator(double m0, double s0, double a0, List<Double> row) {
        this.m0 = m0;
        this.s0 = s0;
        this.a0 = a0;
        this.row = row;
    }

    private void requireValid() {
        if (!valid) {
            avg = row.stream().mapToDouble(x -> x).average().orElse(0);
            dev = row.stream().mapToDouble(x -> (x - avg) * (x - avg)).average().orElse(0);

            //cache = new ArrayList<>();
        }
        valid = true;
    }

    public double getZ(int k) {
        requireValid();
        if (k - 1 + nS >= row.size()) throw new IllegalArgumentException((k - 1) + " + " + nS + ": out of row bounds (" + (row.size() - 1) + ")");

        double res = 0;
        for (int i = k; i < k + nS; i++) {
            double x = row.get(i - 1);
            res += x * Math.sqrt(s0 / (dev * a0 * a2)) * a1 * Math.exp(-a2 * a0 * (i - k));
        }
        res /= nS;
        res += m0;
        return res;
    }

    public void invalidate() {
        valid = false;
    }

    public double getM0() {
        return m0;
    }
    
    public void setM0(double m0) {
        this.m0 = m0;
        invalidate();
    }
    
    public double getS0() {
        return s0;
    }
    
    public void setS0(double s0) {
        this.s0 = s0;
        invalidate();
    }
    
    public double getA0() {
        return a0;
    }
    
    public void setA0(double a0) {
        this.a0 = a0;
        invalidate();
    }
    
    public double getA1() {
        return a1;
    }
    
    public void setA1(double a1) {
        this.a1 = a1;
        invalidate();
    }
    
    public double getA2() {
        return a2;
    }
    
    public void setA2(double a2) {
        this.a2 = a2;
        invalidate();
    }
    
    public double getnS() {
        return nS;
    }
    
    public void setnS(double nS) {
        this.nS = nS;
        invalidate();
    }
    
    public List<Double> getRow() {
        return row;
    }
    
    public void setRow(List<Double> row) {
        this.row = row;
        invalidate();
    }
}
