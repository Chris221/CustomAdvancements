package me.tippie.customadvancements.advancement.types;

import lombok.Getter;
import lombok.ToString;
import lombok.val;
import me.tippie.customadvancements.CustomAdvancements;
import me.tippie.customadvancements.advancement.AdvancementTree;
import me.tippie.customadvancements.advancement.CAdvancement;
import me.tippie.customadvancements.advancement.InvalidAdvancementException;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Represents a type of an {@link CAdvancement}
 */
@ToString
public abstract class AdvancementType<T> implements Listener {

	/**
	 * The label of this {@link AdvancementType}
	 */
	@Getter private final String label;

	/**
	 * The default unit of this advancement type if no other is set for a specific advancement
	 */
	@Getter private final String defaultUnit;

	/**
	 * Creates a new {@link AdvancementType}
	 *
	 * @param label label of this type
	 * @see me.tippie.customadvancements.advancement.AdvancementManager#registerAdvancement(AdvancementType)
	 */
	AdvancementType(final String label, final String defaultUnit) {
		this.label = label;
		this.defaultUnit = defaultUnit;
	}

	/**
	 * Registers progress of an {@link AdvancementType}, MAKE SURE THIS IS CALLED SYNC!!
	 *
	 * @param event      the object of the event this advancement type belongs to
	 * @param playeruuid the uuid of the player who may make progress
	 */
	public void progress(final T event, final UUID playeruuid) {
		val caPlayer = CustomAdvancements.getCaPlayerManager().getPlayer(playeruuid);
		for (final AdvancementTree tree : CustomAdvancements.getAdvancementManager().getAdvancementTrees()) {
			final List<CAdvancement> advancements = tree.getAdvancements().stream().filter(advancement -> advancement.getType().equals(this.label)).collect(Collectors.toList());
			for (final CAdvancement advancement : advancements) {
				try {
					if (caPlayer.checkIfAdvancementActive(tree.getLabel() + "." + advancement.getLabel())) {
						onProgress(event, advancement.getValue(), tree.getLabel() + "." + advancement.getLabel());
					}
				} catch (final InvalidAdvancementException ex) {
					CustomAdvancements.getInstance().getLogger().log(Level.WARNING, "An advancement type tried to check an invalid advancement: " + tree.getLabel() + "." + advancement.getLabel());
				}
			}
		}
	}

	/**
	 * Called when progress is made
	 *
	 * @param e The event what progress is made on
	 * @param value The value of the {@link CAdvancement}
	 * @param path  The path of the {@link CAdvancement}
	 * @see AdvancementType#progress(Object, UUID),CAdvancement
	 */
	protected abstract void onProgress(T e, String value, String path);

	/**
	 * Called when progression is made on an advancement
	 *
	 * @param amount     amount of progress that should be made, can be negative
	 * @param path       path of the advancement progression should be made for
	 * @param playeruuid the UUID of the player progression should be made for
	 */
	public void progression(final int amount, final String path, final UUID playeruuid) {
		progression(amount,path,playeruuid,false);
	}

	/**
	 * Called when progression is made on an advancement
	 *
	 * @param amount     amount of progress that should be made, can be negative
	 * @param path       path of the advancement progression should be made for
	 * @param playeruuid the UUID of the player progression should be made for
	 * @param set boolean if the amount value should be added to the progress or the progress should be set to the amount
	 */
	public void progression(final int amount, final String path, final UUID playeruuid, boolean set) {
		val caPlayer = CustomAdvancements.getCaPlayerManager().getPlayer(playeruuid);
		try {
			caPlayer.updateProgress(path, amount, true, set);
		} catch (final InvalidAdvancementException ex) {
			CustomAdvancements.getInstance().getLogger().log(Level.SEVERE, "AdvancementType " + this.label + " attempting to add progression to an invalid advancement!");
		}
	}

	/**
	 * Compares string with {@link AdvancementType}
	 *
	 * @param in string of type (label)
	 * @return if type label equals to label of this {@link AdvancementType}
	 */
	public boolean equals(final String in) {
		return this.label.equalsIgnoreCase(in);
	}
}
