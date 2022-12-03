package util;

import java.util.Random;

public class MyTransformer {
    public static String longToString(long num) {
        StringBuilder ans = new StringBuilder();
        while (num > 0) {
            ans.append(num % 2);
            num /= 2;
        }
        ans.reverse();
        return ans.toString();
    }

    public static String intToBinary(String numStr) {
        long num = Long.parseLong(numStr);
        //注意负数的补码情况
        if (num < 0) {
            num += (1L << 32);
        }
        return String.format("%32s", longToString(num)).replaceAll(" ", "0");
    }

    public static String binaryToInt(String binStr) {
        long num = Long.parseLong(binStr, 2);
        if (num > Integer.MAX_VALUE) {
            num -= (1L << 32);
        }
        return String.valueOf(num);
    }

    public static String decimalToNBCD(String decimalStr) {
        int num = Integer.parseInt(decimalStr);
        StringBuilder numStr = new StringBuilder();
        String sign;
        if (num < 0) {
            sign = "1101";
            num = Math.abs(num);
        } else {
            sign = "1100";
        }
        while (num > 0) {
            numStr.insert(0, String.format("%4s", longToString(num % 10)).replaceAll(" ", "0"));
            num /= 10;
        }
        return sign + String.format("%28s", numStr).replaceAll(" ", "0");
    }

    public static String NBCDToDecimal(String NBCDStr) {
        int num = 0;
        for (int i = 0; i < 7; i++) {
            int l = 28 - i * 4;
            int r = 32 - i * 4;
            num += Math.pow(10, i) * Integer.parseInt(NBCDStr.substring(l, r), 2);
        }
        if (NBCDStr.startsWith("1101")) {
            num = -num;
        }
        return String.valueOf(num);
    }

    public static String floatToBinary(String floatStr) {
        float num = Float.parseFloat(floatStr);
        if (num > Float.MAX_VALUE) {
            return "+Inf";
        } else if (num < -Float.MAX_VALUE) {
            return "-Inf";
        }
        String sign = num < 0 ? "1" : "0";
        num = Math.abs(num);
        int exp = 0;

        if (num < (float) Math.pow(2, -126)) { //非规格化数字
            num *= (float) Math.pow(2, 126);
        } else {
            exp = 127;
            if (num < 1.0f) {
                while (num < 1.0f) {
                    num *= 2;
                    exp--;
                }
            } else if (num >= 2.0f){
                while (num >= 2.0f) {
                    exp++;
                    num /= 2;
                }
            }
            num -= 1.0f;
        }
        //现在num的范围为 0.0f <= num < 1.0f
        String expStr = String.format("%8s", longToString(exp)).replaceAll(" ", "0");
        StringBuilder sigStr = new StringBuilder();
        for (int i = 0; i < 23; i++) {
            num *= 2.0f;
            if (num >= 1.0f) {
                sigStr.append(1);
                num -= 1.0f;
            } else {
                sigStr.append(0);
            }
        }
        
        return sign + expStr + sigStr;
    }


    public static String binaryToFloat(String binStr) {
        boolean positive = binStr.charAt(0) == '0';
        int exp = Integer.parseInt(binStr.substring(1, 9), 2);
        float ans = exp == 0 ? 0.0f : 1.0f;
        String sig = binStr.substring(9);
        if (exp == 255) {
            if (sig.contains("1")) {
                return "NaN";
            } else {
                return positive ? "+Inf" : "-Inf";
            }
        }
        for (int i = 0; i < 23; i++) {
            if (sig.charAt(i) == '1') {
                ans += (float) Math.pow(2, -i - 1);
            }
        }
        ans *= exp == 0 ? (float) Math.pow(2, -126) : (float) Math.pow(2, exp - 127);
        if (!positive) {
            ans = -ans;
        }
        return String.valueOf(ans);
    }

    //test
    public static void main(String[] args) {
        Random r = new Random();
        for (int i = 0; i < 10; i++) {
            float f = r.nextFloat();
            System.out.println(f);
            System.out.println(floatToBinary(String.valueOf(f)));
            System.out.println(String.format("%32s", Integer.toBinaryString(Float.floatToIntBits(f))).replaceAll(" ", "0"));
        }
    }
}
