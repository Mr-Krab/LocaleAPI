package sawfowl.localeapi.apiclasses;

import java.lang.reflect.Type;

import org.spongepowered.configurate.CommentedConfigurationNodeIntermediary;
import org.spongepowered.configurate.objectmapping.meta.Processor;
import org.spongepowered.configurate.objectmapping.meta.Processor.Factory;

import sawfowl.localeapi.ImplementAPI;
import sawfowl.localeapi.api.LocaleService;
import sawfowl.localeapi.api.LocalisedComment;

public class LocalisedCommentFactory implements Factory<LocalisedComment, Object> {

	private static final LocaleService LOCALE_SERVICE = ImplementAPI.getLocaleService();

	@Override
	public Processor<Object> make(LocalisedComment data, Type type) {
		return (value, destination) -> {
			if (destination instanceof CommentedConfigurationNodeIntermediary<?> node) {
				if(!LOCALE_SERVICE.getOrDefaultLocale(data.plugin(), LOCALE_SERVICE.getSystemOrDefaultLocale()).getLocaleNode((Object[]) data.path()).virtual()) {
					node.comment(LOCALE_SERVICE.getOrDefaultLocale(data.plugin(), LOCALE_SERVICE.getSystemOrDefaultLocale()).getString((Object[]) data.path()));
				} else if(!data.def().equals("")) node.comment(data.def());
			}
		};
	}

}
