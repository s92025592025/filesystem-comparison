package com.flyingapplepie.tool.util;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileSha256Calculator {
    private final Path filePathToCalculate;

    public FileSha256Calculator(Path filePathToCalculate) {
        this.filePathToCalculate = filePathToCalculate;
    }

    public String getChecksumString() {
        if (filePathToCalculate.toFile().isFile()) {
            DigestUtils digestUtils = new DigestUtils("SHA3-256");
            try {
                return digestUtils.digestAsHex(this.filePathToCalculate, StandardOpenOption.READ);
            } catch (IOException e) {
                return "Failed to Read File";
            }
        }

        return "Not a File";
    }
}
