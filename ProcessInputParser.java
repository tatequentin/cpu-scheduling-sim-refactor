import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Turns the text in the input box into a list of processes.
 *
 * Kept separate from the JavaFX UI so it can be unit tested on its own.
 * Every error message includes the line number so the user can fix it quickly.
 */
public final class ProcessInputParser {

    private ProcessInputParser() {
        // static helpers only
    }

    /**
     * Parses lines of the form pid,arrival,burst,priority. Blank lines are skipped.
     *
     * @throws IllegalArgumentException with the line number if any line is invalid
     */
    public static List<ProcessJob> parse(String text) {
        List<ProcessJob> jobs = new ArrayList<>();
        Set<Integer> seenPids = new HashSet<>();
        String[] lines = text.split("\\r?\\n");

        for (int i = 0; i < lines.length; i++) {
            int lineNumber = i + 1;
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }

            String[] parts = line.split(",");
            if (parts.length != 4) {
                throw new IllegalArgumentException(
                        "Line " + lineNumber + " should look like: pid,arrival,burst,priority");
            }

            ProcessJob job;
            try {
                job = new ProcessJob(
                        Integer.parseInt(parts[0].trim()),
                        Integer.parseInt(parts[1].trim()),
                        Integer.parseInt(parts[2].trim()),
                        Integer.parseInt(parts[3].trim()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Line " + lineNumber + " has non-integer values");
            } catch (IllegalArgumentException e) {
                // Range errors from the ProcessJob constructor, e.g. burst <= 0
                throw new IllegalArgumentException("Line " + lineNumber + ": " + e.getMessage());
            }

            // Results are keyed by PID, so a duplicate would silently overwrite another process.
            if (!seenPids.add(job.getPid())) {
                throw new IllegalArgumentException(
                        "Line " + lineNumber + " reuses PID " + job.getPid());
            }
            jobs.add(job);
        }

        return jobs;
    }
}
