package com.recipez.util;

/** Tiny replacement for whatever Log class your old project had. */
public class Log {
    public static void info(String message)    { System.out.println("[INFO] "  + message); }
    public static void warning(String message) { System.err.println("[WARN] "  + message); }
    public static void error(String message)   { System.err.println("[ERROR] " + message); }
}
