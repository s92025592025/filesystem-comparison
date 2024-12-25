package com.flyingapplepie.tool.job;

import com.flyingapplepie.tool.model.ChecksumType;
import com.flyingapplepie.tool.model.ComparedRow;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileComparisonJobTest {

    /**
     * Test {@link FileComparisonJob#executeComparison()} when 2 file comparing are the exact same
     */
    @Test
    public void sameFileComparisonTest() throws IOException {
        Path tmpFilePath = Files.createTempFile("tmp-file-", ".txt");
        tmpFilePath.toFile().deleteOnExit();

        FileComparisonJob fileComparisonJob = new FileComparisonJob(
                tmpFilePath,
                tmpFilePath,
                ChecksumType.SHA256
        );

        ComparedRow actualComparedRow = fileComparisonJob.executeComparison();

        assertEquals(tmpFilePath.toAbsolutePath().toString(), actualComparedRow.getFilePath());
        assertEquals(actualComparedRow.getComparisonSystemChecksum(), actualComparedRow.getMainSystemChecksum());
        assertTrue(actualComparedRow.isSame());
    }

    /**
     * Test {@link FileComparisonJob#executeComparison()} when 2 file comparing are not the same
     */
    @Test
    public void differentFileComparisonTest() throws IOException {
        Path tmpMainFilePath = Files.createTempFile("tmp-file-", ".txt");
        Path tmpReferenceFilePath = Files.createTempFile("tmp-file-", ".txt");
        tmpMainFilePath.toFile().deleteOnExit();
        tmpReferenceFilePath.toFile().deleteOnExit();

        try (PrintWriter fileWriter = new PrintWriter(tmpMainFilePath.toFile())) {
            fileWriter.println("ghsajfghsfghv");
        }

        FileComparisonJob fileComparisonJob = new FileComparisonJob(
                tmpMainFilePath,
                tmpReferenceFilePath,
                ChecksumType.SHA256
        );

        ComparedRow actualComparedRow = fileComparisonJob.executeComparison();

        assertEquals(tmpMainFilePath.toAbsolutePath().toString(), actualComparedRow.getFilePath());
        assertNotEquals(actualComparedRow.getComparisonSystemChecksum(), actualComparedRow.getMainSystemChecksum());
        assertFalse(actualComparedRow.isSame());
    }
}