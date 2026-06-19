import java.io.File;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        // TODO: Uncomment the code below to pass the first stage
        while (true) {
            System.out.print("$ ");
            String com = sc.nextLine();

            if (com.startsWith("exit")) {
                break;
            } 
            
            else if (com.startsWith("echo")) {
                System.out.println(com.substring(5));
            }

            else if (com.equals("pwd")) {
                System.out.println(com + " is a shell builtin");
            }

            else if (com.startsWith("type")) {
                String sub = com.substring(5);
                if (sub.equals("echo") || sub.equals("exit") || sub.equals("type") || sub.equals("pwd")) {
                    System.out.println(System.getProperty("user.dir"));
                } else {
                    String path = System.getenv("PATH");
                    String[] dirs = path.split(File.pathSeparator);

                    boolean found = false;

                    for (String dir : dirs) {
                        File file = new File(dir, sub);

                        if (file.exists() && file.canExecute()) {
                            System.out.println(sub + " is " + file.getAbsolutePath());
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        System.out.println(sub + ": not found");
                    }
                }
            } else {
                String[] parts = com.split(" ");
                String cmd = parts[0];

                String path = System.getenv("PATH");
                String[] dirs = path.split(File.pathSeparator);

                boolean found = false;

                for (String dir : dirs) {
                    File file = new File(dir, cmd);

                    if (file.exists() && file.canExecute()) {
                        found = true;

                        ProcessBuilder pb = new ProcessBuilder(parts);

                        pb.command().set(0, file.getName());

                        pb.inheritIO();

                        Process process = pb.start();
                        process.waitFor();

                        break;
                    }
                }

                if (!found) {
                    System.out.println(com + ": command not found");
                }
            }
        }
    }
}
