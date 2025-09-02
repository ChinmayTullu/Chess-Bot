package com.cmt.chessbot.engine;

import java.util.ArrayList;
import java.util.List;

public class MoveGen {

    public static List<Move> legalMoves(Board bd){
        List<Move> pseudo = pseudoLegal(bd);
        List<Move> legal = new ArrayList<>(pseudo.size());
        for (Move m : pseudo){
            bd.make(m);
            int[] king = bd.kingSquare(!bd.whiteToMove());
            boolean inCheck = bd.squareAttacked(king[0], king[1], bd.whiteToMove());
            bd.undo(m);
            if (!inCheck) legal.add(m);
        }
        return legal;
    }

    public static List<Move> pseudoLegal(Board bd){
        char[][] b = bd.board();
        boolean white = bd.whiteToMove();
        List<Move> out = new ArrayList<>(64);
        for (int r=0;r<8;r++) for (int c=0;c<8;c++){
            char p = b[r][c];
            if (p=='.' || (white != Util.isWhite(p))) continue;
            switch (Character.toLowerCase(p)){
                case 'p': pawnMoves(bd, r, c, p, out); break;
                case 'n': knightMoves(b, r, c, p, out); break;
                case 'b': slideMoves(b, r, c, p, out, new int[][]{{1,1},{1,-1},{-1,1},{-1,-1}}); break;
                case 'r': slideMoves(b, r, c, p, out, new int[][]{{1,0},{-1,0},{0,1},{0,-1}}); break;
                case 'q': slideMoves(b, r, c, p, out, new int[][]{{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}}); break;
                case 'k': kingMoves(bd, r, c, p, out); break;
            }
        }
        return out;
    }

    private static void pawnMoves(Board bd, int r, int c, char p, List<Move> out){
        char[][] b = bd.board();
        boolean white = Util.isWhite(p);
        int dir = white? -1 : 1;
        int startRank = white? 6 : 1;
        int promoRank = white? 0 : 7;

        int nr = r + dir;
        if (Util.inBounds(nr,c) && b[nr][c]=='.'){
            addPawnMove(out, r,c,nr,c,p, false, false, false);
            if (r == startRank && Util.inBounds(nr+dir,c) && b[nr+dir][c]=='.'){
                out.add(new Move(r,c,nr+dir,c,p,(char)0, Move.DOUBLE_PAWN));
            }
        }
        for (int dc : new int[]{-1, 1}){
            int nc = c + dc;
            if (!Util.inBounds(nr, nc)) continue;
            char tgt = b[nr][nc];
            if (tgt!='.' && Util.isWhite(tgt)!=white){
                addPawnMove(out, r,c,nr,nc,p, true, false, false);
            }
        }

        int epFile = bd.getEpFile();
        if (epFile != -1){
            if ((r == (white?3:4)) && Math.abs(epFile - c) == 1){
                int nc = epFile;
                if (Util.inBounds(nr,nc)){
                    out.add(new Move(r,c,nr,nc,p,(char)0, Move.EN_PASSANT | Move.CAPTURE));
                }
            }
        }
    }

    private static void addPawnMove(List<Move> out, int fr,int fc,int tr,int tc, char p, boolean isCapture, boolean isEP, boolean isDouble){
        boolean white = Util.isWhite(p);
        int flags = 0;
        if (isCapture) flags |= Move.CAPTURE;
        if (isEP) flags |= Move.EN_PASSANT;
        if (isDouble) flags |= Move.DOUBLE_PAWN;
        int promoRank = white? 0 : 7;

        if (tr == promoRank){
            out.add(new Move(fr,fc,tr,tc,p, (white? 'Q':'q'), flags | Move.PROMOTION));
            out.add(new Move(fr,fc,tr,tc,p, (white? 'R':'r'), flags | Move.PROMOTION));
            out.add(new Move(fr,fc,tr,tc,p, (white? 'B':'b'), flags | Move.PROMOTION));
            out.add(new Move(fr,fc,tr,tc,p, (white? 'N':'n'), flags | Move.PROMOTION));
        } else {
            out.add(new Move(fr,fc,tr,tc,p,(char)0, flags));
        }
    }

    private static void knightMoves(char[][] b, int r, int c, char p, List<Move> out){
        int[][] d={{2,1},{2,-1},{-2,1},{-2,-1},{1,2},{1,-2},{-1,2},{-1,-2}};
        boolean white = Util.isWhite(p);
        for (int[] x: d){
            int nr=r+x[0], nc=c+x[1];
            if (!Util.inBounds(nr,nc)) continue;
            char t=b[nr][nc];
            if (t=='.' || Util.isWhite(t)!=white){
                out.add(new Move(r,c,nr,nc,p,(char)0, t!='.'? Move.CAPTURE:0));
            }
        }
    }

    private static void slideMoves(char[][] b, int r, int c, char p, List<Move> out, int[][] dirs){
        boolean white = Util.isWhite(p);
        for (int[] d: dirs){
            int nr=r+d[0], nc=c+d[1];
            while (Util.inBounds(nr,nc)){
                char t=b[nr][nc];
                if (t=='.'){ out.add(new Move(r,c,nr,nc,p,(char)0,0)); }
                else{
                    if (Util.isWhite(t)!=white) out.add(new Move(r,c,nr,nc,p,(char)0, Move.CAPTURE));
                    break;
                }
                nr+=d[0]; nc+=d[1];
            }
        }
    }

    private static void kingMoves(Board bd, int r, int c, char p, List<Move> out){
        char[][] b = bd.board();
        boolean white = Util.isWhite(p);
        for (int dr=-1; dr<=1; dr++) for (int dc=-1; dc<=1; dc++){
            if (dr==0 && dc==0) continue;
            int nr=r+dr, nc=c+dc;
            if (!Util.inBounds(nr,nc)) continue;
            char t=b[nr][nc];
            if (t=='.' || Util.isWhite(t)!=white){
                out.add(new Move(r,c,nr,nc,p,(char)0, t!='.'? Move.CAPTURE:0));
            }
        }

        int rights = bd.getCastling();
        if (white){
            if ((rights & 1)!=0){
                if (b[7][5]=='.' && b[7][6]=='.' &&
                    !bd.squareAttacked(7,4,false) && !bd.squareAttacked(7,5,false) && !bd.squareAttacked(7,6,false)){
                    out.add(new Move(7,4,7,6,'K',(char)0, Move.CASTLE));
                }
            }
            if ((rights & 2)!=0){
                if (b[7][3]=='.' && b[7][2]=='.' && b[7][1]=='.' &&
                    !bd.squareAttacked(7,4,false) && !bd.squareAttacked(7,3,false) && !bd.squareAttacked(7,2,false)){
                    out.add(new Move(7,4,7,2,'K',(char)0, Move.CASTLE));
                }
            }
        } else {
            if ((rights & 4)!=0){
                if (b[0][5]=='.' && b[0][6]=='.' &&
                    !bd.squareAttacked(0,4,true) && !bd.squareAttacked(0,5,true) && !bd.squareAttacked(0,6,true)){
                    out.add(new Move(0,4,0,6,'k',(char)0, Move.CASTLE));
                }
            }
            if ((rights & 8)!=0){
                if (b[0][3]=='.' && b[0][2]=='.' && b[0][1]=='.' &&
                    !bd.squareAttacked(0,4,true) && !bd.squareAttacked(0,3,true) && !bd.squareAttacked(0,2,true)){
                    out.add(new Move(0,4,0,2,'k',(char)0, Move.CASTLE));
                }
            }
        }
    }
}
