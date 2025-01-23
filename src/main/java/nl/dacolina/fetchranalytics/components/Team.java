package nl.dacolina.fetchranalytics.components;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Team {

    private String teamName;
    private List<Player> teamMembers;

    public Team(String teamName) {
        this.teamName = teamName;
        this.teamMembers = new ArrayList<>();
    }

    public void addTeamMember(UUID playerUUID, String playerName) {
        teamMembers.add(new Player(playerUUID, playerName));
    }

    public List<Player> getTeamMembers() {
        return this.teamMembers;
    }

    public String getTeamName() {
        return this.teamName;
    }

}
