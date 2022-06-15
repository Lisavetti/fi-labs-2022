package com.company;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Main {

    private static final char[] RUSSIAN_ALPHABET = new char[]{
            'а', 'б', 'в', 'г', 'д', 'е', 'ж', 'з', 'и', 'й',
            'к', 'л', 'м', 'н', 'о', 'п', 'р', 'с', 'т', 'у',
            'ф', 'х', 'ц', 'ч', 'ш', 'щ', 'ы', 'ь', 'э', 'ю', 'я'};

    private static final Set<String> RUSSIAN_BIGRAM = Set.of("ст", "но", "то", "на", "ен");


    public static void main(String[] args) throws IOException {
        String fileContent = beatuifyContent(readFile("src/com/company/03.txt"));

        HashMap<String, Integer> bigramFrequency = bigramFrequency(fileContent);

        List<Integer> cipherTextX = getX(bigramFrequency.keySet());
        List<Integer> openTextX = getX(RUSSIAN_BIGRAM);

        int res = 0;

        for (int i = 0; i < 5; i++) {
            int num1 = 0;
            for (; num1 < 5; num1++) {
                int num2 = 0;
                if (i == num1) {
                    break;
                }
                for (; num2 < 5; num2++) {
                    int num3 = 0;
                    for (; num3 < 5; num3++) {
                        if (num2 == num3) {
                            if (num3 == 4) {
                                break;
                            }
                        }
                        int dY = extendedEuclid(961, cipherTextX.get(i) - cipherTextX.get(num1))[0];
                        int dX = openTextX.get(num2) - openTextX.get(num3);

                        if (dY < 0) {
                            dY += 961;
                        }
                        if (dX < 0) {
                            dX += 961;
                        }

                        res = (dX * dY) % 961;

                        if (res > 0) {
                            int b = (cipherTextX.get(i) - extendedEuclid(961, res)[0] * openTextX.get(num2)) % 961;
                            if (b < 0) {
                                b += 961;
                            }

                            String openText = decrypt(fileContent, res, b);

                            double index = calculateIndex(openText);

                            System.out.printf("(%s:%s) index - %s\n", res, b, index);
                        }
                    }

                }
            }
        }

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

    private static List<Integer> equationSolver(int a, int b, int n) {
        List<Integer> result = new ArrayList<>();
        int d = gcd(a, n);
        if (d == 1) {
            int inversed = extendedEuclid(a, n)[0] * b % n;
            if (inversed < 0) {
                inversed += n;
            }
            result.add(inversed);
        } else {
            if (b % d != 0) {
                System.out.println("error");
            } else {
                for (int i = 0; i < d; i++) {
                    result.add(equationSolver(a / d, b / d, n / d).get(0) + i * (n / d));
                }
                return result;
            }
        }
        return result;
    }

    public static List<Integer> getX(Set<String> keySet) {
        Stream<Character> characterStream = IntStream
                .range(0, RUSSIAN_ALPHABET.length)
                .mapToObj(i -> RUSSIAN_ALPHABET[i]);

        List<Character> alphabetArray = characterStream.collect(Collectors.toList());

        List<Integer> xNumbers = new ArrayList<>();

        Iterator<String> stringIterator = keySet.iterator();

        for (int i = 0; i < keySet.size(); i++) {
            String bigram = stringIterator.next();
            xNumbers.add(alphabetArray.indexOf(bigram.charAt(0)) * alphabetArray.size() + alphabetArray.indexOf(bigram.charAt(1)));
        }
        return xNumbers;
    }

    public static String beatuifyContent(String content) {
        return content.chars()
                .mapToObj(sym -> (char) sym)
                .filter(sym -> (int) sym >= 1072 && (int) sym <= 1103)
                .map(Object::toString)
                .collect(Collectors.joining());
    }

    public static int gcd(int num1, int num2) {
        if (num2 != 0) {
            return gcd(num2, num1 % num2);
        } else {
            return num1;
        }
    }

    private static int findNumber(int firstLetterAscii, int secondLetterAscii) {
        return firstLetterAscii * RUSSIAN_ALPHABET.length + secondLetterAscii;
    }

    public static int[] extendedEuclid(int a, int n) {
        int[] dxy = new int[3];

        if (n == 0) {
            dxy[0] = a;
            dxy[1] = 1;

            return dxy;
        } else {
            int t;
            int t2;
            dxy = extendedEuclid(n, a % n);
            t = dxy[1];
            t2 = dxy[2];
            dxy[1] = dxy[2];
            dxy[2] = t - a / n * t2;

            return dxy;
        }
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
        return map.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
    }

    public static String readFile(String path) throws IOException {
        try {
            return Files.readString(Paths.get(path), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IOException("Unable to read file specified on path %s".formatted(path));
        }
    }

    private static String decrypt(String encryptedText, int a, int b) {
        StringBuilder openText = new StringBuilder();
        for (int i = 0; i < encryptedText.length() - 1; i++) {
            int firstLetterOfBigramm = findNumberInAlphabet(encryptedText.charAt(i));
            int secondLetterOfBigramm = findNumberInAlphabet(encryptedText.charAt(i + 1));

            int Yi = firstLetterOfBigramm * 31 + secondLetterOfBigramm;
            int Xi = (a * (Yi - b)) % 961;

            if (Xi < 0) {
                Xi += 961;
            }

            char fisrtOpenTextLetter = RUSSIAN_ALPHABET[Xi/31];
            char secondOpenTextLetter = RUSSIAN_ALPHABET[Xi % 31];

            openText
                    .append(fisrtOpenTextLetter)
                    .append(secondOpenTextLetter);

        }

        return openText.toString();
    }

    private static int findNumberInAlphabet(char symbol) {
        for (int i = 0; i < RUSSIAN_ALPHABET.length; i++) {
            if (RUSSIAN_ALPHABET[i] == symbol) {
                return i;
            }
        }

        return 0;
    }


}
