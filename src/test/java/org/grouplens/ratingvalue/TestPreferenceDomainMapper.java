package org.grouplens.ratingvalue;

import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.ratingvalue.PreferenceDomainMapper;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestPreferenceDomainMapper {
    @Test
    public void testUniformMapping() {
        PreferenceDomain in = new PreferenceDomain(1.0, 5.0, 0.5);
        PreferenceDomain out = new PreferenceDomain(1.0, 3.0, 1.0);
        PreferenceDomainMapper mapper = new PreferenceDomainMapper(in, out);
        assertEquals(mapper.getCount(), 3);
        assertEquals(mapper.map(1.0), 1.0, 10e-6);
        assertEquals(mapper.map(1.5), 1.0, 10e-6);
        assertEquals(mapper.map(2.0), 1.0, 10e-6);
        assertEquals(mapper.map(2.5), 2.0, 10e-6);
        assertEquals(mapper.map(3.0), 2.0, 10e-6);
        assertEquals(mapper.map(3.5), 2.0, 10e-6);
        assertEquals(mapper.map(4.0), 3.0, 10e-6);
        assertEquals(mapper.map(4.5), 3.0, 10e-6);
        assertEquals(mapper.map(5.0), 3.0, 10e-6);
    }

    @Test
    public void testUniformMapping2() {
        PreferenceDomain in = new PreferenceDomain(1.0, 5.0, 0.5);
        PreferenceDomain out = new PreferenceDomain(1.0, 3.0, 0.5);
        PreferenceDomainMapper mapper = new PreferenceDomainMapper(in, out);
        assertEquals(mapper.getCount(), 5);
        assertEquals(mapper.map(1.0), 1.0, 10e-6);
        assertEquals(mapper.map(1.5), 1.0, 10e-6);
        assertEquals(mapper.map(2.0), 1.5, 10e-6);
        assertEquals(mapper.map(2.5), 1.5, 10e-6);
        assertEquals(mapper.map(3.0), 2.0, 10e-6);
        assertEquals(mapper.map(3.5), 2.0, 10e-6);
        assertEquals(mapper.map(4.0), 2.5, 10e-6);
        assertEquals(mapper.map(4.5), 2.5, 10e-6);
        assertEquals(mapper.map(5.0), 3.0, 10e-6);
    }


    @Test
    public void testCustomMapping() {
        PreferenceDomain in = new PreferenceDomain(1.0, 5.0, 0.5);
        PreferenceDomain out = new PreferenceDomain(1.0, 3.0, 0.5);
        PreferenceDomainMapper mapper = new PreferenceDomainMapper(in, out, new int[] {0, 1, 1, 2, 2, 3, 4, 4, 4});
        assertEquals(mapper.getCount(), 5);
        assertEquals(mapper.map(1.0), 1.0, 10e-6);
        assertEquals(mapper.map(1.5), 1.5, 10e-6);
        assertEquals(mapper.map(2.0), 1.5, 10e-6);
        assertEquals(mapper.map(2.5), 2.0, 10e-6);
        assertEquals(mapper.map(3.0), 2.0, 10e-6);
        assertEquals(mapper.map(3.5), 2.5, 10e-6);
        assertEquals(mapper.map(4.0), 3.0, 10e-6);
        assertEquals(mapper.map(4.5), 3.0, 10e-6);
        assertEquals(mapper.map(5.0), 3.0, 10e-6);
    }


    @Test
    public void testNonDiscreteMapping() {
        PreferenceDomain in = new PreferenceDomain(1.0, 5.0, 0.5);
        PreferenceDomain out = new PreferenceDomain(1.0, 3.0, 0.5);
        PreferenceDomainMapper mapper = new PreferenceDomainMapper(in, out);
        assertEquals(mapper.getCount(), 5);
        assertEquals(mapper.map(1.0), 1.0, 10e-6);
        assertEquals(mapper.map(1.1), 1.0, 10e-6);
        assertEquals(mapper.map(3.0), 2.0, 10e-6);
        assertEquals(mapper.map(3.1), 2.0, 10e-6);
        assertEquals(mapper.map(2.99), 2.0, 10e-6);
    }
}
