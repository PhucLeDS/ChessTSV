package model;

import pieces.*;
import java.awt.Point;

public class Board {
    private Piece[][] board;
    public static final int SIZE = 8;

    public Board() {
        board = new Piece[SIZE][SIZE];
        initializeBoard();
    }

    private void initializeBoard() {
        // Clear board initially
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                board[i][j] = null;
            }
        }

        // Place Black pieces
        board[0][0] = new Rook(Piece.Color.BLACK, new Point(0, 0));
        board[1][0] = new Knight(Piece.Color.BLACK, new Point(1, 0));
        board[2][0] = new Bishop(Piece.Color.BLACK, new Point(2, 0));
        board[3][0] = new Queen(Piece.Color.BLACK, new Point(3, 0));
        board[4][0] = new King(Piece.Color.BLACK, new Point(4, 0));
        board[5][0] = new Bishop(Piece.Color.BLACK, new Point(5, 0));
        board[6][0] = new Knight(Piece.Color.BLACK, new Point(6, 0));
        board[7][0] = new Rook(Piece.Color.BLACK, new Point(7, 0));
        for (int i = 0; i < SIZE; i++) {
            board[i][1] = new Pawn(Piece.Color.BLACK, new Point(i, 1));
        }

        // Place White pieces
        board[0][7] = new Rook(Piece.Color.WHITE, new Point(0, 7));
        board[1][7] = new Knight(Piece.Color.WHITE, new Point(1, 7));
        board[2][7] = new Bishop(Piece.Color.WHITE, new Point(2, 7));
        board[3][7] = new Queen(Piece.Color.WHITE, new Point(3, 7));
        board[4][7] = new King(Piece.Color.WHITE, new Point(4, 7));
        board[5][7] = new Bishop(Piece.Color.WHITE, new Point(5, 7));
        board[6][7] = new Knight(Piece.Color.WHITE, new Point(6, 7));
        board[7][7] = new Rook(Piece.Color.WHITE, new Point(7, 7));
        for (int i = 0; i < SIZE; i++) {
            board[i][6] = new Pawn(Piece.Color.WHITE, new Point(i, 6));
        }
    }

    public Piece getPiece(Point position) {
        if (isValidPosition(position)) {
            return board[position.x][position.y];
        }
        return null;
    }

    public void setPiece(Point position, Piece piece) {
        if (isValidPosition(position)) {
            board[position.x][position.y] = piece;
            if (piece != null) {
                piece.setPosition(position); // Keep piece's internal position updated
            }
        }
    }

    public boolean isValidPosition(Point p) {
        return p != null && p.x >= 0 && p.x < SIZE && p.y >= 0 && p.y < SIZE;
    }

    // Creates a deep copy of the board for move simulation
    public Board copy() {
        Board newBoard = new Board();
        // Clear the new board's initial setup
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                newBoard.board[i][j] = null;
            }
        }

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                Piece originalPiece = this.board[i][j];
                if (originalPiece != null) {
                    // Create a new instance of the specific piece type
                    // and copy its essential state (color, position, and hasMoved for Pawn, King, Rook)
                    Piece copiedPiece = null;
                    Point newPos = new Point(originalPiece.getPosition()); // Copy the point

                    if (originalPiece instanceof Pawn) {
                        Pawn originalPawn = (Pawn) originalPiece;
                        Pawn newPawn = new Pawn(originalPawn.getColor(), newPos);
                        newPawn.setHasMoved(originalPawn.hasMoved());
                        copiedPiece = newPawn;
                    } else if (originalPiece instanceof Rook) {
                        Rook originalRook = (Rook) originalPiece;
                        Rook newRook = new Rook(originalRook.getColor(), newPos);
                        newRook.setHasMoved(originalRook.hasMoved());
                        copiedPiece = newRook;
                    } else if (originalPiece instanceof Knight) {
                        copiedPiece = new Knight(originalPiece.getColor(), newPos);
                    } else if (originalPiece instanceof Bishop) {
                        copiedPiece = new Bishop(originalPiece.getColor(), newPos);
                    } else if (originalPiece instanceof Queen) {
                        copiedPiece = new Queen(originalPiece.getColor(), newPos);
                    } else if (originalPiece instanceof King) {
                        King originalKing = (King) originalPiece;
                        King newKing = new King(originalKing.getColor(), newPos);
                        newKing.setHasMoved(originalKing.hasMoved()); // For castling
                        copiedPiece = newKing;
                    }
                    newBoard.board[i][j] = copiedPiece;
                }
            }
        }
        return newBoard;
    }

    // For console debugging
    public void printBoard() {
        System.out.println("  a b c d e f g h");
        System.out.println(" -----------------");
        for (int y = 0; y < SIZE; y++) {
            System.out.print((8 - y) + "|");
            for (int x = 0; x < SIZE; x++) {
                Piece piece = board[x][y];
                System.out.print((piece == null ? "." : piece.getSymbol()) + " ");
            }
            System.out.println("|" + (8 - y));
        }
        System.out.println(" -----------------");
        System.out.println("  a b c d e f g h");
    }
}