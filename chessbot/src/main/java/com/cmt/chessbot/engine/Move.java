package com.cmt.chessbot.engine;

public class Move {
    public static final int CAPTURE = 1;
    public static final int PROMOTION = 2;
    public static final int EN_PASSANT = 4;
    public static final int CASTLE = 8;
    public static final int DOUBLE_PAWN = 16;

    public final int fr, fc, tr, tc;
    public final char piece; // moving piece before move
    public final char promo; // 'Q','R','B','N' or 0
    public final int flags;

    public Move(int fr, int fc, int tr, int tc, char piece, char promo, int flags) {
        this.fr = fr; this.fc = fc; this.tr = tr; this.tc = tc;
        this.piece = piece; this.promo = promo; this.flags = flags;
    }

    public String uci() {
        return Util.moveToUci(fr, fc, tr, tc, promo);
    }

    @Override public String toString() { return uci(); }
}
