package com.keriteal.awesomeChestShop.commands;

import com.keriteal.awesomeChestShop.*;
import com.keriteal.awesomeChestShop.shop.ChestShop;
import com.keriteal.awesomeChestShop.shop.ShopManager;
import com.keriteal.awesomeChestShop.utils.ItemUtils;
import com.keriteal.awesomeChestShop.utils.ShopUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

public class ProtectionCommand implements CommandExecutor, TabCompleter {
    private final ShopManager shopManager;
    private final Logger logger;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ProtectionCommand(JavaPlugin plugin, ShopManager shopManager) {
        this.logger = plugin.getSLF4JLogger();
        this.shopManager = shopManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!command.getName().equalsIgnoreCase("shop")) {
            return true;
        }

        System.out.println(args.length);
        Location entityLocation;
        if (sender instanceof LivingEntity entity) {
            entityLocation = entity.getLocation();
        } else if (sender instanceof BlockCommandSender blockCommandSender) {
            entityLocation = blockCommandSender.getBlock().getLocation();
        } else {
            logger.warn("Unknown command sender: {}", sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "protection": {
                if (args.length == 1) return false;
                // shop protection query <x> <y> <z>
                if (args[1].equals("query")) {
                    if (!sender.hasPermission("chestshop.protection.query")) {
                        sender.sendMessage(Messages.MESSAGE_NO_PERMISSION);
                        return true;
                    }
                    if (args.length == 2) {
                        if (!(sender instanceof LivingEntity entity)) return false;

                        RayTraceResult rayTraceResult = entity.rayTraceBlocks(5, FluidCollisionMode.NEVER);
                        if (rayTraceResult == null) {
                            sender.sendMessage(Messages.MESSAGE_NO_TARGETING_BLOCK);
                            return true;
                        }
                        Block block = rayTraceResult.getHitBlock();
                        if (block == null) {
                            sender.sendMessage(Messages.MESSAGE_NO_TARGETING_BLOCK);
                            return true;
                        }
                        handleProtectionQuery(sender, block.getX(), block.getY(), block.getZ());
                        return true;
                    }
                    if (args.length == 5) {
                        handleProtectionQuery(sender, Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]));
                    }
                }
            }
            case "create": {

            }
            case "delete": {

            }
            case "list": {

            }
            case "view": {
                if (args.length == 1) {
                    handleView(sender, null);
                    return true;
                }
                if (args.length == 4) {
                    handleView(sender, new Location(entityLocation.getWorld(), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3])));
                    return true;
                }
            }
            case "info": {
                if (args.length == 1) {
                    handleInfo(sender);
                    return true;
                }
            }
            // /shop modify <type|price> <value> [<x> <y> <z>]
            case "modify": {
                if (args.length == 1) {
                    return false;
                }
                switch (args[1].toLowerCase()) {
                    case "type": {
                        logger.info("Modifying shop type");
                        if (args.length < 3) return false;

                        Block block = null;
                        if (args.length == 6) {
                            World world;
                            if (sender instanceof LivingEntity livingEntity) {
                                world = livingEntity.getWorld();
                            } else if (sender instanceof BlockCommandSender commandBlock) {
                                world = commandBlock.getBlock().getWorld();
                            } else {
                                return true;
                            }
                            block = world.getBlockAt(Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]));
                        } else if (args.length == 3 && sender instanceof Player player) {
                            block = player.getTargetBlockExact(5, FluidCollisionMode.NEVER);
                        }
                        if (block == null) {
                            sender.sendMessage(Messages.MESSAGE_NO_TARGETING_BLOCK);
                            return true;
                        }

                        ShopType shopType;
                        switch (args[2].toLowerCase()) {
                            case "sale": {
                                shopType = ShopType.SALE_MODE;
                                break;
                            }
                            case "buy": {
                                shopType = ShopType.BUY_MODE;
                                break;
                            }
                            case "gotcha": {
                                shopType = ShopType.GOTCHA_MODE;
                                break;
                            }
                            default: {
                                sender.sendMessage(Component.text("未知的商店类型：")
                                        .append(Component.text(args[2]))
                                        .append(Component.text("，请使用 sale, buy, gotcha 之一")));
                                return true;
                            }
                        }

                        handleModifyType(sender, shopType, block);
                        return true;
                    }
                    case "price": {

                    }
                }
            }
        }

        return false;
    }

    public void handleModifyType(CommandSender commandSender, ShopType shopType, Block block) {
        ChestShop shop = shopManager.loadShopAt(block.getLocation());
        if (shop == null) {
            commandSender.sendMessage(Messages.MISSING_SHOP);
            return;
        }

        if (commandSender instanceof Player player
                && !shop.getOwnerId().equals(player.getUniqueId())
                && !player.hasPermission("chestshop.admin")
                && !player.isOp()) {
            commandSender.sendMessage(Messages.MESSAGE_NO_PERMISSION);
            return;
        }

        shopManager.changeShopType(shop.getShopUuid(), shopType);
        commandSender.sendMessage(Messages.SHOP_TYPE_CHANGED_LEFT.append(shopType.getColoredName()));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!command.getName().equalsIgnoreCase("protection")) return List.of();
        List<String> resultList = new LinkedList<>();
        if (args.length == 1) {
            if (sender.isOp() || sender.hasPermission("chestshop.protection.query")) {
                resultList.add("query");
            }
            if (sender.isOp() || sender.hasPermission("chestshop.protection.set")) {
                resultList.add("set");
            }
            resultList.add("info");
        }
        if (args.length == 2) {
            return List.of("~", "~ ~", "~ ~ ~");
        }
        if (args.length == 3) {
            return List.of("~", "~ ~");
        }
        if (args.length == 4) {
            return List.of("~");
        }
        return resultList;
    }

    private void handleView(CommandSender sender, Location shopLocation) {
        final ChestShop shop;
        if (!(sender instanceof Player player)) return;
        if (shopLocation == null) {
            Block block = player.getTargetBlockExact(10);
            if (block == null) {
                sender.sendMessage(Messages.MESSAGE_NO_TARGETING_BLOCK);
                return;
            }
            shop = shopManager.loadShopAt(block);
        } else {
            shop = shopManager.loadShopAt(shopLocation);
        }

        if (shop == null) {
            sender.sendMessage(Messages.MISSING_SHOP);
            return;
        }

        if (shop.getChestBlockLocation().getBlock().getState() instanceof InventoryHolder inventoryHolder) {
            logger.info("Opening inventory");
            player.openInventory(shop.getInventory());
        }
    }

    private void handleInfo(CommandSender sender) {
        if (!(sender instanceof LivingEntity entity)) return;

        Block block = entity.getTargetBlockExact(10);
        if (block == null) {
            sender.sendMessage(Messages.MESSAGE_NO_TARGETING_BLOCK);
            return;
        }

        ChestShop shop = shopManager.loadShopAt(block);
        if (shop == null) {
            sender.sendMessage(Messages.MISSING_SHOP);
            return;
        }

        Optional<String> shopOwner = Optional.ofNullable(AwesomeChestShop.getPlugin().getServer().getOfflinePlayer(shop.getOwnerId()).getName());
        Component message = Component.newline()
                .append(Component.text("商店信息", NamedTextColor.GREEN))
                .appendNewline()
                .append(Component.text("商店ID: ", NamedTextColor.WHITE))
                .append(Component.text("[" + shop.getShopUuid() + "]").color(NamedTextColor.AQUA))
                .appendNewline()
                .append(Component.text("坐标: ", NamedTextColor.WHITE))
                .append(Component.text(shop.getChestBlockLocation().getBlockX() + ", " + shop.getChestBlockLocation().getBlockY() + ", " + shop.getChestBlockLocation().getBlockZ()).color(NamedTextColor.GOLD))
                .appendNewline()
                .append(Component.text("店主: ", NamedTextColor.WHITE))
                .append(Component.text(shopOwner.orElse("不存在的玩家"), shopOwner.isPresent() ? NamedTextColor.GREEN : NamedTextColor.RED))
                .appendNewline()
                .append(Component.text("物品: [", NamedTextColor.WHITE))
                .append(ItemUtils.getItemName(shop.getItemStack()).hoverEvent(shop.getItemStack()))
                .append(Component.text("]", NamedTextColor.WHITE))
                .appendNewline()
                .append(Component.text("价格: ", NamedTextColor.WHITE))
                .append(Component.text(shop.getPrice()).color(NamedTextColor.GOLD))
                .appendNewline()
                .append(Component.text("类型: ", NamedTextColor.WHITE))
                .append(shop.getShopType().getColoredName())
                .appendNewline()
                .append(Component.text("库存: ", NamedTextColor.WHITE))
                .append(Component.text(shop.getStock()).color(NamedTextColor.GOLD));
        if (entity instanceof Player player && player.getUniqueId().equals(shop.getOwnerId())) {
            message = message.appendNewline()
                    .append(Component.text("更改商店类型: ", NamedTextColor.WHITE))
                    .append(this.buildChangeModeText(player, shop, ShopType.SALE_MODE))
                    .appendSpace()
                    .append(this.buildChangeModeText(player, shop, ShopType.BUY_MODE))
                    .appendSpace()
                    .append(this.buildChangeModeText(player, shop, ShopType.GOTCHA_MODE));
        }
        sender.sendMessage(Messages.MESSAGE_SEPARATOR.color(NamedTextColor.BLUE)
                .append(message)
                .appendNewline()
                .append(Messages.MESSAGE_SEPARATOR.color(NamedTextColor.BLUE)));
        shop.updateWorld();
    }

    private void handleProtectionQuery(CommandSender sender, int x, int y, int z) {
        Block queryingBlock;
        if (sender instanceof BlockCommandSender commandBlockSender) {
            queryingBlock = commandBlockSender.getBlock().getWorld().getBlockAt(x, y, z);
        } else if (sender instanceof LivingEntity entity) {
            queryingBlock = entity.getWorld().getBlockAt(x, y, z);
        } else {
            return;
        }

        Component locationMessage = miniMessage.deserialize("<x>, <y>, <z>",
                        Formatter.number("x", x),
                        Formatter.number("y", y),
                        Formatter.number("z", z))
                .color(TextColor.fromHexString("#39C5BB"));

        if (shopManager.isShopProtected(ShopUtils.getShopId(queryingBlock))) {
            sender.sendMessage(Component.text("位于")
                    .append(locationMessage)
                    .append(Component.text("的"))
                    .append(Messages.MESSAGE_SHOP_PROTECTED)
                    .color(NamedTextColor.RED));
        } else {
            sender.sendMessage(Component.text("位于")
                    .append(locationMessage)
                    .append(Component.text("的"))
                    .append(Messages.MESSAGE_SHOP_NOT_PROTECTED)
                    .color(NamedTextColor.RED));
        }
    }

    private Component buildChangeModeText(@NotNull Player player, @NotNull ChestShop shop, @NotNull ShopType shopType) {
        return shopType.getColoredName()
                .hoverEvent(Component.text("点击切换商店类型为", NamedTextColor.WHITE).append(shopType.getColoredName()))
                .clickEvent(ClickEvent.runCommand(String.format("/shop modify type %s %d %d %d", shopType.getCommandName(), shop.getChestBlockLocation().getBlockX(), shop.getChestBlockLocation().getBlockY(), shop.getChestBlockLocation().getBlockZ())));
    }
}
