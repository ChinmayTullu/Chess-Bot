import axios from "axios";

const API_BASE = "http://localhost:8080/api/chess";

export interface EvaluateResp {
  // backend might return "score" or "scoreCp"; support both
  score?: number;
  scoreCp?: number;
  bestMove?: string;
  pv?: string[];
  depth?: number;
}

export interface PlayResp {
  botMove?: string;
  score?: number;
  scoreCp?: number;
  pv?: string[];
  depth?: number;
}

export async function evaluatePosition(fen: string, depth = 4): Promise<number> {
  const payload = { fen, depth };
  const r = await axios.post<EvaluateResp>(`${API_BASE}/evaluate`, payload);
  const data = r.data;
  const score = (data.score ?? data.scoreCp) ?? 0;
  return score;
}

export async function playBotMove(fen: string, depth = 4): Promise<PlayResp> {
  const payload = { fen, depth };
  const r = await axios.post<PlayResp>(`${API_BASE}/play`, payload);
  return r.data;
}
