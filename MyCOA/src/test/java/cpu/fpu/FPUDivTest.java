package cpu.fpu;

import org.junit.Test;
import util.DataType;
import util.Transformer;

import static org.junit.Assert.assertEquals;

public class FPUDivTest {

    private final FPU fpu = new FPU();
    private DataType src;
    private DataType dest;
    private DataType result;

    @Test
    public void fpuDivTest1(){
        dest = new DataType(Transformer.floatToBinary( "0.4375" ));
        src = new DataType(Transformer.floatToBinary( "0.5" ));
        result = fpu.div(src, dest);
        assertEquals(Transformer.floatToBinary( "0.875" ), result.toString());
    }

    @Test
    public void fpuDivTest2(){
        dest = new DataType(Transformer.floatToBinary( "1.0" ));
        src = new DataType(Transformer.floatToBinary( "4.0" ));
        result = fpu.div(src, dest);
        assertEquals(Transformer.floatToBinary( "0.25" ), result.toString());
    }

    @Test
    public void fpuDivTest3(){
        dest = new DataType(Transformer.floatToBinary( "-2.0" ));
        src = new DataType(Transformer.floatToBinary( "1.0" ));
        result = fpu.div(src, dest);
        assertEquals(Transformer.floatToBinary( "-2.0" ), result.toString());
    }

    @Test
    public void fpuDivTest4(){
        dest = new DataType(Transformer.floatToBinary( "1" ));
        src = new DataType(Transformer.floatToBinary( "-2.0" ));
        result = fpu.div(src, dest);
        assertEquals(Transformer.floatToBinary( "-0.5" ), result.toString());
    }

    @Test
    public void fpuDivTest5(){
        dest = new DataType(Transformer.floatToBinary( "0.4375" ));
        src = new DataType(Transformer.floatToBinary( "0.625" ));
        result = fpu.div(src, dest);
        assertEquals(Transformer.floatToBinary("0.7"), result.toString());
    }


    @Test(expected = ArithmeticException.class)
    public void fpuDivExceptionTest(){
        dest = new DataType(Transformer.floatToBinary( "2.2" ));
        src = new DataType(Transformer.floatToBinary( "0.0" ));
        result = fpu.div(src, dest);
    }

}
