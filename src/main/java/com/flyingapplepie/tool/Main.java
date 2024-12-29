package com.flyingapplepie.tool;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.flyingapplepie.tool.job.FullFileSystemComparisonJob;
import com.flyingapplepie.tool.job.MultiThreadFullFileSystemComparisonJob;
import com.flyingapplepie.tool.job.SingleThreadFullFileSystemComparisonJob;
import com.flyingapplepie.tool.model.ChecksumType;
import com.flyingapplepie.tool.model.ComparedRow;
import com.flyingapplepie.tool.model.FileSystemComparisonSummary;
import com.flyingapplepie.tool.util.CommandlineHandler;
import org.apache.commons.cli.*;
import org.joda.time.Duration;
import org.joda.time.PeriodType;
import org.joda.time.format.PeriodFormat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
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
                ObjectWriter csvObjectWriter = csvMapper.writerFor(ComparedRow.class).with(csvSchema);

                FullFileSystemComparisonJob fullFileSystemComparisonJob = null;

                if (cmd.hasOption("T")) {
                    // Multi-Thread Logic
                    int threadCount = cmd.getParsedOptionValue("T", 4);
                    System.out.println("Using " + threadCount + " thread to perform the comparison");
                    fullFileSystemComparisonJob = new MultiThreadFullFileSystemComparisonJob(
                            mainFileSystemBase,
                            referenceFileSystemBase,
                            threadCount,
                            ChecksumType.SHA256
                    );
                } else {
                    // Single Thread Logic
                    fullFileSystemComparisonJob = new SingleThreadFullFileSystemComparisonJob(
                            mainFileSystemBase,
                            referenceFileSystemBase,
                            ChecksumType.SHA256
                    );
                }

                try (SequenceWriter sequenceWriter = csvObjectWriter.writeValues(outputCsvFilePath.toFile())) {
                    try (Stream<Path> fsWalker = Files.walk(mainFileSystemBase)) {
                        FileSystemComparisonSummary comparisonSummary = fullFileSystemComparisonJob.executeComparison(fsWalker, sequenceWriter);

                        System.out.println("Total Runtime: " + PeriodFormat.getDefault().print(new Duration(comparisonSummary.getTotalRuntime()).toPeriod().normalizedStandard(PeriodType.dayTime())));
                        System.out.println("Total Runtime(Seconds): " + TimeUnit.MILLISECONDS.toSeconds(comparisonSummary.getTotalRuntime()));
                        System.out.println("Total Files Compared: " + comparisonSummary.getTotalComparedFileCount());
                        System.out.println("Total Same files: " + comparisonSummary.getTotalSameFilesCount());
                        System.out.println("Total Different files: " + comparisonSummary.getTotalDiffFilesCount());
                    } catch (Exception e) {
                        throw new AppException(e);
                    }
                }
            } else {
                System.err.println("Flags -md, -rf, and -o are required, please see the following usage for more details...");
                commandlineHandler.printHelp();
            }
        } catch (ParseException e) {
            commandlineHandler.printHelp();
        } catch (IOException e) {
            throw new AppException(e);
        }
    }
}