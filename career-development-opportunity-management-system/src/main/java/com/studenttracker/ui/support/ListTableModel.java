package com.studenttracker.ui.support;

import java.util.List;
import java.util.function.Function;
import javax.swing.table.AbstractTableModel;

/** Read-only table model over a list of records, described by a list of columns. */
public class ListTableModel<T> extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    public record Column<R>(String title, Class<?> type, Function<R, Object> value, int width) {
    }

    private final transient List<Column<T>> columns;
    private transient List<T> rows = List.of();

    public ListTableModel(List<Column<T>> columns) {
        this.columns = List.copyOf(columns);
    }

    public void setRows(List<T> newRows) {
        this.rows = List.copyOf(newRows);
        fireTableDataChanged();
    }

    public T rowAt(int modelRow) {
        return rows.get(modelRow);
    }

    public List<Column<T>> columns() {
        return columns;
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public String getColumnName(int column) {
        return columns.get(column).title();
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return columns.get(column).type();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return columns.get(columnIndex).value().apply(rows.get(rowIndex));
    }
}
