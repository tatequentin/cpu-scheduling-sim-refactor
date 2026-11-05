/**
 * A block of time on the Gantt chart during which one process runs.
 */
public class ScheduledSlice {

    private final ProcessJob job;
    private final int startTime;
    private final int endTime;

    public ScheduledSlice(ProcessJob job, int startTime, int endTime) {
        if (job == null) {
            throw new IllegalArgumentException("Slice must have a job");
        }
        if (endTime <= startTime) {
            throw new IllegalArgumentException(
                    "Slice end (" + endTime + ") must be after start (" + startTime + ")");
        }
        this.job = job;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public ProcessJob getJob() {
        return job;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        return "P" + job.getPid() + "[" + startTime + " -> " + endTime + "]";
    }
}
