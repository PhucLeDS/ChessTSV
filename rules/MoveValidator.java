package rules;

import model.Board;
import model.Move;
import model.Piece;
import java.awt.Point;

public interface MoveValidator {
    /**
     * Validates if a move is legal considering all chess rules:
     * piece-specific movement, obstructions, capture rules, and if the move
     * puts or leaves the current player's King in check.
     * Special moves like castling and en passant are also validated here.
     *
     * @param board The current state of the chess board.
     * @param move The proposed move.
     * @return true if the move is legal, false otherwise.
     */
    boolean isValidMove(Board board, Move move, Move lastOpponentMove);

    /**
     * Checks if a given player's King is currently in check on the board.
     *
     * @param board The current state of the chess board.
     * @param kingColor The color of the King to check.
     * @return true if the King of the specified color is in check, false otherwise.
     */
    boolean isKingInCheck(Board board, Piece.Color kingColor);

    /**
     * Checks if a proposed move would result in the current player's King
     * being in check (or remaining in check if it was already).
     * This is crucial for legality of moves.
     *
     * @param board The current state of the chess board.
     * @param move The proposed move.
     * @param kingColor The color of the king whose safety is being checked.
     * @return true if the King would be in check after the move, false otherwise.
     */
    boolean leavesKingInCheck(Board board, Move move, Piece.Color kingColor);
}