public class Parser {
    String commandName;
    String[] args;
    
    public Parser() {
    	commandName = "";
    }
    
    //This method will divide the input into commandName and args
    //where "input" is the string command entered by the user
    public boolean parse(String input) {
    	boolean isParsed = false;
		String[] tokens = input.split(" ");
		args = new String[tokens.length - 1];

		commandName = tokens[0];
		for(int i = 1 ; i < tokens.length ; i++) {
			args[i - 1] = tokens[i];
			isParsed = true;
		}
    	return isParsed;
    }
    public String getCommandName(){
		return commandName;
    	
    }
    public String[] getArgs(){
		return args;
    	
    }
}
