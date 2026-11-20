package com.studenttracker.ui.support;

import com.studenttracker.util.Dates;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;

/** A scrollable, sortable table that shows a friendly message when it has no rows. */
public final class DataTable<T> extends JScrollPane {

    private static final long serialVersionUID = 1L;

    private final transient ListTableModel<T> model;
    private final JTable table;
    private String emptyMessage;

    public DataTable(List<ListTableModel.Column<T>> columns, String emptyMessage) {
        this.model = new ListTableModel<>(columns);
        this.emptyMessage = emptyMessage;
        this.table = new JTable(model) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintEmptyMessage(g, this);
            }
        };
        configure(columns);
        setViewportView(table);
        setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        getViewport().setBackground(Theme.SURFACE);
    }

    private void configure(List<ListTableModel.Column<T>> columns) {
        table.setRowHeight(32);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(Theme.BORDER);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(Theme.SELECTION);
        table.setSelectionForeground(Theme.TEXT);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(Theme.font(Font.BOLD, 12f));

        table.setDefaultRenderer(LocalDate.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void setValue(Object value) {
                setText(value == null ? "" : Dates.display((LocalDate) value));
            }
        });
        table.setDefaultRenderer(LocalDateTime.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void setValue(Object value) {
                setText(value == null ? "" : Dates.display((LocalDateTime) value));
            }
        });

        for (int i = 0; i < columns.size(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(columns.get(i).width());
        }
    }

    private void paintEmptyMessage(Graphics g, JTable target) {
        if (target.getRowCount() > 0 || emptyMessage == null || emptyMessage.isEmpty()) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(Theme.MUTED);
        g2.setFont(Theme.font(Font.PLAIN, 14f));
        FontMetrics metrics = g2.getFontMetrics();
        int x = (target.getWidth() - metrics.stringWidth(emptyMessage)) / 2;
        int y = Math.max(target.getHeight() / 3, metrics.getHeight() * 2);
        g2.drawString(emptyMessage, Math.max(x, 12), y);
        g2.dispose();
    }

    public void setRows(List<T> rows) {
        model.setRows(rows);
    }

    public void setEmptyMessage(String message) {
        this.emptyMessage = message;
        table.repaint();
    }

    public Optional<T> selected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return Optional.empty();
        }
        return Optional.of(model.rowAt(table.convertRowIndexToModel(viewRow)));
    }

    public void onSelectionChanged(Runnable listener) {
        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                listener.run();
            }
        });
    }

    /** Runs the action when a row is double-clicked. */
    public void onActivate(Runnable action) {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2 && table.rowAtPoint(event.getPoint()) >= 0) {
                    action.run();
                }
            }
        });
    }
}
