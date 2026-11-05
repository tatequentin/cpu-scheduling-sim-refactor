import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared helpers used by every scheduler.
 */
final class ScheduleMetrics {

    /** Earliest arrival first; ties broken by PID so results are always repeatable. */
    static final Comparator<ProcessJob> BY_ARRIVAL =
            Comparator.comparingInt(ProcessJob::getArrivalTime)
                    .thenComparingInt(ProcessJob::getPid);

    private ScheduleMetrics() {
        // static helpers only
    }

    /** Returns a sorted copy so the caller's list is never modified. */
    static List<ProcessJob> sortedByArrival(List<ProcessJob> jobs) {
        List<ProcessJob> copy = new ArrayList<>(jobs);
        copy.sort(BY_ARRIVAL);
        return copy;
    }

    /**
     * Computes waiting and turnaround times from each job's finish time.
     *
     * Turnaround = finish - arrival. Waiting = turnaround - burst.
     * A missing finish time or a negative waiting time can only mean a bug in
     * the algorithm, so this throws instead of hiding it behind a default value.
     */
    static SchedulerResult buildResult(
            String name,
            List<ProcessJob> jobs,
            List<ScheduledSlice> slices,
            Map<Integer, Integer> finishTimes) {

        Map<Integer, Integer> waitingTimes = new HashMap<>();
        Map<Integer, Integer> turnaroundTimes = new HashMap<>();
        double sumWaiting = 0;
        double sumTurnaround = 0;

        for (ProcessJob job : jobs) {
            int pid = job.getPid();
            Integer finish = finishTimes.get(pid);
            if (finish == null) {
                throw new IllegalStateException(name + ": P" + pid + " never finished");
            }

            int turnaround = finish - job.getArrivalTime();
            int waiting = turnaround - job.getBurstTime();
            if (waiting < 0) {
                throw new IllegalStateException(
                        name + ": P" + pid + " has negative waiting time " + waiting);
            }

            waitingTimes.put(pid, waiting);
            turnaroundTimes.put(pid, turnaround);
            sumWaiting += waiting;
            sumTurnaround += turnaround;
        }

        double avgWaiting = jobs.isEmpty() ? 0 : sumWaiting / jobs.size();
        double avgTurnaround = jobs.isEmpty() ? 0 : sumTurnaround / jobs.size();

        return new SchedulerResult(name, slices, waitingTimes, turnaroundTimes,
                avgWaiting, avgTurnaround);
    }
}
