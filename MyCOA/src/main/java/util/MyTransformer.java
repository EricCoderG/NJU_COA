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
        //注意负数的补码情况
        if (num > Integer.MAX_VALUE) {
            num -= (1L << 32);
        }
        return String.valueOf(num);
    }

    public static String decimalToNBCD(String decimalStr) {
        StringBuilder ans = new StringBuilder();
        String sign;
        int num = Integer.parseInt(decimalStr);
        if (num >= 0) {
            sign = "1100";
        } else {
            sign = "1101";
        }
        num = Math.abs(num);
        for (int i = 0; i < 7; i++) {
            int decimalNum = num % 10;
            String decimalNumStr = longToString(decimalNum);
            ans.insert(0, String.format("%4s", decimalNumStr).replaceAll(" ", "0"));
            num /= 10;
        }
        ans.insert(0, sign);

        return ans.toString();
    }

    public static String NBCDToDecimal(String NBCDStr) {
        int ans = 0;
        for (int i = 0; i < 7; i++) {
            ans += Math.pow(10, i) * Integer.parseInt(NBCDStr.substring(NBCDStr.length() - 4), 2);
            NBCDStr = NBCDStr.substring(0, NBCDStr.length() - 4);
        }
        if (NBCDStr.equals("1101")) {
            ans = -ans;
        }
        return String.valueOf(ans);
    }

    public static String floatToBinary(String floatStr) {
        float num = Float.parseFloat(floatStr);
        //对于特殊情况的处理
        if (num > Float.MAX_VALUE) {
            return "+Inf";
        } else if (num < -Float.MAX_VALUE) {
            return "-Inf";
        }
        //对于常规情况的处理
        return floatToBinaryFormat(num);
    }

    public static String floatToBinaryFormat(float num) {
        String signStr = num < 0 ? "1" : "0";
        num = Math.abs(num);
        int exp = 0;

        if (num <= (float) Math.pow(2, -127)) {//非规格化数字
            num *= (float) Math.pow(2, 126);
        } else {
            exp += 127;
            if (num < 1.0f) {
                while (num < 1.0f) {
                    exp--;
                    num *= 2.0f;
                }
            } else if (num >= 2.0f) {
                while (num >= 2.0f) {
                    exp++;
                    num /= 2.0f;
                }
            }
            //经过处理之后，1.0f <= num < 2.0f，所以要实现规格化
            num -= 1.0f;
        }

        String expStr = String.format("%8s", longToString(exp)).replaceAll(" ", "0");
        StringBuilder fragStr = new StringBuilder();
        //此时 0 <= num < 1.0f
        for (int i = 0; i < 23; i++) {
            num *= 2.0f;
            if (num >= 1.0f) {
                fragStr.append(1);
                num -= 1.0f;
            } else {
                fragStr.append(0);
            }
        }

        return signStr + expStr + fragStr;
    }

    public static String binaryToFloat(String binStr) {
        boolean isNeg = binStr.charAt(0) == '1';
        int exp = Integer.parseInt(binStr.substring(1, 9), 2);
        String frag = binStr.substring(9);
        ////非规格化数字处理
        float ans = exp == 0 ? 0.0f : 1.0f;
        //全1阶码特殊情况处理
        if (exp == 255) {
            if (!frag.contains("1")) {
                return isNeg ? "-Inf" : "+Inf";
            } else {
                return "NaN";
            }
        } else {
            for (int i = 0; i < 23; i++) {
                if (frag.charAt(i) == '1') {
                    ans += (float) Math.pow(2, -i - 1);
                }
            }
        }

        ans *= exp == 0 ? (float) Math.pow(2, -126) : (float) Math.pow(2, exp - 127);
        return String.valueOf(isNeg ? -ans : ans);
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
