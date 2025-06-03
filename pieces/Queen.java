package src.pieces;

import src.model.Board;
import src.model.Piece;
import java.awt.Point;

public class Queen extends Piece {
    public Queen(Color color, Point position) {
        super(color, position);
    }

    @Override
    public boolean isValidMove(Point newPosition, Board board) {
        return false;
    }

    @Override
    public String getSymbol() {
        return null;
    }

}