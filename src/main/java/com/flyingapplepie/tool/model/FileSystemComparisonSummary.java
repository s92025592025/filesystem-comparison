package com.flyingapplepie.tool.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileSystemComparisonSummary {
    /**
     * Total Runtime in milliseconds
     */
    private final long totalRuntime;

    /**
     * The amount of files from main filesystem is compared
     */
    private final long totalComparedFileCount;

    /**
     * The amount of files from main file system that is the same as reference file system
     */
    private final long totalSameFilesCount;

    /**
     * The amount of files from main file system that is not the same as reference file system
     */
    private final long totalDiffFilesCount;
}
