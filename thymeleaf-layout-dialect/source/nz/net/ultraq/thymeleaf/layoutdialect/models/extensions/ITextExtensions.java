package nz.net.ultraq.thymeleaf.layoutdialect.models.extensions;

import org.thymeleaf.model.IText;

/**
 * Meta-programming extensions to the {@link IText} class.
 *
 * @author Emanuel Rabina
 */
public class ITextExtensions {

	/**
	 * Compares this text with another.
	 *
	 * @param self
	 * @param other
	 * @return {@code true} if the text content matches.
	 */
	@SuppressWarnings("EqualsOverloaded")
	public static boolean equals(IText self, Object other) {
		return other instanceof IText && self.getText().equals(((IText) other).getText());
	}

	/**
	 * Returns whether or not this text event is collapsible whitespace.
	 *
	 * @param self
	 * @return {@code true} if, when trimmed, the text content is empty.
	 */
	public static boolean isWhitespace(IText self) {
		return self.getText().trim().isEmpty();
	}

}
