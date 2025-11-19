package me.kall.reminder;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mod(Reminder.MOD_ID)
public final class Reminder {
    public static final String MOD_ID = "reminder";

    private static int lastDay = -1;
    private static int lastSecond = -1;

    public Reminder(@NotNull FMLJavaModLoadingContext context) {
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        IEventBus modBus = context.getModEventBus();

        forgeBus.addListener(this::onServerTick);
        forgeBus.addListener(this::onServerStart);

        modBus.addListener(RemindConfig::reload);

        context.registerConfig(ModConfig.Type.COMMON, RemindConfig.INSTANCE);
    }

    public void onServerStart(ServerStartedEvent event) {
        RemindConfig.validateConfig();
    }

    public void onServerTick(TickEvent.@NotNull ServerTickEvent event) {
        if (event.phase.equals(TickEvent.Phase.START)) {
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
                            for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
                                player.displayClientMessage(Component.translatable(activity), false);
                            }
                        }
                    }
                });
            }
        }
    }
}