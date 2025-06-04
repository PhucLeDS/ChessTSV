package pieces;

import model.Board;
import model.Piece;
import java.awt.Point;

public class Pawn extends Piece {
    private boolean hasMoved;

    public Pawn(Color color, Point position) {
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
        int dx = newPosition.x - getPosition().x;
        int dy = newPosition.y - getPosition().y;
        Piece pieceAtNewPos = board.getPiece(newPosition);

        int forwardDirection = (getColor() == Color.WHITE) ? -1 : 1; // White moves up (decreasing y), Black moves down (increasing y)
        int startRow = (getColor() == Color.WHITE) ? 6 : 1; // White starts on row 6, Black on row 1

        // 1. One square forward
        if (dx == 0 && dy == forwardDirection) {
            return pieceAtNewPos == null; // Must be empty square
        }

        // 2. Two squares forward (initial move)
        if (!hasMoved && dx == 0 && dy == 2 * forwardDirection && getPosition().y == startRow) {
            // Check if both squares are empty
            Point intermediatePos = new Point(getPosition().x, getPosition().y + forwardDirection);
            return board.getPiece(intermediatePos) == null && pieceAtNewPos == null;
        }

        // 3. Diagonal capture (regular)
        if (Math.abs(dx) == 1 && dy == forwardDirection) {
            return pieceAtNewPos != null && pieceAtNewPos.getColor() != getColor();
        }

        // En passant logic is complex and best handled at the MoveValidator/Game level
        // as it depends on the opponent's previous move.
        // This isValidMove only checks the inherent movement pattern of the piece.

        return false;
    }

    @Override
    public String getSymbol() {
        return getColor() == Color.WHITE ? "P" : "p";
    }
}