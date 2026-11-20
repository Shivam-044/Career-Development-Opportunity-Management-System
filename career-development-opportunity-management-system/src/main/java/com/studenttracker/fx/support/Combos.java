package com.studenttracker.fx.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public final class Combos {

    private Combos() {
    }

    public static <E> void fill(ComboBox<E> combo, E[] values) {
        combo.setItems(FXCollections.observableArrayList(values));
    }

    /** Fills the combo with the values plus a leading entry labelled {@code emptyLabel} that reads back as null. */
    public static <E> void fillOptional(ComboBox<E> combo, E[] values, String emptyLabel) {
        List<E> items = new ArrayList<>();
        items.add(null);
        items.addAll(Arrays.asList(values));
        combo.setItems(FXCollections.observableArrayList(items));

        Callback<ListView<E>, ListCell<E>> cells = view -> new ListCell<>() {
            @Override
            protected void updateItem(E item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? emptyLabel : item.toString());
            }
        };
        combo.setCellFactory(cells);
        combo.setButtonCell(cells.call(null));
        combo.getSelectionModel().selectFirst();
    }
}
