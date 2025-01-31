package nl.dacolina.fetchranalytics.components;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Team {

    private String teamName;
    private List<Player> teamMembers;
    private VirtualBingoCard vBingoCard;
    private boolean lineBingo;
    private boolean blackOut;

    public Team(String teamName) {
        this.teamName = teamName;
        this.teamMembers = new ArrayList<>();
        this.vBingoCard = new VirtualBingoCard();
        this.lineBingo = false;
        this.blackOut = false;
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

    public void addItemToVirtualBoard(int itemID) {
        this.vBingoCard.setValueInVirtualBoard(itemID);
    }

    public String checkForLineBingo() {
        return this.vBingoCard.checkForLineBingo();
    }

    public boolean isLineBingo() {
        return this.lineBingo;
    }

    public boolean isBlackOut(){
        return this.blackOut;
    }

    public void setLineBingo(boolean lineBingo) {
        this.lineBingo = lineBingo;
    }

    public void setBlackOut(boolean blackOut) {
        this.blackOut = blackOut;
    }

    public int getItemCount() {
        int itemCount = 0;

        for(Player player : this.teamMembers) {
            itemCount += player.getCollectedItems().size();
        }

        return itemCount;
    }
}
