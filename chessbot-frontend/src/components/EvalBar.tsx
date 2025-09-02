import React from "react";

interface Props {
  centiScore: number; // centipawns, positive = white advantage
}

const clamp = (v: number, a: number, b: number) => Math.max(a, Math.min(b, v));

const EvalBar: React.FC<Props> = ({ centiScore }) => {
  // Map centipawn to percentage. Use a softcap to keep UI pleasant.
  //  +800 => almost full white, -800 => almost full black
  const cap = 800;
  const clamped = clamp(centiScore, -cap, cap);
  const percentWhite = 50 + (clamped / (cap * 2)) * 100 * 2; // convert to 0..100
  // simpler: percentWhite = 50 + (clamped / 16) but we want 0-100 clamp
  const pct = clamp(percentWhite, 0, 100);

  return (
    <div className="flex flex-col items-center gap-3">
      <div className="text-sm font-medium">Evaluation</div>
      <div className="w-12 h-64 bg-gray-200 rounded overflow-hidden shadow">
        <div
          className="w-full bg-white transition-all duration-300"
          style={{ height: `${pct}%` }}
        />
        <div
          className="w-full bg-black transition-all duration-300"
          style={{ height: `${100 - pct}%` }}
        />
      </div>
      <div className="text-sm text-gray-700">{(centiScore / 100).toFixed(2)} pawns</div>
    </div>
  );
};

export default EvalBar;
