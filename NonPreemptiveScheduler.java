import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Shared loop for every non-preemptive algorithm.
 *
 * FCFS, SJF and Priority all work the same way: once a job starts, it runs to
 * completion, and when the CPU is free, the next job is picked from the ready
 * jobs. The only difference between them is HOW the next job is picked, so each
 * subclass supplies just that ordering through pickOrder().
 */
public abstract class NonPreemptiveScheduler implements Scheduler {

    /** Order in which ready jobs are picked. The first job in this order runs next. */
    protected abstract Comparator<ProcessJob> pickOrder();

    @Override
    public SchedulerResult schedule(List<ProcessJob> inputJobs) {
        List<ProcessJob> jobs = ScheduleMetrics.sortedByArrival(inputJobs);

        // A PriorityQueue picks the next job in O(log n) instead of re-sorting the whole list.
        PriorityQueue<ProcessJob> ready = new PriorityQueue<>(pickOrder());
        List<ScheduledSlice> slices = new ArrayList<>();
        Map<Integer, Integer> finishTimes = new HashMap<>();

        int currentTime = 0;
        int nextArrival = 0;

        while (nextArrival < jobs.size() || !ready.isEmpty()) {
            // CPU is idle with nothing ready: jump ahead to the next arrival.
            if (ready.isEmpty() && jobs.get(nextArrival).getArrivalTime() > currentTime) {
                currentTime = jobs.get(nextArrival).getArrivalTime();
            }

            // Move every job that has arrived by now into the ready queue.
            while (nextArrival < jobs.size()
                    && jobs.get(nextArrival).getArrivalTime() <= currentTime) {
                ready.add(jobs.get(nextArrival));
                nextArrival++;
            }

            ProcessJob job = ready.poll();
            int start = currentTime;
            int end = start + job.getBurstTime();

            slices.add(new ScheduledSlice(job, start, end));
            finishTimes.put(job.getPid(), end);
            currentTime = end;
        }

        return ScheduleMetrics.buildResult(getName(), jobs, slices, finishTimes);
    }
}
