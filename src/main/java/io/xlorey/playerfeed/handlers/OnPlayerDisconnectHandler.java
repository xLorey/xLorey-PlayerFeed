package io.xlorey.playerfeed.handlers;

import io.xlorey.fluxloader.events.OnPlayerDisconnect;
import io.xlorey.fluxloader.server.api.ServerUtils;
import io.xlorey.fluxloader.utils.Logger;
import io.xlorey.playerfeed.Main;
import io.xlorey.playerfeed.PluginUtils;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Author: Deknil
 * Date: 19.02.2024
 * GitHub: <a href="https://github.com/Deknil">https://github.com/Deknil</a>
 * Description: Handles the player disconnect event. Notifies other players after a specified delay.
 * <p> xLoreyPlayerFeed © 2024. All rights reserved. </p>
 */
public class OnPlayerDisconnectHandler extends OnPlayerDisconnect {
    @Override
    public void handleEvent(IsoPlayer player, UdpConnection playerConnection) {
        ScheduledFuture<?> disconnectTask = Main.scheduler.schedule(() -> {

            String disconnectText = PluginUtils.getMessageText("notify.playerDisconnect");

            if (disconnectText.isEmpty()) return;

            disconnectText = PluginUtils.handleText(disconnectText, playerConnection.username);

            Logger.print(String.format("PF > Player '%s' (IP: %s | SteamID: %s) has disconnected from the server", playerConnection.username, playerConnection.ip, playerConnection.steamID));

            ServerUtils.sendServerChatMessage(disconnectText);

            Main.disconnectTimers.remove(playerConnection.username);
        }, Main.getDefaultConfig().getInt("disconnectWindowTime"), TimeUnit.SECONDS);

        Main.disconnectTimers.put(playerConnection.username, disconnectTask);
    }
}
