package sawfowl.localeapi.api.placeholders;

import net.kyori.adventure.text.Component;
import sawfowl.localeapi.api.Text;

@FunctionalInterface
public interface Placeholder<T> {

	public Text apply(Text original, T arg, Component def);

}
