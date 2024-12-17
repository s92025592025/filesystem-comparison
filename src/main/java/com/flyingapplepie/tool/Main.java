package com.flyingapplepie.tool;

import org.apache.commons.cli.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        Options cmdOptions = new Options();
        cmdOptions.addOption("h", "help", false, "Help command");
        cmdOptions.addOption("mf", "main-fs", true, "Main file system when comparing");
        cmdOptions.addOption("rf", "reference-fs", true, "Reference file system when comparing");

        CommandLineParser cmdParser = new DefaultParser();
        CommandLine cmd = null;

        try {
            cmd = cmdParser.parse(cmdOptions, args);
        } catch (ParseException e) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("filesystem-comparison", cmdOptions);
        }
    }
}