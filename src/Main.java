public class Main {
	
    public static void main(String[] args) {
       Parser pp = new Parser();
       pp.parse("echo Abdallah Kareem");
       System.out.println(pp.getCommandName());
       System.out.println();
       String[] ss = pp.getArgs();
       for(String rr : ss) {
    	   System.out.println(rr);
       }
    }
}
