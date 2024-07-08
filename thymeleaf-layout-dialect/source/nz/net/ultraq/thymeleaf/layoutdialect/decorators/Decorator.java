package nz.net.ultraq.thymeleaf.layoutdialect.decorators;

import org.thymeleaf.model.IModel;

/**
 * A decorator performs decoration of a target model, using a source model for all the decorations
 * to apply.  What exactly "decoration" means can vary per implementation.
 *
 * @author Emanuel Rabina
 */
public interface Decorator {

	/**
	 * Decorate the target model with the contents of the source model, returning a new model that is
	 * the result of that decoration.
	 *
	 * @param targetModel The target model to be decorated.
	 * @param sourceModel The source model to use for decorating.
	 * @return A new model that is the result of the decoration process.
	 */
	IModel decorate(IModel targetModel, IModel sourceModel);
}
