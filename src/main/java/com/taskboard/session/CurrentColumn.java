package com.taskboard.session;

import com.taskboard.model.Column;

public class CurrentColumn {
    private static Column column;
    public static void set(Column c) { column = c; }
    public static Column get() { return column; }
    public static void clear() { column = null; }
}
