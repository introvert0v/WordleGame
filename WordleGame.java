import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class WordleGame {

    private Scanner scanner;
    private List<String> words;

    private final String GREEN = "\u001B[32m";
    private final String YELLOW = "\u001B[33m";
    private final String WHITE = "\u001B[37m";
    private final String RESET = "\u001B[0m";

    public static void main(String[] args) {
        WordleGame game = new WordleGame();
        game.start(args);
    }

    public void start(String[] args) {

        if (args.length == 0) {
            System.out.println(
                    "Please provide a number as command line argument"
            );
            return;
        }

        int wordNumber;

        try {
            wordNumber = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println(
                    "Invalid command-line argument. Please launch with a valid number."
            );
            return;
        }

        words = new ArrayList<>();

        if (!loadWords()) {
            System.out.println("Word list file not found.");
            return;
        }

        /*
         * IMPORTANT:
         * Tests use 0-based indexing.
         */
        if (wordNumber < 0 || wordNumber >= words.size()) {
            System.out.println("Press Enter to exit...");
            return;
        }

        String secretWord = words.get(wordNumber);

        scanner = new Scanner(System.in);

        playGame(secretWord);
    }

    public boolean loadWords() {

        File file = new File("wordle-words.txt");

        if (!file.exists()) {
            return false;
        }

        try {
            BufferedReader reader =
                    new BufferedReader(
                            new FileReader(file)
                    );

            String line;

            while ((line = reader.readLine()) != null) {

                String word = line.trim();

                if (!word.isEmpty()) {
                    words.add(word);
                }
            }

            reader.close();

            return true;

        } catch (IOException e) {
            return false;
        }
    }

    public void playGame(String secretWord) {

        System.out.print("Enter your username: ");

        if (!scanner.hasNextLine()) {
            return;
        }

        String username = scanner.nextLine();

        System.out.println(
                "Welcome to Wordle! Guess the 5-letter word."
        );

        int attempts = 0;

        String remainingLetters =
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        while (attempts < 6) {

            System.out.print("Enter your guess:  ");

            if (!scanner.hasNextLine()) {
                return;
            }

            String guess = scanner.nextLine();

            /*
             * Length check
             */
            if (guess.length() != 5) {
                System.out.println(
                        "Your guess must be exactly 5 letters long."
                );
                continue;
            }

            /*
             * Lowercase check
             */
            if (!containsOnlyLowercaseLetters(guess)) {
                System.out.println(
                        "Your guess must only contain lowercase letters."
                );
                continue;
            }

            /*
             * Dictionary check
             */
            if (!isWordInList(guess)) {
                System.out.println(
                        "Word not in list. Please enter a valid word."
                );
                continue;
            }

            /*
             * Only valid guesses count.
             */
            attempts++;

            /*
             * IMPORTANT:
             * On correct guess tests expect congratulations
             * immediately, without Feedback / Remaining letters.
             */
            if (guess.equals(secretWord)) {

                System.out.println(
                        "Congratulations! You've guessed the word correctly."
                );

                saveStats(
                        username,
                        secretWord,
                        attempts,
                        "win"
                );

                askForStats(username);

                return;
            }

            /*
             * Incorrect valid guess
             */
            System.out.println(
                    "Feedback: "
                            + getFeedback(
                                    secretWord,
                                    guess
                            )
            );

            remainingLetters =
                    updateRemainingLetters(
                            remainingLetters,
                            secretWord,
                            guess
                    );

            System.out.println(
                    "Remaining letters: "
                            + formatRemainingLetters(
                                    remainingLetters
                            )
            );

            System.out.println(
                    "Attempts remaining: "
                            + (6 - attempts)
            );
        }

        System.out.println(
                "Game over. The correct word was: "
                        + secretWord
        );

        saveStats(
                username,
                secretWord,
                attempts,
                "loss"
        );

        askForStats(username);
    }

    public boolean containsOnlyLowercaseLetters(
            String word
    ) {

        for (int i = 0; i < word.length(); i++) {

            char letter = word.charAt(i);

            if (letter < 'a' || letter > 'z') {
                return false;
            }
        }

        return true;
    }

    public boolean isWordInList(String guess) {

        for (int i = 0; i < words.size(); i++) {

            if (words.get(i).equals(guess)) {
                return true;
            }
        }

        return false;
    }

    public String getFeedback(
            String secretWord,
            String guess
    ) {

        StringBuilder result =
                new StringBuilder();

        boolean[] used =
                new boolean[5];

        int[] status =
                new int[5];

        /*
         * 0 = white
         * 1 = yellow
         * 2 = green
         */

        /*
         * First find green letters.
         */
        for (int i = 0; i < 5; i++) {

            if (guess.charAt(i)
                    == secretWord.charAt(i)) {

                status[i] = 2;
                used[i] = true;
            }
        }

        /*
         * Then find yellow letters.
         */
        for (int i = 0; i < 5; i++) {

            if (status[i] == 2) {
                continue;
            }

            char guessedLetter =
                    guess.charAt(i);

            for (int j = 0; j < 5; j++) {

                if (!used[j]
                        && guessedLetter
                        == secretWord.charAt(j)) {

                    status[i] = 1;
                    used[j] = true;
                    break;
                }
            }
        }

        /*
         * Create ANSI-colored output.
         */
        for (int i = 0; i < 5; i++) {

            char letter =
                    Character.toUpperCase(
                            guess.charAt(i)
                    );

            if (status[i] == 2) {

                result.append(GREEN);
                result.append(letter);
                result.append(RESET);

            } else if (status[i] == 1) {

                result.append(YELLOW);
                result.append(letter);
                result.append(RESET);

            } else {

                result.append(WHITE);
                result.append(letter);
                result.append(RESET);
            }
        }

        return result.toString();
    }

    public String updateRemainingLetters(
            String remainingLetters,
            String secretWord,
            String guess
    ) {

        String result =
                remainingLetters;

        for (int i = 0;
             i < guess.length();
             i++) {

            char letter =
                    guess.charAt(i);

            /*
             * Remove only letters that do not
             * exist anywhere in the answer.
             */
            if (secretWord.indexOf(letter) == -1) {

                char upper =
                        Character.toUpperCase(
                                letter
                        );

                result =
                        removeLetter(
                                result,
                                upper
                        );
            }
        }

        return result;
    }

    public String removeLetter(
            String letters,
            char letter
    ) {

        StringBuilder result =
                new StringBuilder();

        for (int i = 0;
             i < letters.length();
             i++) {

            if (letters.charAt(i) != letter) {
                result.append(
                        letters.charAt(i)
                );
            }
        }

        return result.toString();
    }

    public String formatRemainingLetters(
            String letters
    ) {

        StringBuilder result =
                new StringBuilder();

        for (int i = 0;
             i < letters.length();
             i++) {

            result.append(
                    letters.charAt(i)
            );

            if (i < letters.length() - 1) {
                result.append(" ");
            }
        }

        return result.toString();
    }

    public void saveStats(
            String username,
            String secretWord,
            int attempts,
            String result
    ) {

        try {

            BufferedWriter writer =
                    new BufferedWriter(
                            new FileWriter(
                                    "stats.csv",
                                    true
                            )
                    );

            writer.write(
                    username
                            + ","
                            + secretWord
                            + ","
                            + attempts
                            + ","
                            + result
            );

            writer.newLine();

            writer.close();

        } catch (IOException e) {
            // Do not crash.
        }
    }

    public void askForStats(
            String username
    ) {

        System.out.print(
                "Do you want to see your stats? (yes/no): "
        );

        if (!scanner.hasNextLine()) {
            return;
        }

        String answer =
                scanner.nextLine();

        if (answer.equals("yes")) {
            showStats(username);
        }
    }

    public void showStats(
            String username
    ) {

        File file =
                new File("stats.csv");

        if (!file.exists()) {
            return;
        }

        int gamesPlayed = 0;
        int gamesWon = 0;
        int totalAttempts = 0;

        try {

            BufferedReader reader =
                    new BufferedReader(
                            new FileReader(file)
                    );

            String line;

            while ((line =
                    reader.readLine()) != null) {

                String[] parts =
                        line.split(",");

                if (parts.length != 4) {
                    continue;
                }

                if (!parts[0].equals(username)) {
                    continue;
                }

                gamesPlayed++;

                try {
                    totalAttempts +=
                            Integer.parseInt(
                                    parts[2]
                            );
                } catch (NumberFormatException e) {
                    // Ignore invalid row.
                }

                if (parts[3].equals("win")) {
                    gamesWon++;
                }
            }

            reader.close();

        } catch (IOException e) {
            return;
        }

        double averageAttempts = 0.0;

        if (gamesPlayed > 0) {
            averageAttempts =
                    (double) totalAttempts
                            / gamesPlayed;
        }

        System.out.println(
                "Stats for " + username + ":"
        );

        System.out.println(
                "Games played: " + gamesPlayed
        );

        System.out.println(
                "Games won: " + gamesWon
        );

        System.out.println(
                "Average attempts per game: "
                        + averageAttempts
        );
    }
}
