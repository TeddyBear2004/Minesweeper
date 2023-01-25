package de.teddybear2004.minesweeper.util;

public class Time {

    public static String parse(boolean big, long milli) {
        long days = big ? milli / 86400000 : 0;
        milli %= 86400000;
        long hours = big ? milli / 3600000 : 0;
        milli %= 3600000;
        long minutes = milli / 60000;
        milli %= 60000;
        long seconds = milli / 1000;
        milli %= 1000;
        long milliseconds = milli;

        return (big
                ? days + ":" +
                String.format("%02d", hours) + ":"
                : "") +
                String.format("%02d", minutes) + ":" +
                String.format("%02d", seconds) + ":" +
                String.format("%03d", milliseconds);
    }

}
