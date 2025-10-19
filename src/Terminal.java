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
    	}
    }

}
