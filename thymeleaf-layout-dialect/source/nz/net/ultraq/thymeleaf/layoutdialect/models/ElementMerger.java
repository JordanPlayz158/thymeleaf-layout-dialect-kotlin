package nz.net.ultraq.thymeleaf.layoutdialect.models;

import nz.net.ultraq.thymeleaf.layoutdialect.models.extensions.IModelExtensions;
import org.thymeleaf.context.AbstractEngineContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.AttributeValueQuotes;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IOpenElementTag;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.model.IStandaloneElementTag;

/**
 * Merges an element and all its children into an existing element.
 *
 * @author Emanuel Rabina
 */
public class ElementMerger implements ModelMerger {

	/**
	 * Replace the content of the target element, with the content of the source element.
	 *
	 * @param targetModel
	 * @param sourceModel
	 * @return Model that is the result of the merge.
	 */
	@Override
	public IModel merge(IModel targetModel, IModel sourceModel) {

		// If one of the parameters is missing return a copy of the other, or
		// nothing if both parameters are missing.
		if (targetModel == null || sourceModel == null) {
			return IModelExtensions.cloneModel(targetModel, sourceModel);
		}

		IModelFactory modelFactory = context.getModelFactory();

		// The result we want is the source model, but merged into the target root element attributes
		IProcessableElementTag sourceRootEvent = (IProcessableElementTag) IModelExtensions.first(sourceModel);
		IModel sourceRootElement = modelFactory.createModel(sourceRootEvent);
		IProcessableElementTag targetRootEvent = (IProcessableElementTag) IModelExtensions.first(targetModel);
		IModel targetRootElement = modelFactory.createModel(
			sourceRootEvent instanceof IOpenElementTag ? modelFactory.createOpenElementTag(
				sourceRootEvent.getElementCompleteName(), targetRootEvent.getAttributeMap(),
				AttributeValueQuotes.DOUBLE, false) : sourceRootEvent instanceof IStandaloneElementTag
				? modelFactory.createStandaloneElementTag(sourceRootEvent.getElementCompleteName(),
				targetRootEvent.getAttributeMap(), AttributeValueQuotes.DOUBLE, false,
				((IStandaloneElementTag) sourceRootEvent).isMinimized()) : null);
		IModel mergedRootElement = new AttributeMerger(context).merge(targetRootElement,
			sourceRootElement);
		IModel mergedModel = sourceModel.cloneModel();
		mergedModel.replace(0, IModelExtensions.first(mergedRootElement));
		return ((IModel) (mergedModel));
	}

	public final AbstractEngineContext getContext() {
		return context;
	}

	public ElementMerger(AbstractEngineContext context) {
		this.context = context;
	}

	private final AbstractEngineContext context;
}
