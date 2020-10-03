package ionshield.carbonreactor.math;

public class LineDouble {
    public PointDouble a;
    public PointDouble b;
    
    public LineDouble(PointDouble a, PointDouble b) {
        this.a = a;
        this.b = b;
    }

    public double minX() {
        return Math.min(a.getX(), b.getX());
    }
    public double maxX() {
        return Math.max(a.getX(), b.getX());
    }
    public double minY() {
        return Math.min(a.getY(), b.getY());
    }
    public double maxY() {
        return Math.max(a.getY(), b.getY());
    }
}
