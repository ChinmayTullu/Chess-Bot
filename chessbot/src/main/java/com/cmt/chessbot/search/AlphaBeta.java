package com.cmt.chessbot.search;

import com.cmt.chessbot.engine.Board;
import com.cmt.chessbot.engine.Move;
import com.cmt.chessbot.engine.MoveGen;
import com.cmt.chessbot.evaluation.Evaluation;

import java.util.ArrayList;
import java.util.List;

public class AlphaBeta {

    private final int maxDepth;
    private final long stopAt;

    public AlphaBeta(int depth, int movetimeMs){
        this.maxDepth = Math.max(1, depth);
        this.stopAt = movetimeMs > 0 ? (System.currentTimeMillis() + movetimeMs) : 0;
    }

    public static class Result {
        public final String bestMove;
        public final int scoreCp;
        public final int depth;
        public final List<String> pv;
        public Result(String bestMove,int scoreCp,int depth,List<String> pv){
            this.bestMove=bestMove; this.scoreCp=scoreCp; this.depth=depth; this.pv=pv;
        }
    }

    public Result search(Board b){
        String bestUci = "none";
        int bestScore = b.whiteToMove()? Integer.MIN_VALUE : Integer.MAX_VALUE;
        List<String> bestPv = new ArrayList<>();

        for (int d=1; d<=maxDepth; d++){
            SearchOut out = alphabetaRoot(b, d);
            if (out.stopped) break;
            if (out.bestMoveUci != null) {
                bestUci = out.bestMoveUci;
                bestScore = out.score;
                bestPv = out.pv;
            }
            if (stopAt!=0 && System.currentTimeMillis() >= stopAt) break;
        }
        return new Result(bestUci, bestScore, Math.min(maxDepth, bestPv.size()), bestPv);
    }

    private static class SearchOut {
        String bestMoveUci; int score; boolean stopped; List<String> pv = new ArrayList<>();
    }

    private SearchOut alphabetaRoot(Board b, int depth){
        SearchOut out = new SearchOut();
        int alpha = -300000, beta = 300000;
        int bestScore = b.whiteToMove()? -300000 : 300000;
        String bestUci=null;
        List<Move> moves = MoveGen.legalMoves(b);

        moves.sort((m1,m2)-> Integer.compare((m2.flags & Move.CAPTURE), (m1.flags & Move.CAPTURE)));

        for (Move m: moves){
            if (stopped()) { out.stopped=true; break; }
            b.make(m);
            int sc = -alphabeta(b, depth-1, -beta, -alpha);
            b.undo(m);

            if (b.whiteToMove()? sc>bestScore : sc<bestScore){
                bestScore = sc;
                bestUci = m.uci();
                out.pv = new ArrayList<>();
                out.pv.add(bestUci);
            }
            if (b.whiteToMove()? sc>alpha : sc<beta){
                if (b.whiteToMove()) alpha=sc; else beta=sc;
            }
        }
        out.score = bestScore; out.bestMoveUci=bestUci;
        return out;
    }

    private int alphabeta(Board b, int depth, int alpha, int beta){
        if (stopped()) return Evaluation.evaluate(b);
        List<Move> moves = MoveGen.legalMoves(b);

        if (depth==0) return quiescence(b, alpha, beta);
        if (moves.isEmpty()){
            int[] k = b.kingSquare(b.whiteToMove());
            boolean inCheck = b.squareAttacked(k[0],k[1], !b.whiteToMove());
            if (inCheck) return b.whiteToMove()? -29000 : 29000;
            return 0;
        }

        moves.sort((m1,m2)-> Integer.compare((m2.flags & Move.CAPTURE), (m1.flags & Move.CAPTURE)));

        int best = -300000;
        for (Move m: moves){
            b.make(m);
            int sc = -alphabeta(b, depth-1, -beta, -alpha);
            b.undo(m);
            if (sc > best) best = sc;
            if (best > alpha) alpha = best;
            if (alpha >= beta) break;
        }
        return best;
    }

    private int quiescence(Board b, int alpha, int beta){
        int standPat = Evaluation.evaluate(b);
        if (standPat >= beta) return beta;
        if (alpha < standPat) alpha = standPat;

        List<Move> moves = MoveGen.legalMoves(b);
        for (Move m: moves){
            if ((m.flags & Move.CAPTURE)==0 && (m.flags & Move.PROMOTION)==0) continue;
            b.make(m);
            int sc = -quiescence(b, -beta, -alpha);
            b.undo(m);
            if (sc >= beta) return beta;
            if (sc > alpha) alpha = sc;
        }
        return alpha;
    }

    private boolean stopped(){
        return stopAt!=0 && System.currentTimeMillis() >= stopAt;
    }
}
