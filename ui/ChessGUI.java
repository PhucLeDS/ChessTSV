package ui;

import game.Game;
import model.Board;
import model.Piece;
import pieces.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

public class ChessGUI extends JFrame {
    private JLabel whiteScoreLabel;
    private JLabel blackScoreLabel;
    private static final Map<Class<?>, Integer> PIECE_VALUES = new HashMap<>() {{
        put(Queen.class, 9);
        put(Rook.class, 5);
        put(Bishop.class, 3);
        put(Knight.class, 3);
        put(Pawn.class, 1);
    }};
    private Game game;
    private JPanel boardPanel;
    private JLabel statusLabel;
    private Point selectedSquare = null;
    private List<Point> possibleMovesToHighlight = new ArrayList<>(); // For showing valid moves

    private Map<String, ImageIcon> pieceImages;
    private final int TILE_SIZE = 80;

    // UI elements for the right panel
    private JLabel whiteTimerLabel;
    private JLabel blackTimerLabel;
    private JList<String> moveList;
    private DefaultListModel<String> moveListModel;
    private Timer swingTimer; // For updating game timers

    public ChessGUI(Game game) {
        this.game = game;
        setTitle("Simple Chess Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        loadPieceImages();

        // Main content panel (board on left, info on right)
        JPanel mainContentPanel = new JPanel(new BorderLayout());
        add(mainContentPanel, BorderLayout.CENTER);

        // Board Panel
        boardPanel = new JPanel(new GridLayout(Board.SIZE, Board.SIZE));
        boardPanel.setPreferredSize(new Dimension(Board.SIZE * TILE_SIZE, Board.SIZE * TILE_SIZE));
        mainContentPanel.add(boardPanel, BorderLayout.CENTER);

        // Right-side Panel for score, history, timer
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(250, Board.SIZE * TILE_SIZE));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding
        mainContentPanel.add(rightPanel, BorderLayout.EAST);

        // Timers
        JPanel timerPanel = new JPanel(new GridLayout(2, 1));
        blackTimerLabel = new JLabel("Black: --:--", SwingConstants.CENTER);
        whiteTimerLabel = new JLabel("White: --:--", SwingConstants.CENTER);
        blackTimerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        whiteTimerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        timerPanel.add(blackTimerLabel);
        timerPanel.add(whiteTimerLabel);
        rightPanel.add(timerPanel);
        rightPanel.add(Box.createVerticalStrut(10)); // Spacer

        JLabel historyLabel = new JLabel("Move History:");
        historyLabel.setFont(new Font("Arial", Font.BOLD, 16));
        historyLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center align the label
        rightPanel.add(historyLabel);
        
        moveListModel = new DefaultListModel<>();
        moveList = new JList<>(moveListModel);
        moveList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        moveList.setLayoutOrientation(JList.VERTICAL);
        moveList.setVisibleRowCount(-1);
        moveList.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane moveScrollPane = new JScrollPane(moveList);
        moveScrollPane.setPreferredSize(new Dimension(230, 200));
        rightPanel.add(moveScrollPane);
        rightPanel.add(Box.createVerticalStrut(10)); // Spacer

        // Score Board
        JPanel scorePanel = new JPanel(new GridLayout(2, 1));
        whiteScoreLabel = new JLabel("White: 0 points", SwingConstants.CENTER);
        blackScoreLabel = new JLabel("Black: 0 points", SwingConstants.CENTER);
        whiteScoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        blackScoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        scorePanel.add(whiteScoreLabel);
        scorePanel.add(blackScoreLabel);
        rightPanel.add(scorePanel);
        


        // Status Label (below the board)
        statusLabel = new JLabel("White's Turn", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(statusLabel, BorderLayout.SOUTH);

        // Control Panel (e.g., Undo Button)
        JPanel controlPanel = new JPanel();
        JButton undoButton = new JButton("Undo");
        undoButton.addActionListener(e -> {
            game.undoLastMove();
            updateBoardAndUI();
        });
        controlPanel.add(undoButton);
        add(controlPanel, BorderLayout.NORTH); // Placed at top for now

        setupBoardUI();
        addMouseListenerToBoard();
        startSwingTimer(); // Start the GUI timer updates

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Add these new methods to calculate and update scores
    private int calculateScore(Piece.Color color) {
        int score = 0;
        Board board = game.getBoard();
        
        for (int x = 0; x < Board.SIZE; x++) {
            for (int y = 0; y < Board.SIZE; y++) {
                Piece piece = board.getPiece(new Point(x, y));
                if (piece != null && piece.getColor() == color) {
                    Integer value = PIECE_VALUES.get(piece.getClass());
                    if (value != null) {
                        score += value;
                    }
                }
            }
        }
        return score;
    }

    private void updateScoreLabels() {
        int whiteScore = calculateScore(Piece.Color.WHITE);
        int blackScore = calculateScore(Piece.Color.BLACK);
        whiteScoreLabel.setText(String.format("White: %d points", whiteScore));
        blackScoreLabel.setText(String.format("Black: %d points", blackScore));
    }


    private void updateMoveHistory() {
        moveListModel.clear();
        List<String> history = game.getFormattedMoveHistory();
        for (int i = 0; i < history.size(); i += 2) {
            StringBuilder turnMove = new StringBuilder();
            turnMove.append(String.format("%2d. %-12s", (i / 2) + 1, history.get(i)));
            if (i + 1 < history.size()) {
                turnMove.append(String.format("%-12s", history.get(i + 1)));
            }
            moveListModel.addElement(turnMove.toString());
        }
        if (!moveListModel.isEmpty()) {
            moveList.ensureIndexIsVisible(moveListModel.size() - 1);
        }
    }
    private void loadPieceImages() {
        pieceImages = new HashMap<>();
        String[] pieceSymbols = {"P", "R", "N", "B", "Q", "K", "p", "r", "n", "b", "q", "k"};
        String[] fileNames = {
            "white_pawn.png", "white_rook.png", "white_knight.png", "white_bishop.png",
            "white_queen.png", "white_king.png",
            "black_pawn.png", "black_rook.png", "black_knight.png", "black_bishop.png",
            "black_queen.png", "black_king.png"
        };

        for (int i = 0; i < pieceSymbols.length; i++) {
            try (InputStream is = getClass().getResourceAsStream("/resources/" + fileNames[i])) {
                if (is != null) {
                    Image image = ImageIO.read(is);
                    pieceImages.put(pieceSymbols[i], new ImageIcon(image));
                } else {
                    System.err.println("Resource not found: /resources/" + fileNames[i]);
                }
            } catch (IOException e) {
                System.err.println("Error loading image: " + fileNames[i] + " - " + e.getMessage());
            }
        }
    }

    private void setupBoardUI() {
        boardPanel.removeAll();
        for (int y = 0; y < Board.SIZE; y++) {
            for (int x = 0; x < Board.SIZE; x++) {
                JPanel square = new JPanel(new BorderLayout());
                Color color = ((x + y) % 2 == 0) ? new Color(240, 217, 181) : new Color(181, 136, 99);
                square.setBackground(color);

                // Default border, overridden later for highlights
                square.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));

                Piece piece = game.getBoard().getPiece(new Point(x, y));
                if (piece != null) {
                    JLabel pieceLabel = new JLabel();
                    ImageIcon icon = pieceImages.get(piece.getSymbol());
                    if (icon != null) {
                        Image image = icon.getImage().getScaledInstance(TILE_SIZE, TILE_SIZE, Image.SCALE_SMOOTH);
                        pieceLabel.setIcon(new ImageIcon(image));
                    } else {
                        pieceLabel.setText(piece.getSymbol());
                        pieceLabel.setFont(new Font("Arial", Font.BOLD, 40));
                        pieceLabel.setHorizontalAlignment(SwingConstants.CENTER);
                        pieceLabel.setVerticalAlignment(SwingConstants.CENTER);
                    }
                    square.add(pieceLabel, BorderLayout.CENTER);
                }

                boardPanel.add(square);
            }
        }

        // Apply highlights after all squares are created
        applyHighlights();

        boardPanel.revalidate();
        boardPanel.repaint();
    }

