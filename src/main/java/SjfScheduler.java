import java.util.Comparator;

/**
 * Shortest Job First (non-preemptive): the ready job with the smallest burst runs next.
 */
public class SjfScheduler extends NonPreemptiveScheduler {

    @Override
    public String getName() {
        return "SJF (non-preemptive)";
    }

    @Override
    protected Comparator<ProcessJob> pickOrder() {
        return Comparator.comparingInt(ProcessJob::getBurstTime)
                .thenComparing(ScheduleMetrics.BY_ARRIVAL);
    }
}
