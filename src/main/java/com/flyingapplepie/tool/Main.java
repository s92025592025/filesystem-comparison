package com.flyingapplepie.tool;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.flyingapplepie.tool.job.FileComparisonJob;
import com.flyingapplepie.tool.model.ChecksumType;
import com.flyingapplepie.tool.model.ComparedRow;
import com.flyingapplepie.tool.util.CommandlineHandler;
import com.flyingapplepie.tool.util.FileSha256Calculator;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        CommandlineHandler commandlineHandler = new CommandlineHandler();

        try {
            CommandLine cmd = commandlineHandler.parseCmdInput(args);

            if (cmd.hasOption("mf") && cmd.hasOption("rf") && cmd.hasOption("o")) {
                Path mainFileSystemBase = new File(cmd.getOptionValue("mf")).toPath();
                Path referenceFileSystemBase = new File(cmd.getOptionValue("rf")).toPath();
                Path outputCsvFilePath = new File(cmd.getOptionValue("o")).toPath();

                CsvMapper csvMapper = new CsvMapper();
                CsvSchema csvSchema = CsvSchema.builder()
                        .setUseHeader(true)
                        .addColumn("filePath")
                        .addColumn("mainSystemChecksum")
                        .addColumn("comparisonSystemChecksum")
                        .addColumn("isSame")
                        .build();

                if (cmd.hasOption("T")) {
                    // Multi-Thread Logic
                    int threadCount = cmd.getParsedOptionValue("T", 4);
                    System.out.println("Using " + threadCount + " thread to perform the comparison");
                    try (ForkJoinPool fileComparisonPool = new ForkJoinPool(threadCount)) {
                        ObjectWriter csvObjectWriter = csvMapper.writerFor(ComparedRow.class).with(csvSchema);

                        try (SequenceWriter sequenceWriter = csvObjectWriter.writeValues(outputCsvFilePath.toFile())) {
                            List<ComparedRow> allComparisonResults = fileComparisonPool.submit(() -> {
                                try (Stream<Path> fsWalker = Files.walk(mainFileSystemBase)) {
                                    return fsWalker.parallel().filter(Files::isRegularFile)
                                            .map(mainFileSystemBase::relativize)
                                            .map(relativeFilePath -> new FileComparisonJob(mainFileSystemBase.resolve(relativeFilePath), referenceFileSystemBase.resolve(relativeFilePath), ChecksumType.SHA256))
                                            .map(FileComparisonJob::executeComparison)
                                            .toList();
                                }
                            }).get();

                            sequenceWriter.writeAll(allComparisonResults);
                        } catch (ExecutionException e) {
                            throw new AppException("Failed while performing parallel comparison, please try if single thread runs can remedy this issue\"", e);
                        } catch (InterruptedException e) {
                            throw new AppException("Parallel Comparison Interrupted, please try if single thread runs can remedy this issue", e);
                        }
                    }
                } else {
                    // Single Thread Logic
                    try (SequenceWriter sequenceWriter = csvMapper.writerFor(ComparedRow.class).with(csvSchema).writeValues(outputCsvFilePath.toFile())) {
                        try (Stream<Path> fsWalker = Files.walk(mainFileSystemBase)) {
                            fsWalker.filter(Files::isRegularFile)
                                    .map(mainFileSystemBase::relativize)
                                    .map(relativeFilePath -> new FileComparisonJob(mainFileSystemBase.resolve(relativeFilePath), referenceFileSystemBase.resolve(relativeFilePath), ChecksumType.SHA256))
                                    .map(FileComparisonJob::executeComparison)
                                    .forEach(comparedRow -> {
                                        try {
                                            sequenceWriter.write(comparedRow);
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    });
                        }
                    }
                }
            } else {
                System.err.println("Flags -md, -rf, and -o are required, please see the following usage for more details...");
                commandlineHandler.printHelp();
            }
        } catch (ParseException e) {
            commandlineHandler.printHelp();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}