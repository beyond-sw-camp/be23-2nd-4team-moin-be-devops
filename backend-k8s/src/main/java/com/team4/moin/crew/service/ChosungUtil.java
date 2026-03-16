package com.team4.moin.crew.service;

public class ChosungUtil {
    private static final char[] CHOSUNG = {'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'};

    public static String extractChosung(String text) {
        if (text == null) return "";

        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            // 한글 '가' ~ '힣' 사이일 경우 초성 추출
            if (c >= 0xAC00 && c <= 0xD7A3) {
                int chosungIndex = (c - 0xAC00) / (21 * 28);
                sb.append(CHOSUNG[chosungIndex]);
            } else {
                // 한글이 아닌 영어, 숫자, 띄어쓰기는 그대로 유지
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
