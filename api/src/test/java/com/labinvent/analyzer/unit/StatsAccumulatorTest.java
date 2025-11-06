package com.labinvent.analyzer.unit;

import com.labinvent.analyzer.util.StatsAccumulator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StatsAccumulatorTest {

    @Test
    void testAddValidValues() {
        StatsAccumulator acc = new StatsAccumulator();
        acc.addValid(1.0);
        acc.addValid(3.0);

        assertEquals(2, acc.getCount());
        assertEquals(1.0, acc.getMin());
        assertEquals(3.0, acc.getMax());
        assertEquals(2.0, acc.getMean());
        assertEquals(2, acc.getUniqueCount());
    }

    @Test
    void testAddInvalid() {
        StatsAccumulator acc = new StatsAccumulator();
        acc.addInvalid();
        assertEquals(1, acc.getInvalidCount());
    }
}
