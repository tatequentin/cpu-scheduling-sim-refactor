# CPU Scheduling Simulator

A Java and JavaFX application that simulates four CPU scheduling algorithms:
First Come First Served, non-preemptive Shortest Job First, non-preemptive
Priority, and Round Robin. It runs every algorithm on the same input, compares
average waiting and turnaround times, and draws a Gantt chart for the selected one.

## Design

```
Scheduler (interface)
├── NonPreemptiveScheduler (abstract: shared run-to-completion loop)
│   ├── FcfsScheduler      picks earliest arrival
│   ├── SjfScheduler       picks shortest burst
│   └── PriorityScheduler  picks lowest priority value
└── RoundRobinScheduler    FIFO queue with a time quantum
```

- The UI depends only on the `Scheduler` interface and loops over a list of
  schedulers. Adding an algorithm means writing one class and adding one line
  to that list.
- FCFS, SJF and Priority differ only in how they pick the next ready job, so
  each one supplies just a `Comparator`. The shared loop uses a `PriorityQueue`,
  so each pick is O(log n).
- `ProcessJob`, `ScheduledSlice` and `SchedulerResult` are immutable. Results
  return read-only collections in PID order.
- Ties are broken by arrival time, then PID, so the same input always produces
  the same schedule.
- Invalid input fails loudly: the parser reports the line number for bad
  formats, negative arrival times, non-positive bursts and duplicate PIDs, and
  the metrics code throws if it ever computes an impossible result instead of
  hiding it.

## Run

```
mvn test          # run the unit tests
mvn javafx:run    # launch the app
```

## Tests

- `SchedulerTest`: each algorithm checked against schedules worked out by hand,
  plus idle gaps, empty input, deterministic tie-breaking, immutability, and
  confirming the input list is never modified.
- `ProcessInputParserTest`: valid input, blank lines, and every rejection case.
