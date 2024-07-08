package nz.net.ultraq.thymeleaf.layoutdialect.fragments;

import groovy.lang.Closure;
import java.util.List;
import nz.net.ultraq.thymeleaf.layoutdialect.models.extensions.IModelExtensions;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import org.thymeleaf.standard.expression.Assignation;
import org.thymeleaf.standard.expression.FragmentExpression;

/**
 * Updates the variables at a given element/fragment scope to include those in a fragment
 * expression.
 *
 * @author Emanuel Rabina
 */
public class FragmentParameterVariableUpdater {

	/**
	 * Given a fragment expression, update the local variables of the element being processed.
	 *
	 * @param fragmentExpression
	 * @param fragment
	 * @param structureHandler
	 */
	public void updateLocalVariables(FragmentExpression fragmentExpression, IModel fragment,
		final IElementModelStructureHandler structureHandler) {

		// When fragment parameters aren't named, derive the name from the fragment definition
		if (fragmentExpression.hasSyntheticParameters()) {
			String fragmentDefinition = ((IProcessableElementTag) IModelExtensions.first(fragment)).getAttributeValue(getDialectPrefix(), FragmentProcessor.getPROCESSOR_NAME());
			final List<String> parameterNames = new FragmentParameterNamesExtractor().extract(
				fragmentDefinition);
			DefaultGroovyMethods.eachWithIndex(fragmentExpression.getParameters(),
				new Closure(this, this) {
					public void doCall(Object parameter, Object index) {
						structureHandler.setLocalVariable(parameterNames.get((int) index),
							((Assignation) parameter).getRight().execute(getContext()));
					}

				});
		} else {
			DefaultGroovyMethods.each(fragmentExpression.getParameters(), new Closure(this, this) {
				public void doCall(Assignation parameter) {
					structureHandler.setLocalVariable(
						(String) parameter.getLeft().execute(getContext()),
						parameter.getRight().execute(getContext()));
				}

			});
		}

	}

	public final String getDialectPrefix() {
		return dialectPrefix;
	}

	public final ITemplateContext getContext() {
		return context;
	}

	public FragmentParameterVariableUpdater(String dialectPrefix, ITemplateContext context) {
		this.dialectPrefix = dialectPrefix;
		this.context = context;
	}

	private final String dialectPrefix;
	private final ITemplateContext context;
}
