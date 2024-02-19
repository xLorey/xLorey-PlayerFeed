package io.xlorey.playerfeed;

import io.xlorey.fluxloader.plugin.Configuration;
import io.xlorey.fluxloader.plugin.Plugin;
import io.xlorey.fluxloader.shared.EventManager;
import io.xlorey.playerfeed.handlers.*;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * Author: Deknil
 * Date: 19.02.2024
 * GitHub: <a href="https://github.com/Deknil">https://github.com/Deknil</a>
 * Description: Main class for the PlayerFeed plugin.
 *              This plugin handles various player events such as connect, disconnect, death, kick, and ban, and
 *              notifies other players on the server about these events.
 * <p> xLoreyPlayerFeed © 2024. All rights reserved. </p>
 */
public class Main extends Plugin {
    private static Main instance;
    public static final Map<String, ScheduledFuture<?>> disconnectTimers = new ConcurrentHashMap<>();
    public static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public static final Random random = new Random();

    /**
     * Initializes the plugin and loads the default configuration.
     */
    @Override
    public void onInitialize() {
        saveDefaultConfig();

        EventManager.subscribe(new OnPlayerDisconnectHandler());
        EventManager.subscribe(new OnPlayerBanHandler());
        EventManager.subscribe(new OnPlayerKickHandler());
        EventManager.subscribe(new OnPlayerDeathHandler());
        EventManager.subscribe(new OnPlayerConnectHandler());

        instance = this;
    }

    /**
     * Getting the standard config
     * @return standard config
     */
    public static Configuration getDefaultConfig() {
        return instance.getConfig();
    }
}