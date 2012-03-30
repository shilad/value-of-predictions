package org.grouplens.ratingvalue;

import org.grouplens.lenskit.data.pref.PreferenceDomain;

/**
 */
public class Utils {
    public static double binRating(PreferenceDomain domain, double rating) {
        double multiplier = 1.0 / domain.getPrecision();
        if (Math.abs(multiplier - ((int)multiplier)) > 0.0001) {
            throw new IllegalArgumentException("some multiple of precision must equal 1.0");
        }
        double rounded = Math.round(multiplier * rating) / multiplier;
        double pinched = Math.max(Math.min(rounded, domain.getMaximum()), domain.getMinimum());
        return pinched;
    }
}
