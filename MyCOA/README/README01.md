# COA2022-programming01

> Good luck and have fun!



## 1. 实验要求

在Transformer类中实现以下6个方法

1. 将整数真值（十进制表示）转化成补码表示的二进制，默认长度32位

``` java
 public String intToBinary(String numStr) 
```


2. 将补码表示的二进制转化成整数真值（十进制表示）

``` java
 public String binaryToInt(String binStr)
```


3. 将十进制整数的真值转化成NBCD表示（符号位用4位表示）

``` java
public String decimalToNBCD(String decimal)
```


4. 将NBCD表示（符号位用4位表示）转化成十进制整数的真值

``` java
public String NBCDToDecimal(String NBCDStr)
```

5. 将浮点数真值转化成32位单精度浮点数表示

   - 负数以"-"开头，正数不需要正号

   - 考虑正负无穷的溢出（"+Inf", "-Inf"，见测试用例格式）


```java
public String floatToBinary(String floatStr)
```

6. 将32位单精度浮点数表示转化成浮点数真值
   - 特殊情况同上


```java
public String binaryToFloat(String binStr)
```



## 2. 实验攻略

本次实验推荐使用的库函数有

```java
Integer.parseInt(String s)
Float.parseFloat(String s)
String.valueOf(int i)
String.valueOf(float f)
```

本次实验不允许使用的库函数有

```java
Integer.toBinaryString(int i)
Float.floatToIntBits(float value)
Float.intBitsToFloat(int bits)
```

