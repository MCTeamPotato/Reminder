package me.kall.reminder;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mod(Reminder.MOD_ID)
public final class Reminder {
    public static final String MOD_ID = "reminder";

    private static int lastDay = -1;
    private static int lastSecond = -1;
    private static final Map<String, Component> ACTIVITIES = new Object2ObjectOpenHashMap<>();

    public Reminder(@NotNull IEventBus modBus, Dist dist, @NotNull ModContainer container) {
        IEventBus forgeBus = NeoForge.EVENT_BUS;

        forgeBus.addListener(this::onServerTick);
        forgeBus.addListener(this::onServerStart);

        modBus.addListener(RemindConfig::reload);

        container.registerConfig(ModConfig.Type.COMMON, RemindConfig.INSTANCE);
    }

    public void onServerStart(ServerStartedEvent event) {
        RemindConfig.validateConfig();
    }

    public void onServerTick(ServerTickEvent.Pre event) {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        int minute = now.getMinute();
        int second = now.getSecond();
        int dayOfWeek = now.getDayOfWeek().getValue();
        int dayOfMonth = now.getDayOfMonth();
        int month = now.getMonth().getValue();

        if (lastDay == -1) {
            lastDay = dayOfMonth;
        } else if (lastDay != dayOfMonth){
            RemindConfig.validateConfig();
            lastDay = dayOfMonth;
        }

        if (second != lastSecond) {
            lastSecond = second;
            Optional.ofNullable(RemindConfig.getActivities(hour, minute, second, dayOfWeek, dayOfMonth, month)).ifPresent(lists -> {
                for (List<String> activities : lists) {
                    for (String activity : activities) {
                        Component activityComponent = ACTIVITIES.computeIfAbsent(activity, Component::translatable);
                        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
                            player.displayClientMessage(activityComponent, false);
                        }
                    }
                }
            });
        }
    }
}