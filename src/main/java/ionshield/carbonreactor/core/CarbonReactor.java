package ionshield.carbonreactor.core;

public class CarbonReactor {
    private double r =  8.31;//J/mol⋅deg;

    private double a1 = 1600;
    private double a2 = 600;
    private double a3 = 10;

    private double e1 = 160000; //J / mol
    private double e2 = 127000; //J / mol
    private double e3 = 98000; //J / mol

    private double q1 = 22500; //J / mol
    private double q2 = 12150; //J / mol

    private double density = 1.5; //kg / m^3
    private double ct = 1190; //J / kg*deg
    private double volumeRate = 0.7; //m^3 / s

    private double cCH4;
    private double cC3H4;
    private double cO2;
    private double cC;
    private double cCO2;
    private double cCO;
    private double cH2O;
    private double cH2;

    private double tIn; //deg
    private double volume; //m^3

    private double q; //J
    private double time = 0; //s

    private double mCH4 = 0.01604;
    private double mC3H4 = 0.04006;
    private double mO2 = 0.032;
    private double mC = 0.01201;
    private double mCO2 = 0.04401;
    private double mCO = 0.02801;
    private double mH2O = 0.01802;
    private double mH2 = 0.00202;

    public void init(double cCH4In, double cC3H4In, double cO2In, double volume, double t) {
        this.volume = volume;
        cCH4 = cCH4In;
        cC3H4 = cC3H4In;
        cO2 = cO2In;
        cC = 0;
        cCO2 = 0;
        cCO = 0;
        cH2O = 0;
        cH2 = 0;
        q = t * volume * ct * density;
    }

    public void tick(double seconds, double cCH4In, double cC3H4In, double cO2In, double tIn) {
        double ts = volume / volumeRate;
        double mass = volume * density;
        double massRate = volumeRate * density;
        double heatCapacity = ct * mass;
        double t = q / heatCapacity;

        double k1 = a1 * Math.exp(-e1 / (r * t));
        double k2 = a2 * Math.exp(-e2 / (r * t));
        double k3 = a3 * Math.exp(-e3 / (r * t));
        double r1 = k1 * cCH4 * cO2;
        double r2 = k2 * cC3H4 * cO2;
        double r3 = k3 = cC3H4;

        double dcCH4 = (1 / ts) * (cCH4In - cCH4) - r1;
        double dcC3H4 = (1 / ts) * (cC3H4In - cC3H4) - 2 * r2 - r3;
        double dcO2 = (1 / ts) * (cO2In - cO2) - 2 * r1 - 5 * r2;

        double dcC = -(1 / ts) * cC + 3 * r3;
        double dcCO2 = -(1 / ts) * cCO2 + r1;
        double dcCO = -(1 / ts) * cCO + 6 * r2;
        double dcH2O = -(1 / ts) * cH2O + 2 * r1 + 4 * r2;
        double dcH2 = -(1 / ts) * cH2 + 2 * r3;

        double qIn = ct * massRate * tIn;
        double qOut = ct * massRate * t;
        double dq = qIn - qOut + q1 * r1 + q2 * r2;

        cCH4 += dcCH4 * seconds;
        cC3H4 += dcC3H4 * seconds;
        cO2 += dcO2 * seconds;
        cC += dcC * seconds;
        cCO2 += dcCO2 * seconds;
        cCO += dcCO * seconds;
        cH2O += dcH2O * seconds;
        cH2 += dcH2 * seconds;

        q += dq * seconds;

        time += seconds;
    }

    public double concentrationInMolesPerCubicMeter(double c, double molarMass) {
        return c * density / molarMass;
    }

    public double concentrationInFraction(double c, double molarMass) {
        return c * molarMass / density;
    }

    public double getR() {
        return r;
    }

    public void setR(double r) {
        this.r = r;
    }

    public double getA1() {
        return a1;
    }

    public void setA1(double a1) {
        this.a1 = a1;
    }

    public double getA2() {
        return a2;
    }

    public void setA2(double a2) {
        this.a2 = a2;
    }

    public double getA3() {
        return a3;
    }

    public void setA3(double a3) {
        this.a3 = a3;
    }

    public double getE1() {
        return e1;
    }

    public void setE1(double e1) {
        this.e1 = e1;
    }

    public double getE2() {
        return e2;
    }

    public void setE2(double e2) {
        this.e2 = e2;
    }

    public double getE3() {
        return e3;
    }

    public void setE3(double e3) {
        this.e3 = e3;
    }

    public double getQ1() {
        return q1;
    }

    public void setQ1(double q1) {
        this.q1 = q1;
    }

    public double getQ2() {
        return q2;
    }

    public void setQ2(double q2) {
        this.q2 = q2;
    }

    public double getDensity() {
        return density;
    }

    public void setDensity(double density) {
        this.density = density;
    }

    public double getCt() {
        return ct;
    }

    public void setCt(double ct) {
        this.ct = ct;
    }

    public double getVolumeRate() {
        return volumeRate;
    }

    public void setVolumeRate(double volumeRate) {
        this.volumeRate = volumeRate;
    }

    public double getcCH4() {
        return cCH4;
    }

    public void setcCH4(double cCH4) {
        this.cCH4 = cCH4;
    }

    public double getcC3H4() {
        return cC3H4;
    }

    public void setcC3H4(double cC3H4) {
        this.cC3H4 = cC3H4;
    }

    public double getcO2() {
        return cO2;
    }

    public void setcO2(double cO2) {
        this.cO2 = cO2;
    }

    public double getcC() {
        return cC;
    }

    public void setcC(double cC) {
        this.cC = cC;
    }

    public double getcCO2() {
        return cCO2;
    }

    public void setcCO2(double cCO2) {
        this.cCO2 = cCO2;
    }

    public double getcCO() {
        return cCO;
    }

    public void setcCO(double cCO) {
        this.cCO = cCO;
    }

    public double getcH2O() {
        return cH2O;
    }

    public void setcH2O(double cH2O) {
        this.cH2O = cH2O;
    }

    public double getcH2() {
        return cH2;
    }

    public void setcH2(double cH2) {
        this.cH2 = cH2;
    }

    public double gettIn() {
        return tIn;
    }

    public void settIn(double tIn) {
        this.tIn = tIn;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getQ() {
        return q;
    }

    public void setQ(double q) {
        this.q = q;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public double getmCH4() {
        return mCH4;
    }

    public void setmCH4(double mCH4) {
        this.mCH4 = mCH4;
    }

    public double getmC3H4() {
        return mC3H4;
    }

    public void setmC3H4(double mC3H4) {
        this.mC3H4 = mC3H4;
    }

    public double getmO2() {
        return mO2;
    }

    public void setmO2(double mO2) {
        this.mO2 = mO2;
    }

    public double getmC() {
        return mC;
    }

    public void setmC(double mC) {
        this.mC = mC;
    }

    public double getmCO2() {
        return mCO2;
    }

    public void setmCO2(double mCO2) {
        this.mCO2 = mCO2;
    }

    public double getmCO() {
        return mCO;
    }

    public void setmCO(double mCO) {
        this.mCO = mCO;
    }

    public double getmH2O() {
        return mH2O;
    }

    public void setmH2O(double mH2O) {
        this.mH2O = mH2O;
    }

    public double getmH2() {
        return mH2;
    }

    public void setmH2(double mH2) {
        this.mH2 = mH2;
    }
}
