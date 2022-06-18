package com.company;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static final char[] RUSSIAN_ALPHABET = new char[]{
            'а', 'б', 'в', 'г', 'д', 'е', 'ж', 'з', 'и', 'й',
            'к', 'л', 'м', 'н', 'о', 'п', 'р', 'с', 'т', 'у',
            'ф', 'х', 'ц', 'ч', 'ш', 'щ', 'ъ', 'ы', 'ь', 'э', 'ю', 'я'};

    private static final char[] POSSIBLE_LETTERS = new char[]{
            'о', 'е', 'а', 'и', 'н', 'т', 'с'
    };

    private static final Set<String> KEYS = Set.of(
            "ор", "лес", "нога", "крыса", "последовательность"
    );

    private static final double[] RUSSIAN_LETTER_FREQUENCES = new double[] {
            0.0801d, 0.0159d, 0.0454d, 0.0170d, 0.0298d, 0.0845d,
            0.0094d, 0.0165d, 0.0735d, 0.0121d, 0.0349d, 0.0121d,
            0.0349d, 0.0440d, 0.0321d, 0.0670d, 0.1097d, 0.0281d,
            0.0473d, 0.0547d, 0.0626d, 0.0262d, 0.0026d, 0.0097d,
            0.0048d, 0.0144d, 0.0073d, 0.0036d, 0.0004d, 0.0190d,
            0.0174d, 0.0032d, 0.0064d, 0.0201d
    };

    public static void main(String[] args) {
        String openText = readFile("src/com/company/clearText.txt");
        System.out.printf("File %s , index %s%n", "clearText.txt", calculateIndex(openText));
        for (String key : KEYS) {
            String fileName = encryptContentWithSpecifiedKey(openText, key);
            String fileContent = readFile(fileName);
            decryptContentWithKnownKey(fileContent, key);
            System.out.printf("File %s , index %s%n", fileName, calculateIndex(fileContent));
        }

        String cipheredText = readFile("src/com/company/cipher3.txt");

        for (int i = 2; i <= 30; i++) {
            String period = calculateY(cipheredText, i);
            System.out.printf("Index%s|%s\n", i, calculateIndex(period));
        }

        for (int i = 1; i <= 14; i++) {
            String period = calculateY(cipheredText, i);
            Map.Entry<Character, Integer> entry = calculateFrequency(period, true);
            System.out.printf("Period %s most popular letter %s frequency is %s possible letters is %s\n", i, entry.getKey(), entry.getValue(), poossibleLetters(entry.getKey()));
        }
        String calculateMi = calculateMi(14, cipheredText);

        String miKey = "экмкаятникфско";
        String foundKey = "экомаятникфуко";
        decryptContentWithKnownKey(cipheredText, foundKey);
    }

    private static final String calculateMi(int probableKeyLength, String cipheredText) {
        HashMap<Character, List<Double>> commonLetterMatrice = new LinkedHashMap<>();
        StringBuilder possibleKey = new StringBuilder();
        for (int i = 0; i < probableKeyLength; i++) {
            StringBuilder blockOfText = new StringBuilder();
            for (int j = i; j < cipheredText.length(); j = probableKeyLength + j) {
                blockOfText.append(cipheredText.charAt(j));
            }
            HashMap<Character, Integer> letterFrequenciesMap = calculateFrequency(blockOfText.toString(), false);
            HashMap<Character, Double> letterMatrice = new LinkedHashMap<>();
            for (int k = 0; k < RUSSIAN_ALPHABET.length; k++) {
                double sum = 0.0d;
                for (int j = 0; j < RUSSIAN_ALPHABET.length; j++) {
                    char alphabetChar = RUSSIAN_ALPHABET[(j + k) % 32];
                    int letterFrequencies = letterFrequenciesMap.getOrDefault(alphabetChar, 0);
                    double theoreticalLetterFrequency = RUSSIAN_LETTER_FREQUENCES[j];
                    sum += letterFrequencies * theoreticalLetterFrequency;
                }
                letterMatrice.put(RUSSIAN_ALPHABET[k], sum);

            }
            Character key = letterMatrice.entrySet()
                    .stream()
                    .max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1)
                    .orElseThrow()
                    .getKey();

            possibleKey.append(key);

            for (Character ch : letterMatrice.keySet()) {
                if (commonLetterMatrice.containsKey(ch)) {
                    List<Double> doubles = commonLetterMatrice.get(ch);
                    double doubleValue = BigDecimal.valueOf(letterMatrice.get(ch)).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    doubles.add(doubleValue);
                } else {
                    List<Double> doubles = new ArrayList<>();
                    double value = BigDecimal.valueOf(letterMatrice.get(ch)).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    doubles.add(value);
                    commonLetterMatrice.put(ch, doubles);
                }
            }
        }

        return possibleKey.toString();
    }

    public static List<Character> poossibleLetters(Character letter) {
        List<Character> possibleLetters = new ArrayList<>();
        for (int i = 0; i < POSSIBLE_LETTERS.length - 1; i++) {
            int valueOfPossibleLetter = POSSIBLE_LETTERS[i];
            int possibleLetterAsci = letter - valueOfPossibleLetter;
            if (possibleLetterAsci < 0) {
                possibleLetterAsci += 32;
            }
            possibleLetters.add(RUSSIAN_ALPHABET[possibleLetterAsci]);
        }
        return possibleLetters;
    }

    public static String readFile(String path) {
        String content = "";
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            content = sb.toString();
        } catch (IOException ex) {
            System.out.println("An error occurred while writing to file");
        }
        return beautifyContent(content);
    }

    public static String encryptContentWithSpecifiedKey(String content, String key) {
        StringBuilder encryptedText = new StringBuilder();
        int r = 0;
        for (int i = 0; i < content.length() - 1; i++) {
            r = i % key.length();
            encryptedText.append(RUSSIAN_ALPHABET[(key.charAt(r) + content.charAt(i)) % 32]);
        }
        return writeToFile(encryptedText.toString(), key, "encrypted");
    }

    public static void decryptContentWithKnownKey(String content, String key) {
        StringBuilder decryptedText = new StringBuilder();
        int r;
        int num;
        for (int i = 0; i < content.length() - 1; i++) {
            r = i % key.length();
            num = (content.charAt(i) - key.charAt(r)) % 32;
            if (num < 0) {
                num += 32;
            }
            decryptedText.append(RUSSIAN_ALPHABET[num]);
        }
        writeToFile(decryptedText.toString(), key, "decrypted");
    }

    public static String writeToFile(String content, String key, String type) {
        String fileName = String.format("%s_%s_%s.txt", key, key.length(), type);
        try (PrintWriter writer = new PrintWriter(fileName, StandardCharsets.UTF_8);) {
            writer.println(content);
        } catch (IOException ex) {
            System.out.println("An error occurred while writing to file");
        }
        return fileName;
    }

    public static String beautifyContent(String content) {
        return content.chars()
                .mapToObj(sym -> (char) sym)
                .map(Character::toLowerCase)
                .filter(sym -> (int) sym >= 1072 && (int) sym <= 1103)
                .map(Object::toString)
                .collect(Collectors.joining());
    }

    public static double calculateIndex(String cipherText) {
        HashMap<Character, Integer> map = new HashMap<>();
        double res = 0;
        for (int i = 0; i < cipherText.length(); i++) {
            Character character = cipherText.charAt(i);
            map.merge(character, 1, Integer::sum);
        }

        for (Character key : map.keySet()) {
            res += map.get(key) * (map.get(key) - 1);
        }

        return res * (1 / (double) ((cipherText.length()) * (cipherText.length() - 1)));
    }

    public static String calculateY(String cipherText, int per) {
        StringBuilder content = new StringBuilder();
        for (int i = per; i < cipherText.length(); i = i + per) {
            content.append(cipherText.charAt(i));
        }
        return content.toString();
    }

    public static HashMap<Character, Integer> calculateFrequency(String content, boolean needSort) {
        HashMap<Character, Integer> map = new HashMap<>();
        for (int i = 0; i < content.length(); i++) {
            Character character = content.charAt(i);
            map.merge(character, 1, Integer::sum);
        }
        if (!needSort) {
            return map;
        } else {
            return sortByComparator(map, false);
        }
    }

    private static HashMap<Character, Integer> sortByComparator(Map<Character, Integer> unsortMap, final boolean order) {
        List<Map.Entry<Character, Integer>> list = new LinkedList<>(unsortMap.entrySet());

        // Sorting the list based on values
        list.sort((o1, o2) -> {
            if (order) {
                return o1.getValue().compareTo(o2.getValue());
            } else {
                return o2.getValue().compareTo(o1.getValue());

            }
        });

        HashMap<Character, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<Character, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
}
