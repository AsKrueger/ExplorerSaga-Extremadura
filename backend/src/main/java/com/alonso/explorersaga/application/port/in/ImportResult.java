package com.alonso.explorersaga.application.port.in;

public record ImportResult(
        int processed,
        int inserted,
        int updated,
        int skipped,
        int failed
) {}
