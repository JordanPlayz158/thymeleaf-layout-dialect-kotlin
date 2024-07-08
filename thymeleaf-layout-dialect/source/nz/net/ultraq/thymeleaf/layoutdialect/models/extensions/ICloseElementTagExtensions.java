package nz.net.ultraq.thymeleaf.layoutdialect.models.extensions;

import org.thymeleaf.model.ICloseElementTag;

/**
 * Meta-programming extensions to the {@link ICloseElementTag} class.
 *
 * @author Emanuel Rabina
 */
public class ICloseElementTagExtensions {

	/**
	 * Compares this close tag with another.
	 *
	 * @param self
	 * @param other
	 * @return {@code true} if this tag has the same name as the other element.
	 */
	@SuppressWarnings("EqualsOverloaded")
	public static boolean equals(ICloseElementTag self, Object other) {
		return other instanceof ICloseElementTag && self.getElementDefinition()
			.equals(((ICloseElementTag) other).getElementDefinition());
	}

}
