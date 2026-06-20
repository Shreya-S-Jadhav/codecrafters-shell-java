import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Main {

    static class Job {
        int jobNumber;
        long pid;
        String command;
        Process process;

        Job(int jobNumber, long pid, String command, Process process) {
            this.jobNumber = jobNumber;
            this.pid = pid;
            this.command = command;
            this.process = process;
        }
    }

    static void reapJobs(java.util.List<Job> jobs) {

        java.util.List<Job> completedJobs = new java.util.ArrayList<>();

        for (int i = 0; i < jobs.size(); i++) {

            Job job = jobs.get(i);

            if (!job.process.isAlive()) {

                String marker = " ";

                if (i == jobs.size() - 1) {
                    marker = "+";
                } else if (i == jobs.size() - 2) {
                    marker = "-";
                }

                String commandWithoutAmp = job.command.replaceAll("\\s*&\\s*$", "");

                System.out.printf(
                        "[%d]%s  %-24s%s%n",
                        job.jobNumber,
                        marker,
                        "Done",
                        commandWithoutAmp);

                completedJobs.add(job);
            }
        }

        jobs.removeAll(completedJobs);
    }

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        String currentDir = System.getProperty("user.dir");
        java.util.List<Job> jobs = new java.util.ArrayList<>();

        // TODO: Uncomment the code below to pass the first stage
        while (true) {

            reapJobs(jobs);

            System.out.print("$ ");
            String com = sc.nextLine();

            if (com.startsWith("exit")) {
                break;
            }

            else if (com.startsWith("echo")) {
                java.util.List<String> parsed = parseCommand(com);

                int stdoutRedirectIndex = -1;
                int stderrRedirectIndex = -1;

                boolean appendStdout = false;
                boolean appendStderr = false;

                for (int i = 0; i < parsed.size(); i++) {
                    if (parsed.get(i).equals(">") || parsed.get(i).equals("1>")) {
                        stdoutRedirectIndex = i;
                        appendStdout = false;
                        break;
                    } else if (parsed.get(i).equals(">>") || parsed.get(i).equals("1>>")) {
                        stdoutRedirectIndex = i;
                        appendStdout = true;
                        break;
                    } else if (parsed.get(i).equals("2>")) {
                        stderrRedirectIndex = i;
                        appendStderr = false;
                        break;
                    } else if (parsed.get(i).equals("2>>")) {
                        stderrRedirectIndex = i;
                        appendStderr = true;
                        break;
                    }
                }

                StringBuilder output = new StringBuilder();
                int end = parsed.size();

                if (stdoutRedirectIndex != -1) {
                    end = stdoutRedirectIndex;
                } else if (stderrRedirectIndex != -1) {
                    end = stderrRedirectIndex;
                }
                for (int i = 1; i < end; i++) {
                    if (i > 1) {
                        output.append(" ");
                    }
                    output.append(parsed.get(i));
                }

                if (stdoutRedirectIndex != -1) {
                    if (appendStdout) {
                        Files.writeString(
                                Path.of(parsed.get(stdoutRedirectIndex + 1)),
                                output.toString() + System.lineSeparator(),
                                java.nio.file.StandardOpenOption.CREATE,
                                java.nio.file.StandardOpenOption.APPEND);
                    } else {
                        Files.writeString(
                                Path.of(parsed.get(stdoutRedirectIndex + 1)),
                                output.toString() + System.lineSeparator());
                    }
                } else if (stderrRedirectIndex != -1) {

                    if (appendStderr) {
                        Files.writeString(
                                Path.of(parsed.get(stderrRedirectIndex + 1)),
                                "",
                                java.nio.file.StandardOpenOption.CREATE,
                                java.nio.file.StandardOpenOption.APPEND);
                    } else {
                        Files.writeString(
                                Path.of(parsed.get(stderrRedirectIndex + 1)),
                                "");
                    }

                    System.out.println(output);
                } else {
                    System.out.println(output);
                }
            }

            else if (com.equals("pwd")) {
                System.out.println(currentDir);
            }

            else if (com.equals("jobs")) {
                java.util.List<Job> completedJobs = new java.util.ArrayList<>();

                for (int i = 0; i < jobs.size(); i++) {

                    String marker = " ";

                    if (i == jobs.size() - 1) {
                        marker = "+";
                    } else if (i == jobs.size() - 2) {
                        marker = "-";
                    }

                    Job job = jobs.get(i);

                    if (job.process.isAlive()) {

                        System.out.printf(
                                "[%d]%s  %-24s%s%n",
                                job.jobNumber,
                                marker,
                                "Running",
                                job.command);

                    } else {

                        String commandWithoutAmp = job.command.replaceAll("\\s*&\\s*$", "");

                        System.out.printf(
                                "[%d]%s  %-24s%s%n",
                                job.jobNumber,
                                marker,
                                "Done",
                                commandWithoutAmp);

                        completedJobs.add(job);
                    }
                }

                jobs.removeAll(completedJobs);
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
                        || sub.equals("cd") || sub.equals("jobs")) {
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

                if (parsed.contains("|")) {

                    int pipeIndex = parsed.indexOf("|");

                    java.util.List<String> leftCmd = parsed.subList(0, pipeIndex);

                    java.util.List<String> rightCmd = parsed.subList(pipeIndex + 1, parsed.size());

                    String left = leftCmd.get(0);
                    String right = rightCmd.get(0);

                    if (left.equals("echo")) {

                        StringBuilder output = new StringBuilder();

                        for (int i = 1; i < leftCmd.size(); i++) {
                            if (i > 1)
                                output.append(" ");

                            output.append(leftCmd.get(i));
                        }

                        ProcessBuilder pb = new ProcessBuilder(rightCmd);

                        pb.redirectError(ProcessBuilder.Redirect.INHERIT);

                        Process p = pb.start();

                        p.getOutputStream().write(
                                (output.toString() + "\n").getBytes());

                        p.getOutputStream().close();

                        p.getInputStream().transferTo(System.out);

                        p.waitFor();

                        continue;
                    }

                    if (left.equals("echo")) {

                        StringBuilder output = new StringBuilder();

                        for (int i = 1; i < leftCmd.size(); i++) {
                            if (i > 1)
                                output.append(" ");

                            output.append(leftCmd.get(i));
                        }

                        ProcessBuilder pb = new ProcessBuilder(rightCmd);

                        pb.redirectError(ProcessBuilder.Redirect.INHERIT);

                        Process p = pb.start();

                        p.getOutputStream().write(
                                (output.toString() + "\n").getBytes());

                        p.getOutputStream().close();

                        p.getInputStream().transferTo(System.out);

                        p.waitFor();

                        continue;
                    }

                    ProcessBuilder pb1 = new ProcessBuilder(leftCmd);

                    ProcessBuilder pb2 = new ProcessBuilder(rightCmd);

                    pb1.redirectError(ProcessBuilder.Redirect.INHERIT);
                    pb2.redirectError(ProcessBuilder.Redirect.INHERIT);
                    pb2.redirectOutput(ProcessBuilder.Redirect.INHERIT);

                    java.util.List<Process> processes = ProcessBuilder.startPipeline(
                            java.util.List.of(pb1, pb2));

                    processes.get(processes.size() - 1).waitFor();

                    continue;
                }

                boolean background = false;

                if (!parsed.isEmpty() &&
                        parsed.get(parsed.size() - 1).equals("&")) {

                    background = true;
                    parsed.remove(parsed.size() - 1);
                }

                String cmd = parsed.get(0);

                ProcessBuilder.Redirect stdoutRedirect = null;
                ProcessBuilder.Redirect stderrRedirect = null;

                boolean appendStderr = false;

                for (int i = 0; i < parsed.size(); i++) {

                    if (parsed.get(i).equals(">") || parsed.get(i).equals("1>")) {

                        stdoutRedirect = ProcessBuilder.Redirect.to(
                                new File(parsed.get(i + 1)));

                        parsed = new java.util.ArrayList<>(parsed.subList(0, i));
                        break;
                    }

                    if (parsed.get(i).equals(">>") || parsed.get(i).equals("1>>")) {

                        stdoutRedirect = ProcessBuilder.Redirect.appendTo(
                                new File(parsed.get(i + 1)));

                        parsed = new java.util.ArrayList<>(parsed.subList(0, i));
                        break;
                    }

                    if (parsed.get(i).equals("2>")) {

                        stderrRedirect = ProcessBuilder.Redirect.to(
                                new File(parsed.get(i + 1)));

                        parsed = new java.util.ArrayList<>(parsed.subList(0, i));
                        break;
                    }

                    if (parsed.get(i).equals("2>>")) {

                        stderrRedirect = ProcessBuilder.Redirect.appendTo(
                                new File(parsed.get(i + 1)));

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

                        if (stdoutRedirect != null) {
                            pb.redirectOutput(stdoutRedirect);
                            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
                        } else if (stderrRedirect != null) {
                            pb.redirectError(stderrRedirect);
                            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                        } else {
                            pb.inheritIO();
                        }

                        Process process = pb.start();

                        if (background) {

                            int jobNumber;

                            if (jobs.isEmpty()) {
                                jobNumber = 1;
                            } else {
                                jobNumber = jobs.get(jobs.size() - 1).jobNumber + 1;
                            }

                            Job job = new Job(
                                    jobNumber,
                                    process.pid(),
                                    com,
                                    process);

                            jobs.add(job);

                            System.out.println("[" + jobNumber + "] " + process.pid());

                        } else {
                            process.waitFor();
                        }

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

            if (!inSingleQuote && !inDoubleQuote && c == '\\') {
                if (i + 1 < input.length()) {
                    current.append(input.charAt(i + 1));
                    i++;
                }
            }

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

            else if (c == '|' && !inSingleQuote && !inDoubleQuote) {

                if (current.length() > 0) {
                    args.add(current.toString());
                    current.setLength(0);
                }

                args.add("|");
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
