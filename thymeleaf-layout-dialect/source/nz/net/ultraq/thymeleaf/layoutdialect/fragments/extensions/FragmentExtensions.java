package nz.net.ultraq.thymeleaf.layoutdialect.fragments.extensions;

import groovy.lang.Closure;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import nz.net.ultraq.thymeleaf.layoutdialect.context.extensions.IContextExtensions;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.processor.element.IElementModelStructureHandler;

/**
 * Extensions to Thymeleaf for working with fragments.
 *
 * @author Emanuel Rabina
 */
public class FragmentExtensions {

	/**
	 * Retrieves the fragment collection for the current context.
	 *
	 * @param self
	 * @param fromDecorator
	 * @return A new or existing fragment collection.
	 */
	public static Map<String, List<IModel>> getFragmentCollection(ITemplateContext self,
		boolean fromDecorator) {

		// If the template stack contains only 1 template and we've been called from
		// the decorator, then always return a new fragment collection.  This seems
		// to be one way to know if Thymeleaf is processing a new file and as such
		// should have a fresh collection to work with, otherwise we may be using an
		// older collection from an already-used context.
		// See: https://github.com/ultraq/thymeleaf-layout-dialect/issues/189
		if (self.getTemplateStack().size() == 1 && fromDecorator) {
			return new LinkedHashMap<String, List<IModel>>();
		}

		Object fragmentCollection = IContextExtensions.getAt(self, FRAGMENT_COLLECTION_KEY);
		return ((Map<String, List<IModel>>) (fragmentCollection != null ? fragmentCollection
			: new LinkedHashMap<>()));
	}

	/**
	 * Retrieves the fragment collection for the current context.
	 *
	 * @param self
	 * @return A new or existing fragment collection.
	 */
	public static Map<String, List<IModel>> getFragmentCollection(ITemplateContext self) {
		return FragmentExtensions.getFragmentCollection(self, false);
	}

	/**
	 * Set a fragment cache to contain any existing fragments, plus the given new fragments, with the
	 * same scope as setting a local variable.
	 *
	 * @param self
	 * @param context
	 * @param fragments     The new fragments to add to the cache.
	 * @param fromDecorator Whether the call was from {@code DecorateProcessor}, used for determining
	 *                      if a new fragment collection should be used and the order of collected
	 *                      fragments.
	 */
	public static void setLocalFragmentCollection(IElementModelStructureHandler self,
		ITemplateContext context, Map<String, List<IModel>> fragments, boolean fromDecorator) {

		self.setLocalVariable(FRAGMENT_COLLECTION_KEY,
			DefaultGroovyMethods.inject(getFragmentCollection(context, fromDecorator), fragments,
				new Closure<Map<String, List<IModel>>>(null, null) {
					public Map<String, List<IModel>> doCall(Map<String, List<IModel>> accumulator, Object fragmentName,
						Object fragmentList) {
						if (DefaultGroovyMethods.asBoolean(
							accumulator.get(fragmentName))) {
							accumulator.put((String) fragmentName,
								DefaultGroovyMethods.plus(
									accumulator.get(fragmentName),
									(Collection<IModel>) fragmentList));
							if (!fromDecorator) {
								DefaultGroovyMethods.reverse(
									accumulator.get(fragmentName), true);
							}

						} else {
							accumulator.put((String) fragmentName,
								(List<IModel>) fragmentList);
						}

						return accumulator;
					}

				}));
	}

	/**
	 * Set a fragment cache to contain any existing fragments, plus the given new fragments, with the
	 * same scope as setting a local variable.
	 *
	 * @param self
	 * @param context
	 * @param fragments     The new fragments to add to the cache.
	 */
	public static void setLocalFragmentCollection(IElementModelStructureHandler self,
		ITemplateContext context, Map<String, List<IModel>> fragments) {
		FragmentExtensions.setLocalFragmentCollection(self, context, fragments, false);
	}

	public static String getFRAGMENT_COLLECTION_KEY() {
		return FRAGMENT_COLLECTION_KEY;
	}

	private static final String FRAGMENT_COLLECTION_KEY = "LayoutDialect::FragmentCollection";
}
