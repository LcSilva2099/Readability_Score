package readability;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main {

    public static int totalSyllables;
    public static int totalPolysyllables;
    public static double l;
    public static double s;

    public static int ageAutomated = 0;
    public static int ageFlesch = 0;
    public static int ageSimple = 0;
    public static int ageColeman = 0;

    public static void main(String[] args) {

        File file = new File(args[0]);
        String text = null;

        try(Scanner scanner = new Scanner(file)) {
            while(scanner.hasNext()) {
                text = scanner.nextLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String delimiter = "[.?!]";
        assert text != null;
        String[] regexArray = text.split(delimiter);

        int numberOfSentences = regexArray.length;
        int charCount = text.replaceAll("\\s", "").length();
        int wordCount = countWords(regexArray);

        l = (double)charCount / wordCount * 100; // char per 100 words
        s = (double)numberOfSentences / wordCount * 100; // sentences per 100 words

        int[] syllables = countSyllables(text); // index 0 = number of syllables, index 1 = number of polysyllables
        totalSyllables = syllables[0];
        totalPolysyllables = syllables[1];

        displayText(text);
        displayScore(numberOfSentences, wordCount, charCount, totalSyllables, totalPolysyllables);
    }

    //protected static String readFileAsString(String fileName) throws IOException {
    //  return new String(Files.readAllBytes(Paths.get(fileName)));
    // }

    protected static int countWords(String[] array) {
        int wordCount = 0;

        for (String s : array) {
            String[] tempString = s.replaceAll("\\s+", " ").split("\\pZ");
            for (String j : tempString) {
                if (j.matches("\\S+"))
                    wordCount++;
            }
        }

        return wordCount;
    }

    protected static int[] countSyllables(String text) {
        String[] array = text.replaceAll("[.?!,]", "").replaceAll("\\s+", " ").split("\\pZ");
        int syllablesCount = 0;
        int polysyllables = 0;
        int wordSyllableCount;

        for (String s : array) {
            wordSyllableCount = 0;
            StringBuilder build = new StringBuilder(s);
            if (build.charAt(s.length() - 1) == 'e') {
                build.deleteCharAt(s.length() - 1);
            }

            if (build.length() < 4) {
                syllablesCount++;
            } else {
                for (int j = 0; j < build.length() - 1; j++) {
                    if (isVowel(build.charAt(j)) && isVowel(build.charAt(j + 1))) {
                        syllablesCount++;
                        wordSyllableCount++;
                        j++;
                    } else if (isVowel(build.charAt(j))) {
                        syllablesCount++;
                        wordSyllableCount++;
                    } else if (j == build.length() - 2 && isVowel(build.charAt(j + 1))) {
                        if (build.charAt(j + 1) != 'e') {
                            syllablesCount++;
                            wordSyllableCount++;
                        }
                    }
                }
            }
            if (wordSyllableCount > 2) {
                polysyllables ++;
            }
        }

        if (syllablesCount == 0) syllablesCount++;
        return new int[]{syllablesCount, polysyllables};
    }

    protected static boolean isVowel(char charFromWord) {
        char[] vowels = {'a', 'e', 'i', 'o', 'u', 'y'};

        boolean isVowel = false;

        for (char vowel : vowels) {
            if (charFromWord == vowel) {
                isVowel = true;
                break;
            }
        }
        return isVowel;
    }

    protected static void displayText(String text) {
        System.out.print("The text is:\n");
        System.out.printf("%s \n\n", text);
    }

    protected static void displayScore(int numberOfSentences, int wordCount, int charCount, int syllables, int totalPolysyllables) {
        System.out.printf("Words: %d\n", wordCount);
        System.out.printf("Sentences: %d\n", numberOfSentences);
        System.out.printf("Characters: %d \n", charCount);
        System.out.printf("Syllables: %d\n", syllables);
        System.out.printf("Polysyllables: %d\n", totalPolysyllables);
        System.out.println("Enter the score you want to calculate (ARI, FK, SMOG, CL, all): ");
        Scanner scan = new Scanner(System.in);
        choice(scan.nextLine(), numberOfSentences, wordCount, charCount);
    }

    protected static void choice (String choice, int numberOfSentences, int wordCount, int charCount) {
        switch (choice) {
            case "ARI":
                automatedReadabilityIndex(charCount, wordCount, numberOfSentences);
                System.out.printf("\nThis text should be understood in average by %d year olds.", ageAutomated);
                break;
            case "FK":
                flesch(wordCount, numberOfSentences);
                System.out.printf("\nThis text should be understood in average by %d year olds.", ageFlesch);
                break;
            case "SMOG":
                smog(numberOfSentences);
                System.out.printf("\nThis text should be understood in average by %d year olds.", ageSimple);
                break;
            case "CL":
                coleman();
                System.out.printf("\nThis text should be understood in average by %d year olds.", ageColeman);
                break;
            case "all":
                automatedReadabilityIndex(charCount, wordCount, numberOfSentences);
                flesch(wordCount, numberOfSentences);
                smog(numberOfSentences);
                coleman();
                double averageAge = (double)(ageAutomated + ageColeman + ageSimple + ageFlesch) / 4;
                System.out.printf("\nThis text should be understood in average by %.2f year olds.", averageAge);
                break;
            default:
                System.out.println("\nNo such command");
                break;
        }
    }

    protected static void automatedReadabilityIndex(int numberOfChars, int numberOfWords, int numberOfSentences) {
        double score = 4.71 * ((double)numberOfChars / numberOfWords) + 0.5 * ((double)numberOfWords / numberOfSentences) - 21.43;
        System.out.print("Automated Readability Index: " + Math.round(score * 100.0) / 100.0);
        ageAutomated = returnAgeRange(score);
        System.out.print(" (about " + ageAutomated + " year olds).\n");
    }

    protected static void flesch(int numberOfWords, int numberOfSentences) {
        double score = 0.39 * ((double) numberOfWords / numberOfSentences) + 11.8 * ((double) totalSyllables / numberOfWords) - 15.59;
        System.out.print("Flesch–Kincaid readability tests: " + Math.round(score * 100.0) / 100.0);
        ageFlesch = returnAgeRange(score);
        System.out.print(" (about " + ageFlesch + " year olds).\n");
    }

    protected static void smog(int numberOfSentences) {
        double score = 1.043 * Math.sqrt(totalPolysyllables * ((double)30 / numberOfSentences)) + 3.1291;
        System.out.print("Simple Measure of Gobbledygook: " + Math.round(score * 100.0) / 100.0);
        ageSimple = returnAgeRange(score);
        System.out.print(" (about " + ageSimple + " year olds).\n");
    }

    protected static void coleman() {
        double score = 0.0588 * l - 0.296 * s - 15.8;
        System.out.print("Coleman–Liau index: " + Math.round(score * 100.0) / 100.0);
        ageColeman = returnAgeRange(score);
        System.out.print(" (about " + ageColeman + " year olds).\n");
    }

    protected static int returnAgeRange(double doubleScore) {
        int age;
        int score = (int) Math.ceil(doubleScore);

        if (score == 1) {
            age = 6;
        } else if (score == 2) {
            age = 7;
        } else if (score == 3) {
            age = 9;
        } else if (score == 4) {
            age = 10;
        } else if (score == 5) {
            age = 11;
        } else if (score == 6) {
            age = 12;
        } else if (score == 7) {
            age = 13;
        } else if (score == 8) {
            age = 14;
        } else if (score == 9) {
            age = 15;
        } else if (score == 10) {
            age = 16;
        } else if (score == 11) {
            age = 17;
        } else if (score == 12) {
            age = 18;
        } else if (score == 13) {
            age = 24;
        } else {
            age = 25;
        }

        return age;
    }
}
