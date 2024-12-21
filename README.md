# filesystem-comparison 

This is a tool for someone to compare a main filesystem with a reference one. This tool will traverse from the specified directory, and see if the reference file system directory has the exact same files.

For example, if main filesystem `example/main/source/` has `this/is/an/example/file`, this tool will check if the reference file system `reference/source` has `this/is/an/example/file` with the same checksum.

Eventually, the tool will generate a CSV report with all the compared files, checksums, and whether there are difference between the files compared.

