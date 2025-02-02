# Fetchr Analytics add-on
This is an add-on for a Minecraft bingo datapack created by NeunEinser! It aims to log all the succesful runs and several statistics associated with those runs. These will be stored in a database (MariaDB) and the stats can be seen in a custom web application or simplified data in discord.

# Elements
The complete project will consist of the following elements
- A Minecraft Fabric mod that captures all the in-game data and sends it to a database.
- An API for publishing run information to a global leaderboard
- A Web Application to display all the gathered game data

### Additional notes
In order to get all the statistics required, some minor changes to the original datapack were required. Since these are minor the mod takes care of editing the datapack. The changes were needed to track which player gets what item.

# Features
The mod can currently track the following in a MariaDB Database:

✅ Automatically creates tables and fills them with required data </br>
✅ Track at which time a player got an item in the game </br>
✅ Supports a custom teams/item pool (History is kept in database) </br>
❌ Supports all game modes available (Currently only a simple line or black-out) </br>
❌ Supports API to send data to a global leaderboard
