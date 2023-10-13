package sawfowl.localeapi.api;

import java.util.function.Consumer;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.adventure.SpongeComponents;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.data.persistence.DataSerializable;

import net.kyori.adventure.builder.AbstractBuilder;
import net.kyori.adventure.text.Component;

public interface Text extends DataSerializable {

	private static Builder builder() {
		return Sponge.game().builderProvider().provide(Builder.class);
	}

	static Text of(Component component) {
		return builder().fromComponent(component);
	}

	static Text of(String string) {
		return builder().fromString(string);
	}

	/**
	 * Exiting from the editor and retrieve the {@link Component} object.
	 */
	Component get();

	/**
	 * Exiting from the editor and getting a string without any decoration or additional functionality.<br>
	 * Appropriate for sending a message to the console.
	 */
	String toPlain();

	/**
	 * This method works similarly to {@link String#replace(String, String)}
	 */
	Text replace(String[] keys, String[] values);

	/**
	 * This method works similarly to {@link String#replace(String, String)}
	 */
	Text replace(String[] keys, Object[] values);

	/**
	 * This method works similarly to {@link String#replace(String, String)}
	 */
	Text replaceComponents(String[] keys, Component[] values);

	/**
	 * This method works similarly to {@link String#replace(String, String)}
	 */
	Text replaceTexts(String[] keys, Text[] values);

	/**
	 * Adding the execution of arbitrary code when you click on text.<br>
	 * It is used {@link #createCallBack(Component, Consumer)}
	 */
	Text createCallBack(Runnable runnable);

	/**
	 * Adding the execution of arbitrary code when you click on text.<br>
	 * It is used {@link SpongeComponents#executeCallback(callback)}
	 */
	Text createCallBack(Consumer<CommandCause> callback);

	public interface Builder extends AbstractBuilder<Text>, org.spongepowered.api.util.Builder<Text, Builder> {

		Text fromComponent(Component component);

		Text fromString(String string);

	}

}
