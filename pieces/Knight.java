package pieces;

import model.Board;
import model.Piece;
import java.awt.Point;

public class Knight extends Piece {
    public Knight(Color color, Point position) {
        super(color, position);
    }

    @Override
    public boolean isValidMove(Point newPosition, Board board) {
        int dx = Math.abs(newPosition.x - getPosition().x);
        int dy = Math.abs(newPosition.y - getPosition().y);

        // Knight moves in an L-shape: 2 squares in one direction (horiz/vert) and 1 square perpendicular
        if ((dx == 1 && dy == 2) || (dx == 2 && dy == 1)) {
            Piece pieceAtNewPos = board.getPiece(newPosition);
            return pieceAtNewPos == null || pieceAtNewPos.getColor() != getColor();
        }
        return false;
    }

    @Override
    public String getSymbol() {
        return getColor() == Color.WHITE ? "N" : "n";
    }
}