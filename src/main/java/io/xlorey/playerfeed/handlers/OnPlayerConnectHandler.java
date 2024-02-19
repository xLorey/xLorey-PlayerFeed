package io.xlorey.playerfeed.handlers;

import io.xlorey.fluxloader.events.OnPlayerFullyConnected;
import io.xlorey.fluxloader.server.api.ServerUtils;
import io.xlorey.fluxloader.utils.Logger;
import io.xlorey.playerfeed.Main;
import io.xlorey.playerfeed.PluginUtils;
import zombie.core.raknet.UdpConnection;

import java.nio.ByteBuffer;
import java.util.concurrent.ScheduledFuture;

/**
 * Author: Deknil
 * Date: 19.02.2024
 * GitHub: <a href="https://github.com/Deknil">https://github.com/Deknil</a>
 * Description: Handles the player connect event. Cancels the disconnect message if the player reconnects quickly.
 * <p> xLoreyPlayerFeed © 2024. All rights reserved. </p>
 */
public class OnPlayerConnectHandler extends OnPlayerFullyConnected {
    @Override
    public void handleEvent(ByteBuffer data, UdpConnection playerConnection, String username) {
        ScheduledFuture<?> disconnectTask = Main.disconnectTimers.get(playerConnection.username);
        if (disconnectTask != null && !disconnectTask.isDone()) {
            disconnectTask.cancel(false);
            return;
        }

        String connectText = PluginUtils.getMessageText("notify.playerConnect");

        if (connectText.isEmpty()) return;

        connectText = PluginUtils.handleText(connectText, playerConnection.username);

        Logger.print(String.format("PF > Player '%s' (IP: %s | SteamID: %s) connected to the server", playerConnection.username, playerConnection.ip, playerConnection.steamID));

        ServerUtils.sendServerChatMessage(connectText);
    }
}
