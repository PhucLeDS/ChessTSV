package pieces;

import model.Board;
import model.Piece;
import java.awt.Point;

public class King extends Piece {
    private boolean hasMoved; // For castling

    public King(Color color, Point position) {
        super(color, position);
        this.hasMoved = false;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    @Override
    public boolean isValidMove(Point newPosition, Board board) {
        int dx = Math.abs(newPosition.x - getPosition().x);
        int dy = Math.abs(newPosition.y - getPosition().y);

        // King moves one square in any direction
        if (dx <= 1 && dy <= 1 && (dx + dy > 0)) { // (dx+dy > 0) ensures it's not the same square
            Piece pieceAtNewPos = board.getPiece(newPosition);
            return pieceAtNewPos == null || pieceAtNewPos.getColor() != getColor();
        }

        // Castling logic (King's specific move, complex)
        // This part of isValidMove will only check the king's *pattern* for castling,
        // but the full validation (no pieces in between, not in check, not passing through check)
        // will be handled by the ChessMoveValidator.
        if (!hasMoved && dy == 0 && Math.abs(dx) == 2) { // Castling is a 2-square horizontal move
            // We just return true here, indicating it's a *potential* castling pattern.
            // Full castling rules are in ChessMoveValidator.
            return true;
        }

        return false;
    }

    @Override
    public String getSymbol() {
        return getColor() == Color.WHITE ? "K" : "k";
    }
}