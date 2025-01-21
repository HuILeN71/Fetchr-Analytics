package nl.dacolina.fetchranalytics.components;

import net.minecraft.server.MinecraftServer;

public class Game {

    private boolean isInitialized;

    public Game(MinecraftServer server) {
        // Set init value to false
        this.isInitialized = false;

        // Init bingo board
        BingoCard bingoBoard = new BingoCard(server);

        // Init teams

        this.isInitialized = true;
    }

    public boolean getIsInitialized() {
        return this.isInitialized;
    }

}
