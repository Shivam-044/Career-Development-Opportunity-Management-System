package com.studenttracker.fx.navigation;

/** A page that filters its list from the search box in the header. */
public interface Searchable {

    String searchPrompt();

    void applySearch(String text);
}
