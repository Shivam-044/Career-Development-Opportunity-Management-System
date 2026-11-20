package com.studenttracker.controller;

import com.studenttracker.enums.ApplicationStatus;
import com.studenttracker.model.JobApplication;
import com.studenttracker.model.Opportunity;
import com.studenttracker.service.ApplicationService;
import com.studenttracker.ui.dialogs.ApplyDialog;
import com.studenttracker.ui.dialogs.NotesDialog;
import com.studenttracker.ui.dialogs.StatusDialog;
import com.studenttracker.ui.support.BackgroundTask;
import com.studenttracker.ui.support.Notifier;
import java.awt.Component;
import java.util.EnumSet;
import java.util.Set;

/** Drives the multi-step flows for applying and for moving an application through its statuses. */
public final class ApplicationController {

    private final ApplicationService service;
    private final Component owner;

    public ApplicationController(ApplicationService service, Component owner) {
        this.service = service;
        this.owner = owner;
    }

    public void markApplied(Opportunity opportunity, Runnable onApplied) {
        BackgroundTask.load(
                () -> service.minimumCgpaWarning(opportunity.getOpportunityId()),
                warning -> {
                    if (warning.isPresent() && !Notifier.confirm(owner, "Below the minimum CGPA",
                            warning.get() + " Do you still want to mark it as applied?",
                            "Apply anyway", "Cancel")) {
                        return;
                    }
                    new ApplyDialog(owner, opportunity).showDialog()
                            .ifPresent(details -> submit(opportunity, details, onApplied));
                },
                error -> Notifier.error(owner, error));
    }

    private void submit(Opportunity opportunity, ApplyDialog.Details details, Runnable onApplied) {
        BackgroundTask.perform(
                () -> service.apply(opportunity.getOpportunityId(), details.appliedOn(), details.notes()),
                onApplied,
                error -> Notifier.error(owner, error));
    }

    public void changeStatus(JobApplication application, Runnable onChanged) {
        Set<ApplicationStatus> next = application.getStatus().nextStatuses();
        if (next.isEmpty()) {
            Notifier.info(owner, "Status is final",
                    "An application marked " + application.getStatus()
                            + " can't move on. If it was set by mistake, use Correct status.");
            return;
        }
        new StatusDialog(owner, "Update status",
                "Only the next steps in the workflow are listed.",
                application.describe() + " (" + application.getStatus() + ")", next)
                .showDialog()
                .ifPresent(target -> BackgroundTask.perform(
                        () -> service.changeStatus(application, target), onChanged,
                        error -> Notifier.error(owner, error)));
    }

    public void correctStatus(JobApplication application, Runnable onChanged) {
        Set<ApplicationStatus> others = EnumSet.complementOf(EnumSet.of(application.getStatus()));
        new StatusDialog(owner, "Correct status",
                "Use this only to fix a mistake. It skips the normal workflow rules.",
                application.describe() + " (" + application.getStatus() + ")", others)
                .showDialog()
                .ifPresent(target -> BackgroundTask.perform(
                        () -> service.correctStatus(application, target), onChanged,
                        error -> Notifier.error(owner, error)));
    }

    public void editNotes(JobApplication application, Runnable onSaved) {
        new NotesDialog(owner, application.describe(), application.getNotes())
                .showDialog()
                .ifPresent(notes -> BackgroundTask.perform(
                        () -> service.updateNotes(application, notes), onSaved,
                        error -> Notifier.error(owner, error)));
    }
}
