package nl.dacolina.fetchranalytics.components;

public class SimpleBingoCard {
    private static final int BINGOBOARD_LINE_SIZE = 5;
    private boolean[][] virtualBoard;

    public SimpleBingoCard() {
        // Create new virtual board
        this.virtualBoard = new boolean[BINGOBOARD_LINE_SIZE][BINGOBOARD_LINE_SIZE];
    }
}
