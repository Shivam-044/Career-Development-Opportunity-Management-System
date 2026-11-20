package com.studenttracker.fx.controller;

import com.studenttracker.fx.components.StatusBadge;
import com.studenttracker.fx.controller.dialog.CompanyDialogController;
import com.studenttracker.fx.navigation.Searchable;
import com.studenttracker.fx.support.FxContext;
import com.studenttracker.fx.support.Tables;
import com.studenttracker.model.Company;
import java.util.Comparator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;

public final class CompaniesFxController extends BaseFxController implements Searchable {

    private static final String EMPTY = "No companies yet. Add the companies you want to keep an eye on.";

    @FXML
    private CheckBox showArchived;
    @FXML
    private Button websiteButton;
    @FXML
    private Button archiveButton;
    @FXML
    private Button editButton;
    @FXML
    private Button addButton;
    @FXML
    private TableView<Company> table;

    private String keyword = "";

    public CompaniesFxController(FxContext ctx) {
        super(ctx);
    }

    @FXML
    private void initialize() {
        Tables.configure(table, EMPTY);
        table.getColumns().add(Tables.text("Company", 200, Company::getCompanyName));
        table.getColumns().add(Tables.text("Industry", 140, Company::getIndustry));
        table.getColumns().add(Tables.text("Location", 140, Company::getLocation));
        table.getColumns().add(Tables.text("Website", 220, Company::getWebsite));
        table.getColumns().add(Tables.nodes("Status", 100,
                company -> new StatusBadge(company.isArchived() ? "Archived" : "Active",
                        company.isArchived() ? "muted" : "success"),
                Comparator.comparing(Company::isArchived)));

        table.getSelectionModel().selectedItemProperty().addListener((observable, previous, selected) -> updateButtons());
        table.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                editCompany();
            }
        });
        updateButtons();
    }

    @Override
    public String searchPrompt() {
        return "Search companies";
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

    @FXML
    private void reload() {
        String search = keyword;
        boolean includeArchived = showArchived.isSelected();
        load(() -> services.companies().search(search, includeArchived), companies -> {
            table.setItems(FXCollections.observableArrayList(companies));
            Tables.setEmptyText(table, search.isEmpty() ? EMPTY : "No companies match your search.");
            updateButtons();
        });
    }

    private void updateButtons() {
        Company selected = table.getSelectionModel().getSelectedItem();
        editButton.setDisable(selected == null);
        archiveButton.setDisable(selected == null);
        websiteButton.setDisable(selected == null || selected.getWebsite() == null);
        archiveButton.setText(selected != null && selected.isArchived() ? "Restore" : "Archive");
    }

    @FXML
    private void addCompany() {
        ctx.dialogs().<Company, CompanyDialogController>form("Add company", "Add company", "company-dialog.fxml",
                dialog -> dialog.configure(null))
                .ifPresent(company -> perform(() -> services.companies().save(company), () -> {
                    ctx.toasts().success(company.getCompanyName() + " added.");
                    reload();
                }, addButton));
    }

    @FXML
    private void editCompany() {
        Company selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        ctx.dialogs().<Company, CompanyDialogController>form("Edit company", "Save changes", "company-dialog.fxml",
                dialog -> dialog.configure(selected))
                .ifPresent(company -> perform(() -> services.companies().save(company), () -> {
                    ctx.toasts().success(company.getCompanyName() + " updated.");
                    reload();
                }));
    }

    @FXML
    private void toggleArchived() {
        Company selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        boolean archiving = !selected.isArchived();
        if (archiving && !ctx.dialogs().confirmDestructive("Archive company?",
                "Archive " + selected.getCompanyName() + "? It is hidden from the list and from new opportunities, "
                        + "and its existing records stay as they are.",
                "Archive company", "Keep company")) {
            return;
        }
        perform(() -> services.companies().setArchived(selected, archiving), () -> {
            ctx.toasts().success(selected.getCompanyName() + (archiving ? " archived." : " restored."));
            reload();
        });
    }

    @FXML
    private void openWebsite() {
        Company selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            ctx.openUrl(selected.getWebsite());
        }
    }
}
