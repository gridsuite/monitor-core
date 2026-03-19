/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.DeltaType;
import com.github.difflib.patch.Patch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Radouane KHOUADRI {@literal <redouane.khouadri_externe at rte-france.com>}
 */
public class DiffUtilsCompare {
    private ObjectMapper OBJECT_MAPPER;
    public DiffUtilsCompare(ObjectMapper objectMapper) {
        this.OBJECT_MAPPER = objectMapper;
    }

    public String compare(Object conf1, Object conf2) throws JsonProcessingException {
        List<String> leftLines = toPrettyJsonLines(conf1);
        List<String> rightLines = toPrettyJsonLines(conf2);

        Patch<String> patch = DiffUtils.diff(leftLines, rightLines);
        if (patch.getDeltas().isEmpty()) {
            System.out.println("No differences");
            return null;
        } else {
            var diff = String.join("\n",
                    UnifiedDiffUtils.generateUnifiedDiff(
                            null,
                            null,
                            leftLines,
                            patch,
                            3
                    )
            );

            System.out.println(diff);

            return renderUnifiedDiffAsHtml(diff);
        }
    }

    private List<String> toPrettyJsonLines(Object value) throws JsonProcessingException {
        String json = OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        return Arrays.asList(json.split("\\R"));
    }

    public String renderUnifiedDiffAsHtml(String unifiedDiff) {
        StringBuilder html = new StringBuilder();

        html.append("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <title>Process Config Diff</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        margin: 24px;
                        background-color: #ffffff;
                        color: #24292f;
                    }
                    .diff-container {
                        font-family: monospace;
                        white-space: pre-wrap;
                        border: 1px solid #d0d7de;
                        border-radius: 6px;
                        overflow: hidden;
                    }
                    .diff-line {
                        padding: 2px 12px;
                    }
                    .diff-header {
                        background-color: #f6f8fa;
                        color: #57606a;
                    }
                    .diff-hunk {
                        background-color: #ddf4ff;
                        color: #0969da;
                    }
                    .diff-added {
                        background-color: #dafbe1;
                        color: #1a7f37;
                    }
                    .diff-removed {
                        background-color: #ffebe9;
                        color: #cf222e;
                    }
                    .diff-context {
                        background-color: #ffffff;
                        color: #24292f;
                    }
                    .diff-empty {
                        color: #8c959f;
                        font-style: italic;
                        padding: 12px;
                    }
                </style>
            </head>
            <body>
                <div class="diff-container">
            """);

        if ("No differences".equals(unifiedDiff)) {
            html.append("<div class=\"diff-empty\">No differences</div>");
        } else {
            String[] lines = unifiedDiff.split("\\R", -1);
            for (String line : lines) {
                html.append("<div class=\"diff-line ")
                        .append(resolveCssClass(line))
                        .append("\">")
                        .append(escapeHtml(line))
                        .append("</div>");
            }
        }

        html.append("""
                </div>
            </body>
            </html>
            """);

        return html.toString();
    }

    private String resolveCssClass(String line) {
        if (line.startsWith("---") || line.startsWith("+++")) {
            return "diff-header";
        }
        if (line.startsWith("@@")) {
            return "diff-hunk";
        }
        if (line.startsWith("+")) {
            return "diff-added";
        }
        if (line.startsWith("-")) {
            return "diff-removed";
        }
        return "diff-context";
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }


    public String compareSideBySide(Object conf1, Object conf2) throws JsonProcessingException {
        List<String> leftLines = toPrettyJsonLines(conf1);
        List<String> rightLines = toPrettyJsonLines(conf2);

        Patch<String> patch = DiffUtils.diff(leftLines, rightLines);
        if (patch.getDeltas().isEmpty()) {
            return null;
        }
        return renderSideBySideDiffAsHtml(leftLines, rightLines, patch);
    }

    public String renderSideBySideDiffAsHtml(List<String> leftLines, List<String> rightLines, Patch<String> patch) {
        // Build a per-line tag map: index -> "removed" | "added" | "changed" | "context"
        String[] leftTags = new String[leftLines.size()];
        String[] rightTags = new String[rightLines.size()];
        Arrays.fill(leftTags, "context");
        Arrays.fill(rightTags, "context");

        for (AbstractDelta<String> delta : patch.getDeltas()) {
            int leftPos  = delta.getSource().getPosition();
            int rightPos = delta.getTarget().getPosition();
            DeltaType type = delta.getType();

            switch (type) {
                case DELETE -> {
                    for (int i = 0; i < delta.getSource().size(); i++) leftTags[leftPos + i] = "removed";
                }
                case INSERT -> {
                    for (int i = 0; i < delta.getTarget().size(); i++) rightTags[rightPos + i] = "added";
                }
                case CHANGE -> {
                    for (int i = 0; i < delta.getSource().size(); i++) leftTags[leftPos + i]  = "changed";
                    for (int i = 0; i < delta.getTarget().size(); i++) rightTags[rightPos + i] = "changed";
                }
            }
        }

        // Align both sides into rows
        record Row(String left, String leftTag, String right, String rightTag) {}
        List<Row> rows = new ArrayList<>();

        int l = 0, r = 0;
        for (AbstractDelta<String> delta : patch.getDeltas()) {
            // flush shared context before this delta
            while (l < delta.getSource().getPosition()) {
                rows.add(new Row(leftLines.get(l), "context", rightLines.get(r), "context"));
                l++; r++;
            }
            int srcSize = delta.getSource().size();
            int tgtSize = delta.getTarget().size();
            int maxSize = Math.max(srcSize, tgtSize);
            for (int i = 0; i < maxSize; i++) {
                String lText  = i < srcSize ? leftLines.get(delta.getSource().getPosition() + i)  : "";
                String rText  = i < tgtSize ? rightLines.get(delta.getTarget().getPosition() + i) : "";
                String lTag   = i < srcSize ? leftTags[delta.getSource().getPosition() + i]  : "empty";
                String rTag   = i < tgtSize ? rightTags[delta.getTarget().getPosition() + i] : "empty";
                rows.add(new Row(lText, lTag, rText, rTag));
            }
            l += srcSize;
            r += tgtSize;
        }
        // flush remaining context
        while (l < leftLines.size()) {
            rows.add(new Row(leftLines.get(l), "context", rightLines.get(r), "context"));
            l++; r++;
        }

        // Render HTML
        StringBuilder html = new StringBuilder();
        html.append("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <title>Side-by-Side Config Diff</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 24px; background: #fff; color: #24292f; }
                    table { width: 100%; border-collapse: collapse; font-family: monospace; font-size: 13px; }
                    th { background: #f6f8fa; padding: 6px 12px; border: 1px solid #d0d7de; text-align: left; }
                    td { padding: 2px 12px; border: 1px solid #d0d7de; white-space: pre-wrap; vertical-align: top; }
                    .context  { background: #ffffff; }
                    .removed  { background: #ffebe9; color: #cf222e; }
                    .added    { background: #dafbe1; color: #1a7f37; }
                    .changed  { background: #fff8c5; color: #9a6700; }
                    .empty    { background: #f6f8fa; }
                </style>
            </head>
            <body>
                <table>
                    <tr><th>Config 1</th><th>Config 2</th></tr>
            """);

        for (Row row : rows) {
            html.append("<tr>")
                    .append("<td class=\"").append(row.leftTag()).append("\">").append(escapeHtml(row.left())).append("</td>")
                    .append("<td class=\"").append(row.rightTag()).append("\">").append(escapeHtml(row.right())).append("</td>")
                    .append("</tr>\n");
        }

        html.append("""
                </table>
            </body>
            </html>
            """);

        return html.toString();
    }

}
