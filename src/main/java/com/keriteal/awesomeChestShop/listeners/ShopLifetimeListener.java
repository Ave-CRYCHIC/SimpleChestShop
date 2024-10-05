package com.keriteal.awesomeChestShop.listeners;

import com.keriteal.awesomeChestShop.Messages;
import com.keriteal.awesomeChestShop.shop.ShopManager;
import com.keriteal.awesomeChestShop.utils.BlockUtils;
import com.keriteal.awesomeChestShop.utils.ShopUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.slf4j.Logger;

public class ShopLifetimeListener implements Listener {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Logger logger;
    private final ShopManager shopManager;

    public ShopLifetimeListener(JavaPlugin plugin, ShopManager shopManager) {
        this.logger = plugin.getSLF4JLogger();
        this.shopManager = shopManager;
    }

    @EventHandler
    public void onShopCreationRequest(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.CHEST) return;
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        if (!player.isSneaking()) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.isEmpty()) return;
        Block clickedBlock = event.getClickedBlock();
        if (ShopUtils.isShopBlock(clickedBlock)) return;

        RayTraceResult traceResult = player.rayTraceBlocks(10.0, FluidCollisionMode.NEVER);
        if (traceResult == null) {
            player.sendMessage(Component.text("无法获取告示牌放置的位置"));
            return;
        }

        BlockFace blockFace = traceResult.getHitBlockFace();
        if (blockFace == null) {
            player.sendMessage(Component.text("无法获取告示牌放置的位置"));
            return;
        }
//        logger.info(blockFace.name());

        // 交互了上边或下边
        if (blockFace.getModY() != 0) {
            blockFace = getClosestFace(BlockUtils.getBlockCenterLocation(clickedBlock), traceResult.getHitPosition());
//            logger.info(blockFace.name());
        }

        shopManager.prepareCreate(player, clickedBlock.getLocation(), item, blockFace);
    }

    private BlockFace getClosestFace(Location centerLocation, Vector hitPosition) {
        double x = hitPosition.getX() - centerLocation.getX();
        double z = hitPosition.getZ() - centerLocation.getZ();

        if (Math.abs(x) > Math.abs(z)) {
            return x > 0 ? BlockFace.EAST : BlockFace.WEST;
        } else {
            return z > 0 ? BlockFace.SOUTH : BlockFace.NORTH;
        }
    }
}
