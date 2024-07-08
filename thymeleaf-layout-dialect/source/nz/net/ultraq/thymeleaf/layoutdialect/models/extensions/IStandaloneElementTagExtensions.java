package nz.net.ultraq.thymeleaf.layoutdialect.models.extensions;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.thymeleaf.model.IStandaloneElementTag;

/**
 * Meta-programming extensions to the {@link IStandaloneElementTag} class.
 *
 * @author Emanuel Rabina
 */
public class IStandaloneElementTagExtensions {

	/**
	 * Compares this standalone tag with another.
	 *
	 * @param self
	 * @param other
	 * @return {@code true} if this tag has the same name and attributes as the other element.
	 */
	@SuppressWarnings("EqualsOverloaded")
	public static boolean equals(IStandaloneElementTag self, Object other) {
		return other instanceof IStandaloneElementTag && self.getElementDefinition()
			.equals(((IStandaloneElementTag) other).getElementDefinition())
			&& DefaultGroovyMethods.equals(self.getAttributeMap(),
			((IStandaloneElementTag) other).getAttributeMap());
	}

}
