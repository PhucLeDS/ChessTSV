package game;

import model.Board;
import model.Move;
import model.Piece;
import model.Player;
import pieces.*;
import rules.ChessMoveValidator;
import rules.MoveValidator;

import java.awt.Point;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class Game {
    private Board board;
    private Player whitePlayer;
    private Player blackPlayer;
    private Piece.Color currentPlayerTurn;
    private MoveValidator moveValidator;
    private Stack<Move> moveHistory;
    private Move lastOpponentMove;

    // Timer related fields
    private long whiteTimeMillis;
    private long blackTimeMillis;
    private long lastMoveStartTime; // System.currentTimeMillis() when the last move was made
    private final long defaultTimePerPlayerMillis = 10 * 60 * 1000; // 10 minutes per player

    public Game() {
        this.board = new Board();
        this.whitePlayer = new Player(Piece.Color.WHITE);
        this.blackPlayer = new Player(Piece.Color.BLACK);
        this.currentPlayerTurn = Piece.Color.WHITE;
        this.moveValidator = new ChessMoveValidator();
        this.moveHistory = new Stack<>();
        this.lastOpponentMove = null;

        this.whiteTimeMillis = defaultTimePerPlayerMillis;
        this.blackTimeMillis = defaultTimePerPlayerMillis;
        this.lastMoveStartTime = System.currentTimeMillis(); // Start timer immediately
    }

    public Board getBoard() {
        return board;
    }

    public Piece.Color getCurrentPlayerTurn() {
        return currentPlayerTurn;
    }

    public long getWhiteTimeMillis() {
        return whiteTimeMillis;
    }

    public long getBlackTimeMillis() {
        return blackTimeMillis;
    }

    public void startTimer() {
        lastMoveStartTime = System.currentTimeMillis();
    }

    public void stopTimer() {
        long elapsedTime = System.currentTimeMillis() - lastMoveStartTime;
        if (currentPlayerTurn == Piece.Color.WHITE) {
            whiteTimeMillis -= elapsedTime;
        } else {
            blackTimeMillis -= elapsedTime;
        }
        lastMoveStartTime = 0; // Indicate timer is paused
    }

    // This method needs to be called periodically (e.g., by a Swing Timer)
    public void decrementTimer() {
        if (lastMoveStartTime == 0) return; // Timer is paused

        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastMoveStartTime;
        lastMoveStartTime = currentTime; // Reset for next decrement

        if (currentPlayerTurn == Piece.Color.WHITE) {
            whiteTimeMillis -= elapsedTime;
            if (whiteTimeMillis <= 0) {
                whiteTimeMillis = 0;
                // Handle game over due to timeout
                System.out.println("White ran out of time! Black wins!");
                // (You'd set a game over flag here)
            }
        } else {
            blackTimeMillis -= elapsedTime;
            if (blackTimeMillis <= 0) {
                blackTimeMillis = 0;
                // Handle game over due to timeout
                System.out.println("Black ran out of time! White wins!");
                // (You'd set a game over flag here)
            }
        }
    }


    public boolean makeMove(Point start, Point end, Piece promotedPieceType) {
        Piece pieceToMove = board.getPiece(start);

        if (pieceToMove == null || pieceToMove.getColor() != currentPlayerTurn) {
            System.out.println("Invalid selection or not your turn.");
            return false;
        }

        // Determine if it's a special move and create the appropriate Move object
        Move proposedMove;
        if (pieceToMove instanceof King && Math.abs(start.x - end.x) == 2) {
            proposedMove = new Move(start, end, pieceToMove, true); // Castling move
        } else if (pieceToMove instanceof Pawn && Math.abs(start.x - end.x) == 1 && board.getPiece(end) == null) {
            // This is a diagonal pawn move to an empty square, indicating potential en passant
            proposedMove = new Move(start, end, pieceToMove, board.getPiece(new Point(end.x, start.y)), true); // En passant
        } else if (pieceToMove instanceof Pawn && (end.y == 0 || end.y == 7)) { // Pawn reaches last rank
            if (promotedPieceType == null) { // If no promotion type is provided, it's an error for UI
                System.out.println("Pawn promotion requires a selected piece type.");
                return false;
            }
            proposedMove = new Move(start, end, pieceToMove, board.getPiece(end), promotedPieceType);
        }
        else {
            proposedMove = new Move(start, end, pieceToMove, board.getPiece(end));
        }

        stopTimer(); // Stop current player's timer before validation

        if (moveValidator.isValidMove(board, proposedMove, lastOpponentMove)) {
            // Execute the move on the board
            executeMove(proposedMove);

            // Update piece's state (e.g., hasMoved for King, Rook, Pawn)
            if (pieceToMove instanceof Pawn) {
                ((Pawn) pieceToMove).setHasMoved(true);
            } else if (pieceToMove instanceof King) {
                ((King) pieceToMove).setHasMoved(true);
            } else if (pieceToMove instanceof Rook) {
                ((Rook) pieceToMove).setHasMoved(true);
            }

            // Record the move in history for undo and for lastOpponentMove
            moveHistory.push(proposedMove);
            lastOpponentMove = proposedMove;

            // Switch turns and start next player's timer
            switchTurns();
            startTimer();

            System.out.println("Move successful: " + proposedMove);
            // board.printBoard(); // For console feedback

            return true;
        } else {
            // Restart timer for the current player if move was invalid
            startTimer();
            System.out.println("Invalid move according to chess rules.");
            return false;
        }
    }

    private void executeMove(Move move) {
        Piece pieceToMove = move.getPieceMoved();
        Point start = move.getStart();
        Point end = move.getEnd();

        // Remove piece from start position
        board.setPiece(start, null);

        // Handle special captures (en passant)
        if (move.isEnPassant()) {
            // The captured pawn is on the start.y row, but end.x column
            Point capturedPawnPos = new Point(end.x, start.y);
            board.setPiece(capturedPawnPos, null); // Remove captured pawn
        }

        // Handle castling (move the rook)
        if (move.isCastling()) {
            int kingRow = pieceToMove.getColor() == Piece.Color.WHITE ? 7 : 0;
            Rook rookToMove;
            Point rookStart;
            Point rookEnd;

            if (end.x == 6) { // King-side castling
                rookStart = new Point(7, kingRow);
                rookEnd = new Point(5, kingRow);
            } else { // Queen-side castling
                rookStart = new Point(0, kingRow);
                rookEnd = new Point(3, kingRow);
            }
            rookToMove = (Rook) board.getPiece(rookStart);
            if (rookToMove != null) {
                board.setPiece(rookEnd, rookToMove);
                board.setPiece(rookStart, null);
                rookToMove.setPosition(rookEnd);
                rookToMove.setHasMoved(true); // Rook also moves in castling
            }
        }

        // Handle pawn promotion
        if (move.isPromotion()) {
            Piece promotedPiece = null;
            Piece.Color color = pieceToMove.getColor();
            // Create the new promoted piece based on the type chosen by the player
            // Note: The promotedPieceType in the Move object is a dummy Piece just for its type.
            // We create a new instance with the correct color and position here.
            if (move.getPromotedPieceType() instanceof Queen) {
                promotedPiece = new pieces.Queen(color, end);
            } else if (move.getPromotedPieceType() instanceof Rook) {
                promotedPiece = new pieces.Rook(color, end);
            } else if (move.getPromotedPieceType() instanceof Bishop) {
                promotedPiece = new pieces.Bishop(color, end);
            } else if (move.getPromotedPieceType() instanceof Knight) {
                promotedPiece = new pieces.Knight(color, end);
            }
            board.setPiece(end, promotedPiece);
        } else {
            // Place the moved piece at the end position for regular moves
            board.setPiece(end, pieceToMove);
        }
    }


    private void switchTurns() {
        currentPlayerTurn = (currentPlayerTurn == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;
    }

    // Public method to check if a King of a specific color is in check
    public boolean isKingInCheck(Piece.Color color) {
        return moveValidator.isKingInCheck(board, color);
    }

    public boolean isGameOver() {
        // First check if current player is in check
        boolean kingInCheck = moveValidator.isKingInCheck(board, currentPlayerTurn);

        // Get all legal moves for the current player
        List<Move> legalMoves = getAllLegalMovesForPlayer(currentPlayerTurn);

        if (kingInCheck && legalMoves.isEmpty()) {
            System.out.println("CHECKMATE! " + (currentPlayerTurn == Piece.Color.WHITE ? "Black" : "White") + " wins!");
            stopTimer(); // Stop timers
            return true;
        } else if (!kingInCheck && legalMoves.isEmpty()) {
            System.out.println("STALEMATE! It's a draw!");
            stopTimer(); // Stop timers
            return true;
        }
        // Add more draw conditions later (50-move rule, three-fold repetition)
        return false;
    }

    /**
     * Returns a list of all legal destination points for the piece at the given start point.
     * This is used by the GUI to highlight possible moves.
     * @param start The starting position of the piece.
     * @return A List of Point objects representing legal destination squares.
     */
    private List<Move> getAllLegalMovesForPlayer(Piece.Color color) {
        List<Move> allMoves = new ArrayList<>();
        for (int startX = 0; startX < Board.SIZE; startX++) {
            for (int startY = 0; startY < Board.SIZE; startY++) {
                Point start = new Point(startX, startY);
                Piece piece = board.getPiece(start);
                if (piece != null && piece.getColor() == color) {
                    for (int endX = 0; endX < Board.SIZE; endX++) {
                        for (int endY = 0; endY < Board.SIZE; endY++) {
                            Point end = new Point(endX, endY);
                            Move move = new Move(start, end, piece, board.getPiece(end));
                            if (moveValidator.isValidMove(board, move, lastOpponentMove)) {
                                allMoves.add(move);
                            }
                        }
                    }
                }
            }
        }
        return allMoves;
    }

    public List<Point> getLegalMovesForPiece(Point start) {
        List<Point> possibleEnds = new ArrayList<>();
        Piece piece = board.getPiece(start);

        if (piece == null || piece.getColor() != currentPlayerTurn) {
            return possibleEnds; // No piece or not current player's piece
        }

        for (int x = 0; x < Board.SIZE; x++) {
            for (int y = 0; y < Board.SIZE; y++) {
                Point end = new Point(x, y);
                // Temporarily create a dummy move. The isValidMove method will validate.
                // The actual move object for special moves would be created inside makeMove.
                Move possibleMove = new Move(start, end, piece, board.getPiece(end)); // Dummy move
                if (moveValidator.isValidMove(board, possibleMove, lastOpponentMove)) {
                    possibleEnds.add(end);
                }
            }
        }
        return possibleEnds;
    }

    /**
     * Returns the formatted move history for display in the UI.
     * @return A List of String, each string representing a move.
     */
    public List<String> getFormattedMoveHistory() {
        List<String> formattedMoves = new ArrayList<>();
        for (Move move : moveHistory) {
            Piece piece = move.getPieceMoved();
            String notation = "";
            
            // Handle castling first
            if (move.isCastling()) {
                notation = move.getEnd().x == 6 ? "O-O" : "O-O-O";
            } else {
                // Add piece symbol for non-pawns (N for knight, B for bishop, etc.)
                if (!(piece instanceof Pawn)) {
                    notation += piece.getSymbol().toUpperCase();
                }
                
                // Add capture symbol
                if (move.getPieceCaptured() != null || move.isEnPassant()) {
                    // For pawns, add the file they moved from when capturing
                    if (piece instanceof Pawn) {
                        notation += (char)('a' + move.getStart().x);
                    }
                    notation += "x";
                }
                
                // Add destination square
                notation += convertToAlgebraic(move.getEnd());
                
                // Add en passant notation
                if (move.isEnPassant()) {
                    notation += " e.p.";
                }
                
                // Add promotion notation
                if (move.isPromotion()) {
                    notation += "=" + move.getPromotedPieceType().getSymbol().toUpperCase();
                }
            }
            
            // Add check or checkmate symbol
            if (isKingInCheck(getCurrentPlayerTurn())) {
                boolean isCheckmate = getAllLegalMovesForPlayer(getCurrentPlayerTurn()).isEmpty();
                notation += isCheckmate ? "#" : "+";
            }
            
            formattedMoves.add(notation);
        }
        return formattedMoves;
    }
    private String convertToAlgebraic(Point p) {
        return "" + (char)('a' + p.x) + (8 - p.y);
    }
    // Optional: Undo last move (basic implementation)
    public void undoLastMove() {
        if (!moveHistory.isEmpty()) {
            stopTimer(); // Stop current player's timer before undoing

            Move lastMove = moveHistory.pop();
            // Get the piece that was moved (it's the original piece object)
            Piece movedPiece = lastMove.getPieceMoved();

            // Revert piece's hasMoved state if it was their first move
            if (movedPiece instanceof Pawn) {
                // If it was the first move (two-square move), then set hasMoved to false
                int startRow = (movedPiece.getColor() == Piece.Color.WHITE) ? 6 : 1;
                if (lastMove.getStart().y == startRow &&
                    Math.abs(lastMove.getStart().y - lastMove.getEnd().y) == 2) {
                    ((Pawn) movedPiece).setHasMoved(false);
                }
            } else if (movedPiece instanceof King) {
                 // For King, if it was its first move during castling
                if (lastMove.isCastling()) {
                    ((King) movedPiece).setHasMoved(false);
                }
            } else if (movedPiece instanceof Rook) {
                // For Rook, if it was moved during castling
                if (lastMove.isCastling()) {
                    ((Rook) movedPiece).setHasMoved(false);
                }
            }

            // Put the moved piece back to its start position
            board.setPiece(lastMove.getStart(), movedPiece);
            movedPiece.setPosition(lastMove.getStart());

            // If a piece was captured, put it back
            if (lastMove.getPieceCaptured() != null) {
                board.setPiece(lastMove.getEnd(), lastMove.getPieceCaptured());
                lastMove.getPieceCaptured().setPosition(lastMove.getEnd());
            } else {
                // If no piece was captured, the end square becomes empty (unless en passant)
                board.setPiece(lastMove.getEnd(), null);
            }

            // Revert en passant capture
            if (lastMove.isEnPassant()) {
                Point capturedPawnPos = new Point(lastMove.getEnd().x, lastMove.getStart().y);
                board.setPiece(capturedPawnPos, lastMove.getPieceCaptured());
                lastMove.getPieceCaptured().setPosition(capturedPawnPos);
                board.setPiece(lastMove.getEnd(), null);
            }

            // Revert castling (move the rook back)
            if (lastMove.isCastling()) {
                int kingRow = movedPiece.getColor() == Piece.Color.WHITE ? 7 : 0;
                Point rookOriginalPos;
                Point rookMovedPos;

                if (lastMove.getEnd().x == 6) { // King-side castling
                    rookOriginalPos = new Point(7, kingRow);
                    rookMovedPos = new Point(5, kingRow);
                } else { // Queen-side castling
                    rookOriginalPos = new Point(0, kingRow);
                    rookMovedPos = new Point(3, kingRow);
                }
                Rook movedRook = (Rook) board.getPiece(rookMovedPos);
                if (movedRook != null) {
                    board.setPiece(rookOriginalPos, movedRook);
                    board.setPiece(rookMovedPos, null);
                    movedRook.setPosition(rookOriginalPos);
                    movedRook.setHasMoved(false); // Reset rook's hasMoved state
                }
            }

            // Revert pawn promotion
            if (lastMove.isPromotion()) {
                // The promoted piece was at lastMove.getEnd()
                // It was replaced by the original pawn at lastMove.getStart()
                // Now, if there was a captured piece, it's already back.
                // Otherwise, the end square should be empty.
                // The movedPiece (pawn) has already been put back at start.
                // So, just clear the end square if nothing was captured
                if (lastMove.getPieceCaptured() == null) {
                    board.setPiece(lastMove.getEnd(), null);
                }
            }

            // Update lastOpponentMove (if there are previous moves)
            if (!moveHistory.isEmpty()) {
                lastOpponentMove = moveHistory.peek();
            } else {
                lastOpponentMove = null;
            }

            switchTurns(); // Switch turn back
            startTimer(); // Start the timer for the player whose turn it just became

            System.out.println("Undoing move: " + lastMove);
            // board.printBoard();
        } else {
            System.out.println("No moves to undo.");
        }
    }
}