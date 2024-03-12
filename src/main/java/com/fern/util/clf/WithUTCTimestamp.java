package com.fern.util.clf;

import java.util.Comparator;

/**
 * Implementors contain an accessor method for a UTC Epoch.
 */
public interface WithUTCTimestamp {

    /**
     * Comparator to sort lists of implementors, smaller to greater
     */
    Comparator<WithUTCTimestamp> COMPARING =
            Comparator.comparingLong(WithUTCTimestamp::getUTCTimestamp);

    /**
     * @return a UTC Epoch
     */
    long getUTCTimestamp();
}
