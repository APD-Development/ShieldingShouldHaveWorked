# UIMSD Markdown cell escaping audit — October 16, 2026

## Supported defect

core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/apd/report/ReportRenderer.java line75 currently escapes a pipe before escaping or preserving existing backslashes:

    return s.replace("|", "\\|").replace("\n", " ");

For an existing value containing backslash-plus-pipe (a\|b), the renderer emits two backslashes followed by a pipe. Markdown consumes the slash pair as a literal backslash, leaving the pipe available as a table delimiter. The exported display can therefore gain an unintended column. This is a rendering defect; raw history, query data, caches and scores are not changed.

## Bounded correction

Escape existing backslashes first, then pipes, then preserve the established newline-to-space behavior:

    return s.replace("\\", "\\\\").replace("|", "\\|").replace("\n", " ");

Add a permanent regression that renders a table cell containing a\|b and proves the output remains one Markdown cell after parsing or structural inspection. Retain cases for ordinary pipes, existing backslashes, multiple lines, empty or unknown values and CSV behavior. Do not migrate stored data or rewrite historical exports.

## Status and next safe step

The source defect and bounded fix are identified, but no production commit or passing regression is claimed in this record. A previous prepared change used an obsolete parent and was not pushed after the branch advanced; it must not be treated as tested. Implement source plus regression on the latest real parent, run focused report/export tests and the appropriate broader core gate, then request a new APK only if application source actually changes. This work is not inside TEST258.
