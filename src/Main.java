import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.BinaryOperator;

import static java.lang.Math.sqrt;

public class Main {
    private static final int NUM_PRIMES = 10;
    private static final int CHUNK_SIZE = 32;

    public static void main(String[] args) throws IOException {
        try (Scanner reader = new Scanner(System.in)) {
            System.out.println("Enter a password: ");
            String password = reader.nextLine();
            String hashed = hashPassword(password);
            System.out.println(hashed);
        }
    }

    private static List<Integer> initDConstants() {
        List<Integer> dConstants = new ArrayList<>();
        List<Integer> primes = getPrimes();
        for (int prime : primes) {
            double number = sqrt(prime);
            int decimal = (int) number;
            double d = number - decimal;
            String numString = (Double.toString(d).substring(2));
            long lng = Long.parseLong(numString);
            dConstants.add((int) lng);
        }
        return dConstants;
    }

    private static List<Integer> getPrimes() {
        List<Integer> primes = new ArrayList<>();
        primes.add(2);
        primes.add(3);
        primes.add(5);
        primes.add(7);
        primes.add(11);
        primes.add(13);
        primes.add(17);
        primes.add(19);
        primes.add(23);
        primes.add(29);
        return primes;
    }

    private static List<BinaryOperator<Integer>> initRFunctions() {
        List<BinaryOperator<Integer>> functions = new ArrayList<>();
        functions.add((a, b) -> a % b); // MODULO
        functions.add((a, b) -> a | b); // OR
        functions.add((a, b) -> a & b); // AND
        functions.add((a, b) -> a << b); // BIT-SHIFT LEFT
        functions.add((a, b) -> a >> b); // BIT-SHIFT RIGHT
        functions.add((a, b) -> a ^ b); // XOR
        return functions;
    }

    private static String hashPassword(String input) throws IOException {
        List<BinaryOperator<Integer>> rf = initRFunctions();
        List<Integer> dConstants = initDConstants();
        String bytes = stringToBytes(input);
        bytes += "1";
        int chunks = (int) Math.ceil((double) bytes.length() / CHUNK_SIZE);
        bytes += "0".repeat(CHUNK_SIZE * chunks - bytes.length());

        int[] hValues = new int[NUM_PRIMES];
        for (int i = 0; i < NUM_PRIMES; i++) {
            hValues[i] = dConstants.get((chunks + i) % NUM_PRIMES);
        }

        for (int i = 0; i < chunks; i++) {
            int[] abcdefgh = new int[NUM_PRIMES];
            System.arraycopy(hValues, 0, abcdefgh, 0, NUM_PRIMES);
            List<Integer> w = getChunkWords(bytes, i, chunks);

            for (int j = 8; j < 64; j++) {
                int s0 = w.get(j - 1) ^ (w.get(j - 3) % 4);
                int s1 = w.get(j - 2) ^ (w.get(j - 6) % 3);
                int s2 = s0 + s1;
                int ssum = s2 + w.get(j - 7);
                w.add(ssum);
            }

            for (int j = 0; j < 64; j++) {
                int index = (3 * j + 1) % rf.size();
                BinaryOperator<Integer> fn = rf.get(index);
                int a = abcdefgh[0];
                int b = abcdefgh[1];
                int c = abcdefgh[2];
                int d = abcdefgh[3];
                int e = abcdefgh[4];
                int f = abcdefgh[5];
                int g = abcdefgh[6];
                int h = abcdefgh[7];
                int temp1 = abcdefgh[0] ^ fn.apply(abcdefgh[1], abcdefgh[2]) + (abcdefgh[3] ^ fn.apply(abcdefgh[4], abcdefgh[0])) + w.get(j);
                int temp2 = abcdefgh[5] ^ fn.apply(abcdefgh[0], abcdefgh[6]) + w.get(j);
                abcdefgh[7] = abcdefgh[6];
                abcdefgh[6] = abcdefgh[5] + temp1;
                abcdefgh[5] = abcdefgh[4];
                abcdefgh[4] = abcdefgh[3];
                abcdefgh[3] = abcdefgh[2] + temp2;
                abcdefgh[2] = abcdefgh[1];
                abcdefgh[1] = abcdefgh[0] + temp1 + temp2;
                abcdefgh[0] = abcdefgh[7];
            }

            for (int j = 0; j < NUM_PRIMES; j++) {
                hValues[j] += abcdefgh[j];
            }
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (int value : hValues) {
            byte[] bi = intToBytes(value);
            outputStream.write(bi);
        }

        return bytesToHex(outputStream.toByteArray());
    }

    private static List<Integer> getChunkWords(String bytes, int chunkIndex, int totalChunks) {
        List<Integer> chunkWords = new ArrayList<>();
        int chunkSize = bytes.length() / totalChunks;
        int start = (chunkSize * chunkIndex) / 4;
        int end = ((chunkSize * (chunkIndex + 1)) / 4) - 1;

        for (int j = start; j <= end; j++) {
            String str = bytes.substring(j * 4, (j * 4) + 4);
            chunkWords.add(stringToBytesToInt(str));
        }

        return chunkWords;
    }

    private static byte[] intToBytes(int dInt) {
        return ByteBuffer.allocate(4).putInt(dInt).array();
    }

    private static int stringToBytesToInt(String str) {
        return Integer.parseInt(str);
    }

    private static String stringToBytes(String input) {
        byte[] bytes = input.getBytes();
        StringBuilder binary = new StringBuilder();
        for (byte b : bytes) {
            int val = b;
            for (int i = 0; i < 8; i++) {
                binary.append((val & 128) == 0 ? 0 : 1);
                val <<= 1;
            }
        }
        return binary.toString();
    }

    private static final byte[] HEX_ARRAY = "0123456789abcdef".getBytes(StandardCharsets.US_ASCII);
    public static String bytesToHex(byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }
}
