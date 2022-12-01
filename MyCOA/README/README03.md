# COA2022-programming03

> Good luck and have fun!



## 1 实验要求

在ALU类中实现实现整数的二进制乘法(要求使用布斯乘法实现)。

输入和输出均为32位二进制补码，计算结果直接截取低32位作为最终输出

``` java
 public DataType mul(DataType src, DataType dest)
```



## 2 实验攻略

本次实验我们仍然**明确禁止**，将传入的参数通过transformer转化为int，再通过整数的四则运算后，将结果重新转化为DataType完成实验。
