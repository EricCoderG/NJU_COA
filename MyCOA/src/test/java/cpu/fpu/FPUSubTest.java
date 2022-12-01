package cpu.fpu;

import org.junit.Test;
import util.DataType;
import util.Transformer;

import static org.junit.Assert.assertEquals;

public class FPUSubTest {

    private final FPU fpu = new FPU();
    private DataType src;
    private DataType dest;
    private DataType result;


    @Test
    public void fpuSubTest1(){
        src = new DataType("11000001110101100000000000000000");
        dest = new DataType("01000000111011011100000000000000");
        result = fpu.sub(src, dest);
        assertEquals("01000010000010001011100000000000", result.toString());
    }

    @Test
    public void fpuSubTest9() {
        float pZero = Float.parseFloat(Transformer.binaryToFloat("00000000000000000000000000000000"));
        float nZero = Float.parseFloat(Transformer.binaryToFloat("10000000000000000000000000000000"));
        float pInf = Float.POSITIVE_INFINITY;
        float nInf = Float.NEGATIVE_INFINITY;
        float NAN = Float.NaN;
        float deNorm1 = Float.parseFloat(Transformer.binaryToFloat("00000000000000000000000000000001"));
        float deNorm2 = Float.parseFloat(Transformer.binaryToFloat("00000000000000000000000000000010"));
        float deNorm3 = Float.parseFloat(Transformer.binaryToFloat("10000000010000000000000000000000"));
        float small1 = Float.parseFloat(Transformer.binaryToFloat("00000000100000000000000000000000"));
        float small2 = Float.parseFloat(Transformer.binaryToFloat("00000000100000000000000000000001"));
        float big1 = Float.parseFloat(Transformer.binaryToFloat("01111111000000000000000000000001"));
        float big2 = Float.parseFloat(Transformer.binaryToFloat("11111111000000000000000000000001"));
        float[] input = {pZero, nZero, pInf, nInf, NAN, deNorm1, deNorm2, deNorm3, small1, small2, big1, big2, 10000000, 1.2f, 1.1f, 1, 0.9f, 0.8f, 0.7f, 0.6f, 0.5f, 0.4f, 0.3f, 0.2f, 0.1f, -0.1f, -0.2f, -0.3f, -0.4f, -0.5f, -0.6f, -0.7f, -0.8f, -0.9f, -1, -10000000};

        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input.length; j++) {
                src = new DataType(Transformer.intToBinary(Integer.toString(Float.floatToIntBits(input[i]))));
                dest = new DataType(Transformer.intToBinary(Integer.toString(Float.floatToIntBits(input[j]))));
                result = fpu.sub(src, dest);
                assertEquals(Transformer.intToBinary(Integer.toString(Float.floatToIntBits(input[j] - input[i]))), result.toString());
            }
        }
    }

}
