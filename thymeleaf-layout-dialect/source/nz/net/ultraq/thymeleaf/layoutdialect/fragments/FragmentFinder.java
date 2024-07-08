package nz.net.ultraq.thymeleaf.layoutdialect.fragments;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import nz.net.ultraq.thymeleaf.layoutdialect.models.extensions.IModelExtensions;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IOpenElementTag;
import org.thymeleaf.model.ITemplateEvent;

/**
 * Searches for and returns layout dialect fragments within a given scope/element.
 *
 * @author Emanuel Rabina
 */
public class FragmentFinder {

	/**
	 * Find and return models for layout dialect fragments within the scope of the given model,
	 * without delving into {@code layout:insert} or {@code layout:replace} elements, mapped by the
	 * name of each fragment.
	 *
	 * @param model Model whose events are to be searched.
	 * @return Map of fragment names and their elements.
	 */
	public Map<String, List<IModel>> findFragments(IModel model) {

		LinkedHashMap<String, List<IModel>> fragmentsMap = new LinkedHashMap<>();

		int eventIndex = 0;
		while (eventIndex < model.size()) {
			ITemplateEvent event = model.get(eventIndex);
			if (event instanceof IOpenElementTag) {
				String fragmentName = ((IOpenElementTag) event).getAttributeValue(dialectPrefix,
					FragmentProcessor.getPROCESSOR_NAME());
				boolean collect = false;
				if (!StringGroovyMethods.asBoolean(fragmentName)) {
					collect = true;
					final String value = ((IOpenElementTag) event).getAttributeValue(dialectPrefix,
						CollectFragmentProcessor.getPROCESSOR_DEFINE());
					fragmentName = StringGroovyMethods.asBoolean(value) ? value
						: ((IOpenElementTag) event).getAttributeValue(dialectPrefix,
							CollectFragmentProcessor.getPROCESSOR_COLLECT());
				}

				if (StringGroovyMethods.asBoolean(fragmentName)) {
					IModel fragment = IModelExtensions.getModel(model, eventIndex);
					final List<IModel> object = fragmentsMap.get(fragmentName);
					fragmentsMap.put(fragmentName,
						DefaultGroovyMethods.asBoolean(object) ? object : new ArrayList<>());
					fragmentsMap.get(fragmentName).add(fragment);
					if (!collect) {
						eventIndex += fragment.size();
						continue;
					}

				}

			}

			eventIndex++;
		}

		return fragmentsMap;
	}

	public final String getDialectPrefix() {
		return dialectPrefix;
	}

	public FragmentFinder(String dialectPrefix) {
		this.dialectPrefix = dialectPrefix;
	}

	private final String dialectPrefix;
}
