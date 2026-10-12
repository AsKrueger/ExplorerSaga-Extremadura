package com.alonso.explorersaga.application.port.in;

public record ImportResult(
        int processed,
        int inserted,
        int skipped,
        int failed
) {}
