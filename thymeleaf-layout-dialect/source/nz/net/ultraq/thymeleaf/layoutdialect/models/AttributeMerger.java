package nz.net.ultraq.thymeleaf.layoutdialect.models;

import groovy.lang.Closure;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import nz.net.ultraq.thymeleaf.layoutdialect.context.extensions.IContextExtensions;
import nz.net.ultraq.thymeleaf.layoutdialect.fragments.CollectFragmentProcessor;
import nz.net.ultraq.thymeleaf.layoutdialect.fragments.FragmentProcessor;
import nz.net.ultraq.thymeleaf.layoutdialect.models.extensions.IAttributeExtensions;
import nz.net.ultraq.thymeleaf.layoutdialect.models.extensions.IModelExtensions;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.thymeleaf.context.AbstractEngineContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IAttribute;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.standard.processor.StandardWithTagProcessor;

/**
 * Merges attributes from one element into another.
 *
 * @author Emanuel Rabina
 */
public class AttributeMerger implements ModelMerger {

	/**
	 * Merge the attributes of the source element with those of the target element.  This is basically
	 * a copy of all attributes in the source model with those in the target model, overwriting any
	 * attributes that have the same name, except for the case of {@code th:with} where variable
	 * declarations are preserved, only overwriting same-named declarations.
	 *
	 * @param sourceModel
	 * @param targetModel
	 * @return New element with the merged attributes.
	 */
	@Override
	public IModel merge(IModel targetModel, IModel sourceModel) {

		// If one of the parameters is missing return a copy of the other, or
		// nothing if both parameters are missing.
		if (!IModelExtensions.asBoolean(targetModel) || !IModelExtensions.asBoolean(sourceModel)) {
			final IModel model = targetModel.cloneModel();
			return IModelExtensions.asBoolean(model) ? model : sourceModel.cloneModel();
		}

		final IModel mergedModel = targetModel.cloneModel();
		final String layoutDialectPrefix = IContextExtensions.getPrefixForDialect(context, LayoutDialect.class);
		final String standardDialectPrefix = IContextExtensions.getPrefixForDialect(context,StandardDialect.class);

		// Merge attributes from the source model's root event to the target model's root event
		DefaultGroovyMethods.each(
			DefaultGroovyMethods.findAll(((IProcessableElementTag) IModelExtensions.first(sourceModel)).getAllAttributes(),
				new Closure<Boolean>(this, this) {
					public Boolean doCall(Object sourceAttribute) {
						return !IAttributeExtensions.equalsName((IAttribute) sourceAttribute,
							layoutDialectPrefix, FragmentProcessor.getPROCESSOR_NAME())
							&& !IAttributeExtensions.equalsName((IAttribute) sourceAttribute,
							layoutDialectPrefix, CollectFragmentProcessor.getPROCESSOR_DEFINE());
					}

				}), new Closure(this, this) {
				public void doCall(Object sourceAttribute) {
					IProcessableElementTag mergedEvent = (IProcessableElementTag) IModelExtensions.first(mergedModel);
					String mergedAttributeValue;

					// Merge th:with attributes
					if (IAttributeExtensions.equalsName((IAttribute) sourceAttribute,
						standardDialectPrefix, StandardWithTagProcessor.ATTR_NAME)) {
						mergedAttributeValue = new VariableDeclarationMerger(getContext()).merge(
							((IAttribute) sourceAttribute).getValue(),
							mergedEvent.getAttributeValue(standardDialectPrefix,
								StandardWithTagProcessor.ATTR_NAME));
					} else {
						mergedAttributeValue = ((IAttribute) sourceAttribute).getValue();
					}

					mergedModel.replace(0, getContext().getModelFactory().replaceAttribute(mergedEvent,
						((IAttribute) sourceAttribute).getAttributeDefinition().getAttributeName(),
						((IAttribute) sourceAttribute).getAttributeCompleteName(),
						mergedAttributeValue));
				}

			});

		return mergedModel;
	}

	public final ITemplateContext getContext() {
		return context;
	}

	public AttributeMerger(AbstractEngineContext context) {
		this.context = context;
	}

	private final AbstractEngineContext context;
}
