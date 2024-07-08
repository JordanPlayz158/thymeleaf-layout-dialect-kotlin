package nz.net.ultraq.thymeleaf.layoutdialect.decorators.strategies;

import nz.net.ultraq.thymeleaf.layoutdialect.decorators.SortingStrategy;
import nz.net.ultraq.thymeleaf.layoutdialect.models.extensions.IModelExtensions;
import org.thymeleaf.model.IModel;

/**
 * The standard {@code <head>} merging strategy, which simply appends the content elements to the
 * layout ones.
 *
 * @author Emanuel Rabina
 * @since 2.4.0
 */
public class AppendingStrategy implements SortingStrategy {

	/**
	 * Returns the position at the end of the {@code <head>} section.
	 *
	 * @param headModel
	 * @return The end of the head model.
	 */
	public int findPositionForModel(IModel headModel, IModel childModel) {

		// Discard text/whitespace nodes
		if (IModelExtensions.isWhitespace(childModel)) {
			return -1;
		}

		int positions = headModel.size();
		return positions - (positions > 2 ? 2 : 1);
	}

}
