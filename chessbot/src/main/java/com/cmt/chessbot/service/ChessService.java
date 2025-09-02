package com.cmt.chessbot.service;

import com.cmt.chessbot.engine.Board;
import com.cmt.chessbot.search.AlphaBeta;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ChessService {

    public Map<String,Object> evaluate(String fen, int depth, int movetimeMs) {
        Board b = Board.fromFEN(fen);
        AlphaBeta ab = new AlphaBeta(depth, movetimeMs);
        AlphaBeta.Result r = ab.search(b);
        return Map.of(
            "fen", fen,
            "depth", r.depth,
            "scoreCp", r.scoreCp,
            "bestMove", r.bestMove,
            "pv", r.pv
        );
    }

    public Map<String,Object> bestMove(String fen, int depth, int movetimeMs) {
        Board b = Board.fromFEN(fen);
        AlphaBeta ab = new AlphaBeta(depth, movetimeMs);
        AlphaBeta.Result r = ab.search(b);
        return Map.of(
            "bestMove", r.bestMove,
            "scoreCp", r.scoreCp,
            "pv", r.pv,
            "depth", r.depth
        );
    }
}
