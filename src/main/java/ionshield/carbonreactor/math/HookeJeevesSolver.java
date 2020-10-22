package ionshield.carbonreactor.math;

public class HookeJeevesSolver extends Solver {
    private double alpha = 0.5;
    private double lambda = 1;

    @Override
    protected PointDouble solveInternal(PointDouble... data) {
        if (data.length < 1) {
            throw new IllegalArgumentException("Not enough parameters passed to solver (required 2)");
        }

        PointDouble startPoint = data[0];
        PointDouble startAxis;
        if (data.length >= 2) {
            startAxis = data[1];
        }
        else {
            startAxis = new PointDouble(1, 1);
        }

        double currX1 = startPoint.getX();
        double currX2 = startPoint.getY();

        double axisX1 = startAxis.getX();
        double axisX2 = startAxis.getY();

        int dirX1;
        int dirX2;


        for (;;) {

            double baseX1 = currX1;
            double baseX2 = currX2;

            StepResult step = coordinateStep(currX1, currX2, axisX1, axisX2);
            if (step.fin) return new PointDouble(step.resX1, step.resX2);
            axisX1 = step.axisX1;
            axisX2 = step.axisX2;

            double nextX1 = currX1;
            double nextX2 = currX2;

            double diffX1 = step.resX1 - baseX1;
            double diffX2 = step.resX2 - baseX2;

            do {
                addLine(new PointDouble(currX1, currX2), new PointDouble(nextX1, nextX2));

                currX1 = nextX1;
                currX2 = nextX2;

                nextX1 = currX1 + lambda * diffX1;
                nextX2 = currX2 + lambda * diffX2;

                if (new PointDouble(currX1, currX2).add(new PointDouble(-nextX1, -nextX2)).length() < EPSILON)
                    return new PointDouble(currX1, currX2);

                addPoint(new PointDouble(nextX1, nextX2));

            } while (getF().apply(nextX1, nextX2) < getF().apply(currX1, currX2));
        }
        //return new PointDouble(currX1, currX2);
    }

    public StepResult coordinateStep(double startX1, double startX2, double axisX1, double axisX2) {
        double currX1 = startX1;
        double currX2 = startX2;

        int dirX1;
        int dirX2;

        double value = getF().apply(currX1, currX2);

        while (true) {
            dirX1 = 0;
            dirX2 = 0;

            double x1p = getF().apply(currX1 + axisX1, currX2);
            addPoint(new PointDouble(currX1 + axisX1, currX2));
            if (x1p < value) {
                addLine(new PointDouble(currX1, currX2), new PointDouble(currX1 + axisX1, currX2));
                currX1 += axisX1;
                dirX1 = 1;
                value = x1p;
            }
            else {
                double x1n = getF().apply(currX1 - axisX1, currX2);
                addPoint(new PointDouble(currX1 - axisX1, currX2));
                if (x1n < value) {
                    addLine(new PointDouble(currX1, currX2), new PointDouble(currX1 - axisX1, currX2));
                    currX1 -= axisX1;
                    dirX1 = -1;
                    value = x1n;
                }
            }
            double x2p = getF().apply(currX1, currX2 + axisX2);
            addPoint(new PointDouble(currX1, currX2 + axisX2));
            if (x2p < value) {
                addLine(new PointDouble(currX1, currX2), new PointDouble(currX1, currX2 + axisX2));
                currX2 += axisX2;
                dirX2 = 1;
                value = x2p;
            }
            else {
                double x2n = getF().apply(currX1, currX2 - axisX2);
                addPoint(new PointDouble(currX1, currX2 - axisX2));
                if (x2n < value) {
                    addLine(new PointDouble(currX1, currX2), new PointDouble(currX1, currX2 - axisX2));
                    currX2 -= axisX2;
                    dirX2 = 1;
                    value = x2n;
                }
            }

            if (dirX1 != 0 || dirX2 != 0) return new StepResult(currX1, currX2, dirX1, dirX2, axisX1, axisX2, false);
            axisX1 *= alpha;
            axisX2 *= alpha;
            if (axisX1 < EPSILON && axisX2 < EPSILON) {
                return new StepResult(currX1, currX2, dirX1, dirX2, axisX1, axisX2, true);
            }
        }
    }

    public static class StepResult {
        public double resX1;
        public double resX2;
        public int dirX1;
        public int dirX2;
        public double axisX1;
        public double axisX2;
        public boolean fin;

        public StepResult() {
        }

        public StepResult(double resX1, double resX2, double axisX1, double axisX2) {
            this.resX1 = resX1;
            this.resX2 = resX2;
            this.axisX1 = axisX1;
            this.axisX2 = axisX2;
        }

        public StepResult(double resX1, double resX2, int dirX1, int dirX2, double axisX1, double axisX2, boolean fin) {
            this.resX1 = resX1;
            this.resX2 = resX2;
            this.dirX1 = dirX1;
            this.dirX2 = dirX2;
            this.axisX1 = axisX1;
            this.axisX2 = axisX2;
            this.fin = fin;
        }
    }

    @Override
    protected int getLogBatchSize() {
        return 1;
    }

    public double getLambda() {
        return lambda;
    }

    public void setLambda(double lambda) {
        this.lambda = lambda;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        if (alpha >= 1) throw new IllegalArgumentException("Alpha must be less than one");
        this.alpha = alpha;
    }
}
