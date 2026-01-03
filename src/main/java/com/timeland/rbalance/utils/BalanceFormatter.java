package com.timeland.rbalance.utils;

import java.text.DecimalFormat;

public class BalanceFormatter {
    private static final DecimalFormat FORMAT = new DecimalFormat("#,##0.0");

    public static String format(double amount) {
        return FORMAT.format(amount);
    }
}
