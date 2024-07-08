package nz.net.ultraq.thymeleaf.layoutdialect.decorators.html;

import nz.net.ultraq.thymeleaf.layoutdialect.decorators.Decorator;
import nz.net.ultraq.thymeleaf.layoutdialect.models.AttributeMerger;
import nz.net.ultraq.thymeleaf.layoutdialect.models.extensions.IModelExtensions;
import org.thymeleaf.context.AbstractEngineContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;

/**
 * A decorator specific to processing an HTML {@code <body>} element.
 *
 * @author Emanuel Rabina
 */
public class HtmlBodyDecorator implements Decorator {
	private final AbstractEngineContext context;

	public HtmlBodyDecorator(AbstractEngineContext context) {
		this.context = context;
	}

	/**
	 * Decorate the {@code <body>} part.
	 *
	 * @param targetBodyModel
	 * @param sourceBodyModel
	 * @return Result of the decoration.
	 */
	@Override
	public IModel decorate(IModel targetBodyModel, IModel sourceBodyModel) {
		// If one of the parameters is missing return a copy of the other, or
		// nothing if both parameters are missing.
		if (targetBodyModel == null || sourceBodyModel == null) {
			return IModelExtensions.cloneModel(targetBodyModel, sourceBodyModel);
		}

		return new AttributeMerger(context).merge(targetBodyModel, sourceBodyModel);
	}

	public final AbstractEngineContext getContext() {
		return context;
	}
}
