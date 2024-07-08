package nz.net.ultraq.thymeleaf.layoutdialect.models.extensions;

import org.thymeleaf.model.IAttribute;

/**
 * Meta-programming extensions to the {@link IAttribute} class.
 *
 * @author Emanuel Rabina
 */
public class IAttributeExtensions {

	/**
	 * Returns whether or not an attribute is an attribute processor of the given name, checks both
	 * prefix:processor and data-prefix-processor variants.
	 *
	 * @param self
	 * @param prefix
	 * @param name
	 * @return {@code true} if this attribute is an attribute processor of the matching name.
	 */
	public static boolean equalsName(IAttribute self, final String prefix, final String name) {
		String attributeName = self.getAttributeCompleteName();
		return attributeName.equals(prefix + ":" + name) || attributeName.equals(
			"data-" + prefix + "-" + name);
	}

}
