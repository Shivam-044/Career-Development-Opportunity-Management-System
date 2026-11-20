package com.studenttracker.fx.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** A section heading inside a page: a title, an optional subtitle and a slot for actions on the right. */
public class PageHeader extends HBox {

    private final Label titleLabel = new Label();
    private final Label subtitleLabel = new Label();
    private final HBox actions = new HBox(8);

    public PageHeader() {
        getStyleClass().add("page-header");
        setAlignment(Pos.CENTER_LEFT);
        titleLabel.getStyleClass().add("card-title");
        subtitleLabel.getStyleClass().add("muted");
        subtitleLabel.setVisible(false);
        subtitleLabel.setManaged(false);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        getChildren().addAll(new VBox(2, titleLabel, subtitleLabel), spacer, actions);
    }

    public final void setTitle(String title) {
        titleLabel.setText(title);
    }

    public final String getTitle() {
        return titleLabel.getText();
    }

    public final void setSubtitle(String subtitle) {
        subtitleLabel.setText(subtitle);
        boolean present = subtitle != null && !subtitle.isBlank();
        subtitleLabel.setVisible(present);
        subtitleLabel.setManaged(present);
    }

    public final String getSubtitle() {
        return subtitleLabel.getText();
    }

    public HBox getActions() {
        return actions;
    }
}
