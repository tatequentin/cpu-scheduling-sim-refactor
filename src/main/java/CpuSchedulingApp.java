import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CpuSchedulingApp extends Application {

    /**
     * Every algorithm the app runs. The UI only knows about the Scheduler interface,
     * so adding an algorithm means writing one class and adding one line here.
     */
    private final List<Scheduler> schedulers = List.of(
            new FcfsScheduler(),
            new SjfScheduler(),
            new PriorityScheduler(),
            new RoundRobinScheduler(2)
    );

    private TextArea processInputArea;
    private TextArea metricsOutputArea;
    private HBox ganttChartBox;
    private ComboBox<String> algorithmPicker;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("CPU Scheduling Simulator - Tate York");

        // Top: title and input instructions
        Label titleLabel = new Label("CPU Scheduling Simulator");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        VBox topBox = new VBox(5, titleLabel, new Label(
                "Enter processes as: pid,arrival,burst,priority (one per line)."));
        topBox.setPadding(new Insets(10));

        // Left: process input, pre-filled with sample data
        processInputArea = new TextArea();
        processInputArea.setPromptText("Example:\n1,0,5,2\n2,1,3,1\n3,2,8,3");
        processInputArea.setText(
                "1,0,5,2\n" +
                "2,1,3,1\n" +
                "3,2,8,3\n" +
                "4,3,6,2"
        );
        processInputArea.setPrefColumnCount(25);
        processInputArea.setPrefRowCount(10);

        VBox inputBox = new VBox(5, new Label("Process List:"), processInputArea);
        inputBox.setPadding(new Insets(10));
        inputBox.setPrefWidth(260);

        // Center: Gantt chart, scrollable for long schedules
        ganttChartBox = new HBox(3);
        ganttChartBox.setPadding(new Insets(10));
        ganttChartBox.setAlignment(Pos.CENTER_LEFT);
        ganttChartBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1px;");
        ScrollPane ganttScroll = new ScrollPane(ganttChartBox);
        ganttScroll.setFitToHeight(true);
        ganttScroll.setFitToWidth(false);

        VBox centerBox = new VBox(5, new Label("Gantt Chart:"), ganttScroll);
        centerBox.setPadding(new Insets(10));

        // Right: metrics for all algorithms, side by side
        metricsOutputArea = new TextArea();
        metricsOutputArea.setEditable(false);
        metricsOutputArea.setPrefRowCount(15);
        VBox metricsBox = new VBox(5, new Label("Metrics / Comparison:"), metricsOutputArea);
        metricsBox.setPadding(new Insets(10));
        metricsBox.setPrefWidth(300);

        // Bottom: algorithm picker, filled from the scheduler list
        algorithmPicker = new ComboBox<>();
        for (Scheduler scheduler : schedulers) {
            algorithmPicker.getItems().add(scheduler.getName());
        }
        algorithmPicker.getSelectionModel().selectFirst();

        Button runButton = new Button("Run Schedulers");
        runButton.setOnAction(e -> runSimulation());

        Button clearButton = new Button("Clear Output");
        clearButton.setOnAction(e -> {
            ganttChartBox.getChildren().clear();
            metricsOutputArea.clear();
        });

        HBox controlRow = new HBox(10,
                new Label("Show Gantt for:"), algorithmPicker,
                runButton, clearButton
        );
        controlRow.setPadding(new Insets(10));
        controlRow.setAlignment(Pos.CENTER_LEFT);

        BorderPane root = new BorderPane();
        root.setTop(topBox);
        root.setLeft(inputBox);
        root.setCenter(centerBox);
        root.setRight(metricsBox);
        root.setBottom(controlRow);

        primaryStage.setScene(new Scene(root, 1000, 600));
        primaryStage.show();
    }

    private void runSimulation() {
        List<ProcessJob> jobs;
        try {
            jobs = ProcessInputParser.parse(processInputArea.getText());
        } catch (IllegalArgumentException ex) {
            showAlert("Input Error", ex.getMessage());
            return;
        }

        if (jobs.isEmpty()) {
            showAlert("Input Error", "Please enter at least one process.");
            return;
        }

        // Run every algorithm on the same input, keeping them in display order.
        Map<String, SchedulerResult> results = new LinkedHashMap<>();
        StringBuilder sb = new StringBuilder();
        for (Scheduler scheduler : schedulers) {
            SchedulerResult result = scheduler.schedule(jobs);
            results.put(scheduler.getName(), result);
            appendMetrics(sb, result);
        }
        metricsOutputArea.setText(sb.toString());

        // Show the Gantt chart for the selected algorithm. No if/else chain needed.
        String selected = algorithmPicker.getSelectionModel().getSelectedItem();
        drawGanttChart(results.get(selected));
    }

    private void appendMetrics(StringBuilder sb, SchedulerResult result) {
        sb.append("=== ").append(result.getAlgorithmName()).append(" ===\n");
        sb.append("Average waiting time: ")
                .append(String.format("%.2f", result.getAvgWaiting()))
                .append("\n");
        sb.append("Average turnaround time: ")
                .append(String.format("%.2f", result.getAvgTurnaround()))
                .append("\n");

        sb.append("Per-process details:\n");
        for (Map.Entry<Integer, Integer> entry : result.getWaitingTimes().entrySet()) {
            int pid = entry.getKey();
            sb.append("  P").append(pid)
                    .append(" -> wait=").append(entry.getValue())
                    .append(", turn=").append(result.getTurnaroundTimes().get(pid))
                    .append("\n");
        }
        sb.append("\n");
    }

    private void drawGanttChart(SchedulerResult result) {
        ganttChartBox.getChildren().clear();

        for (ScheduledSlice slice : result.getSlices()) {
            int start = slice.getStartTime();
            int end = slice.getEndTime();
            int width = (end - start) * 40 + 20;

            VBox block = new VBox(2,
                    new Label("P" + slice.getJob().getPid()),
                    new Label(start + " - " + end));
            block.setAlignment(Pos.CENTER);
            block.setMinWidth(width);
            block.setMaxWidth(width);
            block.setPrefHeight(60);
            block.setStyle("-fx-border-color: black; -fx-border-width: 1px; -fx-padding: 3px;");

            ganttChartBox.getChildren().add(block);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
