package util;

public class SizeUtil {
    public static String toNumInUnits(long bytes) {
        int u = 0;
        for ( ; bytes > 1024*1024; bytes >>= 10) {
            u++;
        }
        if (bytes > 1024)
            u++;
        return String.format("%.1f %cB", bytes/1024f, " kMGTPE".charAt(u));
    }
}
