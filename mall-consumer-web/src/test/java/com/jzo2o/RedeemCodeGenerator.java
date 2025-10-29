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
public class RedeemCodeGenerator {


    /**
     * 生成指定数量的兑换码。
     * 兑换码由8个字符组成，字符集包括0-9、A-Z和a-z。
     *
     * @param numberOfCodes 要生成的兑换码数量。
     * @return 包含指定数量独特兑换码的集合。
     */
    public static Set<String> generateRedeemCodes(int numberOfCodes) {
        Set<String> codes = new HashSet<>();
        Random random = new Random();

        while (codes.size() < numberOfCodes) {
            String code = generateRandomCode();
            codes.add(code);
        }
        System.out.println(codes.size());
        return codes;
    }

    /**
     * 生成一个随机的8字符兑换码。
     *
     * @return 8个字符组成的随机兑换码。
     */
    private static String generateRandomCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(8);

        for (int i = 0; i < 8; i++) {
            int charIndex = random.nextInt(62);
            char randomChar;
            if (charIndex < 10) {
                randomChar = (char) ('0' + charIndex);
            } else if (charIndex < 36) {
                randomChar = (char) ('A' + charIndex - 10);
            } else {
                randomChar = (char) ('a' + charIndex - 36);
            }
            sb.append(randomChar);
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        Set<String> redeemCodes = generateRedeemCodes(1000);
        redeemCodes.forEach(System.out::println);
    }
}
