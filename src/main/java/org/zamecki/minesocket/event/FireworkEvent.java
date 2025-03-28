package org.zamecki.minesocket.event;

import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

import static org.zamecki.minesocket.ModData.logger;

public class FireworkEvent implements IGameEvent {
    private final MinecraftServer server;
    private final Random random = new Random();

    public FireworkEvent(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public String getName() {
        return "FireworkEvent";
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            logger.error("FireworkEvent: Nome do jogador não fornecido");
            return;
        }

        String playerName = args[0];
        int quantidade = 5; // Padrão

        if (args.length >= 3) {
            try {
                quantidade = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                logger.warn("FireworkEvent: Quantidade inválida, usando padrão");
            }
        }

        ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerName);
        if (player == null) {
            logger.error("FireworkEvent: Jogador '{}' não encontrado", playerName);
            return;
        }

        spawnFireworks(player, quantidade);
    }

    private void spawnFireworks(ServerPlayerEntity player, int count) {
        for (int i = 0; i < count; i++) {
            double offsetX = (random.nextDouble() * 10) - 5;
            double offsetZ = (random.nextDouble() * 10) - 5;
            Vec3d pos = player.getPos().add(offsetX, 1, offsetZ);

            ItemStack fireworkStack = createRandomFirework();
            FireworkRocketEntity firework = new FireworkRocketEntity(
                player.getWorld(), pos.x, pos.y, pos.z, fireworkStack);

            player.getWorld().spawnEntity(firework);
        }
    }

    private ItemStack createRandomFirework() {
        ItemStack firework = new ItemStack(Items.FIREWORK_ROCKET);
        NbtCompound fireworksNbt = new NbtCompound();

        // Tempo de voo (1-3)
        fireworksNbt.putByte("Flight", (byte)(random.nextInt(3) + 1));

        // Explosões
        NbtList explosions = new NbtList();
        NbtCompound explosion = new NbtCompound();

        // Tipo de explosão (0-4)
        explosion.putByte("Type", (byte)random.nextInt(5));

        // Cores aleatórias
        int[] colors = {0xFF0000, 0x00FF00, 0x0000FF, 0xFFFF00, 0xFF00FF};
        explosion.putIntArray("Colors", new int[]{colors[random.nextInt(colors.length)]});

        if (random.nextBoolean()) explosion.putBoolean("Trail", true);
        if (random.nextBoolean()) explosion.putBoolean("Flicker", true);

        explosions.add(explosion);
        fireworksNbt.put("Explosions", explosions);
        fireworksNbt.put("Fireworks", fireworksNbt);

        return firework;
    }
}
