import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Checks every algorithm against schedules worked out by hand.
 *
 * Sample data (same as the app's default input):
 *   P1 arrives 0, burst 5, priority 2
 *   P2 arrives 1, burst 3, priority 1
 *   P3 arrives 2, burst 8, priority 3
 *   P4 arrives 3, burst 6, priority 2
 */
class SchedulerTest {

    private static final double DELTA = 1e-9;

    private List<ProcessJob> sample;

    @BeforeEach
    void setUp() {
        sample = List.of(
                new ProcessJob(1, 0, 5, 2),
                new ProcessJob(2, 1, 3, 1),
                new ProcessJob(3, 2, 8, 3),
                new ProcessJob(4, 3, 6, 2));
    }

    // ---------- FCFS ----------
    // P1 0-5, P2 5-8, P3 8-16, P4 16-22

    @Test
    void fcfsRunsInArrivalOrder() {
        SchedulerResult r = new FcfsScheduler().schedule(sample);

        assertSlices(r, "P1[0 -> 5]", "P2[5 -> 8]", "P3[8 -> 16]", "P4[16 -> 22]");
        assertEquals(Map.of(1, 0, 2, 4, 3, 6, 4, 13), r.getWaitingTimes());
        assertEquals(Map.of(1, 5, 2, 7, 3, 14, 4, 19), r.getTurnaroundTimes());
        assertEquals(5.75, r.getAvgWaiting(), DELTA);
        assertEquals(11.25, r.getAvgTurnaround(), DELTA);
    }

    // ---------- SJF ----------
    // Only P1 is ready at 0, so it runs 0-5. At 5, P2 (3) < P4 (6) < P3 (8).
    // P1 0-5, P2 5-8, P4 8-14, P3 14-22

    @Test
    void sjfPicksShortestReadyJob() {
        SchedulerResult r = new SjfScheduler().schedule(sample);

        assertSlices(r, "P1[0 -> 5]", "P2[5 -> 8]", "P4[8 -> 14]", "P3[14 -> 22]");
        assertEquals(Map.of(1, 0, 2, 4, 3, 12, 4, 5), r.getWaitingTimes());
        assertEquals(5.25, r.getAvgWaiting(), DELTA);
        assertEquals(10.75, r.getAvgTurnaround(), DELTA);
    }

    // ---------- Priority ----------

    @Test
    void priorityPicksLowestPriorityValue() {
        // At 5: P2 (prio 1) < P4 (prio 2) < P3 (prio 3)
        SchedulerResult r = new PriorityScheduler().schedule(sample);

        assertSlices(r, "P1[0 -> 5]", "P2[5 -> 8]", "P4[8 -> 14]", "P3[14 -> 22]");
        assertEquals(5.25, r.getAvgWaiting(), DELTA);
    }

    @Test
    void sjfAndPriorityDisagreeWhenShortJobHasLowPriority() {
        // Both arrive at 0. P2 is shorter, but P1 has higher priority.
        List<ProcessJob> jobs = List.of(
                new ProcessJob(1, 0, 4, 1),
                new ProcessJob(2, 0, 1, 2));

        assertSlices(new SjfScheduler().schedule(jobs), "P2[0 -> 1]", "P1[1 -> 5]");
        assertSlices(new PriorityScheduler().schedule(jobs), "P1[0 -> 4]", "P2[4 -> 5]");
    }

    // ---------- Round Robin ----------
    // q = 2. New arrivals join the queue before the preempted job.
    // P1 0-2, P2 2-4, P3 4-6, P1 6-8, P4 8-10, P2 10-11 (done),
    // P3 11-13, P1 13-14 (done), P4 14-16, P3 16-18, P4 18-20 (done), P3 20-22 (done)

    @Test
    void roundRobinRotatesByQuantum() {
        SchedulerResult r = new RoundRobinScheduler(2).schedule(sample);

        assertSlices(r,
                "P1[0 -> 2]", "P2[2 -> 4]", "P3[4 -> 6]", "P1[6 -> 8]",
                "P4[8 -> 10]", "P2[10 -> 11]", "P3[11 -> 13]", "P1[13 -> 14]",
                "P4[14 -> 16]", "P3[16 -> 18]", "P4[18 -> 20]", "P3[20 -> 22]");
        assertEquals(Map.of(1, 14, 2, 10, 3, 20, 4, 17), r.getTurnaroundTimes());
        assertEquals(Map.of(1, 9, 2, 7, 3, 12, 4, 11), r.getWaitingTimes());
        assertEquals(9.75, r.getAvgWaiting(), DELTA);
        assertEquals(15.25, r.getAvgTurnaround(), DELTA);
    }

    @Test
    void roundRobinRejectsNonPositiveQuantum() {
        assertThrows(IllegalArgumentException.class, () -> new RoundRobinScheduler(0));
        assertThrows(IllegalArgumentException.class, () -> new RoundRobinScheduler(-1));
    }

    // ---------- Behavior shared by all algorithms ----------

    private List<Scheduler> allSchedulers() {
        return List.of(new FcfsScheduler(), new SjfScheduler(),
                new PriorityScheduler(), new RoundRobinScheduler(2));
    }

    @Test
    void idleGapJumpsToNextArrival() {
        // CPU is idle from 2 to 10, so P2 starts at its arrival with no waiting.
        List<ProcessJob> jobs = List.of(
                new ProcessJob(1, 0, 2, 1),
                new ProcessJob(2, 10, 1, 1));

        for (Scheduler s : allSchedulers()) {
            SchedulerResult r = s.schedule(jobs);
            assertSlices(r, "P1[0 -> 2]", "P2[10 -> 11]");
            assertEquals(0, r.getWaitingTimes().get(2), s.getName());
        }
    }

    @Test
    void firstArrivalAfterZeroStartsAtItsArrival() {
        // Burst of 2 fits in one Round Robin quantum, so every algorithm gives one slice.
        List<ProcessJob> jobs = List.of(new ProcessJob(1, 4, 2, 1));
        for (Scheduler s : allSchedulers()) {
            assertSlices(s.schedule(jobs), "P1[4 -> 6]");
        }
    }

    @Test
    void emptyInputGivesEmptyResult() {
        for (Scheduler s : allSchedulers()) {
            SchedulerResult r = s.schedule(List.of());
            assertTrue(r.getSlices().isEmpty(), s.getName());
            assertEquals(0, r.getAvgWaiting(), DELTA);
        }
    }

    @Test
    void inputListIsNotModified() {
        List<ProcessJob> jobs = new ArrayList<>(List.of(
                new ProcessJob(3, 5, 1, 1),
                new ProcessJob(1, 0, 2, 1),
                new ProcessJob(2, 1, 4, 1)));
        List<ProcessJob> before = new ArrayList<>(jobs);

        for (Scheduler s : allSchedulers()) {
            s.schedule(jobs);
            assertEquals(before, jobs, s.getName() + " modified its input");
        }
    }

    @Test
    void sameInputAlwaysGivesSameSchedule() {
        // Ties on arrival and burst are broken by PID, so results are repeatable.
        List<ProcessJob> jobs = List.of(
                new ProcessJob(2, 0, 3, 1),
                new ProcessJob(1, 0, 3, 1));

        for (Scheduler s : allSchedulers()) {
            String first = s.schedule(jobs).getSlices().toString();
            String second = s.schedule(jobs).getSlices().toString();
            assertEquals(first, second, s.getName());
            assertTrue(first.startsWith("[P1"), s.getName() + " should break the tie by PID");
        }
    }

    @Test
    void resultCannotBeModifiedAfterCreation() {
        SchedulerResult r = new FcfsScheduler().schedule(sample);
        assertThrows(UnsupportedOperationException.class, () -> r.getWaitingTimes().put(1, 999));
        assertThrows(UnsupportedOperationException.class, () -> r.getSlices().clear());
    }

    @Test
    void perProcessResultsAreInPidOrder() {
        List<ProcessJob> jobs = List.of(
                new ProcessJob(30, 0, 1, 1),
                new ProcessJob(4, 0, 1, 1),
                new ProcessJob(17, 0, 1, 1));
        SchedulerResult r = new FcfsScheduler().schedule(jobs);
        assertEquals(List.of(4, 17, 30), new ArrayList<>(r.getWaitingTimes().keySet()));
    }

    // ---------- Helpers ----------

    private static void assertSlices(SchedulerResult result, String... expected) {
        List<String> actual = result.getSlices().stream().map(ScheduledSlice::toString).toList();
        assertEquals(List.of(expected), actual, result.getAlgorithmName());
    }
}
