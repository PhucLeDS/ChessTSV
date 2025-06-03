package src.model;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private Piece.Color color;
    private List<Move> madeMoves; // To track moves for history (e.g., for three-fold repetition)

    public Player(Piece.Color color) {
        this.color = color;
        this.madeMoves = new ArrayList<>();
    }

    public Piece.Color getColor() {
        return color;
    }

    public void addMove(Move move) {
        madeMoves.add(move);
    }

    public List<Move> getMadeMoves() {
        return madeMoves;
    }

    // Method to remove the last move (useful for undo)
    public void removeLastMove() {
        if (!madeMoves.isEmpty()) {
            madeMoves.remove(madeMoves.size() - 1);
        }
    }
}