package com.cmt.chessbot.engine;

public final class Util {
    private Util() {}

    public static final String START_FEN =
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    public static boolean isWhite(char p){ return Character.isUpperCase(p); }
    public static boolean isBlack(char p){ return Character.isLowerCase(p); }
    public static boolean isEmpty(char p){ return p == '.'; }

    public static boolean inBounds(int r, int c){
        return r >= 0 && r < 8 && c >= 0 && c < 8;
    }

    public static String moveToUci(int fr, int fc, int tr, int tc, char promo){
        String s = "" + (char)('a' + fc) + (8 - fr) + (char)('a' + tc) + (8 - tr);
        if (promo != 0) s += Character.toLowerCase(promo);
        return s;
    }
}
