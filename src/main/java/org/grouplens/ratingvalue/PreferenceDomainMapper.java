package org.grouplens.ratingvalue;

import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.norm.PreferenceDomainQuantizer;
import org.grouplens.lenskit.norm.Quantizer;

public class PreferenceDomainMapper implements Quantizer {
    private final PreferenceDomain inDomain;
    private final PreferenceDomain outDomain;
    private final PreferenceDomainQuantizer inQuantizer;
    private final PreferenceDomainQuantizer outQuantizer;
    private final int[] mapping;


    public PreferenceDomainMapper(PreferenceDomain inDomain, PreferenceDomain outDomain) {
        this(inDomain, outDomain, null);
    }

    public PreferenceDomainMapper(PreferenceDomain inDomain, PreferenceDomain outDomain, int[] mapping) {
        this.inDomain = inDomain;
        this.outDomain = outDomain;
        this.inQuantizer = new PreferenceDomainQuantizer(inDomain);
        this.outQuantizer = new PreferenceDomainQuantizer(outDomain);
        this.mapping = (mapping == null) ? makeUniformMapping() : mapping;
        if (this.inQuantizer.getCount() < this.outQuantizer.getCount()) {
            throw new IllegalArgumentException("inDomain must have at least as many rating points as out domain");
        }
        if (this.mapping.length != this.inQuantizer.getCount()) {
            throw new IllegalArgumentException("in domain mapping must have same number of points as in domain");
        }
    }

    /**
     * Creates a mapping from input rating indexes to output rating indexes.
     *
     * From [1,2,3,4,5] -> [1,2], mapping is [0,0,0,1,1]
     *
     * @return mapping
     */
    private int [] makeUniformMapping() {
        int nIn = inQuantizer.getCount();
        int nOut = outQuantizer.getCount();
        int mapping[] = new int[nIn];
        for (int i = 0; i < nIn; i++) {
            mapping[i] = (int) (1.0 * i / nIn * nOut + 10e-6);
            assert(0 <= mapping[i] && mapping[i] < outQuantizer.getCount());
        }
        return mapping;
    }

    @Override
    public double[] getValues() {
        return outQuantizer.getValues();
    }

    @Override
    public double getValue(int i) {
        return outQuantizer.getValue(i);
    }

    @Override
    public int getCount() {
        return outQuantizer.getCount();
    }

    @Override
    public int apply(double val) {
        int index = inQuantizer.apply(val);
        return mapping[index];
    }

    public double map(double val) {
        return getValue(apply(val));
    }

    public int [] getMapping() {
        return this.mapping;
    }

    public PreferenceDomain getInDomain() {
        return inDomain;
    }

    public PreferenceDomain getOutDomain() {
        return outDomain;
    }
}
