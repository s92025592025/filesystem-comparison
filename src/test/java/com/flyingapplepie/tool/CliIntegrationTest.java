package com.flyingapplepie.tool;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

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
    public void exactSameFileSystemTest() {
        fail();
    }

    /**
     * Test when the reference file system has missing file
     */
    @Test
    public void referenceFileSystemHasMissingFile() {}

    /**
     * Test when the reference file system has a file with same relative path but different checksum
     */
    @Test
    public void referenceFileSystemHasDifferentFile() {}
}