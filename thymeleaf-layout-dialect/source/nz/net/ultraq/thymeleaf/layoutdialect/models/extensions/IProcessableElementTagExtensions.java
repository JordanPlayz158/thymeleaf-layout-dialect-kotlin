package nz.net.ultraq.thymeleaf.layoutdialect.models.extensions;

import groovy.lang.Closure;
import java.util.Map;
import nz.net.ultraq.thymeleaf.layoutdialect.context.extensions.IContextExtensions;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.thymeleaf.context.AbstractEngineContext;
import org.thymeleaf.context.IContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.standard.StandardDialect;

/**
 * Meta-programming extensions to the {@link IProcessableElementTag} class.
 *
 * @author Emanuel Rabina
 */
public class IProcessableElementTagExtensions {

	/**
	 * Compare processable elements for equality.
	 *
	 * @param self
	 * @param other
	 * @return {@code true} if this tag has the same name and attributes as the other element.
	 */
	@SuppressWarnings("EqualsOverloaded")
	public static boolean equals(IProcessableElementTag self, Object other) {
		return other instanceof IProcessableElementTag && self.getElementDefinition()
			.equals(((IProcessableElementTag) other).getElementDefinition())
			&& DefaultGroovyMethods.equals(self.getAttributeMap(),
			((IProcessableElementTag) other).getAttributeMap());
	}

	/**
	 * Compare elements, ignoring XML namespace declarations and Thymeleaf's {@code th:with}
	 * processor.
	 *
	 * @param self
	 * @param other
	 * @param context
	 * @return {@code true} if the elements share the same name and all attributes, with exceptions
	 * for of XML namespace declarations and Thymeleaf's {@code th:with} attribute processor.
	 */
	public static boolean equalsIgnoreXmlnsAndWith(IProcessableElementTag self,
		IProcessableElementTag other, AbstractEngineContext context) {

		if (self.getElementDefinition().equals(other.getElementDefinition())) {
			Map<String, String> difference = DefaultGroovyMethods.minus(self.getAttributeMap(),
				other.getAttributeMap());
			final Object standardDialectPrefix = IContextExtensions.getPrefixForDialect(context, StandardDialect.class);
			return difference.isEmpty() || DefaultGroovyMethods.inject(
				DefaultGroovyMethods.collect(difference, new Closure<Boolean>(null, null) {
					public Boolean doCall(Object key, Object value) {
						return ((String) key).startsWith("xmlns:") || (key.equals(
							standardDialectPrefix + ":with") || key.equals(
							"data-" + standardDialectPrefix + "-with"));
					}

				}), true, new Closure<Boolean>(null, null) {
					public Boolean doCall(Object result, Object item) {
						// This was converted to `result && item` only so this could be wrong to cast to boolean
						//  && calls asBoolean under the hood afaik
						return (Boolean) result && (Boolean) item;
					}

				});
		}

		return false;
	}

}
