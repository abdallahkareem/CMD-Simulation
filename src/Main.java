import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Terminal terminal = new Terminal();
        Scanner input = new Scanner(System.in);

        while (true) {
            String text = input.nextLine();

            if (text.equalsIgnoreCase("exit")) break;

            if (terminal.parser.parse(text)) {
                terminal.chooseCommandAction();
            } else {
                System.out.println("Invalid command.");
            }
        }
    }
}
