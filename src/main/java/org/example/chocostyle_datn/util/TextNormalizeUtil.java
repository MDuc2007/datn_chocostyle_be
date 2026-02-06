package org.example.chocostyle_datn.util;

import java.text.Normalizer;

public class TextNormalizeUtil {

    public static String normalize(String input) {
        if (input == null) return "";

        return Normalizer.normalize(input, Normalizer.Form.NFD)
//                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "") // bỏ dấu
                .toLowerCase()
                .replaceAll("\\s+", " ") // nhiều space -> 1
                .trim();
    }
}

