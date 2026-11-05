import java.util.Comparator;

/**
 * First Come First Served: the earliest arrival runs next.
 */
public class FcfsScheduler extends NonPreemptiveScheduler {

    @Override
    public String getName() {
        return "FCFS";
    }

    @Override
    protected Comparator<ProcessJob> pickOrder() {
        return ScheduleMetrics.BY_ARRIVAL;
    }
}
