package com.studenttracker.fx.controller;

import com.studenttracker.enums.OpportunityStatus;
import com.studenttracker.fx.components.StatusBadge;
import com.studenttracker.fx.controller.dialog.ApplyDialogController;
import com.studenttracker.fx.controller.dialog.OpportunityDialogController;
import com.studenttracker.fx.navigation.Searchable;
import com.studenttracker.fx.support.FxContext;
import com.studenttracker.fx.support.Tables;
import com.studenttracker.fx.support.Toggles;
import com.studenttracker.model.Company;
import com.studenttracker.model.Opportunity;
import com.studenttracker.util.Dates;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseButton;

public final class OpportunitiesFxController extends BaseFxController implements Searchable {

    private static final String EMPTY = "No opportunities here yet. Save a role you want to apply for.";
    private static final int URGENT_DAYS = 7;

    @FXML
    private ToggleGroup filterGroup;
    @FXML
    private Button archiveButton;
    @FXML
    private Button linkButton;
    @FXML
    private Button editButton;
    @FXML
    private Button addButton;
    @FXML
    private Button applyButton;
    @FXML
    private TableView<Opportunity> table;

    private OpportunityStatus statusFilter = OpportunityStatus.OPEN;
    private String keyword = "";

    public OpportunitiesFxController(FxContext ctx) {
        super(ctx);
    }

