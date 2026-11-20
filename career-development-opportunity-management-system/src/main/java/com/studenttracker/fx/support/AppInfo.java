package com.studenttracker.fx.support;

public final class AppInfo {

    public static final String NAME = "CareerForge";
    public static final String VERSION = "1.0.0";
    public static final String RESOURCE_ROOT = "/com/studenttracker/fx/";

    private AppInfo() {
    }

    public static String javafxVersion() {
        String version = System.getProperty("javafx.runtime.version");
        return version == null ? "unknown" : version;
    }
}
