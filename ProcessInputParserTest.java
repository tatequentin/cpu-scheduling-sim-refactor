import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProcessInputParserTest {

    @Test
    void parsesValidLinesAndSkipsBlankOnes() {
        List<ProcessJob> jobs = ProcessInputParser.parse("1,0,5,2\n\n 2 , 1 , 3 , 1 \r\n");

        assertEquals(2, jobs.size());
        assertEquals(2, jobs.get(1).getPid());
        assertEquals(3, jobs.get(1).getBurstTime());
    }

    @Test
    void wrongNumberOfFieldsReportsLineNumber() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> ProcessInputParser.parse("1,0,5,2\n2,1,3"));
        assertTrue(e.getMessage().startsWith("Line 2"), e.getMessage());
    }

    @Test
    void nonIntegerValueReportsLineNumber() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> ProcessInputParser.parse("1,0,five,2"));
        assertTrue(e.getMessage().startsWith("Line 1"), e.getMessage());
    }

    @Test
    void negativeArrivalIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> ProcessInputParser.parse("1,-3,5,2"));
        assertTrue(e.getMessage().contains("Arrival time"), e.getMessage());
    }

    @Test
    void zeroBurstIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> ProcessInputParser.parse("1,0,0,2"));
        assertTrue(e.getMessage().contains("Burst time"), e.getMessage());
    }

    @Test
    void duplicatePidIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> ProcessInputParser.parse("1,0,5,2\n2,1,3,1\n1,4,2,1"));
        assertEquals("Line 3 reuses PID 1", e.getMessage());
    }

    @Test
    void emptyInputGivesEmptyList() {
        assertTrue(ProcessInputParser.parse("   \n\n").isEmpty());
    }
}
