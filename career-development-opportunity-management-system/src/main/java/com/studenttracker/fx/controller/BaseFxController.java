package com.studenttracker.fx.controller;

import com.studenttracker.config.Services;
import com.studenttracker.fx.navigation.PageController;
import com.studenttracker.fx.support.FxContext;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.scene.Node;

/** Shared plumbing for page controllers: the context, the services and short helpers for background work. */
abstract class BaseFxController implements PageController {

    protected final FxContext ctx;
    protected final Services services;

    protected BaseFxController(FxContext ctx) {
        this.ctx = ctx;
        this.services = ctx.services();
    }

    /** Runs the work in the background and hands the result to the callback on the JavaFX thread. */
    protected final <T> void load(Supplier<T> work, Consumer<T> onSuccess, Node... guards) {
        ctx.tasks().run(work, onSuccess, guards);
    }

    protected final void perform(Runnable work, Runnable onSuccess, Node... guards) {
        ctx.tasks().runVoid(work, onSuccess, guards);
    }
}