    @FXML
    private void initialize() {
        Tables.configure(table, EMPTY);
        table.getColumns().add(Tables.text("Company", 150, Opportunity::getCompanyName));
        table.getColumns().add(Tables.text("Role", 190, Opportunity::getRoleTitle));
        table.getColumns().add(Tables.nodes("Type", 100, opportunity -> StatusBadge.forOpportunityType(opportunity.getType()),
                Comparator.comparing(Opportunity::getType)));
        table.getColumns().add(Tables.text("Location", 110, Opportunity::getLocation));
        table.getColumns().add(Tables.text("Mode", 80,
                opportunity -> opportunity.getWorkMode() == null ? "" : opportunity.getWorkMode().toString()));
        table.getColumns().add(Tables.column("Deadline", 105, Opportunity::getDeadline, Dates::display));
        table.getColumns().add(Tables.column("Min CGPA", 80, Opportunity::getMinimumCgpa, BigDecimal::toPlainString));
        table.getColumns().add(Tables.text("Required skills", 170,
                opportunity -> String.join(", ", opportunity.getRequiredSkills())));
        table.getColumns().add(Tables.nodes("Status", 100, opportunity -> StatusBadge.forOpportunity(opportunity.getStatus()),
                Comparator.comparing(Opportunity::getStatus)));
        Tables.styleRows(table, opportunity -> isClosingSoon(opportunity) ? "row-urgent" : null);

        Toggles.segmented(filterGroup, key -> {
            statusFilter = "ALL".equals(key) ? null : OpportunityStatus.valueOf(key);
            reload();
        });
        table.getSelectionModel().selectedItemProperty().addListener((observable, previous, selected) -> updateButtons());
        table.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                editOpportunity();
            }
        });
        updateButtons();
    }

    private static boolean isClosingSoon(Opportunity opportunity) {
        LocalDate deadline = opportunity.getDeadline();
        if (deadline == null || opportunity.getStatus() != OpportunityStatus.OPEN) {
            return false;
        }
        LocalDate today = LocalDate.now();
        return !deadline.isBefore(today) && !deadline.isAfter(today.plusDays(URGENT_DAYS));
    }

    @Override
    public String searchPrompt() {
        return "Search role, company or location";
    }

    @Override
    public void applySearch(String text) {
        keyword = text == null ? "" : text;
        reload();
    }

    @Override
    public void onShow() {
        reload();
    }

    private void reload() {
        String search = keyword;
        OpportunityStatus status = statusFilter;
        load(() -> services.opportunities().search(search, status), opportunities -> {
            table.setItems(FXCollections.observableArrayList(opportunities));
            Tables.setEmptyText(table, !search.isEmpty() ? "No opportunities match your search."
                    : status == OpportunityStatus.OPEN ? EMPTY : "No listings with this status.");
            updateButtons();
        });
    }

    private void updateButtons() {
        Opportunity selected = table.getSelectionModel().getSelectedItem();
        boolean any = selected != null;
        applyButton.setDisable(!any || selected.getStatus() != OpportunityStatus.OPEN);
        editButton.setDisable(!any);
        linkButton.setDisable(!any || selected.getSourceUrl() == null);
        archiveButton.setDisable(!any);
        archiveButton.setText(any && selected.getStatus() == OpportunityStatus.ARCHIVED ? "Reopen" : "Archive");
    }

    // ---------------------------------------------------------------- actions

    @FXML
    private void addOpportunity() {
        load(() -> services.companies().search("", false), companies -> {
            if (companies.isEmpty()) {
                ctx.dialogs().info("Add a company first",
                        "Every opportunity belongs to a company. Add one on the Companies page, then come back.");
                return;
            }
            showEditor(companies, null);
        }, addButton);
    }

    @FXML
    private void editOpportunity() {
        Opportunity selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            load(() -> services.companies().search("", true), companies -> showEditor(companies, selected), editButton);
        }
    }

    private void showEditor(List<Company> companies, Opportunity editing) {
        String title = editing == null ? "Add opportunity" : "Edit opportunity";
        ctx.dialogs().<Opportunity, OpportunityDialogController>form(title, editing == null ? "Add opportunity" : "Save changes",
                "opportunity-dialog.fxml", dialog -> dialog.configure(companies, editing))
                .ifPresent(this::save);
    }

    /** Saves the opportunity, first asking what to do if it is open although its deadline has passed. */
    private void save(Opportunity opportunity) {
        boolean keepOpen = false;
        if (services.opportunities().hasPassedDeadline(opportunity)) {
            int choice = ctx.dialogs().choose("Deadline has passed",
                    "The deadline for " + opportunity.getRoleTitle() + " was " + Dates.display(opportunity.getDeadline())
                            + ". Keep the listing open for your records, or mark it as closed?",
                    "Keep open", "Mark as closed", "Go back");
            if (choice == 0) {
                keepOpen = true;
            } else if (choice == 1) {
                opportunity.setStatus(OpportunityStatus.CLOSED);
            } else {
                return;
            }
        }
        boolean keepPastDeadlineOpen = keepOpen;
        perform(() -> services.opportunities().save(opportunity, keepPastDeadlineOpen), () -> {
            ctx.toasts().success(opportunity.getRoleTitle() + " saved.");
            reload();
        }, addButton, editButton);
    }

    @FXML
    private void markApplied() {
        Opportunity opportunity = table.getSelectionModel().getSelectedItem();
        if (opportunity == null) {
            return;
        }
        load(() -> services.applications().minimumCgpaWarning(opportunity.getOpportunityId()), warning -> {
            if (warning.isPresent() && !ctx.dialogs().confirm("Below the minimum CGPA",
                    warning.get() + " Do you still want to mark it as applied?", "Apply anyway", "Cancel")) {
                return;
            }
            ctx.dialogs().<ApplyDialogController.Details, ApplyDialogController>form(
                    "Mark as applied", "Mark as applied", "apply-dialog.fxml", dialog -> dialog.configure(opportunity))
                    .ifPresent(details -> perform(
                            () -> services.applications().apply(opportunity.getOpportunityId(),
                                    details.appliedOn(), details.notes()),
                            () -> {
                                ctx.toasts().success("Marked " + opportunity.getRoleTitle() + " at "
                                        + opportunity.getCompanyName() + " as applied.");
                                reload();
                            }, applyButton));
        }, applyButton);
    }

    @FXML
    private void toggleArchived() {
        Opportunity opportunity = table.getSelectionModel().getSelectedItem();
        if (opportunity == null) {
            return;
        }
        boolean reopening = opportunity.getStatus() == OpportunityStatus.ARCHIVED;
        if (!reopening && !ctx.dialogs().confirmDestructive("Archive this listing?",
                "Archive " + opportunity.getRoleTitle() + " at " + opportunity.getCompanyName()
                        + "? It leaves your active lists, and any applications you made stay on record.",
                "Archive listing", "Keep listing")) {
            return;
        }
        OpportunityStatus target = reopening ? OpportunityStatus.OPEN : OpportunityStatus.ARCHIVED;
        perform(() -> services.opportunities().changeStatus(opportunity, target), () -> {
            ctx.toasts().success(opportunity.getRoleTitle() + (reopening ? " reopened." : " archived."));
            reload();
        }, archiveButton);
    }

    @FXML
    private void openListing() {
        Opportunity selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            ctx.openUrl(selected.getSourceUrl());
        }
    }
}
