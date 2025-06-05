package rules;

import model.Board;
import model.Move;
import model.Piece;
import pieces.*;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class ChessMoveValidator implements MoveValidator {

    @Override
    public boolean isValidMove(Board board, Move move, Move lastOpponentMove) {
        Piece piece = move.getPieceMoved();
        Point start = move.getStart();
        Point end = move.getEnd();

        // Basic checks
        if (piece == null || !board.isValidPosition(end)) {
            return false;
        }
        if (start.equals(end)) {
            return false; // Cannot move to the same square
        }

        Piece pieceAtEnd = board.getPiece(end);
        if (pieceAtEnd != null && pieceAtEnd.getColor() == piece.getColor()) {
            return false; // Cannot capture your own piece
        }

        // Handle special moves first for clarity and specific rules
        if (piece instanceof King && Math.abs(start.x - end.x) == 2 && start.y == end.y) {
            // This is a potential castling move
            return isValidCastling(board, move);
        }

        if (piece instanceof Pawn) {
            // Check for en passant
            if (Math.abs(start.x - end.x) == 1 && (end.y - start.y) == ((piece.getColor() == Piece.Color.WHITE) ? -1 : 1)) {
                // If diagonal move to an empty square, it must be en passant
                if (pieceAtEnd == null) {
                    if (isValidEnPassant(board, move, lastOpponentMove)) {
                        return true;
                    }
                }
            }
        }

        // 1. Validate piece-specific move logic (using OCP: `piece.isValidMove`)
        // This handles standard moves and captures, and checks for obstructions for sliding pieces.
        if (!piece.isValidMove(end, board)) {
            return false;
        }

        // 2. Check if the move puts or leaves the King in check
        if (leavesKingInCheck(board, move, piece.getColor())) {
            return false;
        }

        return true;
    }

    private boolean isValidCastling(Board board, Move move) {
        King king = (King) move.getPieceMoved();
        Point start = move.getStart();
        Point end = move.getEnd();
        Piece.Color kingColor = king.getColor();

        // King must not have moved
        if (king.hasMoved()) {
            return false;
        }

        int kingRow = kingColor == Piece.Color.WHITE ? 7 : 0;
        if (start.y != kingRow) return false; // King must be on its original row

        // Determine castling type (King-side or Queen-side)
        int rookCol;
        int kingDestCol;
        int rookDestCol;
        int step;

        if (end.x == 6) { // King-side castling
            rookCol = 7;
            kingDestCol = 6;
            rookDestCol = 5;
            step = 1;
        } else if (end.x == 2) { // Queen-side castling
            rookCol = 0;
            kingDestCol = 2;
            rookDestCol = 3;
            step = -1;
        } else {
            return false; // Not a valid castling endpoint
        }

        Rook rook = (Rook) board.getPiece(new Point(rookCol, kingRow));
        // Rook must be present and must not have moved
        if (rook == null || rook.getColor() != kingColor || rook.hasMoved()) {
            return false;
        }

        // Path between King and Rook must be empty
        for (int x = start.x + step; x != rookCol; x += step) {
            if (board.getPiece(new Point(x, kingRow)) != null) {
                return false; // Obstruction
            }
        }

        // King must not be in check, and must not pass through or land in check
        if (isKingInCheck(board, kingColor)) {
            return false; // Cannot castle out of check
        }

        // Check squares king passes through and lands on
        Point[] squaresToCheck = new Point[3];
        squaresToCheck[0] = start; // King's current square
        squaresToCheck[1] = new Point(start.x + step, kingRow); // Square king passes through
        squaresToCheck[2] = new Point(start.x + 2 * step, kingRow); // King's destination square

        for (Point p : squaresToCheck) {
            Board tempBoard = board.copy();
            // Simulate moving the king to 'p' (temporarily, just to check for check)
            Piece kingOnTempBoard = tempBoard.getPiece(start);
            if (kingOnTempBoard == null) return false; // Should not happen

            tempBoard.setPiece(p, kingOnTempBoard);
            tempBoard.setPiece(start, null);
            kingOnTempBoard.setPosition(p); // Temporarily update position for check test

            if (isKingInCheck(tempBoard, kingColor)) {
                return false; // King passes through or lands in check
            }
        }

        return true;
    }

    private boolean isValidEnPassant(Board board, Move move, Move lastOpponentMove) {
        Pawn currentPawn = (Pawn) move.getPieceMoved();
        Point start = move.getStart();
        Point end = move.getEnd();

        // Must be a diagonal move to an empty square
        if (board.getPiece(end) != null) {
            return false;
        }

        // Check if the last opponent move was a two-square pawn move
        if (lastOpponentMove == null || !(lastOpponentMove.getPieceMoved() instanceof Pawn)) {
            return false;
        }
        Pawn opponentPawn = (Pawn) lastOpponentMove.getPieceMoved();
        Point opponentPawnStart = lastOpponentMove.getStart();
        Point opponentPawnEnd = lastOpponentMove.getEnd();

        // Opponent pawn must have moved exactly two squares
        int opponentForwardDir = (opponentPawn.getColor() == Piece.Color.WHITE) ? -1 : 1;
        if (Math.abs(opponentPawnStart.y - opponentPawnEnd.y) != 2 || (opponentPawnEnd.y - opponentPawnStart.y) != 2 * opponentForwardDir) {
            return false;
        }

        // Opponent pawn must be adjacent horizontally to the current pawn
        if (opponentPawnEnd.y != start.y || Math.abs(opponentPawnEnd.x - start.x) != 1) {
            return false;
        }

        // The target square for en passant must be behind the opponent pawn's final position
        int currentPawnForwardDir = (currentPawn.getColor() == Piece.Color.WHITE) ? -1 : 1;
        if (end.y != opponentPawnEnd.y + currentPawnForwardDir) {
            return false;
        }
        if (end.x != opponentPawnEnd.x) {
            return false;
        }

        // Check if the move puts the current player's king in check
        // This is implicitly handled by the leavesKingInCheck call in isValidMove.

        return true;
    }


    @Override
    public boolean isKingInCheck(Board board, Piece.Color kingColor) {
        Point kingPosition = findKingPosition(board, kingColor);
        if (kingPosition == null) {
            // This should ideally not happen in a correctly initialized game
            // Or it signifies the king has been captured (game over)
            return false;
        }

        Piece.Color opponentColor = (kingColor == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;

        // Iterate through all squares on the board
        for (int x = 0; x < Board.SIZE; x++) {
            for (int y = 0; y < Board.SIZE; y++) {
                Piece opponentPiece = board.getPiece(new Point(x, y));
                if (opponentPiece != null && opponentPiece.getColor() == opponentColor) {
                    // Check if this opponent piece can move to the king's position
                    // For pawns, a simple isValidMove won't work for capture checking
                    // as it considers empty squares directly ahead.
                    // Special handling for pawns is needed here.
                    if (opponentPiece instanceof Pawn) {
                        Pawn opponentPawn = (Pawn) opponentPiece;
                        int forwardDirection = (opponentPawn.getColor() == Piece.Color.WHITE) ? -1 : 1;
                        if (Math.abs(kingPosition.x - opponentPiece.getPosition().x) == 1 &&
                            (kingPosition.y - opponentPiece.getPosition().y) == forwardDirection) {
                            return true; // Pawn can capture the king diagonally
                        }
                    } else if (opponentPiece.isValidMove(kingPosition, board)) {
                        // For other pieces, isValidMove already checks for obstructions and target validity
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean leavesKingInCheck(Board board, Move move, Piece.Color kingColor) {
        // Create a temporary board to simulate the move
        Board tempBoard = board.copy();
        Piece movedPiece = tempBoard.getPiece(move.getStart());

        if (movedPiece == null) return true; // Should not happen if a piece is supposed to be moved

        // Perform the move on the temporary board
        tempBoard.setPiece(move.getEnd(), movedPiece);
        tempBoard.setPiece(move.getStart(), null);
        movedPiece.setPosition(move.getEnd()); // Update piece's temp position

        // Handle en passant capture on temp board
        if (move.isEnPassant()) {
            // The captured pawn is on a different square
            Point capturedPawnPos = new Point(move.getEnd().x, move.getStart().y);
            tempBoard.setPiece(capturedPawnPos, null);
        }

        // Handle castling rook move on temp board
        if (move.isCastling()) {
            // Find the rook and move it on the temp board
            int kingRow = kingColor == Piece.Color.WHITE ? 7 : 0;
            Rook rookToMove;
            Point rookStart;
            Point rookEnd;

            if (move.getEnd().x == 6) { // King-side castling
                rookStart = new Point(7, kingRow);
                rookEnd = new Point(5, kingRow);
            } else { // Queen-side castling
                rookStart = new Point(0, kingRow);
                rookEnd = new Point(3, kingRow);
            }
            rookToMove = (Rook) tempBoard.getPiece(rookStart);
            if (rookToMove != null) {
                tempBoard.setPiece(rookEnd, rookToMove);
                tempBoard.setPiece(rookStart, null);
                rookToMove.setPosition(rookEnd);
            }
        }

        // If it's a promotion move, the pawn on the temp board will be replaced by the promoted piece
        if (move.isPromotion()) {
            // The movedPiece (pawn) has already been placed at move.getEnd()
            // Now replace it with the new promoted piece type.
            Piece promotedPiece = null;
            if (move.getPromotedPieceType() instanceof Queen) {
                promotedPiece = new Queen(kingColor, move.getEnd());
            } else if (move.getPromotedPieceType() instanceof Rook) {
                promotedPiece = new Rook(kingColor, move.getEnd());
            } else if (move.getPromotedPieceType() instanceof Bishop) {
                promotedPiece = new Bishop(kingColor, move.getEnd());
            } else if (move.getPromotedPieceType() instanceof Knight) {
                promotedPiece = new Knight(kingColor, move.getEnd());
            }
            tempBoard.setPiece(move.getEnd(), promotedPiece);
        }

        // Check if the current player's king is in check on the temporary board
        return isKingInCheck(tempBoard, kingColor);
    }

    private Point findKingPosition(Board board, Piece.Color kingColor) {
        for (int x = 0; x < Board.SIZE; x++) {
            for (int y = 0; y < Board.SIZE; y++) {
                Piece piece = board.getPiece(new Point(x, y));
                if (piece instanceof King && piece.getColor() == kingColor) {
                    return new Point(x, y);
                }
            }
        }
        return null; // King not found (implies an error or game over already)
    }
}