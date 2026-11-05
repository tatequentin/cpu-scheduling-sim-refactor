import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The output of one scheduling algorithm.
 *
 * Fully immutable: the constructor makes read-only copies of the list and maps,
 * so nothing outside this class can change a result after it is created.
 * The maps are TreeMaps, so per-process output is always in PID order.
 */
public class SchedulerResult {

    private final String algorithmName;
    private final List<ScheduledSlice> slices;
    private final Map<Integer, Integer> waitingTimes;
    private final Map<Integer, Integer> turnaroundTimes;
    private final double avgWaiting;
    private final double avgTurnaround;

    public SchedulerResult(
            String algorithmName,
            List<ScheduledSlice> slices,
            Map<Integer, Integer> waitingTimes,
            Map<Integer, Integer> turnaroundTimes,
            double avgWaiting,
            double avgTurnaround) {

        this.algorithmName = algorithmName;
        this.slices = Collections.unmodifiableList(new ArrayList<>(slices));
        this.waitingTimes = Collections.unmodifiableMap(new TreeMap<>(waitingTimes));
        this.turnaroundTimes = Collections.unmodifiableMap(new TreeMap<>(turnaroundTimes));
        this.avgWaiting = avgWaiting;
        this.avgTurnaround = avgTurnaround;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public List<ScheduledSlice> getSlices() {
        return slices;
    }

    public Map<Integer, Integer> getWaitingTimes() {
        return waitingTimes;
    }

    public Map<Integer, Integer> getTurnaroundTimes() {
        return turnaroundTimes;
    }

    public double getAvgWaiting() {
        return avgWaiting;
    }

    public double getAvgTurnaround() {
        return avgTurnaround;
    }
}
