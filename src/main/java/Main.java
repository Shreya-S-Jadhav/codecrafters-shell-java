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
                java.util.List<String> parsed = parseCommand(com);

                int stdoutRedirectIndex = -1;
                int stderrRedirectIndex = -1;

                for (int i = 0; i < parsed.size(); i++) {
                    if (parsed.get(i).equals(">") || parsed.get(i).equals("1>")) {
                        stdoutRedirectIndex = i;
                        break;
                    } else if (parsed.get(i).equals("2>")) {
                        stderrRedirectIndex = i;
                        break;
                    }
                }

                StringBuilder output = new StringBuilder();
                int end = parsed.size();
                

                

                if (stdoutRedirectIndex != -1) {
                    end = stdoutRedirectIndex;
                } else if (stderrRedirectIndex != -1) {
                    end = stderrRedirectIndex;
                } for (int i = 1; i < end; i++) {
    if (i > 1) {
        output.append(" ");
    }
    output.append(parsed.get(i));
}
            }

            else if (com.equals("pwd")) {
                System.out.println(currentDir);
            }

            else if (com.startsWith("cd ")) {
                String dirName = com.substring(3);

                File dir;
                if (dirName.equals("~")) {
                    dir = new File(System.getenv("HOME"));
                } else if (dirName.startsWith("/")) {
                    dir = new File(dirName);
                } else {
                    dir = new File(currentDir, dirName);
                }

                if (dir.exists() && dir.isDirectory()) {
                    currentDir = dir.getCanonicalPath();
                } else {
                    System.out.println("cd: " + dirName + ": No such file or directory");
                }

            }

            else if (com.startsWith("type")) {
                String sub = com.substring(5);
                if (sub.equals("echo") || sub.equals("exit") || sub.equals("type") || sub.equals("pwd")
                        || sub.equals("cd")) {
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
                java.util.List<String> parsed = parseCommand(com);

                String cmd = parsed.get(0);

                File stdoutFile = null;
                File stderrFile = null;

                for (int i = 0; i < parsed.size(); i++) {
                    if (parsed.get(i).equals(">") || parsed.get(i).equals("1>")) {
                        stdoutFile = new File(parsed.get(i + 1));

                        parsed = new java.util.ArrayList<>(parsed.subList(0, i));
                        break;
                    }

                    if (parsed.get(i).equals("2>")) {
                        stderrFile = new File(parsed.get(i + 1));

                        parsed = new java.util.ArrayList<>(parsed.subList(0, i));
                        break;
                    }
                }

                String[] parts = parsed.toArray(new String[0]);

                String path = System.getenv("PATH");
                String[] dirs = path.split(File.pathSeparator);

                boolean found = false;

                for (String dir : dirs) {
                    File file = new File(dir, cmd);

                    if (file.exists() && file.canExecute()) {
                        found = true;

                        ProcessBuilder pb = new ProcessBuilder(parts);

                        pb.command().set(0, file.getName());

                        if (stdoutFile != null) {
                            pb.redirectOutput(stdoutFile);
                            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
                        } else if (stderrFile != null) {
                            pb.redirectError(stderrFile);
                            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                        } else {
                            pb.inheritIO();
                        }

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

    static java.util.List<String> parseCommand(String input) {
        java.util.List<String> args = new java.util.ArrayList<>();

        StringBuilder current = new StringBuilder();

        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            // Outside quotes
            if (!inSingleQuote && !inDoubleQuote && c == '\\') {
                if (i + 1 < input.length()) {
                    current.append(input.charAt(i + 1));
                    i++;
                }
            }

            // Inside double quotes
            else if (inDoubleQuote && c == '\\') {
                if (i + 1 < input.length()) {
                    char next = input.charAt(i + 1);

                    if (next == '"' || next == '\\') {
                        current.append(next);
                        i++;
                    } else {
                        current.append('\\');
                    }
                } else {
                    current.append('\\');
                }
            }

            else if (c == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
            }

            else if (c == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
            }

            else if (c == ' ' && !inSingleQuote && !inDoubleQuote) {
                if (current.length() > 0) {
                    args.add(current.toString());
                    current.setLength(0);
                }
            }

            else {
                current.append(c);
            }
        }

        if (current.length() > 0) {
            args.add(current.toString());
        }

        return args;
    }
}
