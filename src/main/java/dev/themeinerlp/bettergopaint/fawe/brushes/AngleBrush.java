package dev.themeinerlp.bettergopaint.fawe.brushes;

import com.destroystokyo.paper.profile.ProfileProperty;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import dev.themeinerlp.bettergopaint.fawe.util.BrushSettings;
import dev.themeinerlp.bettergopaint.utils.Constants;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public final class AngleBrush implements BetterBrush {

    private final Player player;
    private final BrushSettings brushSettings;
    private final Plugin plugin;
    private final ItemStack itemStack;

    public AngleBrush(Player player, BrushSettings brushSettings, Plugin plugin) {
        this.player = player;
        this.brushSettings = brushSettings;
        this.plugin = plugin;

        itemStack = new ItemStack(Material.PLAYER_HEAD);
        if (itemStack.getItemMeta() instanceof SkullMeta skullMeta) {
            var profile = Bukkit.createProfile(UUID.randomUUID());
            profile.setProperty(new ProfileProperty("textures", Constants.HEAD_ANGLE));
            skullMeta.setPlayerProfile(profile);
            itemStack.setItemMeta(skullMeta);
        }
    }

    @Override
    public Player brushOwner() {
        return player;
    }

    @Override
    public BukkitPlayer actor() {
        return BukkitAdapter.adapt(this.player);
    }



    @Override
    public void build(EditSession editSession, BlockVector3 position, Pattern pattern, double size) throws
            MaxChangedBlocksException {
        BlockVector3 positionA = position.add((int) (-size / 2), (int) -size / 2, (int) -size / 2);
        BlockVector3 positionB = position.add((int) (size / 2), (int) size / 2, (int) size / 2);
        double distanceMath = size / 2;
        Vector3 playerPosAsVec3 = actor().getLocation().toVector();
        for (double x = positionA.getX(); x <= positionB.getX(); x++) {
            for (double z = positionA.getZ(); z <= positionB.getZ(); z++) {
                for (double y = positionA.getY(); y <= positionB.getY(); y++) {
                    BlockVector3 blockInRadius = BlockVector3.at(x, y, z);
                    BaseBlock block = editSession.getFullBlock(blockInRadius);

                    if (blockInRadius.distance(position) >= distanceMath || block.toBlockState().isAir()) {
                        continue;
                    }

                    if (settings().surfaceEnabled && !isOnSurface(editSession, blockInRadius.toVector3(), playerPosAsVec3)) {
                        continue;
                    }

                    if (settings().maskEnabled && (editSession.getMask() != null || !editSession.getMask().test(blockInRadius))) {
                        continue;
                    }

                    if (getAverageHeightDiffAngle(editSession, blockInRadius.toVector3(), 1) >= 0.1) {
                        continue;
                    }

                    if (getAverageHeightDiffAngle(editSession, blockInRadius.toVector3(), settings().angleDistance) >= Math.tan(Math.toRadians(settings().angleHeightDifference))) {
                        continue;
                    }

                    editSession.setBlock(blockInRadius, pattern);

                }
            }
        }
    }

    @Override
    public BrushSettings settings() {
        return this.brushSettings;
    }

    @Override
    public String getName() {
        return "Angle Brush";
    }

    @Override
    public ItemStack getDisplayItem() {
        return itemStack;
    }

}
