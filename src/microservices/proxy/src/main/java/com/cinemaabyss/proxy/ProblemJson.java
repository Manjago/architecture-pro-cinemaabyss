package com.cinemaabyss.proxy;

public class ProblemJson {
    
    public static String create(int status, String title, String detail) {
        return String.format("""
                             {
                               "type": "about:blank",
                               "title": "%s",
                               "status": %d,
                               "detail": "%s"
                             }""",
            escapeJson(title),
            status,
            escapeJson(detail)
        );
    }
    
    private static String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
}