# COA2022-programming02

> Good luck and have fun!



## 1 实验要求

在ALU类中实现2个方法，具体如下

1.计算两个32位二进制整数补码真值的和

``` java
 public DataType add(DataType src, DataType dest)
```

2.计算两个32位二进制整数补码真值的差，dest表示被减数，src表示减数(即计算dest - src)

``` java
 public DataType sub(DataType src, DataType dest) 
```



## 2 实验攻略

### 2.1 代码实现要求
有些同学可能注意到，将传入的参数通过transformer转化为int，再通过整数的加减运算后，将结果重新转化为DataType即可轻松完成实验。在此，我们**明确禁止**各位采用这种方法来完成本次实验。

### 2.2 数据封装
从本次实验开始，我们采用统一的类DataType来封装32位的二进制数，包括二进制补码整数、NBCD码与IEEE754浮点数。核心数据结构如下
``` java
private final byte[] data = new byte[4];
```
采用这样的数据封装将保证DataType类中存放的一定是32位二进制数，并且有利于ALU等运算模块与其他模块的整合。为了方便编码，我们为DataType类提供了构造函数与toString函数，便于DataType对象与String对象之间的转化，具体可阅读DataType类源码。