package com.labinvent.analyzer.unit;

import com.labinvent.analyzer.service.progress.ProgressState;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProgressStateTest {

    @Test
    void testProgressAndCancel() {
        ProgressState state = new ProgressState();
        state.setProgress(42);
        assertEquals(42, state.getProgress());

        assertFalse(state.isCancelled());
        state.cancel();
        assertTrue(state.isCancelled());
    }
}
