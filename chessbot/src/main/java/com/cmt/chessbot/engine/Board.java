package com.cmt.chessbot.engine;

import java.util.ArrayDeque;
import java.util.Deque;

public class Board {

    private final char[][] b = new char[8][8];
    private boolean whiteToMove;
    private int castling; // 1=K,2=Q,4=k,8=q
    private int epFile;   // -1 if none
    private int halfmoveClock;
    private int fullmoveNumber;

    private final Deque<State> history = new ArrayDeque<>();

    public static Board fromFEN(String fen){
        Board bd = new Board();
        bd.loadFEN(fen);
        return bd;
    }

    private void loadFEN(String fen){
        String[] parts = fen.trim().split("\\s+");
        String[] rows = parts[0].split("/");
        for (int r=0; r<8; r++){
            int c=0;
            for (char ch : rows[r].toCharArray()){
                if (Character.isDigit(ch)){
                    int n = ch - '0';
                    for (int k=0;k<n;k++) b[r][c++]='.';
                } else {
                    b[r][c++] = ch;
                }
            }
        }
        whiteToMove = parts[1].equals("w");
        castling = 0;
        if (parts[2].contains("K")) castling |= 1;
        if (parts[2].contains("Q")) castling |= 2;
        if (parts[2].contains("k")) castling |= 4;
        if (parts[2].contains("q")) castling |= 8;
        epFile = parts[3].equals("-") ? -1 : (parts[3].charAt(0) - 'a');
        halfmoveClock = Integer.parseInt(parts[4]);
        fullmoveNumber = Integer.parseInt(parts[5]);
        history.clear();
    }

    public String toFEN(){
        StringBuilder sb = new StringBuilder();
        for (int r=0;r<8;r++){
            int run=0;
            for (int c=0;c<8;c++){
                char p = b[r][c];
                if (p=='.') run++;
                else{
                    if (run>0){ sb.append(run); run=0; }
                    sb.append(p);
                }
            }
            if (run>0) sb.append(run);
            if (r<7) sb.append('/');
        }
        sb.append(whiteToMove? " w " : " b ");
        String cr="";
        if ((castling&1)!=0) cr+="K";
        if ((castling&2)!=0) cr+="Q";
        if ((castling&4)!=0) cr+="k";
        if ((castling&8)!=0) cr+="q";
        sb.append(cr.isEmpty()? "-" : cr).append(" ");
        if (epFile==-1) sb.append("- "); else sb.append((char)('a'+epFile)).append('3').append(" ");
        sb.append(halfmoveClock).append(" ").append(fullmoveNumber);
        return sb.toString();
    }

    public char[][] board(){ return b; }
    public boolean whiteToMove(){ return whiteToMove; }
    public int getCastling(){ return castling; }
    public int getEpFile(){ return epFile; }
    public int getHalfmoveClock(){ return halfmoveClock; }
    public int getFullmoveNumber(){ return fullmoveNumber; }

    public void make(Move m){
        char captured = b[m.tr][m.tc];
        history.push(new State(castling, epFile, halfmoveClock, fullmoveNumber, captured));

        if (Character.toLowerCase(m.piece)=='p' || captured!='.') halfmoveClock = 0;
        else halfmoveClock++;
        if (!whiteToMove) fullmoveNumber++;

        epFile = -1;

        if ((m.flags & Move.EN_PASSANT) != 0){
            int dir = Util.isWhite(m.piece) ? 1 : -1;
            b[m.tr + dir][m.tc] = '.';
        }

        b[m.tr][m.tc] = (m.flags & Move.PROMOTION) != 0 ? m.promo : m.piece;
        b[m.fr][m.fc] = '.';

        if ((m.flags & Move.CASTLE) != 0){
            if (m.tc == 6){
                int r = m.tr;
                b[r][5] = b[r][7];
                b[r][7] = '.';
            } else if (m.tc == 2){
                int r = m.tr;
                b[r][3] = b[r][0];
                b[r][0] = '.';
            }
        }

        if ((m.flags & Move.DOUBLE_PAWN) != 0){
            epFile = m.fc;
        }

        // update castling rights
        if (m.piece=='K') castling &= ~(1|2);
        if (m.piece=='k') castling &= ~(4|8);
        if (m.piece=='R' && m.fr==7 && m.fc==0) castling &= ~2;
        if (m.piece=='R' && m.fr==7 && m.fc==7) castling &= ~1;
        if (m.piece=='r' && m.fr==0 && m.fc==0) castling &= ~8;
        if (m.piece=='r' && m.fr==0 && m.fc==7) castling &= ~4;

        if (captured=='R' && m.tr==7 && m.tc==0) castling &= ~2;
        if (captured=='R' && m.tr==7 && m.tc==7) castling &= ~1;
        if (captured=='r' && m.tr==0 && m.tc==0) castling &= ~8;
        if (captured=='r' && m.tr==0 && m.tc==7) castling &= ~4;

        whiteToMove = !whiteToMove;
    }

