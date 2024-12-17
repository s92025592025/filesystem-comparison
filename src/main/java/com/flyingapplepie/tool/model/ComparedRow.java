package com.flyingapplepie.tool.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ComparedRow {
    @JsonProperty("filePath")
    private String filePath;

    @JsonProperty("mainSystemChecksum")
    private String mainSystemChecksum;

    @JsonProperty("comparisonSystemChecksum")
    private String comparisonSystemChecksum;

    @JsonProperty("isSame")
    private boolean isSame;
}
