package com.taskboard.session;

import com.taskboard.model.Project;

public class CurrentProject {
    private static Project project;
    public static void set(Project p) { project = p; }
    public static Project get() { return project; }
    public static void clear() { project = null; }
}
