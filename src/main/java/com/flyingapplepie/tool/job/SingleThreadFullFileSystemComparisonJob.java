package com.flyingapplepie.tool.job;

import com.fasterxml.jackson.databind.SequenceWriter;
import com.flyingapplepie.tool.model.ChecksumType;
import com.flyingapplepie.tool.model.ComparedRow;
import com.flyingapplepie.tool.model.FileSystemComparisonSummary;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class SingleThreadFullFileSystemComparisonJob extends FullFileSystemComparisonJob{
    public SingleThreadFullFileSystemComparisonJob(
            Path mainFileSystemBasePath,
            Path referenceFileSystemBasePath,
            ChecksumType checksumType
    ) {
        super(mainFileSystemBasePath, referenceFileSystemBasePath, checksumType);
    }

    /**
     * @param fsStream  A {@link Stream<Path>} which contains all the files paths needed to be compared in the main file
     *                  system.
     * @param csvWriter The pre-configured {@link SequenceWriter} for writing resulting into a targeted file. Write need
     *                  to be writing for {@link com.flyingapplepie.tool.model.ComparedRow}
     * @return A {@link FileSystemComparisonSummary} with the comparison summary
     * @throws UncheckedIOException When there are IoException during CVS writes
     */
    @Override
    public FileSystemComparisonSummary executeComparison(Stream<Path> fsStream, SequenceWriter csvWriter) throws UncheckedIOException{
        long startTime = System.currentTimeMillis();
        AtomicLong totalFileCount = new AtomicLong(0);
        AtomicLong totalSameFilesCount = new AtomicLong(0);
        AtomicLong totalDiffFilesCount = new AtomicLong(0);

        ((Stream<ComparedRow>)this.fsStreamPreProcessing(fsStream))
                .peek(comparedRow -> {
                    totalFileCount.incrementAndGet();

                    if (comparedRow.isSame()) {
                        totalSameFilesCount.incrementAndGet();
                    } else {
                        totalDiffFilesCount.incrementAndGet();
                    }
                })
                .forEach(comparedRow -> {
                    try {
                        csvWriter.write(comparedRow);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });

        long endTime = System.currentTimeMillis();

        return FileSystemComparisonSummary.builder()
                .totalRuntime(endTime - startTime)
                .totalComparedFileCount(totalFileCount.get())
                .totalSameFilesCount(totalSameFilesCount.get())
                .totalDiffFilesCount(totalDiffFilesCount.get())
                .build();
    }
}
