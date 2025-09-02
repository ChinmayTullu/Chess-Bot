import React from "react";
import ChessBoard from "./components/ChessBoard";

const App: React.FC = () => {
  return (
    <div className="min-h-screen bg-gradient-to-b from-gray-50 to-gray-200 p-6">
      <div className="max-w-6xl mx-auto">
        <header className="mb-6 text-center">
          <h1 className="text-4xl font-extrabold">Chessbot ♟️</h1>
          <p className="text-sm text-gray-600">Play or analyze positions — powered by your Chessbot backend</p>
        </header>

        <main>
          <ChessBoard />
        </main>
      </div>
    </div>
  );
};

export default App;
