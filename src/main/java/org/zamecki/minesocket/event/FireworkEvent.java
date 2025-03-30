package org.zamecki.minesocket.event;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.zamecki.minesocket.ModData.logger;


/// FireworkEvent: Creates fireworks around a player in a circular pattern.
///
/// Usage via WebSocket:
/// "event FireworkEvent \[playerName] \[duration] \[interval] \[radius] \[angleIncrement]"
///
///
/// Examples:
/// - "event FireworkEvent Player1"
///   Creates 5 fireworks instantly around the player
///
///
/// - "event FireworkEvent Player1 60 5"
///   Creates fireworks every 5 ticks for 60 ticks
///
///
/// - "event FireworkEvent Player1 40 2 10.0 0.3927"
///   Creates fireworks every 2 ticks for 40 ticks in a circle with radius 10.0
///   with angular increment of π/8 (0.3927)
///
///
/// Security limitations:
/// - Maximum duration: 120 ticks
/// - It's recommended to limit usage to avoid server overload
///
public class FireworkEvent implements IGameEvent {
    private final MinecraftServer server;
    private final Random random = new Random();

    private int ticksRemaining;
    private int spawnInterval;
    private int ticksSinceLastSpawn;
    private String playerName;

    private double currentAngle = 0;
    private double radius = 5.0;
    private double angleIncrement = Math.PI / 8;

    public FireworkEvent(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public String getName() {
        return "FireworkEvent";
    }

    @Override
    public void start(String[] args) {
        if (validateArgs(args)) return;

        this.playerName = args[0];

        int rawDuration = getIntArg(args, 1, 1, "duration");
        this.ticksRemaining = Math.min(120, Math.max(1, rawDuration));
        this.spawnInterval = getIntArg(args, 2, 2, "interval");
        this.ticksSinceLastSpawn = 0;

        this.radius = getDoubleArg(args, 3, 5.0, "radius");
        this.angleIncrement = getDoubleArg(args, 4, Math.PI / 8, "angleIncrement");

        if (this.ticksRemaining == 1) {
            ServerPlayerEntity player = findPlayer(this.playerName);
            if (player != null) {
                int amount = getIntArg(args, 2, 5, "amount");
                spawnFireworks(player, amount);
            }
        }

        logger.info("FireworkEvent started for '{}' with duration of {} ticks and interval of {} ticks, radius {} and angleIncrement {}",
            this.playerName, this.ticksRemaining, this.spawnInterval, this.radius, this.angleIncrement);
    }

    @Override
    public boolean tick() {
        this.ticksRemaining--;

        if (this.ticksRemaining <= 0) {
            return true;
        }

        this.ticksSinceLastSpawn++;

        if (this.ticksSinceLastSpawn >= this.spawnInterval) {
            this.ticksSinceLastSpawn = 0;

            ServerPlayerEntity player = findPlayer(this.playerName);
            if (player != null) {
                spawnFireworks(player, 1);
            }
        }

        return this.ticksRemaining <= 0;
    }

    private boolean validateArgs(String[] args) {
        if (args.length < 1) {
            logger.error("FireworkEvent: Player name not provided");
            return true;
        }
        return false;
    }

    private ServerPlayerEntity findPlayer(String playerName) {
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerName);
        if (player == null) {
            logger.error("FireworkEvent: Player '{}' not found", playerName);
        }
        return player;
    }

    private <T> T getArg(String[] args, int index, T defaultValue, String paramName, 
                         java.util.function.Function<String, T> converter) {
        if (args.length <= index) return defaultValue;
    
        try {
            return converter.apply(args[index]);
        } catch (NumberFormatException e) {
            logger.warn("FireworkEvent: Invalid {}, using default", paramName);
            return defaultValue;
        }
    }
    
    private int getIntArg(String[] args, int index, int defaultValue, String paramName) {
        return getArg(args, index, defaultValue, paramName, Integer::parseInt);
    }
    
    private double getDoubleArg(String[] args, int index, double defaultValue, String paramName) {
        return getArg(args, index, defaultValue, paramName, Double::parseDouble);
    }

    private void spawnFireworks(ServerPlayerEntity player, int count) {
        for (int i = 0; i < count; i++) {
            Vec3d pos = getCircularPosition(player.getPos());
            ItemStack fireworkStack = createRandomFirework();
            FireworkRocketEntity firework = new FireworkRocketEntity(
                player.getWorld(), pos.x, pos.y, pos.z, fireworkStack);

            player.getWorld().spawnEntity(firework);
        }
    }

    private Vec3d getCircularPosition(Vec3d basePos) {
        double offsetX = radius * Math.cos(currentAngle);
        double offsetZ = radius * Math.sin(currentAngle);

        currentAngle += angleIncrement;
        if (currentAngle >= 2 * Math.PI) {
            currentAngle -= 2 * Math.PI;
        }

        return basePos.add(offsetX, 1, offsetZ);
    }

    private ItemStack createRandomFirework() {
        ItemStack firework = new ItemStack(Items.FIREWORK_ROCKET);
        List<FireworkExplosionComponent> explosions = new ArrayList<>();

        for (int i = 0; i < random.nextInt(3) + 1; i++) {
            FireworkExplosionComponent.Type type = FireworkExplosionComponent.Type.byId(random.nextInt(5));
            IntList colors = generateRandomColors(random.nextInt(5) + 1);
            IntList fadeColors = generateRandomColors(random.nextInt(5) + 1);
            boolean hasTrail = random.nextBoolean();
            boolean hasTwinkle = random.nextBoolean();

            FireworkExplosionComponent explosion = new FireworkExplosionComponent(
                type, colors, fadeColors, hasTrail, hasTwinkle);

            explosions.add(explosion);
        }

        FireworksComponent fireworks = new FireworksComponent(10, explosions);
        firework.set(DataComponentTypes.FIREWORKS, fireworks);
        return firework;
    }

    private IntList generateRandomColors(int count) {
        int[] colorsArray = new int[count];
        for (int j = 0; j < count; j++) {
            colorsArray[j] = random.nextInt(0xFFFFFF);
        }
        return IntList.of(colorsArray);
    }
}
