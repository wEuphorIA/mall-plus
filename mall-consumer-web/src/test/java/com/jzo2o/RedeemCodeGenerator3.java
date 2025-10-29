package com.jzo2o;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2024/6/3 19:53
 */


import cn.hutool.core.util.RandomUtil;

import java.util.HashSet;
import java.util.Random;

/**
 * 生成兑换码的工具类。
 * 该类提供了生成指定数量独特兑换码的功能。
 */
public class RedeemCodeGenerator3 {


    private static final char[] BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();

    public static void main(String[] args) {
        //********* 方案一 ************
        int length = 6; // 兑换码长度

        // 随机6次，分别得到6个字符
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = RandomUtil.randomInt(0, 32);
            sb.append(BASE32_CHARS[index]);
        }

        System.out.println(sb);


        //********* 方案二 ************
        String randomString = RandomUtil.randomString("ABCDEFGHIJKLMNOPQRSTUVWXYZ234567", length);
        System.out.println(randomString);


        //TODO 之后再将上述生成的兑换码到数据库中比对
    }
}