    private void applyHighlights() {
        // Highlight selected square
        if (selectedSquare != null) {
            getSquarePanel(selectedSquare).setBorder(new LineBorder(Color.BLUE, 3));
        }

        // Highlight valid moves
        for (Point p : possibleMovesToHighlight) {
            JPanel square = getSquarePanel(p);
            // Draw a semi-transparent circle or change background slightly
            square.setBackground(new Color(100, 255, 100, 150)); // Greenish overlay
            // You can also draw a dot directly on the panel
            square.add(new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.setColor(Color.GREEN.darker());
                    g.fillOval(getWidth() / 2 - 10, getHeight() / 2 - 10, 20, 20);
                }
            }, BorderLayout.CENTER); // Add to square panel directly
            square.setOpaque(false); // Allow background to show through
        }

        // Highlight King in check
        if (game.isKingInCheck(game.getCurrentPlayerTurn())) {
            Point kingPos = findKingPosition(game.getBoard(), game.getCurrentPlayerTurn());
            if (kingPos != null) {
                getSquarePanel(kingPos).setBorder(new LineBorder(Color.RED, 4));
            }
        }
    }


    private JPanel getSquarePanel(Point p) {
        return (JPanel) boardPanel.getComponent(p.y * Board.SIZE + p.x);
    }

    private void addMouseListenerToBoard() {
        boardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX() / TILE_SIZE;
                int y = e.getY() / TILE_SIZE;
                Point clickedPoint = new Point(x, y);

                if (game.isGameOver()) {
                    System.out.println("Game is over. No more moves.");
                    return;
                }

                if (selectedSquare == null) {
                    // First click: select a piece
                    Piece clickedPiece = game.getBoard().getPiece(clickedPoint);
                    if (clickedPiece != null && clickedPiece.getColor() == game.getCurrentPlayerTurn()) {
                        selectedSquare = clickedPoint;
                        possibleMovesToHighlight = game.getLegalMovesForPiece(selectedSquare);
                        setupBoardUI(); // Redraw to show selected piece and valid moves
                    } else {
                        System.out.println("No piece or not your piece at selected square.");
                        clearSelection(); // Clear any previous (invalid) selection state
                    }
                } else {
                    // Second click: attempt to move
                    Piece promotedPieceType = null;

                    // Check for pawn promotion (only if the move is legal)
                    Piece pieceToMove = game.getBoard().getPiece(selectedSquare);
                    if (pieceToMove instanceof Pawn) {
                        int lastRank = (pieceToMove.getColor() == Piece.Color.WHITE) ? 0 : 7;
                        if (clickedPoint.y == lastRank) {
                            promotedPieceType = showPromotionDialog(pieceToMove.getColor());
                            if (promotedPieceType == null) { // User cancelled promotion
                                clearSelection();
                                setupBoardUI(); // Redraw to clear highlights
                                return;
                            }
                        }
                    }

                    boolean moveSuccessful = game.makeMove(selectedSquare, clickedPoint, promotedPieceType);

                    if (moveSuccessful) {
                        updateBoardAndUI(); // Redraw board, update status, history
                    } else {
                        System.out.println("Invalid move. Please try again.");
                    }
                    clearSelection(); // Always clear selection after second click
                }
            }
        });
    }

    private void updateBoardAndUI() {
        // Clear old highlights
        possibleMovesToHighlight.clear();
        setupBoardUI();
        updateStatus();
        updateMoveHistory();
        updateScoreLabels();

        // Check for game over conditions
        if (game.isGameOver()) {
            swingTimer.stop(); // Stop the timer
            String message;
            if (game.isKingInCheck(game.getCurrentPlayerTurn())) {
                message = (game.getCurrentPlayerTurn() == Piece.Color.WHITE ? 
                        "Black wins by checkmate!" : "White wins by checkmate!");
            } else {
                message = "Game is drawn by stalemate!";
            }
            showEndGameDialog(message);
        }
    }
    private Piece showPromotionDialog(Piece.Color pawnColor) {
        // Options for promotion (Queen, Rook, Bishop, Knight)
        Object[] options = {
            new Queen(pawnColor, null), new Rook(pawnColor, null),
            new Bishop(pawnColor, null), new Knight(pawnColor, null)
        };

        // Custom renderer for JList to display piece symbols or images
        ListCellRenderer<Object> renderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Piece) {
                    Piece p = (Piece) value;
                    ImageIcon icon = pieceImages.get(p.getSymbol());
                    if (icon != null) {
                        Image image = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                        label.setIcon(new ImageIcon(image));
                        label.setText(p.getClass().getSimpleName()); // Show name next to icon
                    } else {
                        label.setText(p.getClass().getSimpleName() + " (" + p.getSymbol() + ")");
                    }
                }
                return label;
            }
        };


        Piece selectedPiece = (Piece) JOptionPane.showInputDialog(
                this,
                "Pawn Promotion! Choose a piece:",
                "Pawn Promotion",
                JOptionPane.QUESTION_MESSAGE,
                null, // No default icon for dialog
                options,
                options[0] // Default selection: Queen
        );

        return selectedPiece;
    }

    private void clearHighlights() {
        for (int y = 0; y < Board.SIZE; y++) {
            for (int x = 0; x < Board.SIZE; x++) {
                JPanel square = getSquarePanel(new Point(x,y));
                // Reset to original border (gray, 1px)
                square.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
                // Reset background to original checkerboard pattern
                Color originalColor = ((x + y) % 2 == 0) ? new Color(240, 217, 181) : new Color(181, 136, 99);
                square.setBackground(originalColor);
                square.setOpaque(true); // Ensure it's opaque if we made it non-opaque for highlights
                // Remove any dynamically added components (like the green dot)
                for (Component comp : square.getComponents()) {
                    if (comp instanceof JPanel && comp.getParent() == square) {
                         // Check if it's our green dot component and remove it
                        if (comp.getPreferredSize().width == 20 && comp.getPreferredSize().height == 20) { // Simple check
                            square.remove(comp);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void clearSelection() {
        clearHighlights(); // This also clears valid move highlights
        selectedSquare = null;
        possibleMovesToHighlight.clear(); // Ensure the list is empty
    }

    private void updateStatus() {
        String turn = (game.getCurrentPlayerTurn() == Piece.Color.WHITE) ? "White" : "Black";
        String statusText = turn + "'s Turn";

        if (game.isKingInCheck(game.getCurrentPlayerTurn())) {
            statusText += " (in Check!)";
        }

        statusLabel.setText(statusText);
    }


    private void startSwingTimer() {
        swingTimer = new Timer(100, new ActionListener() { // Update every 100ms
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                game.decrementTimer(); // Decrement game model timers
                updateTimerLabels();    // Update GUI labels
                if (game.isGameOver()) {
                    swingTimer.stop(); // Stop the timer if game is over
                    // Additional game over handling for timeout could go here
                }
            }
        });
        swingTimer.start();
    }

    private void updateTimerLabels() {
        long whiteTime = game.getWhiteTimeMillis();
        long blackTime = game.getBlackTimeMillis();

        whiteTimerLabel.setText(formatTime(whiteTime, "White"));
        blackTimerLabel.setText(formatTime(blackTime, "Black"));
    }

    private String formatTime(long millis, String player) {
        long totalSeconds = millis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%s: %02d:%02d", player, minutes, seconds);
    }

    private Point findKingPosition(Board board, Piece.Color kingColor) {
        for (int x = 0; x < Board.SIZE; x++) {
            for (int y = 0; y < Board.SIZE; y++) {
                Piece piece = board.getPiece(new Point(x, y));
                if (piece instanceof King && piece.getColor() == kingColor) {
                    return new Point(x, y);
                }
            }
        }
        return null; // Should not happen in a valid game unless King is captured
    }

    private void showEndGameDialog(String message) {
    JDialog dialog = new JDialog(this, "Game Over", true);
    dialog.setLayout(new BorderLayout(10, 10));
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    // Message panel
    JPanel messagePanel = new JPanel();
    JLabel messageLabel = new JLabel(message);
    messageLabel.setFont(new Font("Arial", Font.BOLD, 18));
    messagePanel.add(messageLabel);
    dialog.add(messagePanel, BorderLayout.CENTER);

    // Button panel
    JPanel buttonPanel = new JPanel();
    JButton restartButton = new JButton("New Game");
    restartButton.setFont(new Font("Arial", Font.BOLD, 14));
    restartButton.addActionListener(e -> {
        game = new Game(); // Create new game
        updateBoardAndUI();
        clearSelection();
        startSwingTimer();
        dialog.dispose();
    });
    buttonPanel.add(restartButton);
    dialog.add(buttonPanel, BorderLayout.SOUTH);

    // Set dialog size and position
    dialog.setSize(300, 150);
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
    }
}