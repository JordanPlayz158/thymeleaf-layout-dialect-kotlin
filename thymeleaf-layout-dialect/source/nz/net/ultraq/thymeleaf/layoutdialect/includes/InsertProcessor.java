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
 * Similar to Thymeleaf's {@code th:insert}, but allows the passing of entire element fragments to
 * the included template.  Useful if you have some HTML that you want to reuse, but whose contents
 * are too complex to determine or construct with context variables alone.
 *
 * @author Emanuel Rabina
 */
public class InsertProcessor extends AbstractAttributeModelProcessor {

	/**
	 * Constructor, sets this processor to work on the 'insert' attribute.
	 *
	 * @param templateMode
	 * @param dialectPrefix
	 */
	public InsertProcessor(TemplateMode templateMode, String dialectPrefix) {

		super(templateMode, dialectPrefix, null, false, PROCESSOR_NAME, true, PROCESSOR_PRECEDENCE,
			true);
	}

	/**
	 * Locates a page fragment and inserts it in the current template.
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

		// Locate the page and fragment to insert
		FragmentExpression fragmentExpression = new ExpressionProcessor(
			context).parseFragmentExpression(attributeValue);
		TemplateModel fragmentForInsertion = new TemplateModelFinder((AbstractEngineContext) context).findFragment(
			fragmentExpression);

		// Gather all fragment parts within this element, scoping them to this element
		Map<String, List<IModel>> insertFragments = new FragmentFinder(
			getDialectPrefix()).findFragments(model);
		FragmentExtensions.setLocalFragmentCollection(structureHandler, context, insertFragments);

		// Keep track of what template is being processed?  Thymeleaf does this for
		// its include processor, so I'm just doing the same here.
		structureHandler.setTemplateData(fragmentForInsertion.getTemplateData());

		// Replace the children of this element with those of the to-be-inserted page fragment
		IModel fragmentForInsertionUse = fragmentForInsertion.cloneModel();
		IModelExtensions.removeChildren(model);
		model.insertModel(1, fragmentForInsertionUse);

		// Scope variables in fragment definition to current fragment
		new FragmentParameterVariableUpdater(getDialectPrefix(), context).updateLocalVariables(
			fragmentExpression, fragmentForInsertionUse, structureHandler);
	}

	public static String getPROCESSOR_NAME() {
		return PROCESSOR_NAME;
	}

	public static int getPROCESSOR_PRECEDENCE() {
		return PROCESSOR_PRECEDENCE;
	}

	private static final String PROCESSOR_NAME = "insert";
	private static final int PROCESSOR_PRECEDENCE = 0;
}
