package util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MyTransformerTest {

    @Test
    public void intToBinaryTest1() {
        assertEquals("00000000000000000000000000000010", MyTransformer.intToBinary("2"));
    }

    @Test
    public void binaryToIntTest1() {
        assertEquals("2", MyTransformer.binaryToInt("00000000000000000000000000000010"));
    }

    @Test
    public void decimalToNBCDTest1() {
        assertEquals("11000000000000000000000000010000", MyTransformer.decimalToNBCD("10"));
    }

    @Test
    public void NBCDToDecimalTest1() {
        assertEquals("10", MyTransformer.NBCDToDecimal("11000000000000000000000000010000"));
    }

    @Test
    public void floatToBinaryTest1() {
        assertEquals("00000000010000000000000000000000", MyTransformer.floatToBinary(String.valueOf(Math.pow(2, -127))));
    }

    @Test
    public void floatToBinaryTest2() {
        assertEquals("+Inf", MyTransformer.floatToBinary("" + Double.MAX_VALUE)); // 对于float来说溢出
    }

    @Test
    public void binaryToFloatTest1() {
        assertEquals(String.valueOf((float) Math.pow(2, -127)), MyTransformer.binaryToFloat("00000000010000000000000000000000"));
    }

}
