import java.util.Comparator;

/**
 * Priority (non-preemptive): the ready job with the lowest priority value runs next.
 * A smaller number means a higher priority.
 */
public class PriorityScheduler extends NonPreemptiveScheduler {

    @Override
    public String getName() {
        return "Priority (non-preemptive)";
    }

    @Override
    protected Comparator<ProcessJob> pickOrder() {
        return Comparator.comparingInt(ProcessJob::getPriority)
                .thenComparing(ScheduleMetrics.BY_ARRIVAL);
    }
}
