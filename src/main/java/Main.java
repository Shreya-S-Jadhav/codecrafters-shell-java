import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in); 
        // TODO: Uncomment the code below to pass the first stage
        while (true){
            System.out.print("$ ");
            String com = sc.nextLine();
            
            if (com.equals("exit")){
                break;
            }
            else if(com.startsWith("echo")){
                System.out.println(com.substring(5));
            }
            else if(com.startsWith("type")){
                String sub = com.substring(5);
                if(sub.equals("echo") || sub.equals("exit") || sub.equals("type")){
                    System.out.println(sub + " is a shell builtin");
                }
                else{
                    System.out.println(sub + ": not found");
                }
            }
            else{
                System.out.println(com + ": command not found");
            }   
        }
    }
}
