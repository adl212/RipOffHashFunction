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
    public static void main(String[] args) throws IOException {
        Scanner reader = new Scanner(System.in);
        System.out.println("Enter a password: ");
        String n = reader.nextLine();
        reader.close();
        String hashed = hashPassword(n);
        System.out.println(hashed);
    }

    private static ArrayList<Integer> initDConstants() {
        ArrayList<Integer> DCONSTANTS = new ArrayList<>();
        ArrayList<Integer> PRIMES = getPrimes();
        for (int n : PRIMES) {
            double number;
            number = sqrt(n);
            int decimal = (int) number;
            double d = number - decimal;
            String numString = (Double.toString(d).substring(2));
            long lng = Long.parseLong(numString);
            //System.out.println(lng);
            DCONSTANTS.add((int) lng);
        }
        return DCONSTANTS;
    }

    private static ArrayList<Integer> getPrimes() {
        ArrayList<Integer> PRIMES = new ArrayList<>();
        PRIMES.add(2);
        PRIMES.add(3);
        PRIMES.add(5);
        PRIMES.add(7);
        PRIMES.add(11);
        PRIMES.add(13);
        PRIMES.add(17);
        PRIMES.add(19);
        PRIMES.add(23);
        PRIMES.add(29);
        return PRIMES;
    }

    private static ArrayList<BinaryOperator<Integer>> initRFunctions() {
        ArrayList<BinaryOperator<Integer>> FUNCTIONS = new ArrayList<>();
        FUNCTIONS.add((a, b) -> a % b); //MODULO
        FUNCTIONS.add((a, b) -> a | b); //OR
        FUNCTIONS.add((a, b) -> a & b); //AND
        FUNCTIONS.add((a, b) -> a << b); //BIT-SHIFT LEFT
        FUNCTIONS.add((a, b) -> a >> b); // BIT-SHIFT RIGHT
        FUNCTIONS.add((a, b) -> a ^ b); // XOR
        return FUNCTIONS;
    }


    private static String hashPassword(String input) throws IOException {
        List<BinaryOperator<Integer>> rf = initRFunctions();
        ArrayList<Integer> DCONSTANTS = initDConstants(); //square root of first 10 primes only decimals
        String bytes = stringToBytes(input);
        bytes += "1";
        int chunks = 1;
        while (true) {
            if (32*chunks < (bytes.length())) chunks += 1;
            else break;
        }
        bytes += "0".repeat(32*chunks - (bytes.length()));
        
        Integer h0,h1,h2,h3,h4,h5,h6,h7;
        h0 = DCONSTANTS.get(chunks % 10);
        h1 = DCONSTANTS.get((chunks+1) % 10);
        h2 = DCONSTANTS.get((chunks+2) % 10);
        h3 = DCONSTANTS.get((chunks+3) % 10);
        h4 = DCONSTANTS.get((chunks+4) % 10);
        h5 = DCONSTANTS.get((chunks+5) % 10);
        h6 = DCONSTANTS.get((chunks+6) % 10);
        h7 = DCONSTANTS.get((chunks+7) % 10);
        //System.out.println(h0);
        for (int i = 0; i < chunks; i++) {
            Integer a,b,c,d,e,f,g,h;
            a=h0;
            b=h1;
            c=h2;
            d=h3;
            e=h4;
            f=h5;
            g=h6;
            h=h7;
            ArrayList<Integer> w = new ArrayList<>();
            for (int j = (bytes.length()/chunks*i)/4; j < ((bytes.length()/chunks)*(i+1))/4; j++) {
                char[] chars = new char[4];
                bytes.getChars(4*j,4+4*j,chars,0);
                String newStr = new String(chars);
                //System.out.println(newStr);
                w.add(stringToBytesToInt(newStr));
            }
            for (int j = 8; j < 64; j++) {
                int s0, s1, s2, ssum;
                s0 = w.get(j-1)^(w.get(j-3)%4);
                s1 = w.get(j-2) ^ (w.get(j-6) % (3));
                s2 = s0 + (s1);
                ssum = s2 + (w.get(j-7));
                w.add(ssum);
            }
            for (int j = 0; j < 64; j++) {
                int abc, cd, efg, temp1, temp2;
                int index = (3*j+1) % rf.size();
                BinaryOperator<Integer> fn = rf.get(index);
                //System.out.println(j);
                abc = a ^ (fn.apply(b, c));
                cd = d ^ (fn.apply(e, a));
                efg = f ^ (fn.apply(a, g));
                temp1 = abc + (cd + (w.get(j)));
                temp2 = efg + (w.get(j));
                h=g;
                g=d + (temp1);
                f=a + (temp1 + (temp2));
                e=d;
                d=c;
                c=b + (temp2);
                b=a;
                a=h;
            }
            h0 = h0 + (a);
            h1 = h1 + (b);
            h2 = h2 + (c);
            h3 = h3 + (d);
            h4 = h4 + (e);
            h5 = h5 + (f);
            h6 = h6 + (g);
            h7 = h7 + (h);
        }
        //System.out.println(h0);
        byte[] bi0 = intToBytes(h0);
        byte[] bi1 = intToBytes(h1);
        byte[] bi2 = intToBytes(h2);
        byte[] bi3 = intToBytes(h3);
        byte[] bi4 = intToBytes(h4);
        byte[] bi5 = intToBytes(h5);
        byte[] bi6 = intToBytes(h6);
        byte[] bi7 = intToBytes(h7);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(bi0);
        outputStream.write(bi1);
        outputStream.write(bi2);
        outputStream.write(bi3);
        outputStream.write(bi4);
        outputStream.write(bi5);
        outputStream.write(bi6);
        outputStream.write(bi7);

        return bytesToHex(outputStream.toByteArray());
    }

    private static byte[] intToBytes(Integer dInt) {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(dInt);
        return b.array();
    }

    private static Integer stringToBytesToInt(String str) {
        return BytesToInt(str);
    }

    private static Integer BytesToInt(String str) {
        return Integer.parseInt(str);
    }

    private static String stringToBytes(String input) {
        byte[] bytes = input.getBytes();
        StringBuilder binary = new StringBuilder();
        for (byte b : bytes)
        {
            int val = b;
            for (int i = 0; i < 8; i++)
            {
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