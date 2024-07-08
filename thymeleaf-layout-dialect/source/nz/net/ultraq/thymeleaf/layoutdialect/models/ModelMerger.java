package nz.net.ultraq.thymeleaf.layoutdialect.models;

import org.thymeleaf.model.IModel;

/**
 * Merges template models by applying the source model to the target model, with the result being
 * implementation-dependant.
 *
 * @author Emanuel Rabina
 */
public interface ModelMerger {

	/**
	 * Merge the source model into the target model.
	 *
	 * @param targetModel
	 * @param sourceModel
	 * @return The result of the merge.
	 */
	IModel merge(IModel targetModel, IModel sourceModel);
}
