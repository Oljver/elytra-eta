package org.kybe;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.rusherhack.client.api.feature.hud.TextHudElement;
import org.rusherhack.core.setting.NumberSetting;

public class ETAHud extends TextHudElement {

	private int glDurability = 0;
	private volatile boolean running = false;
	private Thread updateThread;

	final NumberSetting<Integer> delay = new NumberSetting<Integer>("Delay ms", "Check delay", 500, 0, 10000)
			.incremental(1)
			.onChange(setting -> {
				if (this.running) {
					stopUpdateThread();
					startUpdateThread();
				}
			});

	public ETAHud(String name) {
		super(name);
		this.registerSettings(delay);
	}

	@Override
	public String getText() {
		if (mc.level == null || !this.isToggled()) {
			return "ETA: ERROR";
		}

		if (!running) {
			startUpdateThread();
		}

		// Format the durability information as minutes and seconds
		return glDurability + "s (" + (glDurability / 60) + "m " + (glDurability % 60) + "s)";
	}

	@Override
	public void onDisable() {
		stopUpdateThread();
		super.onDisable();
	}

	private void startUpdateThread() {
		running = true;
		updateThread = new Thread(() -> {
			try {
				while (running && this.isToggled()) {
					glDurability = calculateTotalDurability();
					Thread.sleep(delay.getValue());
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (Exception e) {
				this.getLogger().error("Unexpected error in ETA calculation thread", e);
			} finally {
				running = false;
			}
		});
		updateThread.start();
	}

	private void stopUpdateThread() {
		running = false;
		if (updateThread != null && updateThread.isAlive()) {
			updateThread.interrupt();
		}
	}

	private int calculateTotalDurability() {
		int totalDurability = 0;

		// Check all inventory slots including armor slots for Elytra
		for (int i = 0; i < 36; i++) {
			ItemStack item = mc.player.getInventory().getItem(i);
			if (item.getItem() == Items.ELYTRA) {
				totalDurability += calculateElytraDurability(item);
			}
		}

		// Check the chest armor slot specifically for Elytra
		ItemStack chestArmor = mc.player.getInventory().getArmor(2);
		if (chestArmor.getItem() == Items.ELYTRA) {
			totalDurability += calculateElytraDurability(chestArmor);
		}

		ItemStack offhand = mc.player.getOffhandItem();
		if (offhand.getItem() == Items.ELYTRA) {
			totalDurability += calculateElytraDurability(offhand);
		}

		return totalDurability;
	}

	private int calculateElytraDurability(ItemStack item) {
		if (item.isEmpty()) {
			return 0;
		}

		// Fetch the Unbreaking enchantment level on the Elytra. Thanks to rocoplays for getting the code via chatgpt.
		// I swear chatgpt hates me :(.
        // Had to rewrite it thanks mojang. But thanks to chatgpt for the code.
        HolderLookup.RegistryLookup<Enchantment> enchantmentLookup = mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        Holder<Enchantment> unbreakingEnchantment = enchantmentLookup.getOrThrow(Enchantments.UNBREAKING);

        int unbreakingLevel = item.getEnchantments().getLevel(unbreakingEnchantment);

        return (item.getMaxDamage() - item.getDamageValue() - 1) * (unbreakingLevel + 1);
	}
}
