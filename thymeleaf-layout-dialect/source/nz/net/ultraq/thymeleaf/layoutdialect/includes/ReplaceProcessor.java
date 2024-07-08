package nz.net.ultraq.thymeleaf.layoutdialect.includes;

import java.util.List;
import java.util.Map;
import nz.net.ultraq.thymeleaf.expressionprocessor.ExpressionProcessor;
import nz.net.ultraq.thymeleaf.layoutdialect.fragments.FragmentFinder;
import nz.net.ultraq.thymeleaf.layoutdialect.fragments.FragmentParameterVariableUpdater;
import nz.net.ultraq.thymeleaf.layoutdialect.fragments.extensions.FragmentExtensions;
import nz.net.ultraq.thymeleaf.layoutdialect.models.TemplateModelFinder;
import nz.net.ultraq.thymeleaf.layoutdialect.models.extensions.IModelExtensions;
import org.thymeleaf.context.AbstractEngineContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.engine.TemplateModel;
import org.thymeleaf.model.IModel;
import org.thymeleaf.processor.element.AbstractAttributeModelProcessor;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import org.thymeleaf.standard.expression.FragmentExpression;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * Similar to Thymeleaf's {@code th:replace}, but allows the passing of entire element fragments to
 * the included template.  Useful if you have some HTML that you want to reuse, but whose contents
 * are too complex to determine or construct with context variables alone.
 *
 * @author Emanuel Rabina
 */
public class ReplaceProcessor extends AbstractAttributeModelProcessor {

	/**
	 * Constructor, set this processor to work on the 'replace' attribute.
	 *
	 * @param templateMode
	 * @param dialectPrefix
	 */
	public ReplaceProcessor(TemplateMode templateMode, String dialectPrefix) {

		super(templateMode, dialectPrefix, null, false, PROCESSOR_NAME, true, PROCESSOR_PRECEDENCE,
			true);
	}

	/**
	 * Locates a page fragment and uses it to replace the current element.
	 *
	 * @param context
	 * @param model
	 * @param attributeName
	 * @param attributeValue
	 * @param structureHandler
	 */
	@Override
	protected void doProcess(ITemplateContext context, IModel model, AttributeName attributeName,
		String attributeValue, IElementModelStructureHandler structureHandler) {

		// Locate the page and fragment to use for replacement
		FragmentExpression fragmentExpression = new ExpressionProcessor(
			context).parseFragmentExpression(attributeValue);
		TemplateModel fragmentForReplacement = new TemplateModelFinder((AbstractEngineContext) context).findFragment(
			fragmentExpression);

		// Gather all fragment parts within the include element, scoping them to this element
		Map<String, List<IModel>> replaceFragments = new FragmentFinder(
			getDialectPrefix()).findFragments(model);
		FragmentExtensions.setLocalFragmentCollection(structureHandler, context, replaceFragments);

		// Keep track of what template is being processed?  Thymeleaf does this for
		// its include processor, so I'm just doing the same here.
		structureHandler.setTemplateData(fragmentForReplacement.getTemplateData());

		// Replace this element with the located fragment
		IModel fragmentForReplacementUse = fragmentForReplacement.cloneModel();
		IModelExtensions.replaceModel(model, 0, fragmentForReplacementUse);

		// Scope variables in fragment definition to current fragment
		new FragmentParameterVariableUpdater(getDialectPrefix(), context).updateLocalVariables(
			fragmentExpression, fragmentForReplacementUse, structureHandler);
	}

	public static String getPROCESSOR_NAME() {
		return PROCESSOR_NAME;
	}

	public static int getPROCESSOR_PRECEDENCE() {
		return PROCESSOR_PRECEDENCE;
	}

	private static final String PROCESSOR_NAME = "replace";
	private static final int PROCESSOR_PRECEDENCE = 0;
}
