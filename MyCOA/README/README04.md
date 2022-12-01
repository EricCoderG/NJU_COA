# COA2022-programming04

> Good luck and have fun!



## 1 实验要求

实现整数的二进制除法 (dest ÷ src)，使用恢复余数除法和不恢复余数除法均可。输入为32位二进制补码，输出为32位商，并且将32位余数正确储存在余数寄存器remainderReg中。

注意：除数为0，且被除数不为0时要求能够正确抛出ArithmeticException异常。

``` java
 public DataType div(DataType src, DataType dest)
```

## 

## 2 实验攻略

本次实验我们仍然**明确禁止**，将传入的参数通过transformer转化为int，再通过整数的四则运算后，将结果重新转化为DataType完成实验。