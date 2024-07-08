package nz.net.ultraq.thymeleaf.layoutdialect.decorators;

import org.thymeleaf.model.IModel;

/**
 * Interface for controlling the sort order in which {@code <head>} elements from one source are
 * placed into another.
 *
 * @author Emanuel Rabina
 */
public interface SortingStrategy {

	/**
	 * Returns the position in a {@code <head>} element model to insert a child model.
	 *
	 * @param headModel  Model of a {@code <head>} element.
	 * @param childModel A model that can be found in a {@code <head>} element.
	 * @return Position to insert the child model into.
	 */
	int findPositionForModel(IModel headModel, IModel childModel);
}
