/**
 * =============================================================
 *  Project Name: Command Line Interpreter (CLI)
 *  File Name: Terminal.java
 *  Version: 1.0
 *  Date: October 2025
 *  =============================================================
 *  Description:
 *  -----------------
 *  This project is an implementation of a simplified Command Line Interpreter (CLI)
 *  in Java. It simulates a Linux-like terminal environment that supports multiple
 *  built-in commands such as `echo`, `pwd`, `ls`, `cd`, `cp`, `rmdir`, `mkdir`,
 *  `cat`, `wc`, `touch`, `zip`, `unzip`, and `rm`. 
 *
 *  The system supports input/output redirection using `>` and `>>`,
 *  allowing users to write command outputs into files or append them.
 *
 *  Major Features:
 *  -----------------
 *  - File and directory management (mkdir, rmdir, rm, cp, touch)
 *  - Input/output redirection (>, >>)
 *  - File reading and concatenation (cat, wc)
 *  - File compression/decompression (zip, unzip)
 *  - Navigation commands (cd, pwd)
 *  - Echo command to display or write text
 *
 *  =============================================================
 *  Authors:
 *  -----------------
 *  - Abdallah Kareem Abdallah — ID: 20230228
 *  - Ahmed Mohammed Ahmed — ID: 20231103
 *  - Reyad Youssef Ebrahim — ID: 20230143
 *  - Hady Hassan Ahmed — ID: 20230454
 *  - Abdallah Alaa Ahmed — ID: 20231013
 *  =============================================================
 */

import java.io.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


/**
 * The {@code Terminal} class is the core component of the Command Line Interpreter project.
 * It provides implementations for common Linux-like commands and manages command parsing,
 * execution, and file system interactions.
 */
public class Terminal {
    Parser parser;


    public Terminal() {
        parser = new Parser();
    }

    // ---------------------------
    // Redirection Functions
    // ---------------------------

