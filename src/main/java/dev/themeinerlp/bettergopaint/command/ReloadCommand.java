/*
 * BetterGoPaint is designed to simplify painting inside of Minecraft.
 * Copyright (C) TheMeinerLP
 * Copyright (C) OneLiteFeather
 * Copyright (C) OneLiteFeather team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package dev.themeinerlp.bettergopaint.command;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import dev.themeinerlp.bettergopaint.BetterGoPaint;
import dev.themeinerlp.bettergopaint.objects.other.Settings;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

public final class ReloadCommand {

    private final BetterGoPaint betterGoPaint;

    public ReloadCommand(final BetterGoPaint betterGoPaint) {
        this.betterGoPaint = betterGoPaint;
    }

    @CommandMethod("bgp|gp reload")
    @CommandPermission("bettergopaint.command.admin.reload")
    public void onReload(Player player) {
        betterGoPaint.reload();
        player.sendMessage(MiniMessage.miniMessage().deserialize(Settings.settings().GENERIC.PREFIX + "<red>Reloaded</red>"));
    }

}
