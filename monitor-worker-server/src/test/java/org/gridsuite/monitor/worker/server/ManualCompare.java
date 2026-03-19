package org.gridsuite.monitor.worker.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ManualCompare {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
            .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
            .registerModule(new Jdk8Module());

    public static String compare(Object conf1, Object conf2) throws JsonProcessingException {
        JsonNode leftNode = OBJECT_MAPPER.valueToTree(conf1);
        JsonNode rightNode = OBJECT_MAPPER.valueToTree(conf2);

        List<DiffEntry> entries = new ArrayList<>();
        compareNodes("$", leftNode, rightNode, entries);

        return buildHtml(entries);
    }

    private static void compareNodes(String path, JsonNode left, JsonNode right, List<DiffEntry> entries)
            throws JsonProcessingException {

        if (left == null && right == null) {
            return;
        }

        if (left == null) {
            entries.add(new DiffEntry(path, DiffType.ADDED, "", formatNode(right)));
            return;
        }

        if (right == null) {
            entries.add(new DiffEntry(path, DiffType.REMOVED, formatNode(left), ""));
            return;
        }

        if (left.getNodeType() != right.getNodeType()) {
            entries.add(new DiffEntry(path, DiffType.TYPE_CHANGED, formatNode(left), formatNode(right)));
            return;
        }

        if (left.isObject()) {
            compareObjects(path, left, right, entries);
            return;
        }

        if (left.isArray()) {
            compareArrays(path, left, right, entries);
            return;
        }

        if (!left.equals(right)) {
            entries.add(new DiffEntry(path, DiffType.CHANGED, formatNode(left), formatNode(right)));
        }
    }

    private static void compareObjects(String path, JsonNode left, JsonNode right, List<DiffEntry> entries)
            throws JsonProcessingException {

        Set<String> fieldNames = new TreeSet<>();
        collectFieldNames(left, fieldNames);
        collectFieldNames(right, fieldNames);

        for (String fieldName : fieldNames) {
            compareNodes(path + "." + fieldName, left.get(fieldName), right.get(fieldName), entries);
        }
    }

    private static void compareArrays(String path, JsonNode left, JsonNode right, List<DiffEntry> entries)
            throws JsonProcessingException {

        int maxSize = Math.max(left.size(), right.size());

        for (int i = 0; i < maxSize; i++) {
            JsonNode leftItem = i < left.size() ? left.get(i) : null;
            JsonNode rightItem = i < right.size() ? right.get(i) : null;
            compareNodes(path + "[" + i + "]", leftItem, rightItem, entries);
        }
    }

    private static void collectFieldNames(JsonNode node, Set<String> fieldNames) {
        if (node == null || !node.isObject()) {
            return;
        }

        Iterator<String> iterator = node.fieldNames();
        while (iterator.hasNext()) {
            fieldNames.add(iterator.next());
        }
    }

    private static String formatNode(JsonNode node) throws JsonProcessingException {
        if (node == null || node.isNull()) {
            return "null";
        }

        if (node.isValueNode()) {
            return node.toString();
        }

        return OBJECT_MAPPER.writeValueAsString(node);
    }

    private static String buildHtml(List<DiffEntry> entries) {
        if (entries.isEmpty()) {
            return """
                    <!DOCTYPE html>
                    <html lang="en">
                    <head>
                        <meta charset="UTF-8">
                        <title>JSON Diff</title>
                        <style>
                            body {
                                font-family: Arial, sans-serif;
                                margin: 24px;
                                background-color: #ffffff;
                                color: #24292f;
                            }
                            .empty {
                                border: 1px solid #d0d7de;
                                border-radius: 6px;
                                padding: 16px;
                                color: #57606a;
                                background-color: #f6f8fa;
                                font-style: italic;
                            }
                        </style>
                    </head>
                    <body>
                        <div class="empty">No differences</div>
                    </body>
                    </html>
                    """;
        }

        StringBuilder html = new StringBuilder();

        html.append("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>JSON Diff</title>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            margin: 24px;
                            background-color: #ffffff;
                            color: #24292f;
                        }
                        table {
                            width: 100%;
                            border-collapse: collapse;
                            table-layout: fixed;
                            font-size: 13px;
                        }
                        th, td {
                            border: 1px solid #d0d7de;
                            vertical-align: top;
                            padding: 8px 10px;
                            text-align: left;
                        }
                        th {
                            background-color: #f6f8fa;
                        }
                        .path {
                            width: 34%;
                            font-family: monospace;
                            background-color: #f6f8fa;
                            word-break: break-word;
                        }
                        .type {
                            width: 10%;
                            font-weight: 600;
                            text-transform: uppercase;
                            font-size: 12px;
                        }
                        .value {
                            width: 28%;
                            font-family: monospace;
                            white-space: pre-wrap;
                            word-break: break-word;
                        }
                        .changed {
                            background-color: #fff8c5;
                            color: #9a6700;
                        }
                        .added {
                            background-color: #dafbe1;
                            color: #1a7f37;
                        }
                        .removed {
                            background-color: #ffebe9;
                            color: #cf222e;
                        }
                        .type-changed {
                            background-color: #ddeaff;
                            color: #0969da;
                        }
                    </style>
                </head>
                <body>
                <table>
                    <thead>
                        <tr>
                            <th class="path">Path</th>
                            <th class="value">Left</th>
                            <th class="value">Right</th>
                        </tr>
                    </thead>
                    <tbody>
                """);

        for (DiffEntry entry : entries) {
            String cssClass = toCssClass(entry.type());

            html.append("<tr>");
            html.append("<td class=\"path\">").append(escapeHtml(entry.path())).append("</td>");
            html.append("<td class=\"value ").append(cssClass).append("\">")
                    .append(escapeHtml(entry.leftValue()))
                    .append("</td>");
            html.append("<td class=\"value ").append(cssClass).append("\">")
                    .append(escapeHtml(entry.rightValue()))
                    .append("</td>");
            html.append("</tr>");
        }

        html.append("""
                    </tbody>
                </table>
                </body>
                </html>
                """);

        return html.toString();
    }

    private static String toCssClass(DiffType type) {
        return switch (type) {
            case ADDED -> "added";
            case REMOVED -> "removed";
            case CHANGED -> "changed";
            case TYPE_CHANGED -> "type-changed";
        };
    }

    private static String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private enum DiffType {
        ADDED("added"),
        REMOVED("removed"),
        CHANGED("changed"),
        TYPE_CHANGED("type changed");

        private final String label;

        DiffType(String label) {
            this.label = label;
        }
    }

    private record DiffEntry(String path, DiffType type, String leftValue, String rightValue) {
    }
}
