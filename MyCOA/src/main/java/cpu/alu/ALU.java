package cpu.alu;

import util.DataType;
import util.Transformer;

import java.util.Collections;

import static util.Transformer.oneAdder;
import static util.Transformer.negation;


/**
 * Arithmetic Logic Unit
 * ALU封装类
 */
public class ALU {

    final String ZERO = String.join("", Collections.nCopies(32, "0"));
    final String NegOne = String.join("", Collections.nCopies(32, "1"));
    DataType remainderReg;

    /**
     * 返回两个二进制整数的和
     * dest + src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits
     */
    public DataType add(DataType src, DataType dest) {
        return new DataType(adder(src.toString(), dest.toString(), 0, 32));
    }
    //增加参数carry0,当加法时参数为0，减法时参数为1(按位取反再+1)


    /**
     * 返回两个二进制整数的差
     * dest - src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits
     */
    public DataType sub(DataType src, DataType dest) {
        //预处理只需要按位取反即可
        return new DataType(adder(src.toString(), negation(dest.toString()), 1, 32));
    }
    /**
     * 返回两个二进制整数的乘积(结果低位截取后32位)
     * dest * src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits
     */
    public DataType mul(DataType src, DataType dest) {
        int length = 32;
        String X = src.toString();
        String destStr = dest.toString();
        String negX = Transformer.oneAdder(Transformer.negation(X)).substring(1);
        String XStr = ZERO + destStr;
        int y1 = 0;
        int y2 = XStr.charAt(XStr.length() - 1) - '0';
        for (int i = 0; i < length; i++) {
            if (y2 - y1 == -1) {
                XStr = adder(XStr.substring(0, length), X, 0, length) + XStr.substring(length);
            } else if (y2 - y1 == 1) {
                XStr = adder(XStr.substring(0, length), negX, 0, length) + XStr.substring(length);
            }
            XStr = XStr.charAt(0) + XStr.substring(0, XStr.length() - 1);//算数右移
            y1 = y2;
            y2 = XStr.charAt(XStr.length() - 1) - '0';
        }
        String lower = XStr.substring(length);
        return new DataType(lower);
    }
    /**
     * 返回两个二进制整数的除法结果
     * 请注意使用不恢复余数除法方式实现
     * dest ÷ src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits
     */
    public DataType div(DataType src, DataType dest) {
        String YStr = src.toString();
        String destStr = dest.toString();
        if (isZero(YStr)) throw new ArithmeticException();
        int len = 32;
        String negYStr = oneAdder(negation(YStr)).substring(1);
        String XStr = impleDigits(destStr, len * 2);
        String QStr = "";
        if (destStr.charAt(0) == YStr.charAt(0)) {
            XStr = adder(XStr.substring(0, len), negYStr, 0, len) + XStr.substring(len);
        } else {
            XStr = adder(XStr.substring(0, len), YStr, 0, len) + XStr.substring(len);
        }
        for (int i = 0; i < len; i++) {
            if (XStr.charAt(0) == YStr.charAt(0)) {
                QStr += "1";
                XStr = leftShift(XStr, 1);
                XStr = adder(XStr.substring(0, len), negYStr, 0, len) + XStr.substring(len);
            } else {
                QStr += "0";
                XStr = leftShift(XStr, 1);
                XStr = adder(XStr.substring(0, len), YStr, 0, len) + XStr.substring(len);
            }
        }
        QStr = QStr.substring(1);
        QStr += XStr.charAt(0) == YStr.charAt(0) ? "1" : "0";
        if (destStr.charAt(0) != YStr.charAt(0)) {
            QStr = oneAdder(QStr).substring(1);
        }
        String remainder = XStr.substring(0, len);
        if (destStr.charAt(0) != remainder.charAt(0)) {
            if (destStr.charAt(0) == YStr.charAt(0)) {
                remainder = adder(remainder, YStr, 0, len);
            } else {
                remainder = adder(remainder, negYStr, 0, len);
            }
        }
        if (remainder.equals(YStr)) {
            remainderReg = new DataType(ZERO);
            return new DataType(oneAdder(QStr).substring(1));
        }
        if (isZero(adder(remainder, YStr, 0, len))) {
            remainderReg = new DataType(ZERO);
            return new DataType(adder(QStr, NegOne, 0, len));
        }
        remainderReg = new DataType(remainder);

        return new DataType(QStr);
    }

    public String adder(String add1, String add2, int carry, int length) {
        return carry_adder(add1, add2, carry, length).substring(1);
    }

    public String impleDigits(String oprand, int length) {
        int len = length - oprand.length();
        char sign = oprand.charAt(0);
        StringBuilder res = new StringBuilder(oprand);
        for (int i = 0; i < len; i++) {
            res.insert(0, sign);
        }
        return res.toString();
    }

    public boolean isZero(String s) {
        for (char c : s.toCharArray()) {
            if (c == '1') return false;
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

    public String carry_adder(String add1, String add2, int carry, int length) {
        StringBuilder ans = new StringBuilder();
        int x, y;
        for (int i = length - 1; i >= 0; i--) {//顺序是从低位到高位
            x = add1.charAt(i) - '0';
            y = add2.charAt(i) - '0';
            ans.insert(0, x ^ y ^ carry);
            carry = x & carry | y & carry | x & y;
        }
        return carry + ans.toString();
    }

}
