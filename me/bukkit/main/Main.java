package me.bukkit.main;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import me.bukkit.lore.Data;
import me.bukkit.lore.ItemDrop;
import me.bukkit.lore.ItemInChest;
import me.bukkit.lore.playerdeath;

public class Main extends JavaPlugin {
	private Annoucer n;
	private Map<String, Boolean> onConfirmation = new HashMap<String, Boolean>();
	public SettingsManager settings = SettingsManager.getInstance();
	public Permissions permissions = new Permissions();

	public void onEnable() {
		settings.setup(this);
		this.registerLoreCore();
		Bukkit.getServer().getLogger().info(settings.getLang().getString("Config.onEnable"));
		Bukkit.getServer().getPluginManager().registerEvents(n = new Annoucer(this), this);
	}

	public void onDisable() {
		Bukkit.getServer().getLogger().info(settings.getLang().getString("Config.onDisable"));
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + settings.getLang().getString("Config.consoleCommand"));
			return true;
		}

		Player player = (Player) sender;
		ItemStack item = player.getItemInHand();
		Enchantment enchant;

		if (cmd.getName().equalsIgnoreCase("enhance")) {
			if (args.length == 0) {
				printHelp(this, player);
				return true;
			}

			if ((item.getType() == Material.DIAMOND_SWORD || item.getType() == Material.DIAMOND_AXE)
					&& permissions.enhancingWeapon(this, player)) {
				enchant = Enchantment.DAMAGE_ALL;
			} else if ((item.getType() == Material.DIAMOND_CHESTPLATE || item.getType() == Material.DIAMOND_HELMET
					|| item.getType() == Material.DIAMOND_LEGGINGS || item.getType() == Material.DIAMOND_BOOTS)
					&& permissions.enhancingArmor(this, player)) {
				enchant = Enchantment.PROTECTION_ENVIRONMENTAL;
			} else {
				player.sendMessage(ChatColor.RED + settings.getLang().getString("Enhance.itemInvalid"));
				enchant = null;
			}

			if (args[0].equalsIgnoreCase("hand")) {
				if (!onConfirmation.containsKey(player.getName())) {
					onConfirmation.put(player.getName(), true);
				}
				player.sendMessage(ChatColor.GREEN + "请输入/enhance confirm确认本次强化");
				return true;
			}

			if (args[0].equalsIgnoreCase("confirm")) {
				if (onConfirmation.containsKey(player.getName())) {
					onConfirmation.remove(player.getName());
					double random = Math.random();

					if (random < getChance(item, enchant)) {
						item.addUnsafeEnchantment(enchant, item.getEnchantmentLevel(Enchantment.DAMAGE_ALL) + 1);
						player.sendMessage(ChatColor.GREEN + settings.getLang().getString("Enhance.enhanceSuccess"));
						Data.addLore(item, player,
								ChatColor.translateAlternateColorCodes('&',
										settings.getLang().getString("Lore.UntradeableLore")),
								settings.getLang(), true);
						return true;
					} else {
						item.removeEnchantment(enchant);
						player.sendMessage(ChatColor.RED + settings.getLang().getString("Enhance.enhanceFailed"));
						return true;
					}
				} else {
					player.sendMessage(ChatColor.RED + "你没有什么要确认的!");
					return true;
				}
			}

			if (onConfirmation.containsKey(player.getName())) {
				onConfirmation.remove(player.getName());
				player.sendMessage(ChatColor.GREEN + "您未输入确认指令，本次强化已取消!");
			}

			if ((args[0].equalsIgnoreCase("ver") || args[0].equalsIgnoreCase("version"))
					&& permissions.commandVersion(this, player)) {
				player.sendMessage(ChatColor.GREEN + settings.getLang().getString("Config.checkingVersion")
						.replaceAll("%version%", getDescription().getVersion()));
				return true;
			}

			if (args[0].equalsIgnoreCase("chance") && permissions.commandChance(this, player)) {
				if (enchant != null) {
					player.sendMessage(ChatColor.GOLD + settings.getLang().getString("Enhance.successRate")
							.replaceAll("%chance%", Double.toString(getChance(item, enchant) * 100)));
					return true;
				} else {
					player.sendMessage(ChatColor.RED + settings.getLang().getString("Enhance.itemInvalid"));
				}
			}
			if (args[0].equalsIgnoreCase("reload") && permissions.commandReload(this, player)) {
				settings.reloadConfig();
				settings.reloadData();
				settings.reloadLang();
				settings.saveConfig();
				settings.saveData();
				settings.saveLang();
				player.sendMessage(ChatColor.GREEN + settings.getLang().getString("Config.reload"));
				return true;
			}

			if (args[0].equalsIgnoreCase("help") && permissions.commandHelp(this, player)) {
				printHelp(this, player);
				return true;
			}

		}

		return true;
	}

	/**
	 * this is a getter method.
	 * 
	 * @return the notifier.
	 */
	public Annoucer getAnnoucer() {
		return n;
	}

	/**
	 * this is a helper method.
	 * 
	 * @param sender
	 *            this is a player.
	 */
	private void printHelp(Main m, Player player) {
		String help = "&b&l&m          &d EnchantmentsEnhance&b&l&m          ";
		if (permissions.commandHelp(m, player))
			help += "\n&6/enhance help &7- 查看插件命令帮助.";
		if (permissions.enhancingArmor(m, player) || permissions.enhancingWeapon(m, player))
			help += "\n&6/enhance hand &7- 突破手中物品的潜力";
		if (permissions.commandReload(m, player))
			help += "\n&6/enhance reload &7- 重新载入插件配置文件.";
		if (permissions.commandChance(m, player))
			help += "\n&6/enhance chance &7- 了解潜力突破机率.";
		if (permissions.commandVersion(m, player))
			help += "\n&6/enhance version &7- 检测当前文件版本.";

		player.sendMessage(ChatColor.GOLD + ChatColor.translateAlternateColorCodes('&', help));
	}

	/**
	 * This part includes the initialization of the lore.
	 */
	public void registerLoreCore() {
		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(new ItemDrop(this), this);
		pm.registerEvents(new playerdeath(this), this);
		pm.registerEvents(new ItemInChest(this), this);
	}

	/**
	 * This calculate the chance of the item success rate.
	 * 
	 * @param item
	 *            the item of enhancing.
	 * @param enchant
	 *            enchantment of the item.
	 * @return return the chance.
	 */
	public double getChance(ItemStack item, Enchantment enchant) {
		return (1 / (Math.pow((item.getItemMeta().getEnchantLevel(enchant)), 1.05))
				+ ((item.getItemMeta().getEnchantLevel(enchant) - 1) * settings.getLang().getDouble("baseMultiplier")));
	}

}