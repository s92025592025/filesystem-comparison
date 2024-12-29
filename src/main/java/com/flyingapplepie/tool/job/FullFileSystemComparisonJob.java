package com.flyingapplepie.tool.job;

import com.fasterxml.jackson.databind.SequenceWriter;
import com.flyingapplepie.tool.model.ChecksumType;
import com.flyingapplepie.tool.model.FileSystemComparisonSummary;
import lombok.Getter;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.stream.Stream;

@Getter
public abstract class FullFileSystemComparisonJob {
    private final Path mainFileSystemBasePath;
    private final Path referenceFileSystemBasePath;
    private final ChecksumType checksumType;

    /**
     * @param mainFileSystemBasePath Main File System Base Path to perform the comparison on
     * @param referenceFileSystemBasePath Reference File System Base Path to compare with {@param mainFileSystemBasePath}
     * @param checksumType The {@link ChecksumType} to use for content comparison
     */
    public FullFileSystemComparisonJob(
            Path mainFileSystemBasePath,
            Path referenceFileSystemBasePath,
            ChecksumType checksumType
    ) {
        this.mainFileSystemBasePath = mainFileSystemBasePath;
        this.referenceFileSystemBasePath = referenceFileSystemBasePath;
        this.checksumType = checksumType;
    }

    /**
     * @param fsStream A {@link Stream<Path>} which contains all the files paths needed to be compared in the main file
     *                 system.
     * @param csvWriter The pre-configured {@link SequenceWriter} for writing resulting into a targeted file. Write need
     *                  to be writing for {@link com.flyingapplepie.tool.model.ComparedRow}
     * @return A {@link FileSystemComparisonSummary} with the comparison summary
     */
    public abstract FileSystemComparisonSummary executeComparison(Stream<Path> fsStream, SequenceWriter csvWriter) throws Exception;


    /**
     * Helper applying a setup of intermediate steps on the passed in stream. Will be called by {@link #executeComparison(Stream, SequenceWriter)}
     * before the comparison actual happens
     * @param fsStream A {@link Stream<Path>} which contains all the files paths needed to be compared in the main file
     *                  system.
     * @return An intermediate {@link Stream} with the following applied in sequence
     *          1. apply filter where {@link java.nio.file.Files#isRegularFile(Path, LinkOption...)}
     *          2. find the relative path relative from {@link FullFileSystemComparisonJob#getMainFileSystemBasePath()}
     *          3. create {@link FileComparisonJob} for both main and relative file system
     */
    protected Stream fsStreamPreProcessing(Stream<Path> fsStream) {
        return fsStream.filter(Files::isRegularFile)
                .map(this.mainFileSystemBasePath::relativize)
                .map(relativeFilePath -> new FileComparisonJob(this.getMainFileSystemBasePath().resolve(relativeFilePath), this.getReferenceFileSystemBasePath().resolve(relativeFilePath), ChecksumType.SHA256))
                .map(FileComparisonJob::executeComparison);
    }
}
