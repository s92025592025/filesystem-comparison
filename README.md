# filesystem-comparison 

This is a tool for someone to compare a main filesystem with a reference one. This tool will traverse from the specified directory, and see if the reference file system directory has the exact same files.

For example, if main filesystem `example/main/source/` has `this/is/an/example/file`, this tool will check if the reference file system `reference/source` has `this/is/an/example/file` with the same checksum.

Eventually, the tool will generate a CSV report with all the compared files, checksums, and whether there are difference between the files compared.

## Prerequisite
* Java 21+

## How to Install
1. Download the jar from https://github.com/s92025592025/filesystem-comparison/packages/2348260 Assets Section. The jar should follow a pattern `filesystem-comparision-<versions>-jar-with-dependencies.jar`

## How to Run
To perform the comparison, the command should be formatted as below:
```shell
java -jar .\filesystem-comparision-<version>-jar-with-dependencies.jar \
  -mf <MAIN FILESYSTEM DIRECTORY> \
  -rf <REFERENCE FILESYSTEM DIRECTORY> \
  -o <PATH TO SAVE REPORT CSV FILE> 
```

To get the details of each flag, run the following:
```shell
java -jar .\filesystem-comparision-<version>-jar-with-dependencies.jar -h
```

## Help Output
```text
usage: filesystem-comparison: Check if the files on main file system also
                              exists on the reference file system, and
                              compare the checksum
 -h,--help                  Help command
 -mf,--main-fs <arg>        Main file system base directory when comparing
 -o,--output <arg>          Where the output csv file should go
 -rf,--reference-fs <arg>   Reference file system base directory when
                            comparing
 -T,--threads <arg>         Indicates the amount of threads wanted to
                            perform the comparison. Please note that this
                            will force the tool to write all results at
                            the end of the full comparison, and will lose
                            all progress when error occurs
```