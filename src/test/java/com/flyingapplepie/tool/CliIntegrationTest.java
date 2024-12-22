package com.flyingapplepie.tool;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.flyingapplepie.tool.model.ComparedRow;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
class CliIntegrationTest {
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    /**
     * Tests the behavior which required options are not provided
     */
    @Test
    public void showErrorWhenRequiredOptionsNotProvideTest() {
        ByteArrayOutputStream outArray = new ByteArrayOutputStream();
        ByteArrayOutputStream errArray = new ByteArrayOutputStream();
        String args[] = {};

        System.setErr(new PrintStream(errArray));

        Main.main(args);

        assertEquals(
                "Flags -md, -rf, and -o are required, please see the following usage for more details...\n".trim(),
                errArray.toString().trim()
        );
    }

    /**
     * Test the output when 2 same file systems are the same
     */
    @Test
    public void exactSameFileSystemTest() throws IOException {
        Path mainFSDirectoryPath = Files.createTempDirectory("temp-test-main-fs-").toAbsolutePath();
        Path referenceFSDirectoryPath = Files.createTempDirectory("tmp-test-reference-fs-").toAbsolutePath();
        Path tmpReportFile = Files.createTempFile("report-output-", ".csv").toAbsolutePath();

        mainFSDirectoryPath.toFile().deleteOnExit();
        referenceFSDirectoryPath.toFile().deleteOnExit();
        tmpReportFile.toFile().deleteOnExit();

        int totalFiles = 10;
        String fileNameTemplate = "sample-file-%d.txt";
        String fileContentTemplate = "this is sample content %d";

        for (int i = 0; i < totalFiles; i++) {
            String fileName = fileNameTemplate.formatted(i);
            String fileContent = fileContentTemplate.formatted(i);
            try (PrintWriter mainFsFilePrintWriter
                         = new PrintWriter(mainFSDirectoryPath.resolve(fileName).toString())) {
                mainFsFilePrintWriter.println(fileContent);
            }

            try (PrintWriter referenceFsFilePrintWriter = new PrintWriter(
                    referenceFSDirectoryPath.resolve(fileName).toString()
            )) {
                referenceFsFilePrintWriter.println(fileContent);
            }
        }

        String args[] = {
                "-mf", mainFSDirectoryPath.toString(),
                "-rf", referenceFSDirectoryPath.toString(),
                "-o", tmpReportFile.toString()
        };

        Main.main(args);

        CsvMapper csvMapper = new CsvMapper();
        CsvSchema csvSchema = csvMapper
                .typedSchemaFor(ComparedRow.class)
                .withHeader()
                .withColumnReordering(true);

        try(MappingIterator<ComparedRow> reportRowIterator = csvMapper
                .readerWithSchemaFor(ComparedRow.class)
                .with(csvSchema)
                .readValues(tmpReportFile.toFile())) {
            List<ComparedRow> fullReport = reportRowIterator.readAll();

            assertEquals(totalFiles, fullReport.size());
            fullReport.forEach(row -> {
                assertTrue(row.isSame());
            });
        }
    }

    /**
     * Test when the reference file system has missing file
     */
    @Test
    public void referenceFileSystemHasMissingFile() throws IOException {
        Path mainFSDirectoryPath = Files.createTempDirectory("temp-test-main-fs-").toAbsolutePath();
        Path referenceFSDirectoryPath = Files.createTempDirectory("tmp-test-reference-fs-").toAbsolutePath();
        Path tmpReportFile = Files.createTempFile("report-output-", ".csv").toAbsolutePath();

        mainFSDirectoryPath.toFile().deleteOnExit();
        referenceFSDirectoryPath.toFile().deleteOnExit();
        tmpReportFile.toFile().deleteOnExit();

        String fileNameTemplate = "sample-file-%d.txt";
        String fileContentTemplate = "this is sample content %d";

        try (PrintWriter mainFsFilePrintWriter
                     = new PrintWriter(mainFSDirectoryPath.resolve(fileNameTemplate.formatted(0)).toString())) {
            mainFsFilePrintWriter.println(fileContentTemplate.formatted(0));
        }

        String args[] = {
                "-mf", mainFSDirectoryPath.toString(),
                "-rf", referenceFSDirectoryPath.toString(),
                "-o", tmpReportFile.toString()
        };

        Main.main(args);
        CsvMapper csvMapper = new CsvMapper();
        CsvSchema csvSchema = csvMapper
                .typedSchemaFor(ComparedRow.class)
                .withHeader()
                .withColumnReordering(true);

        try(MappingIterator<ComparedRow> reportRowIterator = csvMapper
                .readerWithSchemaFor(ComparedRow.class)
                .with(csvSchema)
                .readValues(tmpReportFile.toFile())) {
            List<ComparedRow> fullReport = reportRowIterator.readAll();

            assertEquals(1, fullReport.size());
            fullReport.forEach(row -> {
                assertFalse(row.isSame());
                assertEquals("Not a File", row.getComparisonSystemChecksum());
            });
            }
    }

    /**
     * Test when the reference file system has a file with same relative path but different checksum
     */
    @Test
    public void referenceFileSystemHasDifferentFile() throws IOException {
        Path mainFSDirectoryPath = Files.createTempDirectory("temp-test-main-fs-").toAbsolutePath();
        Path referenceFSDirectoryPath = Files.createTempDirectory("tmp-test-reference-fs-").toAbsolutePath();
        Path tmpReportFile = Files.createTempFile("report-output-", ".csv").toAbsolutePath();

        mainFSDirectoryPath.toFile().deleteOnExit();
        referenceFSDirectoryPath.toFile().deleteOnExit();
        tmpReportFile.toFile().deleteOnExit();

        String fileNameTemplate = "sample-file-%d.txt";
        String fileContentTemplate = "this is sample content %d";

        try (PrintWriter mainFsFilePrintWriter
                     = new PrintWriter(mainFSDirectoryPath.resolve(fileNameTemplate.formatted(0)).toString())) {
            mainFsFilePrintWriter.println(fileContentTemplate.formatted(0));
        }

        try (PrintWriter referenceFsFilePrintWriter = new PrintWriter(
                referenceFSDirectoryPath.resolve(fileNameTemplate.formatted(0)).toString()
        )) {
            referenceFsFilePrintWriter.println(fileContentTemplate.formatted(1));
        }

        String args[] = {
                "-mf", mainFSDirectoryPath.toString(),
                "-rf", referenceFSDirectoryPath.toString(),
                "-o", tmpReportFile.toString()
        };

        Main.main(args);
        CsvMapper csvMapper = new CsvMapper();
        CsvSchema csvSchema = csvMapper
                .typedSchemaFor(ComparedRow.class)
                .withHeader()
                .withColumnReordering(true);

        try(MappingIterator<ComparedRow> reportRowIterator = csvMapper
                .readerWithSchemaFor(ComparedRow.class)
                .with(csvSchema)
                .readValues(tmpReportFile.toFile())) {
            List<ComparedRow> fullReport = reportRowIterator.readAll();

            assertEquals(1, fullReport.size());
            fullReport.forEach(row -> {
                assertFalse(row.isSame());
            });
        }
    }
}