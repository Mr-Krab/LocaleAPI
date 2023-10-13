package sawfowl.localeapi.apiclasses;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.adventure.SpongeComponents;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.Queries;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;

import sawfowl.localeapi.api.Text;
import sawfowl.localeapi.api.TextUtils;

public class TextImpl implements Text {

	private String plainString = "";
	private Component component = Component.empty();

	public Builder builder() {
		return new Builder() {
			@Override
			public @NotNull Text build() {
				return TextImpl.this;
			}
			@Override
			public Text fromString(String string) {
				return fromComponent(TextUtils.deserialize(string));
			}
			@Override
			public Text fromComponent(Component component) {
				TextImpl.this.component = component;
				updatePlainString();
				return build();
			}
		};
	}

	@Override
	public int contentVersion() {
		return 0;
	}

	@Override
	public DataContainer toContainer() {
		return DataContainer.createNew()
				.set(Queries.CONTENT_VERSION, contentVersion())
				.set(DataQuery.of("Component"), component);
	}

	@Override
	public Component get() {
		return component;
	}

	@Override
	public String toPlain() {
		return plainString;
	}

	public Text replace(String key, Component value) {
		component = component.replaceText(TextReplacementConfig.builder().match(key).replacement(value).build());
		return this;
	}

	public Text replace(String key, String value) {
		return replace(key, TextUtils.deserialize(value));
	}

	@Override
	public Text replace(String[] keys, String[] values) {
		replace(replaceMap(keys, values));
		updatePlainString();
		return this;
	}

	@Override
	public Text replace(String[] keys, Object[] values) {
		replace(replaceMap(keys, values));
		updatePlainString();
		return this;
	}

	@Override
	public Text replaceComponents(String[] keys, Component[] values) {
		replaceComponents(replaceMapComponents(keys, values));
		updatePlainString();
		return this;
	}

	@Override
	public Text replaceTexts(String[] keys, Text[] values) {
		replaceComponents(replaceMapTexts(keys, values));
		updatePlainString();
		return this;
	}

	@Override
	public Text createCallBack(Runnable runnable) {
		return createCallBack(cause -> {
			runnable.run();
		});
	}

	@Override
	public Text createCallBack(Consumer<CommandCause> callback) {
		component = component.clickEvent(SpongeComponents.executeCallback(callback));
		return this;
	}

	private void replace(Map<String, String> map) {
		map.forEach((k, v) -> replace(k, v));
	}

	private void replaceComponents(Map<String, Component> map) {
		map.forEach((k, v) -> replace(k, v));
	}

	private Map<String, String> replaceMap(String[] keys, Object[] values) {
		Map<String, String> map = new HashMap<String, String>();
		int i = 0;
		for(String key : keys) {
			if(i >= keys.length || i >= values.length) break;
			map.put(key, values[i].toString());
			i++;
		}
		return map;
	}

	private Map<String, Component> replaceMapComponents(String[] keys, Component[] values) {
		Map<String, Component> map = new HashMap<String, Component>();
		int i = 0;
		for(String key : keys) {
			if(i >= keys.length || i >= values.length) break;
			map.put(key, values[i]);
			i++;
		}
		return map;
	}

	private Map<String, Component> replaceMapTexts(String[] keys, Text[] values) {
		Map<String, Component> map = new HashMap<String, Component>();
		int i = 0;
		for(String key : keys) {
			if(i >= keys.length || i >= values.length) break;
			map.put(key, values[i].get());
			i++;
		}
		return map;
	}

	private void updatePlainString() {
		plainString = TextUtils.clearDecorations(component);
	}

}
