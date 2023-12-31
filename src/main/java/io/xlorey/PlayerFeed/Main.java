package io.xlorey.PlayerFeed;

import io.xlorey.FluxLoader.annotations.SubscribeEvent;
import io.xlorey.FluxLoader.plugin.Plugin;
import io.xlorey.FluxLoader.server.api.ServerUtils;
import io.xlorey.FluxLoader.utils.Logger;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoPlayer;
import zombie.core.logger.LoggerManager;
import zombie.core.logger.ZLogger;
import zombie.core.raknet.UdpConnection;
import zombie.network.GameServer;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Main class for the PlayerFeed plugin.
 * This plugin handles various player events such as connect, disconnect, death, kick, and ban, and
 * notifies other players on the server about these events.
 */
public class Main extends Plugin {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<String, ScheduledFuture<?>> disconnectTimers = new ConcurrentHashMap<>();

    /**
     * Initializes the plugin and loads the default configuration.
     */
    @Override
    public void onInitialize() {
        saveDefaultConfig();
    }

    /**
     * Writes a message to the log if it is allowed in the configuration.
     * This method checks whether logging is enabled in the plugin configuration file,
     * and if yes, then writes the provided message to the log using the specified logger.
     * @param text Text message to be written to the log.
     */
    private void writeLog(String text) {
        if (getConfig().getBoolean("writeLogFeed")) {
            ZLogger playerFeed = LoggerManager.getLogger("PlayerFeed");
            Logger.printLog(playerFeed, text);
        }
    }

    /**
     * Handles custom text replacements for messages.
     * @param text     The text with placeholders.
     * @param username The username to replace the placeholder with.
     * @return The text with placeholders replaced.
     */
    private String handleText(String text, String username){
        text = text.replace("<USERNAME>", username);
        text = text.replace("<SPACE_SYMBOL>", "\u200B");
        return text;
    }

    /**
     * Handles the player disconnect event. Notifies other players after a specified delay.
     * @param playerInstance   The instance of the player who disconnected.
     * @param playerConnection The connection information of the player.
     */
    @SubscribeEvent(eventName="onPlayerDisconnect")
    public void onPlayerDisconnectHandler(IsoPlayer playerInstance, UdpConnection playerConnection){
        ScheduledFuture<?> disconnectTask = scheduler.schedule(() -> {

            String disconnectText = getConfig().getString("notify.playerDisconnect");
            disconnectText = handleText(disconnectText, playerConnection.username);

            writeLog(String.format("Player '%s' (IP: %s | SteamID: %s) has disconnected from the server", playerConnection.username, playerConnection.ip, playerConnection.steamID));

            ServerUtils.sendServerChatMessage(disconnectText);

            disconnectTimers.remove(playerConnection.username);
        }, getConfig().getInt("disconnectWindowTime"), TimeUnit.SECONDS);

        disconnectTimers.put(playerConnection.username, disconnectTask);
    }

    /**
     * Handles the player connect event. Cancels the disconnect message if the player reconnects quickly.
     * @param playerData       The data of the player who connected.
     * @param playerConnection The connection information of the player.
     * @param username         The username of the player.
     */
    @SubscribeEvent(eventName="onPlayerConnect")
    public void onPlayerConnectHandler(ByteBuffer playerData, UdpConnection playerConnection, String username){
        ScheduledFuture<?> disconnectTask = disconnectTimers.get(playerConnection.username);
        if (disconnectTask != null && !disconnectTask.isDone()) {
            disconnectTask.cancel(false);
            return;
        }

        String connectText = getConfig().getString("notify.playerConnect");
        connectText = handleText(connectText, playerConnection.username);

        writeLog(String.format("Player '%s' (IP: %s | SteamID: %s) connected to the server", playerConnection.username, playerConnection.ip, playerConnection.steamID));

        ServerUtils.sendServerChatMessage(connectText);
    }

    /**
     * Handles player death events and notifies other players.
     * @param character The character who died.
     */
    @SubscribeEvent(eventName="OnCharacterDeath")
    public void onPlayerDeathHandler(IsoGameCharacter character){
        if (!(character instanceof IsoPlayer player)) return;

        IsoPlayer attacker = null;

        if (character.getAttackedBy() instanceof IsoPlayer) {
            attacker = (IsoPlayer) character.getAttackedBy();
        }


        String deathText;
        UdpConnection connectionPlayer = GameServer.getConnectionFromPlayer(player);
        if (attacker == null) {
            if (connectionPlayer != null) {
                writeLog(String.format("Player '%s' (IP: %s | SteamID: %s) died", player.username, connectionPlayer.ip, connectionPlayer.steamID));
            } else {
                writeLog(String.format("Player '%s' died",
                        player.username
                ));
            }

            deathText = getConfig().getString("notify.playerDeath");
        } else{
            UdpConnection connectionAttacker = GameServer.getConnectionFromPlayer(attacker);
            if (connectionPlayer != null && connectionAttacker != null) {
                writeLog(String.format("Player '%s' (IP: %s | SteamID: %s) died in battle with '%s' (IP: %s | SteamID: %s)",
                        player.username, connectionPlayer.ip, connectionPlayer.steamID,
                        attacker.username, connectionAttacker.ip, connectionAttacker.steamID
                ));
            } else {
                writeLog(String.format("Player '%s' died in battle with '%s'",
                        player.username,
                        attacker.username
                ));
            }
            deathText = getConfig().getString("notify.playerDeathPvp");
            deathText = deathText.replace("<ATTACKER>", attacker.getUsername());
        }

        deathText = handleText(deathText, player.getUsername());

        ServerUtils.sendServerChatMessage(deathText);
    }

    /**
     * Handles player kick events and notifies other players.
     * @param player The player who was kicked.
     * @param reason The reason for the kick.
     */
    @SubscribeEvent(eventName="onPlayerKick")
    public void onPlayerKickHandler(IsoPlayer player, String reason){
        if (reason.isEmpty()) reason = "-";

        String kickText = getConfig().getString("notify.playerKick");
        kickText = handleText(kickText, player.username);
        kickText = kickText.replace("<REASON>", reason);

        UdpConnection connectionPlayer = GameServer.getConnectionFromPlayer(player);
        if (connectionPlayer != null) {
            writeLog(String.format("Player '%s' (IP: %s | SteamID: %s) was kicked for a reason '%s'", player.username, connectionPlayer.ip, connectionPlayer.steamID, reason));
        } else {
            writeLog(String.format("Player '%s' was kicked for a reason '%s'",
                    player.username,
                    reason
            ));
        }

        ServerUtils.sendServerChatMessage(kickText);
    }

    /**
     * Handles player ban events and notifies other players.
     * @param player The player who was banned.
     * @param reason The reason for the ban.
     */
    @SubscribeEvent(eventName="onPlayerBan")
    public void onPlayerBanHandler(IsoPlayer player, String reason){
        if (reason.isEmpty()) reason = "-";

        String banText = getConfig().getString("notify.playerBan");
        banText = handleText(banText, player.username);
        banText = banText.replace("<REASON>", reason);

        UdpConnection connectionPlayer = GameServer.getConnectionFromPlayer(player);
        if (connectionPlayer != null) {
            writeLog(String.format("Player '%s' (IP: %s | SteamID: %s) was banned for a reason '%s'", player.username, connectionPlayer.ip, connectionPlayer.steamID, reason));
        } else {
            writeLog(String.format("Player '%s' was banned for a reason '%s'",
                    player.username,
                    reason
            ));
        }

        ServerUtils.sendServerChatMessage(banText);
    }
}