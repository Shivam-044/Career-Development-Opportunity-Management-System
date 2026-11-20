package com.studenttracker.ui.panels;

import com.studenttracker.config.Services;
import com.studenttracker.controller.ApplicationController;
import com.studenttracker.controller.OpportunityController;
import com.studenttracker.enums.OpportunityStatus;
import com.studenttracker.model.Company;
import com.studenttracker.model.Opportunity;
import com.studenttracker.service.CompanyService;
import com.studenttracker.service.OpportunityService;
import com.studenttracker.ui.dialogs.OpportunityDialog;
import com.studenttracker.ui.support.DataTable;
import com.studenttracker.ui.support.Fields;
import com.studenttracker.ui.support.ListTableModel.Column;
import com.studenttracker.ui.support.Notifier;
import com.studenttracker.ui.support.SearchField;
import com.studenttracker.ui.support.Theme;
import com.studenttracker.ui.support.Toolbar;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

public final class OpportunitiesPanel extends FeaturePanel {

    private static final long serialVersionUID = 1L;
    private static final String EMPTY = "No opportunities here yet. Save a role you want to apply for.";

    private final transient OpportunityService service;
    private final transient CompanyService companies;
    private final transient OpportunityController controller;
    private final transient ApplicationController applications;

    private final SearchField search = new SearchField("Search role, company or location", this::onShow);
    private final JComboBox<OpportunityStatus> statusFilter =
            Fields.optionalCombo(OpportunityStatus.values(), "All listings");
    private final DataTable<Opportunity> table = new DataTable<>(List.of(
            new Column<Opportunity>("Company", String.class, Opportunity::getCompanyName, 150),
            new Column<Opportunity>("Role", String.class, Opportunity::getRoleTitle, 190),
            new Column<Opportunity>("Type", String.class, opportunity -> opportunity.getType().toString(), 80),
            new Column<Opportunity>("Location", String.class, opportunity -> text(opportunity.getLocation()), 110),
            new Column<Opportunity>("Work mode", String.class,
                    opportunity -> opportunity.getWorkMode() == null ? "" : opportunity.getWorkMode().toString(), 80),
            new Column<Opportunity>("Deadline", LocalDate.class, Opportunity::getDeadline, 100),
            new Column<Opportunity>("Min CGPA", String.class, opportunity -> cgpa(opportunity.getMinimumCgpa()), 70),
            new Column<Opportunity>("Required skills", String.class,
                    opportunity -> String.join(", ", opportunity.getRequiredSkills()), 160),
            new Column<Opportunity>("Status", String.class, opportunity -> opportunity.getStatus().toString(), 80)),
            EMPTY);

    private final JButton apply = Theme.primaryButton("Mark as applied");
    private final JButton edit = Theme.secondaryButton("Edit");
    private final JButton openLink = Theme.secondaryButton("Open listing");
    private final JButton archive = Theme.secondaryButton("Archive");

    public OpportunitiesPanel(Services services) {
        super("Opportunities", "Jobs and internships you have saved. Mark one as applied to start tracking it.");
        this.service = services.opportunities();
        this.companies = services.companies();
        this.controller = new OpportunityController(service, this);
        this.applications = new ApplicationController(services.applications(), this);

        statusFilter.setSelectedItem(OpportunityStatus.OPEN);
        statusFilter.addActionListener(event -> onShow());

        JButton add = Theme.secondaryButton("Add opportunity");
        add.addActionListener(event -> addOpportunity());
        edit.addActionListener(event -> editOpportunity());
        apply.addActionListener(event -> markApplied());
        openLink.addActionListener(event -> openSourceLink());
        archive.addActionListener(event -> toggleArchived());
        table.onSelectionChanged(this::updateButtons);
        table.onActivate(this::editOpportunity);
        updateButtons();

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(new Toolbar().left(search, statusFilter).right(archive, openLink, edit, add, apply),
                BorderLayout.NORTH);
        body.add(table, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
    }

    @Override
    public void onShow() {
        String keyword = search.query();
        OpportunityStatus status = (OpportunityStatus) statusFilter.getSelectedItem();
        load(() -> service.search(keyword, status), opportunities -> {
            table.setRows(opportunities);
            table.setEmptyMessage(keyword.isEmpty() && status == OpportunityStatus.OPEN
                    ? EMPTY : keyword.isEmpty() ? "No listings with this status." : "No opportunities match your search.");
        });
    }

    private void updateButtons() {
        table.selected().ifPresentOrElse(opportunity -> {
            apply.setEnabled(opportunity.getStatus() == OpportunityStatus.OPEN);
            edit.setEnabled(true);
            openLink.setEnabled(opportunity.getSourceUrl() != null);
            archive.setEnabled(true);
            archive.setText(opportunity.getStatus() == OpportunityStatus.ARCHIVED ? "Reopen" : "Archive");
        }, () -> {
            apply.setEnabled(false);
            edit.setEnabled(false);
            openLink.setEnabled(false);
            archive.setEnabled(false);
            archive.setText("Archive");
        });
    }

    private void addOpportunity() {
        load(() -> companies.search("", false), list -> {
            if (list.isEmpty()) {
                Notifier.info(this, "Add a company first",
                        "Every opportunity belongs to a company. Add one on the Companies screen, then come back.");
                return;
            }
            showEditor(list, null);
        });
    }

    private void editOpportunity() {
        table.selected().ifPresent(selected ->
                load(() -> companies.search("", true), list -> showEditor(list, selected)));
    }

    private void showEditor(List<Company> choices, Opportunity editing) {
        new OpportunityDialog(this, choices, editing).showDialog()
                .ifPresent(opportunity -> controller.save(opportunity, this::onShow));
    }

    private void markApplied() {
        table.selected().ifPresent(opportunity -> applications.markApplied(opportunity, () -> {
            Notifier.info(this, "Application saved",
                    "Marked " + opportunity.getRoleTitle() + " at " + opportunity.getCompanyName()
                            + " as applied. You can follow it on the Applications screen.");
            onShow();
        }));
    }

    private void toggleArchived() {
        table.selected().ifPresent(opportunity -> {
            OpportunityStatus target = opportunity.getStatus() == OpportunityStatus.ARCHIVED
                    ? OpportunityStatus.OPEN : OpportunityStatus.ARCHIVED;
            controller.changeStatus(opportunity, target, this::onShow);
        });
    }

    private void openSourceLink() {
        table.selected().ifPresent(opportunity -> {
            try {
                if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Notifier.info(this, "Can't open the link",
                            "This computer has no default browser set up. The address is " + opportunity.getSourceUrl());
                    return;
                }
                Desktop.getDesktop().browse(URI.create(opportunity.getSourceUrl()));
            } catch (IOException | IllegalArgumentException ex) {
                Notifier.info(this, "Can't open the link",
                        "The listing address couldn't be opened. Copy it into your browser instead: "
                                + opportunity.getSourceUrl());
            }
        });
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }

    private static String cgpa(BigDecimal value) {
        return value == null ? "" : value.toPlainString();
    }
}
