package src.model;

import java.awt.Point;

public abstract class Piece {
    private Color color;
    private Point position;

    public enum Color { WHITE, BLACK }

    public Piece(Color color, Point position) {
        this.color = color;
        this.position = position;
    }

    public Color getColor() {
        return color;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    // This method checks the piece's own valid move logic (e.g., how a Rook moves)
    // It does NOT check for obstructions, check, or other board-level rules.
    public abstract boolean isValidMove(Point newPosition, Board board);

    public abstract String getSymbol(); // e.g., "P", "R", "N", "B", "Q", "K"

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + getColor();
    }
}