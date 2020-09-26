package ionshield.carbonreactor.graphics;

import ionshield.carbonreactor.math.InterpolationException;
import ionshield.carbonreactor.math.Interpolator;
import ionshield.carbonreactor.math.PointDouble;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GraphDisplay extends JPanel {
    private boolean autoModeX = true;
    private boolean autoModeY = true;
    
    private boolean extrapolationEnabled = false;
    
    private boolean boundRoundingXEnabled = false;
    private boolean boundRoundingYEnabled = false;
    private double boundRoundingX = 1;
    private double boundRoundingY = 1;
    
    private int marginX = 50;
    private int marginY = 50;
    
    private int gridX = 0;
    private int gridY = 0;
    
    private int valueDrawOffsetX = 5;
    private int valueDrawOffsetY = 5;
    
    private double extraAmount = 0.0;
    private Color[] graphColors = new Color[] {
            new Color(0xff7a81),
            new Color(0x9cff67),
            new Color(0x526aff),
            new Color(0xe6bc00),
            new Color(0xdf2a6f),
            new Color(0x01a343),
    };
    private Color[] graphHighlightColors = new Color[] {
            new Color(0x00ffff),
    };
    
    private Color[] pointColors = new Color[] {
            Color.YELLOW
    };
    private Color[] pointHighlightColors = new Color[] {
            Color.GREEN
    };
    
    private int pointSize = 1;
    private int precision = 3;
    private int maxNumberLength = 8;
    
    private List<Interpolator> interpolators = new ArrayList<>();
    private List<Interpolator> interpolatorsHighlighted = new ArrayList<>();
    private List<PointDouble> points = new ArrayList<>();
    private List<PointDouble> pointsHighlighted = new ArrayList<>();
    
    private double lowerX = 0;
    private double upperX = 0;
    private double lowerY = 0;
    private double upperY = 0;
    
    private double minX = 0;
    private double maxX = 1;
    private double minY = 0;
    private double maxY = 1;
    
    public GraphDisplay() {
        super();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        calculateBounds();
        
        drawGrid(g);
        for (int i = 0; i < interpolators.size(); i++) {
            drawGraph(g, interpolators.get(i), graphColors[i % graphColors.length]);
        }
        for (int i = 0; i < interpolatorsHighlighted.size(); i++) {
            drawGraph(g, interpolatorsHighlighted.get(i), graphHighlightColors[i % graphHighlightColors.length]);
        }
        drawPoints(g);
        if (upperX != lowerX && upperY != lowerY && interpolatorCount() + pointCount() > 0) {
            drawValues(g);
        }
    }
    
    public int interpolatorCount() {
        return interpolators.size() + interpolatorsHighlighted.size();
    }
    
    public int pointCount() {
        return points.size() + pointsHighlighted.size();
    }
    
    private void drawPoints(Graphics g) {
        for (int i = 0; i < points.size(); i++) {
            g.setColor(pointColors[i % pointColors.length]);
            PointDouble p = valueToGraph(points.get(i));
            g.drawOval((int)Math.round(p.getX()) - pointSize / 2, (int)Math.round(p.getY()) - pointSize / 2, pointSize, pointSize);
        }
        for (int i = 0; i < pointsHighlighted.size(); i++) {
            g.setColor(pointHighlightColors[i % pointHighlightColors.length]);
            PointDouble p = valueToGraph(pointsHighlighted.get(i));
            g.drawOval((int)Math.round(p.getX()) - pointSize / 2, (int)Math.round(p.getY()) - pointSize / 2, pointSize, pointSize);
        }
    }
    
    private void drawGraph(Graphics g, Interpolator interpolator, Color color) {
        g.setColor(color);
        int prev = 0;
        int start = outerLeft();
        int end = outerRight();
        
        if (!extrapolationEnabled) {
            start = Math.max(start, (int)Math.ceil(valueToGraph(new PointDouble(interpolator.lower(), 0)).getX()));
            end = Math.min(end, (int)Math.floor(valueToGraph(new PointDouble(interpolator.upper(), 0)).getX()));
        }
        
        for (int i = start; i < end; i++) {
            try {
                PointDouble val = graphToValue(new PointDouble(i, 0));
                val = new PointDouble(val.getX(), interpolator.evaluate(val.getX()));
                val = valueToGraph(val);
                if (i != start) {
                    GraphUtils.drawLine(new GraphUtils.Line(i - 1, prev, (int) Math.round(val.getX()), (int)Math.round(val.getY())), g, color);
                    //g.drawLine(MARGIN_X + i - 1, prev, (int) Math.round(val.getX()), (int) Math.round(val.getY()));
                }
                prev = (int) Math.round(val.getY());
            } catch (InterpolationException ignored) {}
        }
    }
    
    private int outerLeft() {
        return marginX;
    }
    private int innerLeft() {
        return marginX + (int)(graphWidth() * extraAmount);
    }
    private int outerRight() {
        return getWidth() - marginX;
    }
    private int innerRight() {
        return getWidth() - marginX - (int)(graphWidth() * extraAmount);
    }
    private int outerTop() {
        return marginY;
    }
    private int innerTop() {
        return marginY + (int)(graphHeight() * extraAmount);
    }
    private int outerBottom() {
        return getHeight() - marginY;
    }
    private int innerBottom() {
        return getHeight() - marginY - (int)(graphHeight() * extraAmount);
    }
    
    
    private void drawGrid(Graphics g) {
        g.setColor(getForeground());
        g.drawLine(outerLeft(), outerBottom(), outerRight(), outerBottom());
    
        for (int i = 0; i <= gridX + 1; i++) {
            double alpha = (double)i / (gridX + 1);
            int val = (int)Math.round(innerLeft() * (1 - alpha) + innerRight() * alpha);
            g.drawLine(val, outerBottom(), val, outerTop());
        }
        
        //g.drawLine(marginX, marginY + (int)(graphHeight() * (1 - extraAmount)), getWidth() - marginX, marginY + (int)(graphHeight() * (1 - extraAmount)));
        //g.drawLine(marginX, marginY + (int)(graphHeight() * extraAmount), getWidth() - marginX, marginY + (int)(graphHeight() * extraAmount));
        
        g.drawLine(outerLeft(), outerBottom(), outerLeft(), outerTop());
    
        for (int i = 0; i <= gridY + 1; i++) {
            double alpha = (double)i / (gridY + 1);
            int val = (int)Math.round(innerBottom() * (1 - alpha) + innerTop() * alpha);
            g.drawLine(outerLeft(), val, outerRight(), val);
        }
        
        //g.drawLine(marginX + (int)(graphWidth() * extraAmount), getHeight() - marginY, marginX + (int)(graphWidth() * extraAmount), marginY);
        //g.drawLine(marginX + (int)(graphWidth() * (1 - extraAmount)), getHeight() - marginY, marginX + (int)(graphWidth() * (1 - extraAmount)), marginY);
        
    }
    
    private void drawValues(Graphics g) {
        g.setColor(getForeground());
        //g.drawString(BigDecimal.valueOf(lowerX()).setScale(precision, RoundingMode.HALF_UP).toString(), marginX + (int)(graphWidth() * extraAmount), getHeight() - marginY / 2);
        //g.drawString(BigDecimal.valueOf(upperX()).setScale(precision, RoundingMode.HALF_UP).toString(), marginX + (int)(graphWidth() * (1 - extraAmount)), getHeight() - marginY / 2);
        //g.drawString(BigDecimal.valueOf(lowerY()).setScale(precision, RoundingMode.HALF_UP).toString(), marginX / 4, marginY + (int)(graphHeight() * (1 - extraAmount)));
        //g.drawString(BigDecimal.valueOf(upperY()).setScale(precision, RoundingMode.HALF_UP).toString(), marginX / 4, marginY + (int)(graphHeight() * extraAmount));
    
        for (int i = 0; i <= gridX + 1; i++) {
            double alpha = (double)i / (gridX + 1);
            int pos = (int)Math.round(innerLeft() * (1 - alpha) + innerRight() * alpha);
            double val = lowerX() * (1 - alpha) + upperX() * alpha;
            drawDoubleBottom(g, val, pos, outerBottom());
        }
    
        for (int i = 0; i <= gridY + 1; i++) {
            double alpha = (double)i / (gridY + 1);
            int pos = (int)Math.round(innerBottom() * (1 - alpha) + innerTop() * alpha);
            double val = lowerY() * (1 - alpha) + upperY() * alpha;
            drawDoubleLeft(g, val, outerLeft(), pos);
        }
        
    }
    
    private void drawDouble(Graphics g, double val, int x, int y) {
        String str = GraphUtils.roundDouble(val, precision, maxNumberLength, true);
        g.drawString(str, x - g.getFontMetrics().stringWidth(str) / 2, y + g.getFontMetrics().getAscent() / 2);
    }
    
    private void drawDoubleLeft(Graphics g, double val, int x, int y) {
        String str = GraphUtils.roundDouble(val, precision, maxNumberLength, true);
        g.drawString(str, x - g.getFontMetrics().stringWidth(str) - valueDrawOffsetX, y + g.getFontMetrics().getAscent() / 2);
    }
    
    private void drawDoubleBottom(Graphics g, double val, int x, int y) {
        String str = GraphUtils.roundDouble(val, precision, maxNumberLength, true);
        g.drawString(str, x - g.getFontMetrics().stringWidth(str) / 2, y + g.getFontMetrics().getAscent() + valueDrawOffsetY);
    }
    
    private int graphWidth() {
        return getWidth() - 2 * marginX;
    }
    
    private int graphHeight() {
        return getHeight() - 2 * marginY;
    }
    
    private double lowerX() {
        return lowerX;
    }
    
    private double upperX() {
        return upperX;
    }
    
    private double lowerY() {
        return lowerY;
    }
    
    private double upperY() {
        return upperY;
    }
    
    public List<Interpolator> getInterpolators() {
        return interpolators;
    }
    
    public void setInterpolators(List<Interpolator> interpolators) {
        this.interpolators = interpolators;
    }
    
    public List<Interpolator> getInterpolatorsHighlighted() {
        return interpolatorsHighlighted;
    }
    
    public void setInterpolatorsHighlighted(List<Interpolator> interpolatorsHighlighted) {
        this.interpolatorsHighlighted = interpolatorsHighlighted;
    }
    
    public List<PointDouble> getPoints() {
        return points;
    }
    
    public void setPoints(List<PointDouble> points) {
        this.points = points;
    }
    
    public List<PointDouble> getPointsHighlighted() {
        return pointsHighlighted;
    }
    
    public void setPointsHighlighted(List<PointDouble> pointsHighlighted) {
        this.pointsHighlighted = pointsHighlighted;
    }
    
    private void calculateBounds() {
        if (interpolators == null) {
            interpolators = new ArrayList<>();
        }
        if (interpolatorsHighlighted == null) {
            interpolatorsHighlighted = new ArrayList<>();
        }
    
        List<Interpolator> all = new ArrayList<>(interpolators);
        all.addAll(interpolatorsHighlighted);
    
        if (points == null) {
            points = new ArrayList<>();
        }
        if (pointsHighlighted == null) {
            pointsHighlighted = new ArrayList<>();
        }
    
        List<PointDouble> allP = new ArrayList<>(points);
        allP.addAll(pointsHighlighted);
        
        if (autoModeX) {
            double lowerX = all.stream().map(Interpolator::lower).min(Comparator.naturalOrder()).orElse(+Double.MAX_VALUE);
            double upperX = all.stream().map(Interpolator::upper).max(Comparator.naturalOrder()).orElse(-Double.MAX_VALUE);
    
            double lowerXp = allP.stream().map(PointDouble::getX).min(Comparator.naturalOrder()).orElse(+Double.MAX_VALUE);
            double upperXp = allP.stream().map(PointDouble::getX).max(Comparator.naturalOrder()).orElse(-Double.MAX_VALUE);
    
            this.lowerX = Math.min(lowerX, lowerXp);
            this.upperX = Math.max(upperX, upperXp);
        }
        else {
            this.lowerX = minX;
            this.upperX = maxX;
        }
    
        if (autoModeY) {
            double lowerY = all.stream().map(Interpolator::lowerVal).min(Comparator.naturalOrder()).orElse(+Double.MAX_VALUE);
            double upperY = all.stream().map(Interpolator::upperVal).max(Comparator.naturalOrder()).orElse(-Double.MAX_VALUE);
            
            double lowerYp = allP.stream().map(PointDouble::getY).min(Comparator.naturalOrder()).orElse(+Double.MAX_VALUE);
            double upperYp = allP.stream().map(PointDouble::getY).max(Comparator.naturalOrder()).orElse(-Double.MAX_VALUE);
    
            this.lowerY = Math.min(lowerY, lowerYp);
            this.upperY = Math.max(upperY, upperYp);
        }
        else {
            this.lowerY = minY;
            this.upperY = maxY;
        }
        
        if (boundRoundingXEnabled) {
            this.lowerX = Math.floor(this.lowerX / boundRoundingX) * boundRoundingX;
            this.upperX = Math.ceil(this.upperX / boundRoundingX) * boundRoundingX;
        }
    
        if (boundRoundingYEnabled) {
            this.lowerY = Math.floor(this.lowerY / boundRoundingY) * boundRoundingY;
            this.upperY = Math.ceil(this.upperY / boundRoundingY) * boundRoundingY;
        }
        
    }
    
    private PointDouble valueToGraph(PointDouble point) {
        double valX = (point.getX() - lowerX()) / (upperX() - lowerX());
        double valY = (point.getY() - lowerY()) / (upperY() - lowerY());
        return new PointDouble(marginX + (int)((graphWidth() * extraAmount) * (1 - valX) + (graphWidth() * (1 - extraAmount)) * valX), getHeight() - marginY - (int)((graphHeight() * extraAmount) * (1 - valY) + (graphHeight() * (1 - extraAmount)) * valY));
    }
    
    private PointDouble graphToValue(PointDouble point) {
        double valX = (point.getX() - (marginX + (graphWidth() * extraAmount))) / ((marginX + (graphWidth() * (1 - extraAmount))) - (marginX + (graphWidth() * extraAmount)));
        double valY = (point.getY() - (marginY + (graphHeight() * (1 - extraAmount)))) / ((marginY + (graphHeight() * extraAmount)) - (marginY + (graphHeight() * (1 - extraAmount))));
        return new PointDouble(lowerX() * (1 - valX) + upperX() * valX, lowerY() * (1 - valY) + upperY() * valY);
    }
    
    public double getMinX() {
        return minX;
    }
    
    public void setMinX(double minX) {
        this.minX = minX;
    }
    
    public double getMaxX() {
        return maxX;
    }
    
    public void setMaxX(double maxX) {
        this.maxX = maxX;
    }
    
    public double getMinY() {
        return minY;
    }
    
    public void setMinY(double minY) {
        this.minY = minY;
    }
    
    public double getMaxY() {
        return maxY;
    }
    
    public void setMaxY(double maxY) {
        this.maxY = maxY;
    }
    
    public int getMarginX() {
        return marginX;
    }
    
    public void setMarginX(int marginX) {
        this.marginX = marginX;
    }
    
    public int getMarginY() {
        return marginY;
    }
    
    public void setMarginY(int marginY) {
        this.marginY = marginY;
    }
    
    public double getExtraAmount() {
        return extraAmount;
    }
    
    public void setExtraAmount(double extraAmount) {
        this.extraAmount = extraAmount;
    }
    
    public Color[] getGraphColors() {
        return graphColors;
    }
    
    public void setGraphColors(Color[] graphColors) {
        this.graphColors = graphColors;
    }
    
    public Color[] getGraphHighlightColors() {
        return graphHighlightColors;
    }
    
    public void setGraphHighlightColors(Color[] graphHighlightColors) {
        this.graphHighlightColors = graphHighlightColors;
    }
    
    public Color[] getPointColors() {
        return pointColors;
    }
    
    public void setPointColors(Color[] pointColors) {
        this.pointColors = pointColors;
    }
    
    public Color[] getPointHighlightColors() {
        return pointHighlightColors;
    }
    
    public void setPointHighlightColors(Color[] pointHighlightColors) {
        this.pointHighlightColors = pointHighlightColors;
    }
    
    public int getPointSize() {
        return pointSize;
    }
    
    public void setPointSize(int pointSize) {
        this.pointSize = pointSize;
    }
    
    public int getPrecision() {
        return precision;
    }
    
    public void setPrecision(int precision) {
        this.precision = precision;
    }
    
    public int getGridX() {
        return gridX;
    }
    
    public void setGridX(int gridX) {
        this.gridX = Math.max(0, gridX);
    }
    
    public int getGridY() {
        return gridY;
    }
    
    public void setGridY(int gridY) {
        this.gridY = Math.max(0, gridY);
    }
    
    public int getValueDrawOffsetX() {
        return valueDrawOffsetX;
    }
    
    public void setValueDrawOffsetX(int valueDrawOffsetX) {
        this.valueDrawOffsetX = valueDrawOffsetX;
    }
    
    public int getValueDrawOffsetY() {
        return valueDrawOffsetY;
    }
    
    public void setValueDrawOffsetY(int valueDrawOffsetY) {
        this.valueDrawOffsetY = valueDrawOffsetY;
    }
    
    public boolean isAutoModeX() {
        return autoModeX;
    }
    
    public void setAutoModeX(boolean autoModeX) {
        this.autoModeX = autoModeX;
    }
    
    public boolean isAutoModeY() {
        return autoModeY;
    }
    
    public void setAutoModeY(boolean autoModeY) {
        this.autoModeY = autoModeY;
    }
    
    public boolean isExtrapolationEnabled() {
        return extrapolationEnabled;
    }
    
    public void setExtrapolationEnabled(boolean extrapolationEnabled) {
        this.extrapolationEnabled = extrapolationEnabled;
    }
    
    public double getBoundRoundingX() {
        return boundRoundingX;
    }
    
    public void setBoundRoundingX(double boundRoundingX) {
        this.boundRoundingX = boundRoundingX;
    }
    
    public double getBoundRoundingY() {
        return boundRoundingY;
    }
    
    public void setBoundRoundingY(double boundRoundingY) {
        this.boundRoundingY = boundRoundingY;
    }
    
    public int getMaxNumberLength() {
        return maxNumberLength;
    }
    
    public void setMaxNumberLength(int maxNumberLength) {
        this.maxNumberLength = maxNumberLength;
    }
    
    public boolean isBoundRoundingXEnabled() {
        return boundRoundingXEnabled;
    }
    
    public void setBoundRoundingXEnabled(boolean boundRoundingXEnabled) {
        this.boundRoundingXEnabled = boundRoundingXEnabled;
    }
    
    public boolean isBoundRoundingYEnabled() {
        return boundRoundingYEnabled;
    }
    
    public void setBoundRoundingYEnabled(boolean boundRoundingYEnabled) {
        this.boundRoundingYEnabled = boundRoundingYEnabled;
    }
}
