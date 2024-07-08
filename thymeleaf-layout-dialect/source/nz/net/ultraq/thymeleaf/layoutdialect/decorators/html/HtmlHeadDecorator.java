package nz.net.ultraq.thymeleaf.layoutdialect.decorators.html;

import groovy.lang.Closure;
import nz.net.ultraq.thymeleaf.layoutdialect.decorators.Decorator;
import nz.net.ultraq.thymeleaf.layoutdialect.decorators.SortingStrategy;
import nz.net.ultraq.thymeleaf.layoutdialect.models.AttributeMerger;
import nz.net.ultraq.thymeleaf.layoutdialect.models.extensions.IModelExtensions;
import nz.net.ultraq.thymeleaf.layoutdialect.models.extensions.ITemplateEventExtensions;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.thymeleaf.context.AbstractEngineContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.ITemplateEvent;

/**
 * A decorator specific to processing an HTML {@code <head>} element.
 *
 * @author Emanuel Rabina
 */
public class HtmlHeadDecorator implements Decorator {
	private final ITemplateContext context;
	private final SortingStrategy sortingStrategy;

	public HtmlHeadDecorator(ITemplateContext context, SortingStrategy sortingStrategy) {
		this.context = context;
		this.sortingStrategy = sortingStrategy;
	}


	/**
	 * Decorate the {@code <head>} part.
	 *
	 * @param targetHeadModel
	 * @param sourceHeadModel
	 * @return Result of the decoration.
	 */
	@Override
	public IModel decorate(IModel targetHeadModel, IModel sourceHeadModel) {

		// If none of the parameters are present, return nothing
		if (!IModelExtensions.asBoolean(targetHeadModel) && !IModelExtensions.asBoolean(
			sourceHeadModel)) {
			return null;
		}

		final IModelFactory modelFactory = context.getModelFactory();

		// New head model based off the target being decorated
		final IModel resultHeadModel = new AttributeMerger((AbstractEngineContext) context).merge(targetHeadModel,
			sourceHeadModel);
		if (IModelExtensions.asBoolean(sourceHeadModel) && IModelExtensions.asBoolean(targetHeadModel)) {
			DefaultGroovyMethods.each(IModelExtensions.childModelIterator(sourceHeadModel),
				new Closure(this, this) {
					public void doCall(Object model) {
						IModelExtensions.insertModelWithWhitespace(resultHeadModel,
							getSortingStrategy().findPositionForModel(resultHeadModel, (IModel) model),
							(IModel) model, modelFactory);
					}

				});
		}

		// Replace <title>s in the result with a proper merge of the source and target <title> elements
		Closure titleFinder = new Closure(this, this) {
			public Object doCall(ITemplateEvent event) {
				return ITemplateEventExtensions.isOpeningElementOf(event, "title");
			}

		};

		int indexOfTitle = IModelExtensions.findIndexOf(resultHeadModel, titleFinder);
		if (indexOfTitle != -1) {
			IModelExtensions.removeAllModels(resultHeadModel, titleFinder);
			IModel resultTitle = new HtmlTitleDecorator(context).decorate(
				IModelExtensions.findModel(targetHeadModel, titleFinder),
				IModelExtensions.findModel(sourceHeadModel, titleFinder));
			IModelExtensions.insertModelWithWhitespace(resultHeadModel, indexOfTitle, resultTitle,
				modelFactory);
		}

		return ((IModel) (resultHeadModel));
	}

	public final ITemplateContext getContext() {
		return context;
	}

	public final SortingStrategy getSortingStrategy() {
		return sortingStrategy;
	}
}
