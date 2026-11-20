package com.studenttracker.fx.support;

import java.util.Comparator;
import java.util.Locale;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

/** Builders that keep the table definitions in the controllers short and the tables consistent. */
public final class Tables {

    private Tables() {
    }

    public static <T> void configure(TableView<T> table, String emptyText) {
        table.getStyleClass().add("data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        setEmptyText(table, emptyText);
    }

    public static <T> void setEmptyText(TableView<T> table, String emptyText) {
        Label placeholder = new Label(emptyText);
        placeholder.getStyleClass().add("table-placeholder");
        placeholder.setWrapText(true);
        table.setPlaceholder(placeholder);
    }

    /** A column that shows plain text; blanks stay blank. */
    public static <T> TableColumn<T, String> text(String title, double width, Function<T, String> value) {
        return column(title, width, row -> {
            String text = value.apply(row);
            return text == null ? "" : text;
        }, text -> text);
    }

    /** A typed column: sorts by the value itself and displays it through the formatter. */
    public static <T, C> TableColumn<T, C> column(String title, double width, Function<T, C> value,
                                                  Function<C, String> format) {
        TableColumn<T, C> column = new TableColumn<>(title.toUpperCase(Locale.ROOT));
        column.setPrefWidth(width);
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(value.apply(cell.getValue())));
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(C item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : format.apply(item));
            }
        });
        return column;
    }

    /** A column that shows a node (badge, bar) per row; {@code order} decides how it sorts. */
    public static <T> TableColumn<T, T> nodes(String title, double width, Function<T, Node> graphic,
                                              Comparator<T> order) {
        TableColumn<T, T> column = new TableColumn<>(title.toUpperCase(Locale.ROOT));
        column.setPrefWidth(width);
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        column.setComparator(order);
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                setGraphic(empty || item == null ? null : graphic.apply(item));
            }
        });
        return column;
    }

    /** Adds a style class to rows for which the function returns one (for example to flag urgent rows). */
    public static <T> void styleRows(TableView<T> table, Function<T, String> styleClass) {
        table.setRowFactory(view -> new TableRow<>() {
            private String applied;

            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (applied != null) {
                    getStyleClass().remove(applied);
                    applied = null;
                }
                if (!empty && item != null) {
                    String name = styleClass.apply(item);
                    if (name != null) {
                        getStyleClass().add(name);
                        applied = name;
                    }
                }
            }
        });
    }
}
