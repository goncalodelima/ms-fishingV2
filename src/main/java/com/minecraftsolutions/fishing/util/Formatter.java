package com.minecraftsolutions.fishing.util;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

public class Formatter {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

    private final List<String> suffixes;

    public Formatter() {
        suffixes = Arrays.asList(
                "", "K", "M", "B", "T", "Q", "QQ", "S", "SS", "OC",
                "N", "D", "UN", "DD", "TR", "QT", "QN", "SD", "SPD",
                "OD", "ND", "VG", "UVG", "DVG", "TVG", "QTV", "QNV", "SEV",
                "SPV", "OVG", "NVG", "TG"
        );
    }

    public String formatNumber(double value) {
        boolean negative = value < 0;
        int index = 0;
        value = Math.abs(value);

        double tmp;
        while ((tmp = value / 1000) >= 1) {
            if (index + 1 == suffixes.size())
                break;

            value = tmp;
            ++index;
        }

        return (negative ? "-" : "") + DECIMAL_FORMAT.format(value) + suffixes.get(index);
    }

    public double parseFormattedNumber(String formattedNumber) {

        formattedNumber = formattedNumber.toUpperCase();
        boolean negative = formattedNumber.startsWith("-");
        if (negative) {
            formattedNumber = formattedNumber.substring(1);
        }

        int suffixIndex = -1;
        for (int i = suffixes.size() - 1; i >= 1 ; i--) {
            String suffix = suffixes.get(i);
            if (formattedNumber.endsWith(suffix)) {
                suffixIndex = i;
                formattedNumber = formattedNumber.replace(suffix, "");
                break;
            }
        }

        double value;
        try {
            value = Double.parseDouble(formattedNumber);

            if (Double.isNaN(value) || Double.isInfinite(value))
                return -1;

        } catch (NumberFormatException e) {
            return -1;
        }

        for (int i = 1; i <= suffixIndex; i++) {
            value *= 1000;
        }

        return negative ? -value : value;
    }

}
