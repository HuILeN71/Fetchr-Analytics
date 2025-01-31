package nl.dacolina.fetchranalytics.components;

import nl.dacolina.fetchranalytics.FetchrAnalytics;

public class VirtualBingoCard {

    private static final int BINGOBOARD_LINE_SIZE = 5;
    private boolean[][] virtualBoard;

    public VirtualBingoCard() {
        // Create new virtual board
        this.virtualBoard = new boolean[BINGOBOARD_LINE_SIZE][BINGOBOARD_LINE_SIZE];

        initVirtualBingoCard();

    }

    public void initVirtualBingoCard() {
        for (int i = 0; i < BINGOBOARD_LINE_SIZE; i++) {
            for(int j = 0; j < BINGOBOARD_LINE_SIZE; j++) {
                this.virtualBoard[i][j] = false;
            }
        }
    }

    public void setValueInVirtualBoard(int itemID) {
        this.virtualBoard[getRowPos(itemID)][getColumnPos(itemID)] = true;
        debugVirtualBingoCard();
    }

    private int getRowPos (int itemID) {
        return itemID / BINGOBOARD_LINE_SIZE;
    }

    private int getColumnPos (int itemID) {
        return itemID % BINGOBOARD_LINE_SIZE;
    }

    public String checkForLineBingo() {
        for (int row = 0; row < BINGOBOARD_LINE_SIZE; row++) {
            if(hasRowBingo(row)) {
                return "row." + row;
            }
        }

        for (int col = 0; col < BINGOBOARD_LINE_SIZE; col++) {
            if(hasColumnBingo(col)) {
                return "col." + col;
            }
        }

        if (hasMainDiagonal()) {
            return "diagonal.main";
        }

        if (hasAntiDiagonal()) {
            return "diagonal.anti";
        }

        return "none";
    }

    private boolean hasRowBingo(int row) {
        for (int col = 0; col < BINGOBOARD_LINE_SIZE; col++) {
            if(!this.virtualBoard[row][col]) {
                return false;
            }
        }
        return true;
    }

    private boolean hasColumnBingo(int col) {
        for (int row = 0; row < BINGOBOARD_LINE_SIZE; row++) {
            if(!this.virtualBoard[row][col]) {
                return false;
            }
        }
        return true;
    }

    private boolean hasMainDiagonal() {
        for (int i = 0; i < BINGOBOARD_LINE_SIZE; i++) {
            if(!this.virtualBoard[i][i]) {
                return false;
            }
        }
        return true;
    }

    private boolean hasAntiDiagonal() {
        for (int i = 0; i < BINGOBOARD_LINE_SIZE; i++) {
            if(!this.virtualBoard[i][(BINGOBOARD_LINE_SIZE - 1) - i]) {
                return false;
            }
        }
        return true;
    }

    public void debugVirtualBingoCard () {
        FetchrAnalytics.LOGGER.info("Current Virtual board for team:");
        for (int i = 0; i < BINGOBOARD_LINE_SIZE; i++) {

            StringBuilder sBuilder = new StringBuilder();

            for(int j = 0; j < BINGOBOARD_LINE_SIZE; j++) {
                sBuilder.append(" | ");
                sBuilder.append(this.virtualBoard[i][j]);

            }

            sBuilder.append(" |");
            FetchrAnalytics.LOGGER.info(String.valueOf(sBuilder));
        }
    }

}
