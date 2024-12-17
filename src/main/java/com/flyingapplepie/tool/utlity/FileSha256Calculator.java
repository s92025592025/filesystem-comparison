package com.flyingapplepie.tool.utlity;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class FileSha256Calculator {
    private final Path filePathToCalculate;

    public FileSha256Calculator(Path filePathToCalculate) {
        this.filePathToCalculate = filePathToCalculate;
    }

    public String getChecksumString() {
        if (filePathToCalculate.toFile().isFile()) {
            try (InputStream inputStream = new FileInputStream(this.filePathToCalculate.toFile())) {
                DigestUtils digestUtils = new DigestUtils("SHA3-256");
                return digestUtils.digestAsHex(inputStream);
            } catch (
                    FileNotFoundException e) {
                return "File Not Found";
            } catch (IOException e) {
                return "Failed to Read File";
            }
        }

        return "Not a File";
    }
}
