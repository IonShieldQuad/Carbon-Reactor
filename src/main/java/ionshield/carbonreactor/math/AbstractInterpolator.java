package ionshield.carbonreactor.math;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class AbstractInterpolator implements Interpolator {
    private final List<PointDouble> points;
    
    AbstractInterpolator(List<PointDouble> points) {
        this.points = new ArrayList<>();
        points.forEach(p -> this.points.add(new PointDouble(p.getX(), p.getY())));
        this.points.sort(Comparator.comparing(PointDouble::getX));
    }
    
    protected List<PointDouble> getPoints() {
        return points;
    }
    
    
    @Override
    public abstract double evaluate(double value);
    
    @Override
    public double lower() {
        return points.get(0).getX();
    }
    @Override
    public double upper() {
        return points.get(points.size() - 1).getX();
    }
}
