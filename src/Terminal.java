import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.util.stream.Stream;
import java.nio.file.*;
import java.io.*;


public class Terminal {
    Parser parser;
    
    public Terminal() {
    	parser = new Parser();
    }

    //Implement each command in a method, for example:
    
    
    // echo command: prints the arguments
    public void echo() {
        String[] ss = parser.getArgs();
        for (String rr : ss) {
            System.out.print(rr + " ");
        }
        System.out.println();
    }
    
    // pwd command: give me the current path
    public String pwd(){
    	String path = Paths.get("").toAbsolutePath().toString();
		return path;
    	
    }
	
    public void cd(String[] args){
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
            System.out.println("Error: " + e.getMessage());
        }
    }

	public void zip() {
        String[] args = parser.getArgs();
        try {
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
            System.out.println("Error zipping");
        }
    }

	private void zipFiles(String[] files, String zipName) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipName))) {
            for (String file : files) {
                Path f = Paths.get(file);
                if (!f.isAbsolute())
                    f = Paths.get(System.getProperty("user.dir")).resolve(f);
                zos.putNextEntry(new ZipEntry(f.getFileName().toString()));
                Files.copy(f, zos);
                zos.closeEntry();
            }
        }
        System.out.println("Zip created: " + zipName);
    }

    private void zipDirectory(Path sourceDir, Path zipFile) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile.toFile()))) {
            Files.walk(sourceDir).forEach(path -> {
                try {
                    String entry = sourceDir.relativize(path).toString();
                    if (Files.isDirectory(path)) return;
                    zos.putNextEntry(new ZipEntry(entry));
                    Files.copy(path, zos);
                    zos.closeEntry();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        System.out.println("Directory zipped successfully.");
    }
	
 // rmdir command: remove the empty Folders(Directories)
    public void rmdir() {

        if (parser.getArgs().length != 1) {
            System.out.println("Error: rmdir takes only one argument");
            return;
        }

        String arg = parser.getArgs()[0]; // the element in the args
        File dir = new File(arg);

        // Special case: delete all empty directories in the current working directory
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
                if (!foundEmpty) {
                    System.out.println("No empty directories found to delete.");
                }
            } else {
                System.out.println("No directories found in current folder.");
            }
            return;
        }

        // Normal single-directory delete
        if (dir.isDirectory() && dir.exists()) {
            if (dir.list().length == 0) {
                if (dir.delete()) {
                    System.out.println("Directory deleted successfully.");
                } else {
                    System.out.println("Error: Could not delete directory.");
                }
            } else {
                System.out.println("Directory not empty to delete.");
            }
        } else {
            System.out.println("Error: Directory does not exist or invalid path.");
        }
    }

	// cp -r command: copy folders and subfolders and files
	public void cpDashR(Path source , Path destination) {
		
		try{
			Files.walk(source).forEach(src -> {
				Path target = destination.resolve(source.relativize(src));
				try{
					if (Files.isDirectory(src)) {
						if (Files.notExists(target)) {
							Files.createDirectories(target);
						}
					}else{
						Files.copy(src, target , StandardCopyOption.REPLACE_EXISTING);
					}
					
				}catch (Exception e){
					e.printStackTrace();
				}
			});
			System.out.println("Copy completed successfully.");
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	// cp command: copy only files
	public void cp(Path source , Path destination){
		try{
			Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
			System.out.println("copy completed successfully");
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void touch() {
		String[] args = parser.getArgs();

		if (args.length != 1) {
			System.out.println("Usage: touch <filename>");
			return;
		}

		File file = new File(args[0]);
		try {
			if (file.createNewFile()) {
				System.out.println("File created: " + file.getName());
			} else {
				System.out.println("File already exists.");
			}
		} catch (IOException e) {
			System.out.println("Error creating file: " + e.getMessage());
		}
	}


	// function to handle the cp command
	public void copy(){
		try {
			String[] paths = parser.getArgs();

			if (paths[0].equals("-r")) {

				if (paths.length != 3) {
					throw new IllegalArgumentException("invalid number of arguments");
				}
				Path source = Paths.get(paths[1]);
				Path destination = Paths.get(paths[2]);
				cpDashR(source , destination);
			}else if (!paths[0].equals("-r")) {
				if (paths.length != 2) {
					throw new IllegalArgumentException("invalid number of arguments");
				}
				Path source = Paths.get(paths[0]);
				Path destination =  Paths.get(paths[1]);
				cp(source , destination);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	// cat command : print a single file or takes two files concatanate and print them
	public void cat(){
		try {
			String[] args = parser.getArgs();
			if (args.length == 1) {
				File file = new File(args[0]);
				if (!file.exists()) {
					throw new FileNotFoundException("the file is not found");
				}
                Stream<String> lines = Files.lines(Paths.get(args[0]));
                lines.forEach(System.out::println);
			}else if (args.length == 2) {
				File file1 = new File(args[0]);
                File file2 = new File(args[1]);
                if (!file1.exists() || !file2.exists()) {
                    throw new FileNotFoundException("the file is not found");
                }
                Stream<String> file1Lines = Files.lines(Paths.get(args[0]));
                Stream<String> file2Lines = Files.lines(Paths.get(args[1]));
				file1Lines.forEach(System.out::println);
                file2Lines.forEach(System.out::println);
			}else{
				throw new IllegalArgumentException("cat command needs 1 or 2 arguments");
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}


	public void ls() {
		File currentDir = new File(System.getProperty("user.dir"));
		File[] files = currentDir.listFiles();

		if (files == null) {
			System.out.println("Error reading directory");
			return;
		}

		for (File f : files) {
			if (f.isDirectory()) {
				System.out.println("[DIR]  " + f.getName());
			} else {
				System.out.println("       " + f.getName());
			}
		}
	}

	public void wc() {
		String[] args = parser.getArgs();

		if (args.length != 1) {
			System.out.println("Usage: wc <filename>");
			return;
		}

		File file = new File(args[0]);
		if (!file.exists() || file.isDirectory()) {
			System.out.println("Error: file not found");
			return;
		}

		int lines = 0, words = 0, bytes = 0;

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				lines++;
				words += line.split("\\s+").length;
				bytes += line.getBytes().length + 1; // +1 for newline
			}
		} catch (IOException e) {
			System.out.println("Error reading file: " + e.getMessage());
			return;
		}

		System.out.println(lines + " " + words + " " + bytes + " " + file.getName());
	}
	public boolean isPath(String s) {
        return s.contains("/") || s.contains("\\");
    }

    public void mkdir() {
        String[] args = parser.getArgs();
        for (String dir : args) {
            File newDir;
            if (isPath(dir)) {
                newDir = new File(dir);
            }
            else {
                String currentDir = System.getProperty("user.dir");
                newDir = new File(currentDir, dir);
            }
            try {
                if (newDir.exists()) {
                    System.out.println("Directory already exists: " + newDir.getAbsolutePath());
                }
                else if (newDir.mkdirs()) {
                    System.out.println("Directory created: " + newDir.getAbsolutePath());
                }
                else {
                    System.out.println("Failed to create: " + newDir.getAbsolutePath());
                }
            }
            catch (Exception e) {
                System.out.println("An unexpected error occurred while creating " + newDir.getAbsolutePath());
            }

        }
    }
    public void rm() {
        String[] args = parser.getArgs();

        if (args.length == 1) {
            Path path = Paths.get(args[0]);
            try {
                Files.delete(path);
                System.out.println("File or directory deleted!");
            }
            catch (IOException e) {
                System.out.println("Error deleting: " + e.getMessage());
            }
            catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
            }
        }
        else {
            System.out.println("Usage: rm <filename or directory>");
        }
    }
    public void unzip() {
        System.out.println("Arguments:");
        String[] args = parser.getArgs();
        String currentDir = System.getProperty("user.dir");
        File dir;
        try {
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
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (FileNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("I/O Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }
    }
    
    

	//This method will choose the suitable command method to be called
    public void chooseCommandAction(){
    	String cmd = parser.getCommandName();


    	switch(cmd) {
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
            case "rm" : rm(); break;
            case "unzip": unzip();break;
    	}
    }

}
