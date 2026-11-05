import java.util.List;

/**
 * Common contract for every scheduling algorithm.
 *
 * The UI only depends on this interface, so adding an algorithm means writing
 * one new class and adding it to the list in CpuSchedulingApp. Nothing else changes.
 */
public interface Scheduler {

    /** Display name shown in the UI and in the metrics output. */
    String getName();

    /**
     * Schedules the given processes. Implementations must not modify the input list.
     */
    SchedulerResult schedule(List<ProcessJob> jobs);
}