    public void undo(Move m){
        State st = history.pop();
        whiteToMove = !whiteToMove;

        if ((m.flags & Move.CASTLE) != 0){
            if (m.tc==6){
                int r=m.tr;
                b[r][7] = b[r][5]; b[r][5]='.';
            } else if (m.tc==2){
                int r=m.tr;
                b[r][0] = b[r][3]; b[r][3]='.';
            }
        }

        b[m.fr][m.fc] = m.piece;
        b[m.tr][m.tc] = st.captured;

        if ((m.flags & Move.EN_PASSANT) != 0){
            int dir = Util.isWhite(m.piece)? 1 : -1;
            b[m.tr + dir][m.tc] = (char)(Util.isWhite(m.piece)? 'p' : 'P');
            b[m.tr][m.tc] = '.';
        }

        this.castling = st.castling;
        this.epFile = st.epFile;
        this.halfmoveClock = st.halfmoveClock;
        this.fullmoveNumber = st.fullmoveNumber;
    }

    public boolean squareAttacked(int r, int c, boolean byWhite){
        int dir = byWhite? -1 : 1;
        int pr = r + dir;
        if (Util.inBounds(pr, c-1) && b[pr][c-1] == (byWhite? 'P':'p')) return true;
        if (Util.inBounds(pr, c+1) && b[pr][c+1] == (byWhite? 'P':'p')) return true;

        int[][] kdirs = {{2,1},{2,-1},{-2,1},{-2,-1},{1,2},{1,-2},{-1,2},{-1,-2}};
        for (int[] d : kdirs){
            int nr=r+d[0], nc=c+d[1];
            if (Util.inBounds(nr,nc) && b[nr][nc] == (byWhite? 'N':'n')) return true;
        }

        for (int dr=-1; dr<=1; dr++) for (int dc=-1; dc<=1; dc++){
            if (dr==0 && dc==0) continue;
            int nr=r+dr, nc=c+dc;
            if (Util.inBounds(nr,nc) && b[nr][nc] == (byWhite? 'K':'k')) return true;
        }

        int[][] bdirs = {{1,1},{1,-1},{-1,1},{-1,-1}};
        for (int[] d: bdirs){
            int nr=r+d[0], nc=c+d[1];
            while (Util.inBounds(nr,nc)){
                char p = b[nr][nc];
                if (p!='.'){
                    if (byWhite? (p=='B'||p=='Q') : (p=='b'||p=='q')) return true;
                    break;
                }
                nr+=d[0]; nc+=d[1];
            }
        }

        int[][] rdirs = {{1,0},{-1,0},{0,1},{0,-1}};
        for (int[] d: rdirs){
            int nr=r+d[0], nc=c+d[1];
            while (Util.inBounds(nr,nc)){
                char p = b[nr][nc];
                if (p!='.'){
                    if (byWhite? (p=='R'||p=='Q') : (p=='r'||p=='q')) return true;
                    break;
                }
                nr+=d[0]; nc+=d[1];
            }
        }

        return false;
    }

    public int[] kingSquare(boolean white){
        for (int r=0;r<8;r++) for (int c=0;c<8;c++){
            if (b[r][c] == (white? 'K':'k')) return new int[]{r,c};
        }
        return null;
    }
}
