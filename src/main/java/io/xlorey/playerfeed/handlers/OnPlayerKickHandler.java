package io.xlorey.playerfeed.handlers;

import io.xlorey.fluxloader.events.OnPlayerKick;
import io.xlorey.fluxloader.server.api.ServerUtils;
import io.xlorey.fluxloader.utils.Logger;
import io.xlorey.playerfeed.PluginUtils;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.network.GameServer;

/**
 * Author: Deknil
 * Date: 19.02.2024
 * GitHub: <a href="https://github.com/Deknil">https://github.com/Deknil</a>
 * Description: Handles player kick events and notifies other players.
 * <p> xLoreyPlayerFeed © 2024. All rights reserved. </p>
 */
public class OnPlayerKickHandler extends OnPlayerKick {
    @Override
    public void handleEvent(IsoPlayer player, String adminName, String reason) {
        if (reason.isEmpty()) reason = "-";

        String kickText = PluginUtils.getMessageText("notify.playerKick");
        if (kickText.isEmpty()) return;

        kickText = PluginUtils.handleText(kickText, player.username);
        kickText = kickText.replace("<REASON>", reason);

        UdpConnection connectionPlayer = GameServer.getConnectionFromPlayer(player);
        if (connectionPlayer != null) {
            Logger.print(String.format("PF > Player '%s' (IP: %s | SteamID: %s) was kicked for a reason '%s'", player.username, connectionPlayer.ip, connectionPlayer.steamID, reason));
        } else {
            Logger.print(String.format("PF > Player '%s' was kicked for a reason '%s'",
                    player.username,
                    reason
            ));
        }

        ServerUtils.sendServerChatMessage(kickText);
    }
}
