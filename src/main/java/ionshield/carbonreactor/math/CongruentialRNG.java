package ionshield.carbonreactor.math;

import java.math.BigInteger;

public class CongruentialRNG implements RNG<Double> {
    private BigInteger next;
    private BigInteger m;
    private BigInteger p;
    
    public CongruentialRNG(long seed, long multiplier, long modulus) {
        this.next = BigInteger.valueOf(seed % modulus);
        this.p = BigInteger.valueOf(multiplier);
        this.m = BigInteger.valueOf(modulus);
        getNext();
    }
    
    public BigInteger getNext() {
        BigInteger curr = next;
        next = p.multiply(curr).mod(m);
        return curr;
    }
    
    @Override
    public Double getInRange(Double start, Double end) {
        return (getNext().doubleValue() / m.doubleValue()) * (end - start) + start;
    }
}
