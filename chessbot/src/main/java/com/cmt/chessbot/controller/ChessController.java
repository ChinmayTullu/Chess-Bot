package com.cmt.chessbot.controller;

import com.cmt.chessbot.service.ChessService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chess")
@CrossOrigin
public class ChessController {

    private final ChessService service;

    public ChessController(ChessService service) {
        this.service = service;
    }

    // Evaluate position and return score + PV etc.
    @PostMapping("/evaluate")
    public Map<String, Object> evaluate(@RequestBody Map<String, Object> req) {
        String fen = (String) req.getOrDefault("fen", com.cmt.chessbot.engine.Util.START_FEN);
        int depth = (int) ((Number) req.getOrDefault("depth", 4)).intValue();
        int movetimeMs = (int) ((Number) req.getOrDefault("movetimeMs", 0)).intValue();
        return service.evaluate(fen, depth, movetimeMs);
    }

    // Ask bot to play a move from a FEN; returns bot move and evaluation
    @PostMapping("/play")
    public Map<String, Object> play(@RequestBody Map<String, Object> req) {
        String fen = (String) req.getOrDefault("fen", com.cmt.chessbot.engine.Util.START_FEN);
        int depth = (int) ((Number) req.getOrDefault("depth", 4)).intValue();
        int movetimeMs = (int) ((Number) req.getOrDefault("movetimeMs", 0)).intValue();
        return service.bestMove(fen, depth, movetimeMs);
    }
}
