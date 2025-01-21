package nl.dacolina.fetchranalytics.managers;

import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.MinecraftServer;
import nl.dacolina.fetchranalytics.FetchrAnalytics;

public class GameManager {

    private MinecraftServer server;

    public GameManager(MinecraftServer server) {
        this.server = server;
    }

    public void tick() {
        FetchrAnalytics.LOGGER.info("Current game state: " + getGameState());
    }

    private int getGameState () {
        // Data from mod suggests: he current game stat 0 = Lobby / Not in game; 1 = Starting / chunk pre-gen; 2 = Skybox phase; 3 = Game started
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
