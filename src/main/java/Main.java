import java.io.File;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        String currentDir = System.getProperty("user.dir");
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
                System.out.println(currentDir);
            }

            else if (com.startsWith("cd ")) {
                String dirName = com.substring(3);

                File dir = new File(dirName);

                if (dir.exists() && dir.isDirectory()) {
                    currentDir = dir.getAbsolutePath();
                } else {
                    System.out.println("cd: " + dirName + ": No such file or directory");
                }
            }

            else if (com.startsWith("type")) {
                String sub = com.substring(5);
                if (sub.equals("echo") || sub.equals("exit") || sub.equals("type") || sub.equals("pwd") || sub.equals("cd")) {
                    System.out.println(sub + " is a shell builtin");
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
