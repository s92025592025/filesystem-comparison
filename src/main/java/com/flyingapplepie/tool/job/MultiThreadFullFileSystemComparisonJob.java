package com.flyingapplepie.tool.job;

import com.fasterxml.jackson.databind.SequenceWriter;
import com.flyingapplepie.tool.model.ChecksumType;
import com.flyingapplepie.tool.model.ComparedRow;
import com.flyingapplepie.tool.model.FileSystemComparisonSummary;
import lombok.Getter;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

public class MultiThreadFullFileSystemComparisonJob extends FullFileSystemComparisonJob {
    @Getter
    private final int threadCount;

    /**
     * @param mainFileSystemBasePath      Main File System Base Path to perform the comparison on
     * @param referenceFileSystemBasePath Reference File System Base Path to compare with {@param mainFileSystemBasePath}
     * @param checksumType                The {@link ChecksumType} to use for content comparison
     */
    public MultiThreadFullFileSystemComparisonJob(Path mainFileSystemBasePath, Path referenceFileSystemBasePath, int threadCount, ChecksumType checksumType) {
        super(mainFileSystemBasePath, referenceFileSystemBasePath, checksumType);
        this.threadCount = threadCount;
    }

    @Override
    public FileSystemComparisonSummary executeComparison(Stream<Path> fsStream, SequenceWriter csvWriter) throws Exception {

        try (ForkJoinPool fileComparisonPool = new ForkJoinPool(this.threadCount)) {
            long startTime = System.currentTimeMillis();
            List<ComparedRow> allComparisonResult = fileComparisonPool.submit(() ->
                    ((Stream<ComparedRow>)this.fsStreamPreProcessing(fsStream.parallel()))
                            .toList()).get();
            csvWriter.writeAll(allComparisonResult);

            long endTime = System.currentTimeMillis();

            return FileSystemComparisonSummary.builder()
                    .totalRuntime(endTime - startTime)
                    .totalComparedFileCount(allComparisonResult.size())
                    .totalSameFilesCount(allComparisonResult.stream().filter(ComparedRow::isSame).count())
                    .totalDiffFilesCount(allComparisonResult.stream().filter(row -> !row.isSame()).count())
                    .build();
        }
    }
}
