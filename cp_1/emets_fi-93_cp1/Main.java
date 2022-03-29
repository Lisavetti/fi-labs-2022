package com.company;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException {
        String fileContent = readFile("src/com/company/text.txt");
        String clearContent = beautifyContent(fileContent, true);
        String contentWithSpaces = beautifyContent(fileContent, false);
        System.out.println("===========================MONOGRAM NO SPACES=============================");
        System.out.println("Letter frequency");
        Map<Character, Integer> monogramFrequenciesWithoutSpaces = letterFrequency(clearContent);
        monogramFrequenciesWithoutSpaces.forEach((key, value) -> System.out.println(key + "|" + value + "|" + (double) value / clearContent.length()));
        System.out.println("Entropy: " + calculateMonogramEntropy(monogramFrequenciesWithoutSpaces, clearContent.length()));
        System.out.println("Redundancy: " + calculateRedundancy(calculateMonogramEntropy(monogramFrequenciesWithoutSpaces, clearContent.length())));
        System.out.println("===========================MONOGRAM WITH SPACES=============================");
        System.out.println("Letter frequency");
        Map<Character, Integer> monogramFrequenciesWithSpaces = letterFrequency(contentWithSpaces);
        monogramFrequenciesWithSpaces.forEach((key, value) -> System.out.println(key + "|" + value + "|" + (double) value / contentWithSpaces.length()));
        System.out.println("Entropy: " + calculateMonogramEntropy(monogramFrequenciesWithSpaces, contentWithSpaces.length()));
        System.out.println("Redundancy: " + calculateRedundancy(calculateMonogramEntropy(monogramFrequenciesWithSpaces, contentWithSpaces.length())));
        System.out.println("===========================BIGRAM NO SPACES=============================");
        System.out.println("Letter frequency");
        HashMap<String, Integer> bigramFrequenciesWithoutSpaces = bigramFrequency(clearContent);
        bigramFrequenciesWithoutSpaces.forEach((key, value) -> System.out.println(key + "|" + value + "|" + (double) value / clearContent.length()));
        System.out.println("Entropy: " + calculateBigrammEntropy(bigramFrequenciesWithoutSpaces, clearContent.length(), false));
        System.out.println("Redundancy: " + calculateRedundancy(calculateBigrammEntropy(bigramFrequenciesWithoutSpaces, clearContent.length(), false)));
        System.out.println("===========================BIGRAM WITH SPACES=============================");
        System.out.println("Letter frequency");
        HashMap<String, Integer> bigramFrequenciesWithSpaces = bigramFrequency(contentWithSpaces);
        bigramFrequenciesWithSpaces.forEach((key, value) -> System.out.println(key + "|" + value + "|" + (double) value / contentWithSpaces.length()));
        System.out.println("Entropy: " + calculateBigrammEntropy(bigramFrequenciesWithSpaces, contentWithSpaces.length(), false));
        System.out.println("Redundancy: " + calculateRedundancy(calculateBigrammEntropy(bigramFrequenciesWithSpaces, contentWithSpaces.length(), false)));
        System.out.println("===========================BIGRAM NO SPACES OVERLAYED=============================");
        System.out.println("Letter frequency");
        HashMap<String, Integer> bigramFrequenciesWithoutSpacesOverlay = bigramFrequencyOverlay(clearContent);
        bigramFrequenciesWithoutSpacesOverlay.forEach((key, value) -> System.out.println(key + "|" + value + "|" + (double) value / clearContent.length()));
        System.out.println("Entropy: " + calculateBigrammEntropy(bigramFrequenciesWithoutSpacesOverlay, clearContent.length(), true));
        System.out.println("Redundancy: " + calculateRedundancy(calculateBigrammEntropy(bigramFrequenciesWithoutSpacesOverlay, clearContent.length(), true)));
        System.out.println("===========================BIGRAM WITH SPACES OVERLAYED=============================");
        System.out.println("Letter frequency");
        HashMap<String, Integer> bigramFrequenciesWithSpacesOverlay = bigramFrequencyOverlay(contentWithSpaces);
        bigramFrequenciesWithSpacesOverlay.forEach((key, value) -> System.out.println(key + "|" + value + "|" + (double) value / contentWithSpaces.length()));
        System.out.println("Entropy: " + calculateBigrammEntropy(bigramFrequenciesWithSpacesOverlay, contentWithSpaces.length(), true));
        System.out.println("Redundancy: " + calculateRedundancy(calculateBigrammEntropy(bigramFrequenciesWithSpacesOverlay, contentWithSpaces.length(), true)));
    }

    private static double calculateBigrammEntropy(HashMap<String, Integer> bigramFrequenciesWithoutSpaces, int contentSize, boolean overlay) {
        double entropy = 0.0;
        if (!overlay) {
            for (String key : bigramFrequenciesWithoutSpaces.keySet()) {
                entropy += (((double) bigramFrequenciesWithoutSpaces.get(key) / contentSize) * ((Math.log((double) bigramFrequenciesWithoutSpaces.get(key) / contentSize)) / Math.log(2.0)) / 2);
            }
        } else {
            for (String key : bigramFrequenciesWithoutSpaces.keySet()) {
                entropy += (((double) bigramFrequenciesWithoutSpaces.get(key) / contentSize) * ((Math.log((double) bigramFrequenciesWithoutSpaces.get(key) / contentSize)) / Math.log(2.0)));
            }
            entropy /= 2;
        }
        return entropy * (-1);
    }

    public static double calculateRedundancy(double entropy) {
        return (((1 - entropy)) / (Math.log(31) / Math.log(2))) * (-1);
    }

    public static String readFile(String path) throws IOException {
        try {
            return Files.readString(Paths.get(path), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IOException("Unable to read file specified on path %s".formatted(path));
        }
    }

    public static double calculateMonogramEntropy(Map<Character, Integer> monograms, int contentSize) {
        double entropy = 0.0;
        for (Character key : monograms.keySet()) {
            entropy += (((double) monograms.get(key) / contentSize) * ((Math.log((double) monograms.get(key) / contentSize)) / Math.log(2.0)));
        }
        return entropy * (-1);
    }

    public static String beautifyContent(String content, boolean deleteSpaces) {
        return content.chars()
                .mapToObj(sym -> (char) sym)
                .map(Character::toLowerCase)
                .filter(sym -> (int) sym >= 1072 && (int) sym <= 1103 || (sym == ' ' && !deleteSpaces))
                .map(Object::toString)
                .collect(Collectors.joining());
    }

    public static Map<Character, Integer> letterFrequency(String content) {
        HashMap<Character, Integer> map = new HashMap<>();
        for (int i = 0; i < content.length(); i++) {
            Character character = content.charAt(i);
            map.merge(character, 1, Integer::sum);
        }
        return map;
    }

    public static HashMap<String, Integer> bigramFrequency(String content) {
        HashMap<String, Integer> map = new HashMap<>();
        for (int i = 0; i < content.length() - 1; i++) {
            char firstCharacter = content.charAt(i);
            char secondCharacter = content.charAt(i + 1);
            if (secondCharacter != ' ' && firstCharacter != ' ') {
                String bigram = firstCharacter + String.valueOf(secondCharacter);
                map.merge(bigram, 1, Integer::sum);
            }
        }
        return map;
    }

    public static HashMap<String, Integer> bigramFrequencyOverlay(String content) {
        HashMap<String, Integer> map = new HashMap<>();
        for (int i = 0; i < content.length() - 1; ++i) {
            char firstCharacter = content.charAt(i);
            char secondCharacter = content.charAt(i + 1);
            if (secondCharacter != ' ' && firstCharacter != ' ') {
                String bigram = firstCharacter + String.valueOf(secondCharacter);
                map.merge(bigram, 1, Integer::sum);
            }
        }
        return map;
    }
}
