package com.pekara.tailcall;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        try {
            String input = readInput(args);
            Scanner scanner = new Scanner(input);
            List<Token> tokens = scanner.scan();
            tokens.forEach(System.out::println);
        } catch (TailCallAnalyzerException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println("I/O Error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static String readInput(String[] args) throws IOException {
        if (args.length == 0 || args[0].equals("-")) {
            return new String(System.in.readAllBytes());
        }
        return Files.readString(Path.of(args[0]));
    }
}
