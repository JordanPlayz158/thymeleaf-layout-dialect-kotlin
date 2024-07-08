package nz.net.ultraq.thymeleaf.layoutdialect.models.extensions;

import org.thymeleaf.model.ICloseElementTag;
import org.thymeleaf.model.IOpenElementTag;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.model.IStandaloneElementTag;
import org.thymeleaf.model.ITemplateEvent;
import org.thymeleaf.model.IText;

/**
 * Meta-programming extensions to the {@link ITemplateEvent} class.
 *
 * @author Emanuel Rabina
 */
public class ITemplateEventExtensions {

	/**
	 * Returns whether or not this event represents an opening element.
	 *
	 * @param self
	 * @return {@code true} if this event is an opening tag.
	 */
	public static boolean isClosingElement(ITemplateEvent self) {
		return self instanceof ICloseElementTag || self instanceof IStandaloneElementTag;
	}

	/**
	 * Returns whether or not this event represents a closing element of the given name.
	 *
	 * @param self
	 * @param tagName
	 * @return {@code true} if this event is a closing tag and has the given tag name.
	 */
	public static boolean isClosingElementOf(ITemplateEvent self, String tagName) {
		return isClosingElement(self) && ((ICloseElementTag) self).getElementCompleteName().equals(tagName);
	}

	/**
	 * Returns whether or not this event represents an opening element.
	 *
	 * @param self
	 * @return {@code true} if this event is an opening tag.
	 */
	public static boolean isOpeningElement(ITemplateEvent self) {
		return self instanceof IOpenElementTag || self instanceof IStandaloneElementTag;
	}

	/**
	 * Returns whether or not this event represents an opening element of the given name.
	 *
	 * @param self
	 * @param tagName
	 * @return {@code true} if this event is an opening tag and has the given tag name.
	 */
	public static boolean isOpeningElementOf(ITemplateEvent self, String tagName) {
		return isOpeningElement(self) && ((IProcessableElementTag) self).getElementCompleteName().equals(tagName);
	}

	/**
	 * Returns whether or not this event represents collapsible whitespace.
	 *
	 * @param self
	 * @return {@code true} if this is a collapsible text node.
	 */
	public static boolean isWhitespace(ITemplateEvent self) {
		return self instanceof IText && ITextExtensions.isWhitespace((IText) self);
	}

}
