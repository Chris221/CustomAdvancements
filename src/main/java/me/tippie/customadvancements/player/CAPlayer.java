package me.tippie.customadvancements.player;

import lombok.Getter;
import lombok.val;
import me.tippie.customadvancements.CustomAdvancements;
import me.tippie.customadvancements.player.datafile.AdvancementProgress;
import me.tippie.customadvancements.player.datafile.AdvancementProgressFile;

import java.util.Map;
import java.util.UUID;

/**
 * Represents a player
 */
public class CAPlayer {
	/**
	 * The UUID of this player.
	 */
	@Getter private final UUID uuid;

	/**
	 * Map with the path of an advancement with their {@link AdvancementProgress}.
	 */
	@Getter private final Map<String, AdvancementProgress> advancementProgress;

	/**
	 * The {@link AdvancementProgressFile} of this player.
	 */
	@Getter private final AdvancementProgressFile advancementProgressFile;

	/**
	 * Creates a new {@link CAPlayer} and loads their progress.
	 *
	 * @param playeruuid the UUID of the player
	 */
	CAPlayer(final UUID playeruuid) {
		advancementProgressFile = new AdvancementProgressFile(playeruuid);
		advancementProgress = advancementProgressFile.loadFile();
		uuid = playeruuid;
	}

	/**
	 * Updates progress for any {@link me.tippie.customadvancements.advancement.CAdvancement}
	 * for this player with a certain amount and optionally checks if it is completed right away.
	 *
	 * @param path             the path of the completed advancement formatted as 'treeLabel.advancementLabel'
	 * @param amount           amount of increasement or decreasement
	 * @param checkIfCompleted boolean if this advancement should be check completed after progress is set
	 */
	public void updateProgress(final String path, final int amount, final boolean checkIfCompleted) {
		val progress = advancementProgress.get(path);
		progress.setProgress(progress.getProgress() + amount);
		if (checkIfCompleted) checkCompleted(path);
	}

	/**
	 * Checks if quest is active for this player
	 *
	 * @param path the path of an advancement formatted as 'treeLabel.advancementLabel'
	 * @return boolean if the quest is active
	 */
	public boolean checkIfQuestActive(final String path) {
		return advancementProgress.get(path).isActive();
	}

	/**
	 * Checks if a quest is completed and executes completion actions if completed. Does not return a boolean! Use {@link AdvancementProgress#isCompleted()} to get boolean if it's completed or not.
	 *
	 * @param path the path of an advancement formatted as 'treeLabel.advancementLabel'
	 */
	public void checkCompleted(final String path) {
		val caProgress = advancementProgress.get(path);
		val progress = caProgress.getProgress();
		val maxProgress = CustomAdvancements.getAdvancementManager().getAdvancement(path).getMaxProgress();
		if (maxProgress <= progress) {
			caProgress.setCompleted(true);
			caProgress.setActive(false);
			CustomAdvancements.getAdvancementManager().complete(path, uuid);
		}
	}


	public int amountCompleted() {
		return amountCompleted(null);
	}

	public int amountCompleted(final String tree) {
		int result = 0;
		for (final Map.Entry<String, AdvancementProgress> entry : advancementProgress.entrySet()) {
			if (tree == null && entry.getValue().isCompleted()) {
				result++;
			} else if (entry.getKey().startsWith(tree + ".") && entry.getValue().isCompleted()) {
				result++;
			}
		}
		return result;
	}
}
