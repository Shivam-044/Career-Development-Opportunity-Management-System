package com.studenttracker.ui.panels;

import com.studenttracker.config.Services;
import com.studenttracker.model.Company;
import com.studenttracker.service.CompanyService;
import com.studenttracker.ui.dialogs.CompanyDialog;
import com.studenttracker.ui.support.DataTable;
import com.studenttracker.ui.support.ListTableModel.Column;
import com.studenttracker.ui.support.Notifier;
import com.studenttracker.ui.support.SearchField;
import com.studenttracker.ui.support.Theme;
import com.studenttracker.ui.support.Toolbar;
import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

public final class CompaniesPanel extends FeaturePanel {

    private static final long serialVersionUID = 1L;
    private static final String EMPTY = "No companies yet. Add the companies you want to track.";

    private final transient CompanyService service;
    private final SearchField search = new SearchField("Search companies", this::onShow);
    private final JCheckBox showArchived = new JCheckBox("Show archived");
    private final DataTable<Company> table = new DataTable<>(List.of(
            new Column<Company>("Company", String.class, Company::getCompanyName, 200),
            new Column<Company>("Industry", String.class, company -> nullToEmpty(company.getIndustry()), 140),
            new Column<Company>("Location", String.class, company -> nullToEmpty(company.getLocation()), 140),
            new Column<Company>("Website", String.class, company -> nullToEmpty(company.getWebsite()), 220),
            new Column<Company>("Status", String.class, company -> company.isArchived() ? "Archived" : "Active", 80)),
            EMPTY);
    private final JButton edit = Theme.secondaryButton("Edit");
    private final JButton archive = Theme.secondaryButton("Archive");

    public CompaniesPanel(Services services) {
        super("Companies", "Companies you are interested in. Each can have several opportunities.");
        this.service = services.companies();

        JButton add = Theme.primaryButton("Add company");
        add.addActionListener(event -> addCompany());
        edit.addActionListener(event -> editCompany());
        archive.addActionListener(event -> toggleArchived());
        showArchived.setOpaque(false);
        showArchived.addActionListener(event -> onShow());
        table.onSelectionChanged(this::updateButtons);
        table.onActivate(this::editCompany);
        updateButtons();

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(new Toolbar().left(search, showArchived).right(archive, edit, add), BorderLayout.NORTH);
        body.add(table, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
    }

    @Override
    public void onShow() {
        String keyword = search.query();
        boolean includeArchived = showArchived.isSelected();
        load(() -> service.search(keyword, includeArchived), companies -> {
            table.setRows(companies);
            table.setEmptyMessage(keyword.isEmpty() ? EMPTY : "No companies match your search.");
        });
    }

    private void updateButtons() {
        table.selected().ifPresentOrElse(company -> {
            edit.setEnabled(true);
            archive.setEnabled(true);
            archive.setText(company.isArchived() ? "Restore" : "Archive");
        }, () -> {
            edit.setEnabled(false);
            archive.setEnabled(false);
            archive.setText("Archive");
        });
    }

    private void addCompany() {
        new CompanyDialog(this, null).showDialog().ifPresent(company ->
                perform(() -> service.save(company), this::onShow));
    }

    private void editCompany() {
        table.selected().ifPresent(selected ->
                new CompanyDialog(this, selected).showDialog().ifPresent(company ->
                        perform(() -> service.save(company), this::onShow)));
    }

    private void toggleArchived() {
        table.selected().ifPresent(company -> {
            boolean archiving = !company.isArchived();
            if (archiving && !Notifier.confirm(this, "Archive company?",
                    "Archive " + company.getCompanyName() + "? It is hidden from the list and from new "
                            + "opportunities, and its existing records stay as they are.",
                    "Archive company", "Keep company")) {
                return;
            }
            perform(() -> service.setArchived(company, archiving), this::onShow);
        });
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
