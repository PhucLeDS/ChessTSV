package pieces;

import model.Board;
import model.Piece;
import java.awt.Point;

public class Queen extends Piece {
    public Queen(Color color, Point position) {
        super(color, position);
    }

    @Override
    public boolean isValidMove(Point newPosition, Board board) {
        int dx = newPosition.x - getPosition().x;
        int dy = newPosition.y - getPosition().y;

        // Queen combines Rook and Bishop moves
        boolean isStraight = (dx == 0 && dy != 0) || (dx != 0 && dy == 0);
        boolean isDiagonal = (Math.abs(dx) == Math.abs(dy) && dx != 0);

        if (!isStraight && !isDiagonal) {
            return false; // Not a valid Queen move pattern
        }

        // Check for obstructions (similar to Rook and Bishop)
        if (isStraight) { // Horizontal or vertical
            if (dx != 0) { // Horizontal movement
                int stepX = (dx > 0) ? 1 : -1;
                for (int x = getPosition().x + stepX; x != newPosition.x; x += stepX) {
                    if (board.getPiece(new Point(x, getPosition().y)) != null) {
                        return false; // Obstruction
                    }
                }
            } else { // Vertical movement
                int stepY = (dy > 0) ? 1 : -1;
                for (int y = getPosition().y + stepY; y != newPosition.y; y += stepY) {
                    if (board.getPiece(new Point(getPosition().x, y)) != null) {
                        return false; // Obstruction
                    }
                }
            }
        } else { // Diagonal
            int stepX = (dx > 0) ? 1 : -1;
            int stepY = (dy > 0) ? 1 : -1;

            int currentX = getPosition().x + stepX;
            int currentY = getPosition().y + stepY;

            while (currentX != newPosition.x || currentY != newPosition.y) {
                if (board.getPiece(new Point(currentX, currentY)) != null) {
                    return false; // Obstruction
                }
                currentX += stepX;
                currentY += stepY;
            }
        }

        Piece pieceAtNewPos = board.getPiece(newPosition);
        return pieceAtNewPos == null || pieceAtNewPos.getColor() != getColor();
    }

    @Override
    public String getSymbol() {
        return getColor() == Color.WHITE ? "Q" : "q";
    }
}