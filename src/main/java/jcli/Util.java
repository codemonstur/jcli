package jcli;

public enum Util {;

    public static boolean isNullOrEmpty(final String value) {
        return value == null || value.isEmpty();
    }

    public static String padRight(final String s, final int n) {
        if (s.length() == n) return s;
        return String.format("%1$-" + n + "s", s);
    }

}
