package nz.net.ultraq.thymeleaf.layoutdialect.includes;

import groovy.lang.Closure;
import java.util.List;
import java.util.Map;
import nz.net.ultraq.thymeleaf.expressionprocessor.ExpressionProcessor;
import nz.net.ultraq.thymeleaf.layoutdialect.PojoLoggerFactory;
import nz.net.ultraq.thymeleaf.layoutdialect.fragments.FragmentFinder;
import nz.net.ultraq.thymeleaf.layoutdialect.fragments.FragmentParameterNamesExtractor;
import nz.net.ultraq.thymeleaf.layoutdialect.fragments.FragmentProcessor;
import nz.net.ultraq.thymeleaf.layoutdialect.fragments.extensions.FragmentExtensions;
import nz.net.ultraq.thymeleaf.layoutdialect.models.TemplateModelFinder;
import nz.net.ultraq.thymeleaf.layoutdialect.models.extensions.IModelExtensions;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.slf4j.Logger;
import org.thymeleaf.context.AbstractEngineContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.engine.TemplateModel;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractAttributeModelProcessor;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import org.thymeleaf.standard.expression.Assignation;
import org.thymeleaf.standard.expression.FragmentExpression;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * Similar to Thymeleaf's {@code th:include}, but allows the passing of entire element fragments to
 * the included template.  Useful if you have some HTML that you want to reuse, but whose contents
 * are too complex to determine or construct with context variables alone.
 *
 * @author Emanuel Rabina
 * @deprecated Use {@link InsertProcessor} ({@code layout:insert}) instead.
 */
@Deprecated
public class IncludeProcessor extends AbstractAttributeModelProcessor {

	/**
	 * Constructor, sets this processor to work on the 'include' attribute.
	 *
	 * @param templateMode
	 * @param dialectPrefix
	 */
	public IncludeProcessor(TemplateMode templateMode, String dialectPrefix) {

		super(templateMode, dialectPrefix, null, false, PROCESSOR_NAME, true, PROCESSOR_PRECEDENCE,
			true);
	}

	/**
	 * Locates a page fragment and includes it in the current template.
	 *
	 * @param context
	 * @param model
	 * @param attributeName
	 * @param attributeValue
	 * @param structureHandler
	 */
	@Override
	@SuppressWarnings("AssignmentToStaticFieldFromInstanceMethod")
	protected void doProcess(final ITemplateContext context, final IModel model,
		AttributeName attributeName, String attributeValue,
		final IElementModelStructureHandler structureHandler) {

		if (!warned) {
			logger.warn(
				"The layout:include/data-layout-include processor is deprecated and will be removed in the next major version of the layout dialect.  "
					+ "Use the layout:insert/data-layout-insert processor instead.  "
					+ "See https://github.com/ultraq/thymeleaf-layout-dialect/issues/107 for more information.");
			warned = true;
		}

		// Locate the page and fragment for inclusion
		FragmentExpression fragmentExpression = new ExpressionProcessor(
			context).parseFragmentExpression(attributeValue);
		TemplateModel fragmentForInclusion = new TemplateModelFinder((AbstractEngineContext) context).findFragment(
			fragmentExpression);

		// Gather all fragment parts within the include element, scoping them to this element
		Map<String, List<IModel>> includeFragments = new FragmentFinder(
			getDialectPrefix()).findFragments(model);
		FragmentExtensions.setLocalFragmentCollection(structureHandler, context, includeFragments);

		// Keep track of what template is being processed?  Thymeleaf does this for
		// its include processor, so I'm just doing the same here.
		structureHandler.setTemplateData(fragmentForInclusion.getTemplateData());

		// Replace the children of this element with the children of the included page fragment
		IModel fragmentForInclusionUse = fragmentForInclusion.cloneModel();
		IModelExtensions.removeChildren(model);
		IModelExtensions.trim(fragmentForInclusionUse);
		DefaultGroovyMethods.each(IModelExtensions.childModelIterator(fragmentForInclusionUse),
			new Closure(this, this) {
				public void doCall(Object fragmentChildModel) {
					model.insertModel(model.size() - 1, (IModel) fragmentChildModel);
				}

			});

		// When fragment parameters aren't named, derive the name from the fragment definition
		// TODO: Common code across all the inclusion processors
		if (fragmentExpression.hasSyntheticParameters()) {
			String fragmentDefinition = ((IProcessableElementTag) IModelExtensions.first(fragmentForInclusionUse))
				.getAttributeValue(getDialectPrefix(), FragmentProcessor.getPROCESSOR_NAME());
			final List<String> parameterNames = new FragmentParameterNamesExtractor().extract(
				fragmentDefinition);
			DefaultGroovyMethods.eachWithIndex(fragmentExpression.getParameters(),
				new Closure(this, this) {
					public void doCall(Object parameter, Object index) {
						structureHandler.setLocalVariable(parameterNames.get((int) index),
							((Assignation) parameter).getRight().execute(context));
					}

				});
		} else {
			DefaultGroovyMethods.each(fragmentExpression.getParameters(), new Closure(this, this) {
				public void doCall(Assignation parameter) {
					structureHandler.setLocalVariable((String) parameter.getLeft().execute(context),
						parameter.getRight().execute(context));
				}

			});
		}

	}

	public static String getPROCESSOR_NAME() {
		return PROCESSOR_NAME;
	}

	public static int getPROCESSOR_PRECEDENCE() {
		return PROCESSOR_PRECEDENCE;
	}

	private static final Logger logger = new PojoLoggerFactory().getLogger(IncludeProcessor.class);
	private static boolean warned = false;
	private static final String PROCESSOR_NAME = "include";
	private static final int PROCESSOR_PRECEDENCE = 0;
}
