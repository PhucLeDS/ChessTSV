package pieces;

import model.Board;
import model.Piece;
import java.awt.Point;

public class Bishop extends Piece {
    public Bishop(Color color, Point position) {
        super(color, position);
    }

    @Override
    public boolean isValidMove(Point newPosition, Board board) {
        int dx = newPosition.x - getPosition().x;
        int dy = newPosition.y - getPosition().y;

        // Bishop moves diagonally
        if (Math.abs(dx) != Math.abs(dy)) {
            return false; // Not a diagonal move
        }

        // Check for obstructions
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

        Piece pieceAtNewPos = board.getPiece(newPosition);
        return pieceAtNewPos == null || pieceAtNewPos.getColor() != getColor();
    }

    @Override
    public String getSymbol() {
        return getColor() == Color.WHITE ? "B" : "b";
    }
}