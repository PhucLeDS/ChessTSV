package model;

import java.awt.Point;

public class Move {
    private Point start;
    private Point end;
    private Piece pieceMoved;
    private Piece pieceCaptured; // Null if no piece is captured
    private boolean isEnPassant; // For en passant special move
    private boolean isCastling;  // For castling special move
    private boolean isPromotion; // For pawn promotion
    private Piece promotedPieceType; // For pawn promotion, the type the pawn promotes to

    // Constructor for standard moves
    public Move(Point start, Point end, Piece pieceMoved, Piece pieceCaptured) {
        this.start = start;
        this.end = end;
        this.pieceMoved = pieceMoved;
        this.pieceCaptured = pieceCaptured;
        this.isEnPassant = false;
        this.isCastling = false;
        this.isPromotion = false;
        this.promotedPieceType = null;
    }

    // Constructor for pawn promotion moves
    public Move(Point start, Point end, Piece pieceMoved, Piece pieceCaptured, Piece promotedPieceType) {
        this(start, end, pieceMoved, pieceCaptured);
        this.isPromotion = true;
        this.promotedPieceType = promotedPieceType;
    }

    // Constructor for en passant moves
    public Move(Point start, Point end, Piece pieceMoved, Piece pieceCaptured, boolean isEnPassant) {
        this(start, end, pieceMoved, pieceCaptured);
        this.isEnPassant = isEnPassant;
    }

    // Constructor for castling moves
    public Move(Point start, Point end, Piece pieceMoved, boolean isCastling) {
        this(start, end, pieceMoved, null); // No capture in castling
        this.isCastling = isCastling;
    }


    public Point getStart() {
        return start;
    }

    public Point getEnd() {
        return end;
    }

    public Piece getPieceMoved() {
        return pieceMoved;
    }

    public Piece getPieceCaptured() {
        return pieceCaptured;
    }

    public boolean isEnPassant() {
        return isEnPassant;
    }

    public boolean isCastling() {
        return isCastling;
    }

    public boolean isPromotion() {
        return isPromotion;
    }

    public Piece getPromotedPieceType() {
        return promotedPieceType;
    }

    @Override
    public String toString() {
        String moveStr = (pieceMoved.getColor() == Piece.Color.WHITE ? "White " : "Black ") +
                         pieceMoved.getClass().getSimpleName() +
                         " " + (char)('a' + start.x) + (8 - start.y) +
                         " to " + (char)('a' + end.x) + (8 - end.y);
        if (pieceCaptured != null) {
            moveStr += " (captures " + pieceCaptured.getClass().getSimpleName() + ")";
        }
        if (isEnPassant) {
            moveStr += " (en passant)";
        }
        if (isCastling) {
            moveStr += " (castling)";
        }
        if (isPromotion) {
            moveStr += " (promotes to " + promotedPieceType.getClass().getSimpleName() + ")";
        }
        return moveStr;
    }
}