package com.flyingapplepie.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.flyingapplepie.tool.model.ComparedRow;
import com.flyingapplepie.tool.util.CommandlineHandler;
import com.flyingapplepie.tool.utlity.FileSha256Calculator;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

                try (SequenceWriter sequenceWriter = csvMapper.writerFor(ComparedRow.class).with(csvSchema).writeValues(outputCsvFilePath.toFile())) {
                    try (Stream<Path> fsWalker = Files.walk(mainFileSystemBase)) {
                        fsWalker.filter(Files::isRegularFile)
                                .forEach(mainFsFilePath -> {
                                    Path relativePath = mainFileSystemBase.relativize(mainFsFilePath);
                                    Path referenceFsFilePath = referenceFileSystemBase.resolve(relativePath);

                                    Map<Path, FileSha256Calculator> processingBucket = new HashMap<>();
                                    processingBucket.put(mainFsFilePath, new FileSha256Calculator(mainFsFilePath));
                                    processingBucket.put(referenceFsFilePath, new FileSha256Calculator(referenceFsFilePath));

                                    Map<Path, String> processedChecksums = processingBucket.entrySet().parallelStream()
                                            .collect(Collectors.toMap(
                                                    Map.Entry::getKey,
                                                    entry -> entry.getValue().getChecksumString()
                                            ));

                                    ComparedRow comparedRow = new ComparedRow();
                                    comparedRow.setFilePath(mainFsFilePath.toAbsolutePath().toString());
                                    comparedRow.setMainSystemChecksum(processedChecksums.get(mainFsFilePath));
                                    comparedRow.setComparisonSystemChecksum(processedChecksums.get(referenceFsFilePath));
                                    comparedRow.setSame(comparedRow.getMainSystemChecksum().equals(comparedRow.getComparisonSystemChecksum()));
                                    try {
                                        sequenceWriter.write(comparedRow);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                });
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