package com.studenttracker.fx.controller;

import com.studenttracker.enums.ApplicationStatus;
import com.studenttracker.enums.InterviewResult;
import com.studenttracker.fx.components.StatCard;
import com.studenttracker.fx.support.FxContext;
import com.studenttracker.fx.support.Palette;
import com.studenttracker.model.DashboardSummary;
import com.studenttracker.model.Interview;
import com.studenttracker.service.InterviewService;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;

public final class ReportsFxController extends BaseFxController {

    private record Snapshot(DashboardSummary summary, List<Interview> interviews) {
    }

    @FunctionalInterface
    private interface ExportAction {
        int run(Path destination) throws IOException;
    }

    @FXML
    private StatCard totalCard;
    @FXML
    private StatCard offersCard;
    @FXML
    private StatCard roundsCard;
    @FXML
    private StatCard passedCard;
    @FXML
    private BarChart<String, Number> statusChart;
    @FXML
    private Label statusEmpty;
    @FXML
    private PieChart interviewChart;
    @FXML
    private Label interviewEmpty;
    @FXML
    private Button exportApplicationsButton;
    @FXML
    private Button exportInterviewsButton;
    @FXML
    private Button refreshButton;

    public ReportsFxController(FxContext ctx) {
        super(ctx);
    }

    @FXML
    private void initialize() {
        interviewChart.setStartAngle(90);
        interviewChart.setLegendVisible(true);
    }

    @Override
    public void onShow() {
        refresh();
    }

    @FXML
    private void refresh() {
        load(() -> new Snapshot(services.dashboard().summary(), services.interviews().list(InterviewService.Scope.ALL)),
                this::render, refreshButton);
    }

    private void render(Snapshot snapshot) {
        DashboardSummary summary = snapshot.summary();
        Map<InterviewResult, Integer> results = new EnumMap<>(InterviewResult.class);
        for (InterviewResult result : InterviewResult.values()) {
            results.put(result, 0);
        }
        snapshot.interviews().forEach(interview -> results.merge(interview.getResult(), 1, Integer::sum));

        totalCard.setValue(summary.totalApplications());
        offersCard.setValue(summary.offers());
        roundsCard.setValue(snapshot.interviews().size());
        passedCard.setValue(results.get(InterviewResult.PASSED));

        showStatusChart(summary);
        showInterviewChart(results, snapshot.interviews().size());
    }

    private void showStatusChart(DashboardSummary summary) {
        boolean empty = summary.totalApplications() == 0;
        statusChart.setVisible(!empty);
        statusEmpty.setVisible(empty);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (ApplicationStatus status : ApplicationStatus.values()) {
            XYChart.Data<String, Number> point = new XYChart.Data<>(
                    status.toString(), summary.applicationsByStatus().getOrDefault(status, 0));
            colour(point.nodeProperty().get(), "-fx-bar-fill", Palette.applicationStatus(status));
            point.nodeProperty().addListener((observable, previous, node) ->
                    colour(node, "-fx-bar-fill", Palette.applicationStatus(status)));
            series.getData().add(point);
        }
        statusChart.getData().setAll(List.of(series));
    }

    private void showInterviewChart(Map<InterviewResult, Integer> results, int total) {
        interviewChart.setVisible(total > 0);
        interviewEmpty.setVisible(total == 0);
        var data = FXCollections.<PieChart.Data>observableArrayList();
        for (InterviewResult result : InterviewResult.values()) {
            PieChart.Data slice = new PieChart.Data(result + "  " + results.get(result), results.get(result));
            slice.nodeProperty().addListener((observable, previous, node) ->
                    colour(node, "-fx-pie-color", Palette.interviewResult(result)));
            data.add(slice);
        }
        interviewChart.setData(data);
    }

    private static void colour(Node node, String property, String hex) {
        if (node != null) {
            node.setStyle(property + ": " + hex + ";");
        }
    }

    // ----------------------------------------------------------------- export

    @FXML
    private void exportApplications() {
        export("applications.csv", "Export applications", services.exports()::exportApplications,
                exportApplicationsButton);
    }

    @FXML
    private void exportInterviews() {
        export("interviews.csv", "Export interviews", services.exports()::exportInterviews,
                exportInterviewsButton);
    }

    private void export(String suggestedName, String title, ExportAction action, Button trigger) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.setInitialFileName(suggestedName);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        File chosen = chooser.showSaveDialog(trigger.getScene().getWindow());
        if (chosen == null) {
            return;
        }
        if (!chosen.getName().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            chosen = new File(chosen.getParentFile(), chosen.getName() + ".csv");
        }
        Path destination = chosen.toPath();
        load(() -> {
            try {
                return action.run(destination);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }, rows -> ctx.toasts().success(rows + (rows == 1 ? " row" : " rows") + " exported to "
                + destination.getFileName() + "."), trigger);
    }
}
