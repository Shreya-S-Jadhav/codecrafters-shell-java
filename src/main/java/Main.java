import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in); 
        // TODO: Uncomment the code below to pass the first stage
        while (true){
            System.out.print("$ ");
            String com = sc.nextLine();
            System.out.println(com + ": command not found");

            if (com.equals("exit")){
                break;
            }   
            System.out.println(com + ": command not found");
        }
    }
}
