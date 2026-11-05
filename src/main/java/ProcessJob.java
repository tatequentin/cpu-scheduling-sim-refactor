/**
 * One process to be scheduled. Immutable, and validated on creation so an
 * invalid process can never reach a scheduling algorithm.
 */
public class ProcessJob {

    private final int pid;
    private final int arrivalTime;
    private final int burstTime;
    private final int priority;

    public ProcessJob(int pid, int arrivalTime, int burstTime, int priority) {
        if (pid < 0) {
            throw new IllegalArgumentException("PID must be 0 or greater, got " + pid);
        }
        if (arrivalTime < 0) {
            throw new IllegalArgumentException("Arrival time must be 0 or greater, got " + arrivalTime);
        }
        if (burstTime <= 0) {
            throw new IllegalArgumentException("Burst time must be greater than 0, got " + burstTime);
        }
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
    }

    public int getPid() {
        return pid;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getBurstTime() {
        return burstTime;
    }

    /** Lower value means higher priority. */
    public int getPriority() {
        return priority;
    }

    @Override
    public String toString() {
        return "P" + pid
                + " (arr=" + arrivalTime
                + ", burst=" + burstTime
                + ", prio=" + priority + ")";
    }
}
