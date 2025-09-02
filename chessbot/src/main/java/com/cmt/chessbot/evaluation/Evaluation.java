package com.cmt.chessbot.evaluation;

import com.cmt.chessbot.engine.Board;

public class Evaluation {
    private static final int[] V = new int[128];
    static {
        V['P']=100; V['N']=320; V['B']=330; V['R']=500; V['Q']=900; V['K']=20000;
        V['p']=-100; V['n']=-320; V['b']=-330; V['r']=-500; V['q']=-900; V['k']=-20000;
    }

    public static int evaluate(Board board){
        int s=0;
        char[][] a = board.board();
        for (int r=0;r<8;r++) for (int c=0;c<8;c++){
            s += V[a[r][c]];
        }
        return s;
    }
}
