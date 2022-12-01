package cpu.fpu;

import util.DataType;
import util.IEEE754Float;

import java.util.Collections;

import static util.Transformer.oneAdder;
import static util.Transformer.negation;

public class FPU {

    final int eLength = 8;
    final int sLength = 23;
    final int gLength = 3;


    class FLOAT {
        char sign;
        int exp;
        String sig;

        FLOAT(String s) {
            sign = s.charAt(0);
            exp = Integer.parseInt(s.substring(1, eLength + 1), 2);
            sig = (exp == 0 ? "0" : "1");
            if (exp == 0) exp++;
            sig += s.substring(1 + eLength) + "000";
        }
    }


    private final String[][] addCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.P_INF, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.P_INF, IEEE754Float.NaN}
    };

    private final String[][] subCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.P_INF, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.N_INF, IEEE754Float.NaN}
    };

    private final String[][] mulCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.P_ZERO, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.N_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.N_ZERO, IEEE754Float.NaN}
    };

    private final String[][] divCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.NaN},
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.P_INF, IEEE754Float.NaN},
    };

    //dest + src
    public DataType add(DataType src, DataType dest) {
        String a = src.toString();
        String b = dest.toString();
        if (Corner(a, b, addCorner) != null) {
            return Corner(a, b, addCorner);
        }
        return new DataType(floatAddition(new FLOAT(a), new FLOAT(b)));
    }

    //dest - src
    public DataType sub(DataType src, DataType dest) {
        String a = src.toString();
        String b = dest.toString();
        if (Corner(a, b, subCorner) != null) {
            return Corner(a, b, subCorner);
        }
        a = negation("" + a.charAt(0)) + a.substring(1);
        return new DataType(floatAddition(new FLOAT(a), new FLOAT(b)));
    }

    //dest * src
    public DataType mul(DataType src, DataType dest) {
        String a = src.toString();
        String b = dest.toString();
        if (Corner(a, b, mulCorner) != null) {
            return Corner(a, b, mulCorner);
        }
        return new DataType(floatMul(new FLOAT(a), new FLOAT(b)));
    }

    //dest / src
    public DataType div(DataType src, DataType dest) {
        String a = src.toString();
        String b = dest.toString();
        if (Corner(a, b, divCorner) != null) {
            return Corner(a, b, divCorner);
        }
        boolean sameSign = a.charAt(0) == b.charAt(0);
        if (isZero(b.substring(1))) {
            return sameSign ? new DataType(IEEE754Float.P_ZERO) : new DataType(IEEE754Float.N_ZERO);
        }
        if (isZero(a.substring(1))) throw new ArithmeticException();
        return new DataType(floatDiv(new FLOAT(b), new FLOAT(a)));
    }

    public String floatAddition(FLOAT a, FLOAT b) {
        int exp = Math.max(a.exp, b.exp);
        if (a.exp > b.exp) b.sig = rightShift(b.sig, a.exp - b.exp);
        if (b.exp > a.exp) a.sig = rightShift(a.sig, b.exp - a.exp);
        String temp = signedAdder(a.sign + a.sig, b.sign + b.sig);
        boolean overFlow = temp.charAt(0) == '1';
        char sign = temp.charAt(1);
        String sig = temp.substring(2);
        if (overFlow) {
            exp++;
            sig = "1" + sig.substring(0, sig.length() - 1);
        }
        if (exp >= 255) {
            return "" + sign + "11111111" + String.join("", Collections.nCopies(sLength, "0"));
        }
        return NormalTreatment1(sig, exp, sign, sig.length());
    }

    public String floatMul(FLOAT a, FLOAT b) {
        int exp = a.exp + b.exp - 127;
        char sign = (char) ((a.sign - '0') ^ (b.sign - '0') + '0');
        if (a.exp == 255 || b.exp == 255) {
            return "" + sign + "11111111" + String.join("", Collections.nCopies(sLength, "0"));
        }
        String sig = unsignedMul(a.sig, b.sig, a.sig.length());
        //乘积的隐藏位是2位
        exp++;
        return NormalTreatment2(sig, exp, sign, a.sig.length());
    }
    // a / b
    public String floatDiv(FLOAT a, FLOAT b) {
        char sign = (char) ((a.sign - '0') ^ (b.sign - '0') + '0');
        int exp = a.exp - b.exp + 127;
        if (a.exp == 255) {
            return "" + sign + "11111111" + String.join("", Collections.nCopies(sLength, "0"));
        }
        if (b.exp == 255) {
            return "" + sign + "00000000" + String.join("", Collections.nCopies(sLength, "0"));
        }
        String temp = unsignedDiv(a.sig, b.sig, a.sig.length());
        return NormalTreatment2(temp, exp, sign, a.sig.length());
    }


    public String NormalTreatment1(String sig, int exp, char sign, int length) {
        while (sig.charAt(0) != '1' && exp > 0) { //加减乘除均需要
            sig = leftShift(sig, 1);
            exp--;
        }
        if (exp == 0) {
            sig = rightShift(sig, 1);
        }
        if (isZero(sig)) {  //仅在加减中需要注意符号位设为0
            return "" + String.join("", Collections.nCopies(eLength + sLength + 1, "0"));
        }
        String expStr = String.format("%8s", Integer.toBinaryString(exp)).replaceAll(" ", "0");
        return round(sign, expStr, sig);
    }

    public String NormalTreatment2(String sig, int exp, char sign, int length) {
        while (sig.charAt(0) != '1' && exp > 0) { //加减乘除均需要
            sig = leftShift(sig, 1);
            exp--;
        }
        while (!isZero(sig.substring(0, length)) && exp < 0) { //仅在乘除中需要
            sig = rightShift(sig, 1);
            exp++;
        }
        if (exp >= 255) { //乘除中需要
            return "" + sign + "11111111" + String.join("", Collections.nCopies(sLength, "0"));
        }
        if (exp == 0) {
            sig = rightShift(sig, 1);
        }
        if (exp < 0) {  //仅在乘除中需要，注意符号位设为sign
            return "" + sign + String.join("", Collections.nCopies(eLength + sLength, "0"));
        }
        String expStr = String.format("%8s", Integer.toBinaryString(exp)).replaceAll(" ", "0");
        return round(sign, expStr, sig);
    }

    //第一位是符号位的加法
    public String signedAdder(String a, String b) {
        char signA = a.charAt(0);
        char signB = b.charAt(0);
        //对于零情况的特殊判断
        if (isZero(a.substring(1))) return "0" + b;
        if (isZero(b.substring(1))) return "0" + a;
        a = a.substring(1);
        b = b.substring(1);
        if (signA == signB) { //用 temp.charAt(0) 判断是否溢出
            String temp = carry_adder(a, b, 0, a.length());
            return "" + temp.charAt(0) + signA + temp.substring(1);
        } else {
            b = oneAdder(negation(b)).substring(1);
            String temp = carry_adder(a, b, 0, a.length());
            if (temp.charAt(0) == '1') return "0" + signA + temp.substring(1);
            return "0" + negation("" + signA) + oneAdder(negation(temp.substring(1))).substring(1);
        }
    }

    String unsignedMul(String x, String y, int length) {
        String ans = String.join("", Collections.nCopies(length, "0")) + y;
        for (int i = 0; i < length; i++) {
            char carry = '0';
            if (ans.charAt(2 * length - 1) == '1') {
                String temp = carry_adder(x, ans.substring(0, length), 0, length);
                carry = temp.charAt(0);
                ans = temp.substring(1) + ans.substring(length);
            }
            ans = carry + ans.substring(0, 2 * length - 1);
        }
        return ans;
    }
    // x / y
    String unsignedDiv(String x, String y, int length) {
        String Qstr = "";
        x += String.join("", Collections.nCopies(length, "0"));
        String negy = oneAdder(negation(y)).substring(1);
        for (int i = 0; i < length; i++) {
            String temp = carry_adder(x.substring(0, length), negy, 0, length).substring(1);
            if (temp.charAt(0) == '0') {
                x = temp.substring(1) + x.substring(length) + "1";
            } else {
                x = leftShift(x, 1);
            }
        }
        Qstr = x.substring(length);
        return Qstr;
    }

    public String carry_adder(String add1, String add2, int carry, int length) {
        StringBuilder ans = new StringBuilder();
        int x, y;
        for (int i = length - 1; i >= 0; i--) {//顺序是从低位到高位
            x = add1.charAt(i) - '0';
            y = add2.charAt(i) - '0';
            ans.insert(0, x ^ y ^ carry);
            carry = x & carry | y & carry | x & y;
        }
        return "" + carry + ans;
    }

    public boolean isZero(String s) {
        for (char c : s.toCharArray()) {
            if (c != '0') return false;
        }
        return true;
    }

    public String leftShift(String s, int n) {
        StringBuilder res = new StringBuilder(s.substring(n));
        for (int i = 0; i < n; i++) {
            res.append(0);
        }
        return res.toString();
    }

    public DataType Corner(String a, String b, String[][] corner) {
        String check = cornerCheck(corner, a, b);
        if (check != null) return new DataType(check);
        if (a.matches(IEEE754Float.NaN_Regular) || b.matches(IEEE754Float.NaN_Regular)) {
            return new DataType(IEEE754Float.NaN);
        }
        return null;
    }

    private String cornerCheck(String[][] cornerMatrix, String oprA, String oprB) {
        for (String[] matrix : cornerMatrix) {
            if (oprA.equals(matrix[0]) && oprB.equals(matrix[1])) {
                return matrix[2];
            }
        }
        return null;
    }

    private String rightShift(String operand, int n) {
        StringBuilder result = new StringBuilder(operand);  //保证位数不变
        boolean sticky = false;
        for (int i = 0; i < n; i++) {
            sticky = sticky || result.toString().endsWith("1");
            result.insert(0, "0");
            result.deleteCharAt(result.length() - 1);
        }
        if (sticky) {
            result.replace(operand.length() - 1, operand.length(), "1");
        }
        return result.substring(0, operand.length());
    }

    /**
     * 对GRS保护位进行舍入
     *
     * @param sign    符号位
     * @param exp     阶码
     * @param sig_grs 带隐藏位和保护位的尾数
     * @return 舍入后的结果
     */
    private String round(char sign, String exp, String sig_grs) {
        int grs = Integer.parseInt(sig_grs.substring(24, 27), 2);
        if ((sig_grs.substring(27).contains("1")) && (grs % 2 == 0)) {
            grs++;
        }
        String sig = sig_grs.substring(0, 24); // 隐藏位+23位
        if (grs > 4 || (grs == 4 && sig.endsWith("1"))) {
            sig = oneAdder(sig);
            if (sig.charAt(0) == '1') {
                exp = oneAdder(exp).substring(1);
                sig = sig.substring(1);
            }
        }

        if (Integer.parseInt(sig.substring(0, sig.length() - 23), 2) > 1) {
            sig = rightShift(sig, 1);
            exp = oneAdder(exp).substring(1);
        }
        if (exp.equals("11111111")) {
            return sign == '0' ? IEEE754Float.P_INF : IEEE754Float.N_INF;
        }

        return sign + exp + sig.substring(sig.length() - 23);
    }


}

