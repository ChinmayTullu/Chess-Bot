import React from "react";

interface Props {
  onReset: () => void;
  onUndo: () => void;
  mode: "analysis" | "play";
  setMode: (m: "analysis" | "play") => void;
  depth: number;
  setDepth: (d: number) => void;
  thinking: boolean;
}

const Controls: React.FC<Props> = ({ onReset, onUndo, mode, setMode, depth, setDepth, thinking }) => {
  return (
    <div className="flex flex-col gap-3">
      <div className="flex items-center justify-between">
        <div className="flex gap-2">
          <button
            onClick={onReset}
            className="px-3 py-1 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
            aria-disabled={false}
          >
            Reset
          </button>
          <button
            onClick={onUndo}
            className="px-3 py-1 bg-gray-600 text-white rounded hover:bg-gray-700"
          >
            Undo
          </button>
        </div>

        <div className="text-sm text-gray-600">
          Mode:
          <select
            value={mode}
            onChange={(e) => setMode(e.target.value as "analysis" | "play")}
            className="ml-2 border rounded px-2 py-1"
          >
            <option value="analysis">Analysis</option>
            <option value="play">Play vs Bot</option>
          </select>
        </div>
      </div>

      <div className="flex items-center justify-between">
        <div>
          <label className="text-sm text-gray-700">Bot depth</label>
          <div className="mt-1 flex gap-2">
            <button
              onClick={() => setDepth(Math.max(1, depth - 1))}
              className="px-2 py-1 bg-gray-200 rounded"
            >
              -
            </button>
            <div className="px-3 py-1 bg-white rounded border">{depth}</div>
            <button
              onClick={() => setDepth(Math.min(8, depth + 1))}
              className="px-2 py-1 bg-gray-200 rounded"
            >
              +
            </button>
            <div className="text-xs text-gray-500 ml-2">Depth 1–8</div>
          </div>
        </div>

        <div>
          <button
            disabled={thinking}
            className={`px-3 py-1 rounded text-white ${thinking ? "bg-yellow-400" : "bg-green-600 hover:bg-green-700"}`}
          >
            {thinking ? "Thinking..." : mode === "play" ? "Play vs Bot" : "Analysis"}
          </button>
        </div>
      </div>
    </div>
  );
};

export default Controls;
