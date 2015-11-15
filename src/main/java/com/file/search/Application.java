package com.file.search;

import com.file.search.indexing.FileIndexer;
import org.apache.commons.cli.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * @author ahmad
 */
public final class Application {

    private static final String OPT_D = "d";
    private static final String OPT_O = "o";
    private static final String OPT_A = "a";
    private static final String OPT_H = "h";
    private static final String OPT_I = "i";
    private static final String OPT_LOAD = "load";
    private static final String OPT_SAVE = "save";
    private static final String OPT_HELP = "help";

    public static void main(String[] args) throws Exception {
//        final Console console = System.console();
//        if (console == null) {
//            System.err.println("Console not supported!");
//            System.exit(1);
//        }
        Scanner console = new Scanner(System.in);
        final CommandLineParser parser = new DefaultParser();
        final Options options = new Options();
        final Option d = new Option(OPT_D, true, "base dir(s)");
        d.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(d);
        options.addOption(OPT_O, true, "output file");
        options.addOption(OPT_A, "append to output file");
        options.addOption(OPT_H, "include hidden files");
        options.addOption(OPT_I, "case insensitive search");
        options.addOption(OPT_LOAD, "load index files");
        options.addOption(OPT_SAVE, "save index files");
        options.addOption(OPT_HELP, "help");
        final FileIndexer indexer = new FileIndexer();
        final List<Path> baseDirs = new ArrayList<>();
        while (!Thread.currentThread().isInterrupted()) {
            System.out.print("> ");
            String input = console.nextLine();
            if (input == null) {
                break;
            }
            CommandLine cli;
            try {
                cli = parser.parse(options, translateCommandline(input));
            } catch (Throwable e) {
                System.err.printf("%ninvalid command. (due to : %s)%n%n", e.getCause());
                continue;
            }
            if (cli.hasOption(OPT_LOAD)) {
                if (!indexer.loadFromDisk()) {
                    System.err.println("\nno index files found.\n");
                }
                continue;
            }
            if (cli.hasOption(OPT_SAVE)) {
                indexer.saveToDisk();
                continue;
            }
            if (cli.hasOption(OPT_HELP)) {
                printHelp(options);
                continue;
            }
            String[] arguments = cli.getArgs();
            String p;
            if (arguments == null || arguments.length == 0 || (p = arguments[0]) == null || p.isEmpty()) {
                System.err.println("\ninvalid command. (due to : No Args)\n");
                continue;
            }
            if (!cli.hasOption(OPT_D)) {
                System.err.println("\nuse option -d to specify base dir(s).\n");
                continue;
            }
            String[] values = cli.getOptionValues(OPT_D);
            for (String value : values) {
                Path path = Paths.get(value.trim()).toAbsolutePath();
                if (Files.exists(path) && Files.isDirectory(path)) {
                    baseDirs.add(path);
                }
            }
            if (baseDirs.isEmpty()) {
                System.err.println("\nno valid directory found.\n");
                continue;
            }
            SearchListener listener;
            if (cli.hasOption(OPT_O)) {
                String f = cli.getOptionValue(OPT_O);
                if (f == null || f.isEmpty()) {
                    System.err.println("\nplease specify output file.\n");
                    continue;
                }
                File outFile = new File(f);
                if (outFile.isDirectory()) {
                    System.err.println("\ninvalid output file.\n");
                    continue;
                }
                Files.createDirectories(Paths.get(outFile.getParentFile().getAbsolutePath()));
                listener = new DefaultSearchListener(outFile, cli.hasOption(OPT_A));
            } else {
                listener = new DefaultSearchListener();
            }
            final FileMatcher matcher = new DefaultFileMatcher();
            matcher.setName(p);
            matcher.setBaseDirectories(baseDirs);
            if (cli.hasOption(OPT_H)) {
                matcher.setHiddenFilesIncluded(true);
            }
            if (cli.hasOption(OPT_I)) {
                matcher.setCaseInsensitive(true);
            }
            System.out.println();
            FileSearch.search(indexer, matcher, listener, baseDirs);
            System.out.println();
            baseDirs.clear();
        }
        console.close();
        System.exit(0);
    }

    public static String[] translateCommandline(String toProcess) {
        if (toProcess != null && toProcess.length() != 0) {
            byte state = 0;
            StringTokenizer tok = new StringTokenizer(toProcess, "\"\' ", true);
            List<String> list = new ArrayList<>();
            StringBuilder current = new StringBuilder();
            boolean lastTokenHasBeenQuoted = false;
            while (tok.hasMoreTokens()) {
                String args = tok.nextToken();
                switch (state) {
                    case 1:
                        if ("\'".equals(args)) {
                            lastTokenHasBeenQuoted = true;
                            state = 0;
                        } else {
                            current.append(args);
                        }
                        continue;
                    case 2:
                        if ("\"".equals(args)) {
                            lastTokenHasBeenQuoted = true;
                            state = 0;
                        } else {
                            current.append(args);
                        }
                        continue;
                }
                switch (args) {
                    case "\'":
                        state = 1;
                        break;
                    case "\"":
                        state = 2;
                        break;
                    case " ":
                        if (lastTokenHasBeenQuoted || current.length() != 0) {
                            list.add(current.toString());
                            current = new StringBuilder();
                        }
                        break;
                    default:
                        current.append(args);
                        break;
                }
                lastTokenHasBeenQuoted = false;
            }
            if (lastTokenHasBeenQuoted || current.length() != 0) {
                list.add(current.toString());
            }
            if (state != 1 && state != 2) {
                return list.toArray(new String[list.size()]);
            }
            throw new IllegalStateException("unbalanced quotes in " + toProcess);
        }
        return new String[0];
    }

    private static void printHelp(Options options) {
        System.out.println("\nOptions : \n");
        for (Option o : options.getOptions()) {
            System.out.printf("\t-%s\t%s%n", o.getOpt(), o.getDescription());
        }
        System.out.println("\n\t-examples\n\n\t\t<file_name> -d <base_dir(s)> -i\n\t\t<file_name> -d <base_dir(s)> -o <out_put> -h -a\n");
    }

}