    // For ">" — overwrite file
    public void writeToFileOverwrite(String output, String filename) {
        try {
            Files.write(Paths.get(filename), output.getBytes(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.out.println("Error writing to file (>): " + e.getMessage());
        }
    }
    
    // For ">>" — append to file
    public void writeToFileAppend(String output, String filename) {
        try {
            Files.write(Paths.get(filename), output.getBytes(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Error writing to file (>>): " + e.getMessage());
        }
    }
    
    // Optional helper to detect and handle > or >>
    public boolean redirectOutput(String output, String[] args) {
        if (args.length >= 2) {
            String operand = args[args.length - 2];
            String filename = args[args.length - 1];

            if (operand.equals(">")) {
                writeToFileOverwrite(output, filename);
                return true;
            } else if (operand.equals(">>")) {
                writeToFileAppend(output, filename);
                return true;
            }
        }
        return false;
    }

    // echo command : print the args in terminal 
    public void echo() {
        try {
            String[] args = parser.getArgs();
            if (args.length == 0) return;

            // If > or >> exist, get text before them only
            String output;
            if (args.length >= 2 && (args[args.length - 2].equals(">") || args[args.length - 2].equals(">>"))) {
                output = String.join(" ", Arrays.copyOf(args, args.length - 2));
                String operand = args[args.length - 2];
                String filename = args[args.length - 1];

                if (operand.equals(">")) {
                    writeToFileOverwrite(output, filename);
                } else {
                    writeToFileAppend(output, filename);
                }
            } else {
                output = String.join(" ", args);
                System.out.println(output);
            }
        } catch (Exception e) {
            System.out.println("Error in echo: " + e.getMessage());
        }
    }

    public String pwd() {
        try {
            return System.getProperty("user.dir");
        } catch (Exception e) {
            System.out.println("Error getting current directory: " + e.getMessage());
            return "";
        }
    }

    public void cd(String[] args) {
        try {
            if (args.length == 0) {
                System.setProperty("user.dir", System.getProperty("user.home"));
            } else if (args.length == 1 && args[0].equals("..")) {
                Path current = Paths.get(System.getProperty("user.dir"));
                Path parent = current.getParent();
                if (parent != null)
                    System.setProperty("user.dir", parent.toString());
            } else if (args.length == 1) {
                Path newPath = Paths.get(args[0]);
                if (!newPath.isAbsolute())
                    newPath = Paths.get(System.getProperty("user.dir")).resolve(newPath);
                if (Files.exists(newPath) && Files.isDirectory(newPath))
                    System.setProperty("user.dir", newPath.normalize().toString());
                else
                    System.out.println("Error: invalid directory path");
            } else {
                System.out.println("Error: too many arguments");
            }
            System.out.println("Current directory: " + System.getProperty("user.dir"));
        } catch (Exception e) {
            System.out.println("Error in cd: " + e.getMessage());
        }
    }

    public void zip() {
        try {
            String[] args = parser.getArgs();
            if (args.length < 2) {
                System.out.println("Error: zip requires files");
                return;
            }
            boolean recursive = args[0].equals("-r");
            String zipName = recursive ? args[1] : args[0];
            if (recursive) {
                String dirName = args[2];
                zipDirectory(Paths.get(dirName), Paths.get(zipName));
            } else {
                zipFiles(Arrays.copyOfRange(args, 1, args.length), zipName);
            }
        } catch (Exception e) {
            System.out.println("Error in zip: " + e.getMessage());
        }
    }

    private void zipFiles(String[] files, String zipName) {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipName))) {
            for (String file : files) {
                Path f = Paths.get(file);
                if (!f.isAbsolute())
                    f = Paths.get(System.getProperty("user.dir")).resolve(f);
                zos.putNextEntry(new ZipEntry(f.getFileName().toString()));
                Files.copy(f, zos);
                zos.closeEntry();
            }
            System.out.println("Zip created: " + zipName);
        } catch (Exception e) {
            System.out.println("Error zipping files: " + e.getMessage());
        }
    }

    private void zipDirectory(Path sourceDir, Path zipFile) {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile.toFile()))) {
            Files.walk(sourceDir).forEach(path -> {
                try {
                    String entry = sourceDir.relativize(path).toString();
                    if (Files.isDirectory(path)) return;
                    zos.putNextEntry(new ZipEntry(entry));
                    Files.copy(path, zos);
                    zos.closeEntry();
                } catch (Exception e) {
                    System.out.println("Error processing file in zip: " + e.getMessage());
                }
            });
            System.out.println("Directory zipped successfully.");
        } catch (Exception e) {
            System.out.println("Error zipping directory: " + e.getMessage());
        }
    }

    public void rmdir() {
        try {
            if (parser.getArgs().length != 1) {
                System.out.println("Error: rmdir takes only one argument");
                return;
            }
            String arg = parser.getArgs()[0];
            File dir = new File(arg);
            if (arg.equals("*")) {
                File currentDir = new File(pwd());
                File[] files = currentDir.listFiles();
                if (files != null && files.length > 0) {
                    boolean foundEmpty = false;
                    for (File f : files) {
                        if (f.isDirectory() && f.list().length == 0) {
                            if (f.delete()) {
                                System.out.println("Deleted empty directory: " + f.getName());
                                foundEmpty = true;
                            }
                        }
                    }
                    if (!foundEmpty)
                        System.out.println("No empty directories found to delete.");
                } else {
                    System.out.println("No directories found in current folder.");
                }
                return;
            }
            if (dir.isDirectory() && dir.exists()) {
                if (dir.list().length == 0) {
                    if (dir.delete())
                        System.out.println("Directory deleted successfully.");
                    else
                        System.out.println("Error: Could not delete directory.");
                } else {
                    System.out.println("Directory not empty to delete.");
                }
            } else {
                System.out.println("Error: Directory does not exist or invalid path.");
            }
        } catch (Exception e) {
            System.out.println("Error in rmdir: " + e.getMessage());
        }
    }

    public void cpDashR(Path source, Path destination) {
        try {
            Files.walk(source).forEach(src -> {
                Path target = destination.resolve(source.relativize(src));
                try {
                    if (Files.isDirectory(src)) {
                        if (Files.notExists(target)) {
                            Files.createDirectories(target);
                        }
                    } else {
                        Files.copy(src, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (Exception e) {
                    System.out.println("Error copying file: " + e.getMessage());
                }
            });
            System.out.println("Copy completed successfully.");
        } catch (Exception e) {
            System.out.println("Error in cp -r: " + e.getMessage());
        }
    }

    // cp command ==> copy file content to another file 
    public void cp(Path source, Path destination) {
        try {
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Copy completed successfully");
        } catch (Exception e) {
            System.out.println("Error copying file: " + e.getMessage());
        }
    }

    public void touch() {
        try {
            String[] args = parser.getArgs();
            if (args.length != 1) {
                System.out.println("Usage: touch <filename>");
                return;
            }
            File file = new File(args[0]);
            if (file.createNewFile())
                System.out.println("File created: " + file.getName());
            else
                System.out.println("File already exists.");
        } catch (Exception e) {
            System.out.println("Error in touch: " + e.getMessage());
        }
    }

    public void copy() {
        try {
            String[] paths = parser.getArgs();
            if (paths[0].equals("-r")) {
                if (paths.length != 3)
                    throw new IllegalArgumentException("invalid number of arguments");
                Path source = Paths.get(paths[1]);
                Path destination = Paths.get(paths[2]);
                cpDashR(source, destination);
            } else {
                if (paths.length != 2)
                    throw new IllegalArgumentException("invalid number of arguments");
                Path source = Paths.get(paths[0]);
                Path destination = Paths.get(paths[1]);
                cp(source, destination);
            }
        } catch (Exception e) {
            System.out.println("Error in cp: " + e.getMessage());
        }
    }

    // cat command : 
    public void cat() {
        try {
            String[] args = parser.getArgs();
            if (args.length == 0) {
                System.out.println("Usage: cat <file> [file2...]");
                return;
            }

            StringBuilder output = new StringBuilder();
            for (String fileName : args) {
                if (fileName.equals(">") || fileName.equals(">>")) break;
                Path filePath = Paths.get(fileName);
                if (!Files.exists(filePath)) {
                    System.out.println("File not found: " + fileName);
                    return;
                }
                Files.lines(filePath).forEach(line -> output.append(line).append(System.lineSeparator()));
            }

            if (!redirectOutput(output.toString(), args)) {
                System.out.print(output.toString());
            }

        } catch (Exception e) {
            System.out.println("Error in cat: " + e.getMessage());
        }
    }


    // ls command
    public void ls() {
        try {
            String[] args = parser.getArgs();
            StringBuilder output = new StringBuilder();

            File currentDir = new File(System.getProperty("user.dir"));
            File[] files = currentDir.listFiles();

            if (files == null) {
                System.out.println("Error reading directory");
                return;
            }

            for (File f : files) {
                output.append(f.isDirectory() ? "[DIR]  " : "       ")
                      .append(f.getName()).append("\n");
            }

            // Try redirect
            if (!redirectOutput(output.toString(), args)) {
                System.out.print(output.toString());
            }

        } catch (Exception e) {
            System.out.println("Error in ls: " + e.getMessage());
        }
    }

    // wc command ==> Displays # of lines, words and chars in any file
    public void wc() {
        try {
            String[] args = parser.getArgs();
            if (args.length == 0) {
                System.out.println("Usage: wc <filename>");
                return;
            }
            int totalLines = 0, totalWords = 0, totalBytes = 0;
            for(String filename : args) {
                File file = new File(filename);
                if (!file.exists() || file.isDirectory()) {
                    System.out.println("Error: file not found");
                    return;
                }
                int lines = 0, words = 0, bytes = 0;
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        lines++;
                        totalLines++;
                        words += line.split("\\s+").length;
                        totalWords += line.split("\\s+").length;
                        totalBytes+= line.getBytes().length + 1;
                        bytes += line.getBytes().length + 1;
                    }
                }
            System.out.println(file + " " + lines + " " + words + " " + bytes + " ");
            }
            System.out.println("Total: " + totalLines + " " + totalWords + " " + totalBytes);
        } catch (Exception e) {
            System.out.println("Error in wc: " + e.getMessage());
        }
    }

    public boolean isPath(String s) {
        try {
            return s.contains("/") || s.contains("\\");
        } catch (Exception e) {
            System.out.println("Error checking path: " + e.getMessage());
            return false;
        }
    }

    public void mkdir() {
        try {
            String[] args = parser.getArgs();
            for (String dir : args) {
                File newDir;
                if (isPath(dir))
                    newDir = new File(dir);
                else
                    newDir = new File(System.getProperty("user.dir"), dir);

                if (newDir.exists())
                    System.out.println("Directory already exists: " + newDir.getAbsolutePath());
                else if (newDir.mkdirs())
                    System.out.println("Directory created: " + newDir.getAbsolutePath());
                else
                    System.out.println("Failed to create: " + newDir.getAbsolutePath());
            }
        } catch (Exception e) {
            System.out.println("Error in mkdir: " + e.getMessage());
        }
    }

 // rm command ==> remove files from directories
    public void rm() {
    String[] args = parser.getArgs();

    if (args.length == 0) {
        System.out.println("Usage: rm <file1> <file2> ...");
        return;
    }

    for (String fileName : args) {
        Path path = Paths.get(fileName);
        try {
            if (Files.notExists(path)) {
                System.out.println("Error: " + fileName + " does not exist.");
                continue;
            }

            if (Files.isDirectory(path)) {
                System.out.println("Error: " + fileName + " is a directory. Use rm -r to delete directories.");
                continue;
            }

            Files.delete(path);
            System.out.println("Deleted: " + fileName);

        } catch (NoSuchFileException e) {
            System.out.println("File not found: " + fileName);
        } catch (DirectoryNotEmptyException e) {
            System.out.println("Cannot delete directory (not empty): " + fileName);
        } catch (IOException e) {
            System.out.println("I/O error deleting " + fileName + ": " + e.getMessage());
        } catch (SecurityException e) {
            System.out.println("Permission denied deleting " + fileName);
        }
    }
}


    public void unzip() {
        try {
            System.out.println("Arguments:");
            String[] args = parser.getArgs();
            String currentDir = System.getProperty("user.dir");
            File dir;
            if (args.length == 1) {
                dir = new File(currentDir);
            } else if (args.length > 1) {
                String cleanedDest = args[args.length - 1].replace("\"", "");
                dir = new File(cleanedDest);
            } else {
                throw new IllegalArgumentException("Usage: unzip <zip-file> [destination-folder]");
            }

            String zipFilePath = args[0].replace("\"", "");
            Path zipPath = Paths.get(zipFilePath);
            if (!Files.exists(zipPath)) {
                throw new FileNotFoundException("ZIP file not found!");
            }
            if (!dir.exists()) {
                Files.createDirectories(dir.toPath());
            }
            try (ZipInputStream zip = new ZipInputStream(new FileInputStream(zipPath.toFile()))) {
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    Path newPath = dir.toPath().resolve(entry.getName());
                    if (entry.isDirectory()) {
                        Files.createDirectories(newPath);
                    } else {
                        if (newPath.getParent() != null) {
                            Files.createDirectories(newPath.getParent());
                        }
                        Files.copy(zip, newPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                    zip.closeEntry();
                }
                System.out.println("Unzip completed successfully!");
            }
        } catch (Exception e) {
            System.out.println("Error in unzip: " + e.getMessage());
        }
    }

    public void chooseCommandAction() {
        try {
            String cmd = parser.getCommandName();
            switch (cmd) {
                case "echo": echo(); break;
                case "pwd": System.out.println(pwd()); break;
                case "rmdir": rmdir(); break;
                case "cp": copy(); break;
                case "cd": cd(parser.getArgs()); break;
                case "zip": zip(); break;
                case "ls": ls(); break;
                case "touch": touch(); break;
                case "cat": cat(); break;
                case "wc": wc(); break;
                case "mkdir": mkdir(); break;
                case "rm": rm(); break;
                case "unzip": unzip(); break;
                default: System.out.println("Unknown command");
            }
        } catch (Exception e) {
            System.out.println("Error choosing command: " + e.getMessage());
        }
    }
                                     }
                        
