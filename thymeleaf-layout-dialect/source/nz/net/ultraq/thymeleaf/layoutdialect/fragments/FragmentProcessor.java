package nz.net.ultraq.thymeleaf.layoutdialect.fragments;

import java.util.List;
import nz.net.ultraq.thymeleaf.layoutdialect.fragments.extensions.FragmentExtensions;
import nz.net.ultraq.thymeleaf.layoutdialect.models.ElementMerger;
import nz.net.ultraq.thymeleaf.layoutdialect.models.extensions.IModelExtensions;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.thymeleaf.context.AbstractEngineContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * This processor serves a dual purpose: to mark sections of the template that can be replaced, and
 * to do the replacing when they're encountered.
 *
 * @author Emanuel Rabina
 */
public class FragmentProcessor extends AbstractAttributeTagProcessor {

	/**
	 * Constructor, sets this processor to work on the 'fragment' attribute.
	 *
	 * @param templateMode
	 * @param dialectPrefix
	 */
	public FragmentProcessor(TemplateMode templateMode, String dialectPrefix) {

		super(templateMode, dialectPrefix, null, false, PROCESSOR_NAME, true, PROCESSOR_PRECEDENCE,
			true);
	}

	/**
	 * Inserts the content of fragments into the encountered fragment placeholder.
	 *
	 * @param context
	 * @param attributeName
	 * @param attributeValue
	 * @param structureHandler
	 */
	@Override
	@SuppressWarnings("AssignmentToStaticFieldFromInstanceMethod")
	protected void doProcess(ITemplateContext context, IProcessableElementTag tag,
		AttributeName attributeName, String attributeValue,
		IElementTagStructureHandler structureHandler) {

		// Locate the fragment that corresponds to this decorator/include fragment
		List<IModel> fragments = FragmentExtensions.getFragmentCollection(context).get(attributeValue);

		// Replace the tag body with the fragment
		if (DefaultGroovyMethods.asBoolean(fragments)) {
			IModel fragment = DefaultGroovyMethods.last(fragments);
			IModelFactory modelFactory = context.getModelFactory();
			IModel replacementModel = new ElementMerger((AbstractEngineContext) context).merge(modelFactory.createModel(tag),
				fragment);

			// Remove the layout:fragment attribute - Thymeleaf won't do it for us
			// when using StructureHandler.replaceWith(...)
			replacementModel.replace(0,
				modelFactory.removeAttribute((IProcessableElementTag) IModelExtensions.first(replacementModel), getDialectPrefix(),
					PROCESSOR_NAME));

			structureHandler.replaceWith(replacementModel, true);
		}

	}

	public static String getPROCESSOR_NAME() {
		return PROCESSOR_NAME;
	}

	public static int getPROCESSOR_PRECEDENCE() {
		return PROCESSOR_PRECEDENCE;
	}

	private static final String PROCESSOR_NAME = "fragment";
	private static final int PROCESSOR_PRECEDENCE = 1;
}
