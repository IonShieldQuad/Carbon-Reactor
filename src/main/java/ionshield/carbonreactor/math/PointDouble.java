package ionshield.carbonreactor.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PointDouble {
    private Double x;
    private Double y;
    
    public PointDouble(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public PointDouble copy() {
        return new PointDouble(x, y);
    }
    
    public Double getX() {
        return x;
    }
    
    public void setX(Double x) {
        this.x = x;
    }
    
    public Double getY() {
        return y;
    }
    
    public void setY(Double y) {
        this.y = y;
    }
    
    public PointDouble add(PointDouble b) {
        return new PointDouble(x + b.x, y + b.y);
    }
    
    public PointDouble add(double x, double y) {
        return new PointDouble(this.x + x, this.y + y);
    }
    
    public PointDouble scale(double k) {
        return new PointDouble(k * x, k * y);
    }
    
    public double lengthSquared() {
        return x * x + y * y;
    }
    
    public double length() {
        return Math.sqrt(lengthSquared());
    }
    
    public PointDouble normalize() {
        return scale(1 / length());
    }
    
    @Override
    public String toString() {
        return "(" + x + "; " + y + ")";
    }
    
    public String toString(int precision) {
        if (precision < 0) {
            return toString();
        }
        if (x.isInfinite() || x.isNaN() || y.isInfinite() || y.isNaN()) {
            return toString();
        }
        
        double x = this.x;
        double y = this.y;
        
        x = BigDecimal.valueOf(x).setScale(precision, RoundingMode.HALF_UP).doubleValue();
        y = BigDecimal.valueOf(y).setScale(precision, RoundingMode.HALF_UP).doubleValue();
        
        return "(" + x + "; " + y + ")";
    }
}
