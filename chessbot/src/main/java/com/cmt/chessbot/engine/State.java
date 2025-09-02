package com.cmt.chessbot.engine;

public class State {
    public final int castling;
    public final int epFile;
    public final int halfmoveClock;
    public final int fullmoveNumber;
    public final char captured;

    public State(int castling, int epFile, int halfmoveClock, int fullmoveNumber, char captured) {
        this.castling = castling;
        this.epFile = epFile;
        this.halfmoveClock = halfmoveClock;
        this.fullmoveNumber = fullmoveNumber;
        this.captured = captured;
    }
}
