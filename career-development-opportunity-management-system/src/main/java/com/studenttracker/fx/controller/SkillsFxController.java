package com.studenttracker.fx.controller;

import com.studenttracker.enums.Proficiency;
import com.studenttracker.enums.SkillStatus;
import com.studenttracker.fx.components.StatusBadge;
import com.studenttracker.fx.controller.dialog.SkillDialogController;
import com.studenttracker.fx.navigation.Searchable;
import com.studenttracker.fx.support.FxContext;
import com.studenttracker.fx.support.Tables;
import com.studenttracker.fx.support.Toggles;
import com.studenttracker.model.Skill;
import com.studenttracker.model.StudentSkill;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

public final class SkillsFxController extends BaseFxController implements Searchable {

    private static final String EMPTY = "No skills yet. Add one to start tracking what you are learning.";

    @FXML
    private ToggleGroup filterGroup;
    @FXML
    private Label summaryLabel;
    @FXML
    private Button removeButton;
    @FXML
    private Button editButton;
    @FXML
    private Button addButton;
    @FXML
    private TableView<StudentSkill> table;

    private List<StudentSkill> all = List.of();
    private SkillStatus statusFilter;
    private String searchText = "";

    public SkillsFxController(FxContext ctx) {
        super(ctx);
    }

    @FXML
    private void initialize() {
        Tables.configure(table, EMPTY);
        table.getColumns().add(Tables.text("Skill", 180, StudentSkill::getSkillName));
        table.getColumns().add(Tables.nodes("Proficiency", 190, SkillsFxController::proficiencyBar,
                Comparator.comparing(StudentSkill::getProficiency)));
        table.getColumns().add(Tables.nodes("Status", 120,
                skill -> StatusBadge.forSkillStatus(skill.getLearningStatus()),
                Comparator.comparing(StudentSkill::getLearningStatus)));
        table.getColumns().add(Tables.text("Notes", 360, StudentSkill::getNotes));

        Toggles.segmented(filterGroup, key -> {
            statusFilter = "ALL".equals(key) ? null : SkillStatus.valueOf(key);
            applyFilters();
        });
        table.getSelectionModel().selectedItemProperty().addListener((observable, previous, selected) -> updateButtons());
        table.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                editSkill();
            }
        });
        updateButtons();
    }

    @Override
    public String searchPrompt() {
        return "Search skills";
    }

    @Override
    public void applySearch(String text) {
        searchText = text == null ? "" : text.toLowerCase(Locale.ROOT);
        applyFilters();
    }

    @Override
    public void onShow() {
        load(services.skills()::list, skills -> {
            all = skills;
            applyFilters();
        });
    }

    private void applyFilters() {
        List<StudentSkill> visible = all.stream()
                .filter(skill -> statusFilter == null || skill.getLearningStatus() == statusFilter)
                .filter(skill -> searchText.isEmpty()
                        || skill.getSkillName().toLowerCase(Locale.ROOT).contains(searchText)
                        || (skill.getNotes() != null && skill.getNotes().toLowerCase(Locale.ROOT).contains(searchText)))
                .toList();
        table.setItems(FXCollections.observableArrayList(visible));
        Tables.setEmptyText(table, all.isEmpty() ? EMPTY : "No skills match the current filter.");

        long learning = all.stream().filter(skill -> skill.getLearningStatus() == SkillStatus.LEARNING).count();
        long completed = all.size() - learning;
        summaryLabel.setText(all.isEmpty() ? "" : all.size() + " skills  |  " + learning + " learning  |  "
                + completed + " completed");
        updateButtons();
    }

    private void updateButtons() {
        boolean selected = table.getSelectionModel().getSelectedItem() != null;
        editButton.setDisable(!selected);
        removeButton.setDisable(!selected);
    }

    @FXML
    private void addSkill() {
        load(() -> services.skills().catalog().stream().map(Skill::getSkillName).toList(), names ->
                ctx.dialogs().<StudentSkill, SkillDialogController>form("Add skill", "Add skill", "skill-dialog.fxml",
                        dialog -> dialog.configure(names, null))
                        .ifPresent(skill -> perform(() -> services.skills().add(skill), () -> {
                            ctx.toasts().success(skill.getSkillName() + " added.");
                            onShow();
                        }, addButton)), addButton);
    }

    @FXML
    private void editSkill() {
        StudentSkill selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        ctx.dialogs().<StudentSkill, SkillDialogController>form("Edit skill", "Save changes", "skill-dialog.fxml",
                dialog -> dialog.configure(List.of(), selected))
                .ifPresent(skill -> perform(() -> services.skills().update(skill), () -> {
                    ctx.toasts().success(skill.getSkillName() + " updated.");
                    onShow();
                }));
    }

    @FXML
    private void removeSkill() {
        StudentSkill selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        boolean confirmed = ctx.dialogs().confirmDestructive("Remove skill?",
                "Remove " + selected.getSkillName() + " from your skills? This can't be undone.",
                "Remove skill", "Keep skill");
        if (confirmed) {
            perform(() -> services.skills().remove(selected), () -> {
                ctx.toasts().success(selected.getSkillName() + " removed.");
                onShow();
            });
        }
    }

    /** Three small segments filled according to the proficiency level, followed by its name. */
    private static Node proficiencyBar(StudentSkill skill) {
        int level = skill.getProficiency().ordinal() + 1;
        HBox segments = new HBox(4);
        segments.setAlignment(Pos.CENTER_LEFT);
        for (int i = 1; i <= Proficiency.values().length; i++) {
            Region segment = new Region();
            segment.getStyleClass().add("prof-segment");
            if (i <= level) {
                segment.getStyleClass().add("prof-filled");
            }
            segments.getChildren().add(segment);
        }
        Label name = new Label(skill.getProficiency().toString());
        name.getStyleClass().add("muted");
        HBox box = new HBox(10, segments, name);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }
}
