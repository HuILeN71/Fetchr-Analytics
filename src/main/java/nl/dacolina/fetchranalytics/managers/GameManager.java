package nl.dacolina.fetchranalytics.managers;

import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.MinecraftServer;
import nl.dacolina.fetchranalytics.FetchrAnalytics;
import nl.dacolina.fetchranalytics.components.Game;

public class GameManager {
    private static final int STATE_SKYBOX = 2;
    private static final int BEFORE_START = 3;
    private static final int GAME_RUNNING = 4;

    private MinecraftServer server;
    private int gameState;
    private Game game;

    public GameManager(MinecraftServer server) {
        this.server = server;
    }

    public void tick() {
        // Debug get game state
        // FetchrAnalytics.LOGGER.info("Current game state: " + getGameState());
        // Get game state
        this.gameState = getGameState();

        Boolean isInitialized = false;

        if (game != null) {
            if (game.getIsInitialized()) {
                isInitialized = true;
            }
        }


        if (gameState == STATE_SKYBOX) {

        }

        if (gameState == BEFORE_START && !isInitialized) {
            // Init new Game
            game = new Game(server);
        }

        if (gameState == GAME_RUNNING) {
            if(game != null) {
                game.tick(server);
            }
        }
    }

    private int getGameState () {
        // Data from mod suggests: he current game stat 0 = Lobby / Not in game; 1 = Starting / chunk pre-gen; 2 = Skybox phase;
        // 3 = Game starting; 4 = Game started
        return getStateFromServerInstance(server);
    }

    private static int getStateFromServerInstance(MinecraftServer server) {
        Scoreboard scoreboard = server.getScoreboard();

        ScoreboardObjective objective = scoreboard.getNullableObjective("fetchr.state");

        if(objective != null) {
            ScoreHolder gameStatePlayer = ScoreHolder.fromName("$game_state"); // The special "player" name in the scoreboard

            ReadableScoreboardScore gameStateScore = scoreboard.getScore(gameStatePlayer, objective);

            // Get the score value\
            int gameStateValue = gameStateScore.getScore();
            return gameStateValue;
        }

        return -1;

    }

}
