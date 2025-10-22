import java.util.Arrays;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;

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
    	
    	String arg = parser.getArgs()[0]; // the element in the args
    	File dir = new File(arg);
    	
    	if(parser.getArgs().length != 1) {
    		System.out.println("Error: rmdir takes only one argument");
    	}
    	else {
    		if(arg.equals("*")) {
    			// waiting
    		}
    		
    	    if (dir.isDirectory() && dir.delete()) {
    	        System.out.println("Directory deleted successfully");
    	    }
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
				// Scanner scan = new Scanner(file);
				// while (scan.hasNextLine()) {
				// 	String line = scan.nextLine();
				// 	System.out.println(line);
				// }
				// scan.close();
				
			}else if (args.length == 2) {
				Path file1 = Paths.get(args[0]);
				Path file2 = Paths.get(args[1]);
				// concatanate the files and print them
			}else{
				throw new IllegalArgumentException("cat command needs 1 or 2 arguments");
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

    //This method will choose the suitable command method to be called
    public void chooseCommandAction(){
    	String cmd = parser.getCommandName();
    	
    	
    	switch(cmd) {
    	case "echo":
    		echo();
    		break;
    		
    	case "pwd":
    		System.out.println(pwd());
    		break;
    		
    	case "rmdir":
    		rmdir();
    		break;
		
		case "cp":
			copy();
			break;

        case "cd":
            cd(parser.getArgs());
            break;

        case "zip":
            zip();
            break;
    	}
    }

}
