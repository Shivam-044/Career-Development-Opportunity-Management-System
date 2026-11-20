package com.studenttracker.ui.panels;

import com.studenttracker.config.Services;
import com.studenttracker.enums.SkillStatus;
import com.studenttracker.model.Skill;
import com.studenttracker.model.StudentSkill;
import com.studenttracker.service.SkillService;
import com.studenttracker.ui.dialogs.SkillDialog;
import com.studenttracker.ui.support.DataTable;
import com.studenttracker.ui.support.Fields;
import com.studenttracker.ui.support.ListTableModel.Column;
import com.studenttracker.ui.support.Notifier;
import com.studenttracker.ui.support.Theme;
import com.studenttracker.ui.support.Toolbar;
import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public final class SkillsPanel extends FeaturePanel {

    private static final long serialVersionUID = 1L;

    private final transient SkillService service;
    private final JComboBox<SkillStatus> statusFilter = Fields.optionalCombo(SkillStatus.values(), "All statuses");
    private final DataTable<StudentSkill> table = new DataTable<>(List.of(
            new Column<StudentSkill>("Skill", String.class, StudentSkill::getSkillName, 180),
            new Column<StudentSkill>("Proficiency", String.class, skill -> skill.getProficiency().toString(), 110),
            new Column<StudentSkill>("Status", String.class, skill -> skill.getLearningStatus().toString(), 110),
            new Column<StudentSkill>("Notes", String.class, skill -> skill.getNotes() == null ? "" : skill.getNotes(), 420)),
            "No skills yet. Add one to start tracking what you are learning.");
    private final JButton edit = Theme.secondaryButton("Edit");
    private final JButton remove = Theme.secondaryButton("Remove");

    public SkillsPanel(Services services) {
        super("Skills", "Track what you are learning and what you have completed.");
        this.service = services.skills();

        JButton add = Theme.primaryButton("Add skill");
        add.addActionListener(event -> addSkill());
        edit.addActionListener(event -> editSkill());
        remove.addActionListener(event -> removeSkill());
        statusFilter.addActionListener(event -> onShow());
        table.onSelectionChanged(this::updateButtons);
        table.onActivate(this::editSkill);
        updateButtons();

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(new Toolbar().left(new JLabel("Show"), statusFilter).right(remove, edit, add), BorderLayout.NORTH);
        body.add(table, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
    }

    @Override
    public void onShow() {
        SkillStatus filter = (SkillStatus) statusFilter.getSelectedItem();
        load(() -> service.list(filter), skills -> {
            table.setRows(skills);
            table.setEmptyMessage(filter == null
                    ? "No skills yet. Add one to start tracking what you are learning."
                    : "No skills with this status.");
        });
    }

    private void updateButtons() {
        boolean selected = table.selected().isPresent();
        edit.setEnabled(selected);
        remove.setEnabled(selected);
    }

    private void addSkill() {
        load(() -> service.catalog().stream().map(Skill::getSkillName).toList(), names ->
                new SkillDialog(this, names, null).showDialog().ifPresent(skill ->
                        perform(() -> service.add(skill), this::onShow)));
    }

    private void editSkill() {
        table.selected().ifPresent(selected ->
                new SkillDialog(this, List.of(), selected).showDialog().ifPresent(skill ->
                        perform(() -> service.update(skill), this::onShow)));
    }

    private void removeSkill() {
        table.selected().ifPresent(selected -> {
            boolean confirmed = Notifier.confirm(this, "Remove skill?",
                    "Remove " + selected.getSkillName() + " from your skills? This can't be undone.",
                    "Remove skill", "Keep skill");
            if (confirmed) {
                perform(() -> service.remove(selected), this::onShow);
            }
        });
    }
}
