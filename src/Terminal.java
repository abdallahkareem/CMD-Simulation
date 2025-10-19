import java.util.Scanner;
import java.io.File;
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

	// cp -r command: copy folders and subfolders
	public void cpDashR() {
		try{
			String[] paths = parser.getArgs();
			
			if(paths.length != 2){
				throw new IllegalArgumentException("Invalid number of arguments");
			}

			Path source = Paths.get(paths[0]);
			Path dest = Paths.get(paths[1]);

			
			try{
				Files.walk(source).forEach(src -> {
					Path target = dest.resolve(source.relativize(src));
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
			}catch (Exception e){
				e.printStackTrace();
			}

		}catch (Exception e){
			System.out.println(e.getMessage());
			return;
		}
		
		System.out.println("Copy completed successfully.");
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
		
		case "cp-r":
			cpDashR();
			break;
    	}
    }

}
