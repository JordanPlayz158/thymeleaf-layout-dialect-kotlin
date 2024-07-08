package nz.net.ultraq.thymeleaf.layoutdialect.decorators.strategies;

import groovy.lang.Closure;
import nz.net.ultraq.thymeleaf.layoutdialect.decorators.SortingStrategy;
import nz.net.ultraq.thymeleaf.layoutdialect.models.extensions.IModelExtensions;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.thymeleaf.model.IComment;
import org.thymeleaf.model.IElementTag;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IOpenElementTag;
import org.thymeleaf.model.IProcessableElementTag;

/**
 * The {@code <head>} merging strategy which groups like elements together.
 *
 * @author Emanuel Rabina
 * @since 2.4.0
 */
public class GroupingStrategy implements SortingStrategy {

	/**
	 * Returns the index of the last set of elements that are of the same 'type' as the content node.
	 * eg: groups scripts with scripts, stylesheets with stylesheets, and so on.
	 *
	 * @param headModel
	 * @param childModel
	 * @return Position of the end of the matching element group.
	 */
	public int findPositionForModel(IModel headModel, IModel childModel) {

		// Discard text/whitespace nodes
		if (IModelExtensions.isWhitespace(childModel)) {
			return -1;
		}

		// Find the last element of the same type, and return the point after that
		final HeadEventTypes type = HeadEventTypes.findMatchingType(childModel);
		IModel matchingModel = (IModel) DefaultGroovyMethods.find(
			DefaultGroovyMethods.reverse(IModelExtensions.childModelIterator(headModel)),
			new Closure<Boolean>(this, this) {
				public Boolean doCall(Object headSubModel) {
					return type.equals(HeadEventTypes.findMatchingType((IModel) headSubModel));
				}

			});
		if (DefaultGroovyMethods.asBoolean(matchingModel)) {
			return IModelExtensions.findIndexOfModel(headModel, matchingModel)
				+ (matchingModel).size();
		}

		// Otherwise, do what the AppendingStrategy does
		int positions = headModel.size();
		return positions - (positions > 2 ? 2 : 1);
	}

	/**
	 * Enum for the types of elements in the {@code <head>} section that we might need to sort.
	 */
	private static enum HeadEventTypes {
		COMMENT(new Closure<Boolean>(null, null) {
			public Boolean doCall(Object event) {
				return event instanceof IComment;
			}

		}), META(new Closure<Boolean>(null, null) {
			public Boolean doCall(Object event) {
				return event instanceof IProcessableElementTag && ((IProcessableElementTag) event).getElementCompleteName().equals("meta");
			}

		}), SCRIPT(new Closure<Boolean>(null, null) {
			public Boolean doCall(Object event) {
				return event instanceof IOpenElementTag && ((IOpenElementTag) event).getElementCompleteName().equals("script");
			}

		}), STYLE(new Closure<Boolean>(null, null) {
			public Boolean doCall(Object event) {
				return event instanceof IOpenElementTag && ((IOpenElementTag) event).getElementCompleteName().equals("style");
			}

		}), STYLESHEET(new Closure<Boolean>(null, null) {
			public Boolean doCall(Object event) {
				return event instanceof IProcessableElementTag && ((IProcessableElementTag) event).getElementCompleteName().equals("link")
					&& ((IProcessableElementTag) event).getAttributeValue("rel").equals("stylesheet");
			}

		}), TITLE(new Closure<Boolean>(null, null) {
			public Boolean doCall(Object event) {
				return event instanceof IOpenElementTag && ((IOpenElementTag) event).getElementCompleteName().equals("title");
			}

		}), OTHER(new Closure<Boolean>(null, null) {
			public Boolean doCall(Object event) {
				return event instanceof IElementTag;
			}

		});

		/**
		 * Constructor, set the test that matches this type of head node.
		 *
		 * @param determinant
		 */
		private HeadEventTypes(Closure<Boolean> determinant) {

			this.determinant = determinant;
		}

		/**
		 * Figure out the enum for the given model.
		 *
		 * @param model
		 * @return Matching enum to describe the model.
		 */
		private static HeadEventTypes findMatchingType(final IModel model) {

			return DefaultGroovyMethods.find(values(), new Closure(null, null) {
				public Object doCall(HeadEventTypes headEventType) {
					return headEventType.determinant.call(IModelExtensions.first(model));
				}

			});
		}

		private final Closure<Boolean> determinant;
	}
}
