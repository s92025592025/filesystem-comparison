package com.flyingapplepie.tool.util;

import org.apache.commons.cli.*;

public class CommandlineHandler {
    private final Options cmdOptions;

    public CommandlineHandler() {
        this.cmdOptions = new Options();
        cmdOptions.addOption("h", "help", false, "Help command");
        cmdOptions.addOption("mf", "main-fs", true, "Main file system base directory when comparing");
        cmdOptions.addOption("rf", "reference-fs", true, "Reference file system base directory when comparing");
        cmdOptions.addOption("o", "output", true, "Where the output csv file should go");
        cmdOptions.addOption(Option.builder()
                        .option("T")
                        .longOpt("threads")
                        .hasArg(true)
                        .desc("Indicates the amount of threads wanted to perform the comparison. Please note that this will force the tool to write all results at the end of the full comparison, and will lose all progress when error occurs")
                        .type(Integer.class)
                        .build());
//        cmdOptions.addOption("T", "threads", true, "Indicates the amount of threads wanted to perform the comparison. Please note that this will force the tool to write all results at the end of the full comparison, and will lose all progress when error occurs");
    }

    /**
     * @param args Arguments array passed into main function
     * @return {@link CommandLine} with parsed commandline input
     * @throws ParseException When unknown flags are passed
     */
    public CommandLine parseCmdInput(String[] args) throws ParseException {
        CommandLineParser cmdParser = new DefaultParser();

        return cmdParser.parse(this.cmdOptions, args);
    }

    /**
     * Prints cli help message
     */
    public void printHelp() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(
                "filesystem-comparison: Check if the files on main file system also exists on the reference file system, and compare the checksum",
                this.cmdOptions
        );
    }
}
