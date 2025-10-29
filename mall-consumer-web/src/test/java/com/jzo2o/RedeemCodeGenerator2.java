package com.jzo2o;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2024/6/3 19:53
 */

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * 生成兑换码的工具类。
 * 该类提供了生成指定数量独特兑换码的功能。
 */
public class RedeemCodeGenerator2 {


    private static final char[] BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();


    public static String generator(int inputNumber){
        // 将int数字转换为32位二进制字符串
        String binaryString = String.format("%32s", Integer.toBinaryString(inputNumber)).replace(' ', '0');

        // 在32位二进制字符串前补3位随机二进制
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            sb.append(random.nextInt(2));
        }
        sb.append(binaryString);

        // 将35位二进制字符串按照5位一组映射为Base32字符
        StringBuilder base32String = new StringBuilder();
        for (int i = 0; i < sb.length(); i += 5) {
            String chunk = sb.substring(i, Math.min(i + 5, sb.length()));
            int decimalValue = Integer.parseInt(chunk, 2);
            base32String.append(BASE32_CHARS[decimalValue]);
        }

        System.out.println("Base32编码结果: " + base32String.toString());
        return base32String.toString();
    }
    public static void main(String[] args) {
//        int inputNumber = 3; // 随机输入的int数字
        HashSet<String> set = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            String generator = generator(i);
            set.add(generator);
            System.out.println(generator);
        }

        System.out.println(set.size());

    }
}
