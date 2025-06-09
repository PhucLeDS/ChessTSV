import game.Game;
import ui.ChessGUI;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Game chessGame = new Game();
            new ChessGUI(chessGame);
        });
    }
}