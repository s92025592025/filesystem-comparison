package com.flyingapplepie.tool.job;

import com.flyingapplepie.tool.model.ChecksumType;
import com.flyingapplepie.tool.model.ComparedRow;
import com.flyingapplepie.tool.util.FileSha256Calculator;
import lombok.Data;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class FileComparisonJob {
    private final Path mainFsFilePath;
    private final Path referenceFsFilePath;
    private final ChecksumType checksumType;

    public FileComparisonJob(Path mainFsFilePath, Path referenceFsFilePath, ChecksumType checksumType) {
        this.mainFsFilePath = mainFsFilePath;
        this.referenceFsFilePath = referenceFsFilePath;
        this.checksumType = checksumType;
    }

    public ComparedRow executeComparison() {
        Map<Path, FileSha256Calculator> processingBucket = new HashMap<>();
        processingBucket.put(this.mainFsFilePath, new FileSha256Calculator(this.mainFsFilePath));
        processingBucket.put(this.referenceFsFilePath, new FileSha256Calculator(this.referenceFsFilePath));

        Map<Path, String> processedChecksums = processingBucket.entrySet().parallelStream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getChecksumString()
                ));

        ComparedRow comparedRow = new ComparedRow();
        comparedRow.setFilePath(this.mainFsFilePath.toAbsolutePath().toString());
        comparedRow.setMainSystemChecksum(processedChecksums.get(this.mainFsFilePath));
        comparedRow.setComparisonSystemChecksum(processedChecksums.get(this.referenceFsFilePath));
        comparedRow.setSame(comparedRow.getMainSystemChecksum().equals(comparedRow.getComparisonSystemChecksum()));

        return comparedRow;
    }
}
