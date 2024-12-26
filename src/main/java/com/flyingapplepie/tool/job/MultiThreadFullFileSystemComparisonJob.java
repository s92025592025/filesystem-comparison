package com.flyingapplepie.tool.job;

import com.fasterxml.jackson.databind.SequenceWriter;
import com.flyingapplepie.tool.model.ChecksumType;
import com.flyingapplepie.tool.model.ComparedRow;
import com.flyingapplepie.tool.model.FileSystemComparisonSummary;
import lombok.Getter;

import java.nio.file.Path;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicLong;
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
        long startTime = System.currentTimeMillis();
        AtomicLong totalFileCount = new AtomicLong(0);
        AtomicLong totalSameFilesCount = new AtomicLong(0);
        AtomicLong totalDiffFilesCount = new AtomicLong(0);

        try (ForkJoinPool fileComparisonPool = new ForkJoinPool(this.threadCount)) {
            csvWriter.writeAll(
                    fileComparisonPool.submit(() ->
                            ((Stream<ComparedRow>)this.fsStreamPreProcessing(fsStream.parallel()))
                                    .peek(comparedRow -> {
                                        totalFileCount.incrementAndGet();

                                        if (comparedRow.isSame()) {
                                            totalSameFilesCount.incrementAndGet();
                                        } else {
                                            totalDiffFilesCount.incrementAndGet();
                                        }
                                    })
                                    .toList()).get()
            );

        }

        long endTime = System.currentTimeMillis();

        return FileSystemComparisonSummary.builder()
                .totalRuntime(endTime - startTime)
                .totalComparedFileCount(totalFileCount.get())
                .totalSameFilesCount(totalSameFilesCount.get())
                .totalDiffFilesCount(totalDiffFilesCount.get())
                .build();
    }
}
