package pieces;

import model.Board;
import model.Piece;
import java.awt.Point;

public class Rook extends Piece {
    private boolean hasMoved; // For castling

    public Rook(Color color, Point position) {
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

        // Rook moves horizontally or vertically
        if (dx != 0 && dy != 0) {
            return false; // Cannot move diagonally
        }

        // Check for obstructions
        if (dx != 0) { // Horizontal movement
            int stepX = (dx > 0) ? 1 : -1;
            for (int x = getPosition().x + stepX; x != newPosition.x; x += stepX) {
                if (board.getPiece(new Point(x, getPosition().y)) != null) {
                    return false; // Obstruction
                }
            }
        } else if (dy != 0) { // Vertical movement
            int stepY = (dy > 0) ? 1 : -1;
            for (int y = getPosition().y + stepY; y != newPosition.y; y += stepY) {
                if (board.getPiece(new Point(getPosition().x, y)) != null) {
                    return false; // Obstruction
                }
            }
        }

        Piece pieceAtNewPos = board.getPiece(newPosition);
        return pieceAtNewPos == null || pieceAtNewPos.getColor() != getColor();
    }

    @Override
    public String getSymbol() {
        return getColor() == Color.WHITE ? "R" : "r";
    }
}