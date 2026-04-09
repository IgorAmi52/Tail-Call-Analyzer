package com.pekara.tailcall;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        try {
            String input = readInput(args);
            int count = run(input);
            System.out.println("Tail-recursive calls: " + count);
        } catch (TailCallAnalyzerException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println("I/O Error: " + e.getMessage());
            System.exit(1);
        }
    }

    static int run(String input) {
        Scanner scanner = new Scanner(input);
        List<Token> tokens = scanner.scan();
        Parser parser = new Parser(tokens);
        Definition definition = parser.parse();
        TailCallAnalyzer analyzer = new TailCallAnalyzer();
        return analyzer.countTailCalls(definition);
    }

    private static String readInput(String[] args) throws IOException {
        if (args.length == 0 || args[0].equals("-")) {
            return new String(System.in.readAllBytes());
        }
        return Files.readString(Path.of(args[0]));
    }
}
