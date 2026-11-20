package com.studenttracker.controller;

import com.studenttracker.enums.OpportunityStatus;
import com.studenttracker.model.Opportunity;
import com.studenttracker.service.OpportunityService;
import com.studenttracker.ui.support.BackgroundTask;
import com.studenttracker.ui.support.Notifier;
import com.studenttracker.util.Dates;
import java.awt.Component;

/** Handles the prompts around saving and re-statusing opportunities. */
public final class OpportunityController {

    private final OpportunityService service;
    private final Component owner;

    public OpportunityController(OpportunityService service, Component owner) {
        this.service = service;
        this.owner = owner;
    }

    /** Saves the opportunity, first asking what to do if it is open with a deadline in the past. */
    public void save(Opportunity opportunity, Runnable onSaved) {
        boolean keepOpen = false;
        if (service.hasPassedDeadline(opportunity)) {
            int choice = Notifier.choose(owner, "Deadline has passed",
                    "The deadline for " + opportunity.getRoleTitle() + " was "
                            + Dates.display(opportunity.getDeadline())
                            + ". Do you want to keep the listing open for your records, or mark it as closed?",
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
        BackgroundTask.perform(() -> service.save(opportunity, keepPastDeadlineOpen), onSaved,
                error -> Notifier.error(owner, error));
    }

    public void changeStatus(Opportunity opportunity, OpportunityStatus target, Runnable onChanged) {
        if (target == OpportunityStatus.ARCHIVED) {
            boolean confirmed = Notifier.confirm(owner, "Archive this listing?",
                    "Archive " + opportunity.getRoleTitle() + " at " + opportunity.getCompanyName()
                            + "? It leaves your active lists, and any applications you made stay on record.",
                    "Archive listing", "Keep listing");
            if (!confirmed) {
                return;
            }
        }
        BackgroundTask.perform(() -> service.changeStatus(opportunity, target), onChanged,
                error -> Notifier.error(owner, error));
    }
}
