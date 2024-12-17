package com.flyingapplepie.tool.model;

import lombok.Data;

@Data
public class ComparedRow {
    private String filePath;
    private String mainSystemChecksum;
    private String comparisonSystemChecksum;
    private boolean isSame;
}
