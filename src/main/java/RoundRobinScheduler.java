import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Round Robin: each ready job runs for at most one time quantum, then goes to the
 * back of a FIFO queue if it still has work left.
 *
 * Convention: jobs that arrive during a time slice join the queue BEFORE the job
 * that was just preempted.
 */
public class RoundRobinScheduler implements Scheduler {

    private final int timeQuantum;

    public RoundRobinScheduler(int timeQuantum) {
        if (timeQuantum <= 0) {
            throw new IllegalArgumentException("Time quantum must be greater than 0, got " + timeQuantum);
        }
        this.timeQuantum = timeQuantum;
    }

    public int getTimeQuantum() {
        return timeQuantum;
    }

    @Override
    public String getName() {
        return "Round Robin (q=" + timeQuantum + ")";
    }

    /** A job plus how much of its burst is still left to run. */
    private static final class RunningJob {
        final ProcessJob job;
        int remainingTime;

        RunningJob(ProcessJob job) {
            this.job = job;
            this.remainingTime = job.getBurstTime();
        }
    }

    @Override
    public SchedulerResult schedule(List<ProcessJob> inputJobs) {
        List<ProcessJob> jobs = ScheduleMetrics.sortedByArrival(inputJobs);

        Queue<RunningJob> readyQueue = new ArrayDeque<>();
        List<ScheduledSlice> slices = new ArrayList<>();
        Map<Integer, Integer> finishTimes = new HashMap<>();

        int currentTime = 0;
        int nextArrival = 0;

        while (nextArrival < jobs.size() || !readyQueue.isEmpty()) {
            // CPU is idle with nothing ready: jump ahead to the next arrival.
            if (readyQueue.isEmpty() && jobs.get(nextArrival).getArrivalTime() > currentTime) {
                currentTime = jobs.get(nextArrival).getArrivalTime();
            }
            nextArrival = admitArrivals(jobs, nextArrival, currentTime, readyQueue);

            RunningJob running = readyQueue.poll();
            int runTime = Math.min(timeQuantum, running.remainingTime);
            int start = currentTime;
            currentTime += runTime;
            running.remainingTime -= runTime;
            slices.add(new ScheduledSlice(running.job, start, currentTime));

            // New arrivals go into the queue before the preempted job.
            nextArrival = admitArrivals(jobs, nextArrival, currentTime, readyQueue);

            if (running.remainingTime > 0) {
                readyQueue.add(running);
            } else {
                finishTimes.put(running.job.getPid(), currentTime);
            }
        }

        return ScheduleMetrics.buildResult(getName(), jobs, slices, finishTimes);
    }

    /** Adds every job that has arrived by currentTime. Returns the index of the next unarrived job. */
    private static int admitArrivals(List<ProcessJob> jobs, int nextArrival, int currentTime,
                                     Queue<RunningJob> readyQueue) {
        while (nextArrival < jobs.size() && jobs.get(nextArrival).getArrivalTime() <= currentTime) {
            readyQueue.add(new RunningJob(jobs.get(nextArrival)));
            nextArrival++;
        }
        return nextArrival;
    }
}
