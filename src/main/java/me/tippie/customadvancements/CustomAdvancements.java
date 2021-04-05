package me.tippie.customadvancements;

import lombok.Getter;
import me.tippie.customadvancements.advancement.AdvancementManager;
import me.tippie.customadvancements.advancement.requirement.types.Advancement;
import me.tippie.customadvancements.advancement.requirement.types.Permission;
import me.tippie.customadvancements.advancement.reward.types.ConsoleCommand;
import me.tippie.customadvancements.advancement.reward.types.Message;
import me.tippie.customadvancements.advancement.types.BlockBreak;
import me.tippie.customadvancements.advancement.types.Chat;
import me.tippie.customadvancements.advancement.types.Join;
import me.tippie.customadvancements.advancement.types.Leave;
import me.tippie.customadvancements.commands.CommandListener;
import me.tippie.customadvancements.listeners.PlayerJoinQuitListener;
import me.tippie.customadvancements.player.CAPlayerManager;
import me.tippie.customadvancements.utils.ConfigWrapper;
import me.tippie.customadvancements.utils.Lang;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Level;

/**
 * The main plugin class
 */
public final class CustomAdvancements extends JavaPlugin {

	/**
	 * The file that has the messages (messages.yml)
	 */
	private final ConfigWrapper messagesFile = new ConfigWrapper(this, "", "messages.yml");

	/**
	 * {@link me.tippie.customadvancements.CustomAdvancements}
	 */
	@Getter private static CustomAdvancements instance;

	/**
	 * {@link me.tippie.customadvancements.commands.CommandListener}
	 */
	@Getter private static CommandListener commandListener;

	/**
	 * {@link me.tippie.customadvancements.advancement.AdvancementManager}
	 */

	@Getter private static AdvancementManager advancementManager;

	/**
	 * {@link me.tippie.customadvancements.player.CAPlayerManager}
	 */
	@Getter private static CAPlayerManager caPlayerManager;

	/**
	 * Executed on enabling the plugin:
	 * loads the messages, creates the listeners / managers, registers commands and loads data for online players
	 */
	@Override
	public void onEnable() {
		messagesFile.createNewFile("Loading CustomAdvancements messages.yml",
				"Advancements messages file");
		loadMessages();
		advancementManager = new AdvancementManager();
		commandListener = new CommandListener();
		caPlayerManager = new CAPlayerManager();
		instance = this;
		Objects.requireNonNull(this.getCommand("customadvancements")).setExecutor(commandListener);
		Objects.requireNonNull(this.getCommand("customadvancements")).setTabCompleter(commandListener);
		getServer().getPluginManager().registerEvents(new PlayerJoinQuitListener(), this);
		registerAdvancementTypes();
		advancementManager.loadAdvancements();
		for (final Player player : Bukkit.getServer().getOnlinePlayers()) {
			caPlayerManager.loadPlayer(player);
		}
		this.getLogger().log(Level.INFO, "Enabled successfully");
	}

	/**
	 * Registers built-in advancement types.
	 */
	private void registerAdvancementTypes() {
		advancementManager.registerAdvancement(new BlockBreak());
		advancementManager.registerAdvancement(new Join());
		advancementManager.registerAdvancement(new Leave());
		advancementManager.registerAdvancement(new Chat());

		advancementManager.registerAdvancementReward(new Message());
		advancementManager.registerAdvancementReward(new ConsoleCommand());

		advancementManager.registerAdvancementRequirement(new Advancement());
		advancementManager.registerAdvancementRequirement(new Permission());
	}


	/**
	 * Executed when plugin disables:
	 * saves and unloads all online players.
	 */
	@Override
	public void onDisable() {
		for (final Player player : Bukkit.getServer().getOnlinePlayers()) {
			caPlayerManager.savePlayer(player);
			caPlayerManager.unloadPlayer(player);
		}
		advancementManager.unregisterAll();
		advancementManager = null;
		commandListener = null;
		caPlayerManager = null;
		this.getLogger().log(Level.INFO, "Disabled successfully");
	}

	/**
	 * Loads and enables messages.yml
	 */
	private void loadMessages() {
		Lang.setFile(messagesFile.getConfig());
		for (final Lang value : Lang.values()) {
			messagesFile.getConfig().addDefault(value.getPath(),
					value.getDefault());
		}

		messagesFile.getConfig().options().copyDefaults(true);
		messagesFile.saveConfig();
	}
}
