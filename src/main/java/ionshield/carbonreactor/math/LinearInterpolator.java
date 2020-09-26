package ionshield.carbonreactor.math;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class LinearInterpolator extends AbstractInterpolator {
    
    public LinearInterpolator(List<PointDouble> points) {
        super(points);
    }
    
    @Override
    public double lowerVal() {
        Optional<Double> res = getPoints().stream().map(PointDouble::getY).min(Comparator.naturalOrder());
        return res.map(aDouble -> !Double.isNaN(aDouble) ? aDouble : -Double.MAX_VALUE).orElse(0.0);
    }
    
    @Override
    public double upperVal() {
        Optional<Double> res = getPoints().stream().map(PointDouble::getY).max(Comparator.naturalOrder());
        return res.map(aDouble -> !Double.isNaN(aDouble) ? aDouble : Double.MAX_VALUE).orElse(0.0);
    }
    
    @Override
    public double evaluate(double value) {
        if (getPoints().size() == 0) {
            return 0;
        }
        int lastIndex = getPoints().size() - 1;
        if (value <= lower()) {
            if (getPoints().size() == 1) {
                return getPoints().get(0).getY();
            }
            PointDouble l = getPoints().get(0);
            PointDouble r = getPoints().get(1);
            double alpha = (value - l.getX()) / (r.getX() - l.getX());
            return alpha * r.getY() + (1 - alpha) * l.getY();
        }
        if (value >= upper()) {
            if (getPoints().size() == 1) {
                return getPoints().get(lastIndex).getY();
            }
            PointDouble l = getPoints().get(lastIndex - 1);
            PointDouble r = getPoints().get(lastIndex);
            double alpha = (value - l.getX()) / (r.getX() - l.getX());
            return alpha * r.getY() + (1 - alpha) * l.getY();
        }
        int i0 = 0;
        int i1 = lastIndex;
        int i = (int)Math.floor((i0 + i1) / 2.0);
        while (i0 <= i1) {
            int m = (int)Math.floor((i0 + i1) / 2.0);
            if (m + 1 > lastIndex) {
                i = m;
                break;
            }
            if (getPoints().get(m).getX() > value) {
                i1 = m - 1;
            }
            else {
                if (getPoints().get(m + 1).getX() < value) {
                    i0 = m + 1;
                }
                else {
                    i = m;
                    break;
                }
            }
        }
        PointDouble l = getPoints().get(i);
        PointDouble r = getPoints().get(i + 1);
        double alpha = (value - l.getX()) / (r.getX() - l.getX());
        
        return alpha * r.getY() + (1 - alpha) * l.getY();
    }
}
