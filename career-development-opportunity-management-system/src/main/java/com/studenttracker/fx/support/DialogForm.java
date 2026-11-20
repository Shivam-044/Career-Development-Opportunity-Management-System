package com.studenttracker.fx.support;

/** Implemented by the controller of a form dialog; the dialog service supplies the surrounding chrome. */
public interface DialogForm<T> {

    /**
     * Reads the fields and returns the result.
     *
     * @throws com.studenttracker.exception.ValidationException if the input is not acceptable; the
     *         message is shown inside the dialog
     */
    T collect();

    /** Called once the dialog is on screen so the first field can take focus. */
    default void focusFirstField() {
    }
}
