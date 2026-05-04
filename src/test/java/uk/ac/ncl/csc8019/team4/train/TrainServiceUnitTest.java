package uk.ac.ncl.csc8019.team4.train;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class TrainServiceUnitTest {

    @Test
    void calculateDelayMinutesReturnsZeroForOnTime() throws Exception {
        assertThat(invokeCalculateDelayMinutes("10:00", "On time")).isZero();
    }

    @Test
    void calculateDelayMinutesReturnsZeroForCancelled() throws Exception {
        assertThat(invokeCalculateDelayMinutes("10:00", "Cancelled")).isZero();
    }

    @Test
    void calculateDelayMinutesReturnsPositiveDelay() throws Exception {
        assertThat(invokeCalculateDelayMinutes("10:00", "10:07")).isEqualTo(7);
    }

    @Test
    void calculateDelayMinutesReturnsZeroForInvalidTimeFormat() throws Exception {
        assertThat(invokeCalculateDelayMinutes("10:00", "Delayed")).isZero();
    }

    private int invokeCalculateDelayMinutes(String scheduled, String estimated) throws Exception {
        Method method = TrainService.class.getDeclaredMethod("calculateDelayMinutes", String.class, String.class);
        method.setAccessible(true);
        return (int) method.invoke(null, scheduled, estimated);
    }
}
