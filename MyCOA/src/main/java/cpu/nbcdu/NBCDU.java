package cpu.nbcdu;

import cpu.alu.ALU;
import util.DataType;
import util.Transformer;

import java.util.Collections;

//考虑0的情况
public class NBCDU {

    ALU alu = new ALU();

    //src + dest
    public DataType add(DataType src, DataType dest) {
        String b = src.toString();
        String a = dest.toString();
        if (isZero(a.substring(4)) && isZero(b.substring(4))) {
            return new DataType("1100" + a.substring(4));
        } else if (isZero(a.substring(4))) {
            return new DataType(b);
        } else if (isZero(b.substring(4))) {
            return new DataType(a);
        }
        String bSign = b.substring(0, 4);
        String aSign = a.substring(0, 4);
        //若两个数字符号不同，将负数转化为NBCD的补码形式
        if (!bSign.equals(aSign)) {
            if (bSign.equals("1101")) {
                b = Reverse(b);
            } else {
                a = Reverse(a);
            }
        }
        String res = alu.add(new DataType(b), new DataType(a)).toString();
        boolean[] hasCarry = new boolean[8];
        //二种情况产生进位方式
        for (int i = 0; i < 7; i++) {
            int l = 28 - i * 4;
            int r = 32 - i * 4;
            hasCarry[i] = isCarry(a.substring(l, r), b.substring(l, r), res.substring(l, r));
            if (Integer.parseInt(res.substring(l, r), 2) >= 10) hasCarry[i] = true;
        }
        for (int i = 0; i < 7; i++) {
            int l = 28 - i * 4;
            int r = 32 - i * 4;
            if (hasCarry[i]) {
                res = alu.add(new DataType(String.join("", Collections.nCopies(l, "0"))
                                + "0110" + String.join("", Collections.nCopies(28 - l, "0"))),
                        new DataType(res)).toString();
                if (Integer.parseInt(res.substring(l - 4, r - 4), 2) >= 10) {
                    hasCarry[i + 1] = true;
                }
            }
        }

        if (aSign.equals(bSign)) {
            res = aSign + res.substring(4);
        } else {
            res = "1100" + res.substring(4);
            if (!hasCarry[6]) {
                res = "1101" + Reverse(res).substring(4);
                hasCarry = new boolean[8];
                for (int i = 0; i < 7; i++) {
                    int l = 28 - i * 4;
                    int r = 32 - i * 4;
                    if (Integer.parseInt(res.substring(l, r), 2) >= 10) hasCarry[i] = true;
                }
                for (int i = 0; i < 7; i++) {
                    int l = 28 - i * 4;
                    int r = 32 - i * 4;
                    if (hasCarry[i]) {
                        res = alu.add(new DataType(String.join("", Collections.nCopies(l, "0"))
                                        + "0110" + String.join("", Collections.nCopies(28 - l, "0"))),
                                new DataType(res)).toString();
                        if (Integer.parseInt(res.substring(l - 4, r - 4), 2) >= 10) {
                            hasCarry[i + 1] = true;
                        }
                    }
                }
            }
        }

        return new DataType(res);
    }


    //dest - drc
    public DataType sub(DataType src, DataType dest) {
        String b = src.toString();

        if (b.startsWith("1101")) {
            b = "1100" + b.substring(4);
        } else {
            b = "1101" + b.substring(4);
        }
        return add(new DataType(b), dest);
    }

    public boolean isCarry(String a, String b, String res) {
        int aInt = Integer.parseInt(a, 2);
        int bInt = Integer.parseInt(b, 2);
        int resInt = Integer.parseInt(res, 2);
        return aInt > resInt && bInt > resInt;
    }

    public String ReverseDigit(String s) {
        if (s.length() != 4) throw new NumberFormatException();
        int num = Integer.parseInt(s, 2);
        num = 9 - num;
        return String.format("%4s", Integer.toBinaryString(num)).replaceAll(" ", "0");
    }

    public String Reverse(String s) {
        for (int i = 0; i < 7; i++) {
            int l = 28 - i * 4;
            int r = 32 - i * 4;
            s = s.substring(0, l) + ReverseDigit(s.substring(l, r)) + s.substring(r);
        }
        String sign = s.substring(0, 4);
        s = sign + Transformer.oneAdder(s.substring(4)).substring(1);
        return s;
    }

    boolean isZero(String s) {
        for (char c : s.toCharArray()) {
            if (c != '0') return false;
        }
        return true;
    }

}
