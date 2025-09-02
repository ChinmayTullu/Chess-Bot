import React, { useEffect, useRef, useState } from "react";
import { Chess } from "chess.js";
import { Chessboard } from "react-chessboard";
import EvalBar from "./EvalBar";
import Controls from "./Controls";
import { evaluatePosition, playBotMove } from "../utils/api.js";

interface Props { }

/**
 * Component maintains a Chess instance in a ref for correct undo behavior.
 * State stores current FEN (string) and evaluation centipawns.
 */
const ChessBoard: React.FC<Props> = () => {
  const gameRef = useRef(new Chess());
  const [fen, setFen] = useState<string>(gameRef.current.fen());
  const [evalCp, setEvalCp] = useState<number>(0);
  const [mode, setMode] = useState<"analysis" | "play">("analysis");
  const [depth, setDepth] = useState<number>(4);
  const [thinking, setThinking] = useState<boolean>(false);
  const [, forceRerender] = useState(0); // small trick to rerender when needed

  useEffect(() => {
    // evaluate initial position
    (async () => {
      try {
        const s = await evaluatePosition(fen, 2);
        setEvalCp(s);
      } catch (e) {
        console.warn("Eval failed", e);
      }
    })();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const refreshFromRef = () => {
    setFen(gameRef.current.fen());
    forceRerender((n) => n + 1);
  };

  const onDrop = async (sourceSquare: string, targetSquare: string) => {
    // Create move object; default promote to queen if applicable
    const moveObj = { from: sourceSquare, to: targetSquare, promotion: "q" as "q" | "r" | "b" | "n" };
    const result = gameRef.current.move(moveObj);
    if (result === null) return false; // illegal
    refreshFromRef();

    // Evaluate new position
    try {
      setThinking(true);
      const score = await evaluatePosition(gameRef.current.fen(), 2);
      setEvalCp(score);
    } catch (e) {
      console.error("Evaluation error", e);
    } finally {
      setThinking(false);
    }

    // If play mode, call bot
    if (mode === "play") {
      await triggerBotMove();
    }

    return true;
  };

  const triggerBotMove = async () => {
    try {
      setThinking(true);
      const resp = await playBotMove(gameRef.current.fen(), depth);
      const uci = resp.botMove ?? "";
      if (uci && uci.length >= 4) {
        const from = uci.substring(0, 2);
        const to = uci.substring(2, 4);
        const promo = uci.length > 4 ? uci[4] : undefined;
        const moveObj: any = { from, to };
        if (promo) moveObj.promotion = promo;
        const res = gameRef.current.move(moveObj);
        if (res !== null) refreshFromRef();
      }
      // update evaluation (backend may provide)
      const score = (resp.score ?? resp.scoreCp) ?? await evaluatePosition(gameRef.current.fen(), 2);
      setEvalCp(score);
    } catch (e) {
      console.error("Bot move failed", e);
    } finally {
      setThinking(false);
    }
  };

  const onReset = () => {
    gameRef.current = new Chess();
    refreshFromRef();
    setEvalCp(0);
  };

  const onUndo = () => {
    gameRef.current.undo();
    refreshFromRef();
    // re-evaluate after undo
    evaluatePosition(gameRef.current.fen(), 2).then((s: number) => setEvalCp(s)).catch(() => {});
  };

  return (
    <div className="w-full max-w-5xl flex gap-8">
      <div className="flex-1 flex items-center justify-center p-4">
        <Chessboard
          position={fen}
          onPieceDrop={async (src: string, dst: string) => await onDrop(src, dst)}
          boardWidth={560}
        />
      </div>

      <div className="w-80 bg-white p-4 rounded-lg shadow">
        <div className="mb-4">
          <div className="text-xl font-semibold">Chessbot</div>
          <div className="text-xs text-gray-500">Local analysis & play</div>
        </div>

        <div className="mb-6">
          <EvalBar centiScore={evalCp} />
        </div>

        <Controls
          onReset={onReset}
          onUndo={onUndo}
          mode={mode}
          setMode={setMode}
          depth={depth}
          setDepth={setDepth}
          thinking={thinking}
        />

        <div className="mt-4 text-xs text-gray-500">
          Tip: drag and drop to move pieces. Promotes to Queen by default.
        </div>
      </div>
    </div>
  );
};

export default ChessBoard;
