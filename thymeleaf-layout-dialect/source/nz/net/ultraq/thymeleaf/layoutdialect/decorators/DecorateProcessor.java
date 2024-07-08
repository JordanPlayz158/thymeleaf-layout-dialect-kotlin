package nz.net.ultraq.thymeleaf.layoutdialect.decorators;

import groovy.lang.Closure;
import java.util.List;
import java.util.Map;
import nz.net.ultraq.thymeleaf.expressionprocessor.ExpressionProcessor;
import nz.net.ultraq.thymeleaf.layoutdialect.decorators.html.HtmlDocumentDecorator;
import nz.net.ultraq.thymeleaf.layoutdialect.decorators.xml.XmlDocumentDecorator;
import nz.net.ultraq.thymeleaf.layoutdialect.fragments.FragmentFinder;
import nz.net.ultraq.thymeleaf.layoutdialect.fragments.extensions.FragmentExtensions;
import nz.net.ultraq.thymeleaf.layoutdialect.models.TemplateModelFinder;
import nz.net.ultraq.thymeleaf.layoutdialect.models.extensions.IModelExtensions;
import nz.net.ultraq.thymeleaf.layoutdialect.models.extensions.IProcessableElementTagExtensions;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.thymeleaf.context.AbstractEngineContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.engine.TemplateData;
import org.thymeleaf.engine.TemplateModel;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.model.ITemplateEvent;
import org.thymeleaf.processor.element.AbstractAttributeModelProcessor;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import org.thymeleaf.standard.expression.Assignation;
import org.thymeleaf.standard.expression.FragmentExpression;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * Specifies the name of the template to decorate using the current template.
 *
 * @author Emanuel Rabina
 */
public class DecorateProcessor extends AbstractAttributeModelProcessor {

	/**
	 * Constructor, configure this processor to work on the 'decorate' attribute and to use the given
	 * sorting strategy.
	 *
	 * @param templateMode
	 * @param dialectPrefix
	 * @param sortingStrategy
	 * @param autoHeadMerging
	 */
	public DecorateProcessor(TemplateMode templateMode, String dialectPrefix,
		SortingStrategy sortingStrategy, boolean autoHeadMerging) {

		this(templateMode, dialectPrefix, sortingStrategy, autoHeadMerging, PROCESSOR_NAME);
	}

	/**
	 * Constructor, configurable processor name for the purposes of the deprecated
	 * {@code layout:decorator} alias.
	 *
	 * @param templateMode
	 * @param dialectPrefix
	 * @param sortingStrategy
	 * @param autoHeadMerging
	 * @param attributeName
	 */
	protected DecorateProcessor(TemplateMode templateMode, String dialectPrefix,
		SortingStrategy sortingStrategy, boolean autoHeadMerging, String attributeName) {

		super(templateMode, dialectPrefix, null, false, attributeName, true, PROCESSOR_PRECEDENCE,
			false);

		this.sortingStrategy = sortingStrategy;
		this.autoHeadMerging = autoHeadMerging;
	}

	/**
	 * Locates the template to decorate and, once decorated, inserts it into the processing chain.
	 *
	 * @param context
	 * @param model
	 * @param attributeName
	 * @param attributeValue
	 * @param structureHandler
	 */
	@Override
	protected void doProcess(final ITemplateContext context, IModel model,
		AttributeName attributeName, String attributeValue,
		final IElementModelStructureHandler structureHandler) {

		TemplateModelFinder templateModelFinder = new TemplateModelFinder(
			(AbstractEngineContext) context);

		// Load the entirety of this template so we can access items outside of the root element
		String contentTemplateName = context.getTemplateData().getTemplate();
		IModel contentTemplate = templateModelFinder.findTemplate(contentTemplateName).cloneModel();

		// Check that the root element is the same as the one currently being processed
		IProcessableElementTag contentRootEvent = (IProcessableElementTag) IModelExtensions.find(contentTemplate,
			new Closure<Boolean>(this, this) {
				public Boolean doCall(Object event) {
					return event instanceof IProcessableElementTag;
				}

			});
		IProcessableElementTag rootElement = (IProcessableElementTag) IModelExtensions.first(model);
		if (!DefaultGroovyMethods.asBoolean(
			IProcessableElementTagExtensions.equalsIgnoreXmlnsAndWith(contentRootEvent, rootElement,
				(AbstractEngineContext) context))) {
			throw new IllegalArgumentException(
				"layout:decorate/data-layout-decorate must appear in the root element of your template");
		}

		// Remove the decorate processor from the root element
		if (rootElement.hasAttribute(attributeName)) {
			rootElement = context.getModelFactory().removeAttribute(rootElement, attributeName);
			model.replace(0, rootElement);
		}

		IModelExtensions.replaceModel(contentTemplate,
			IModelExtensions.findIndexOf(contentTemplate, new Closure<Boolean>(this, this) {
				public Boolean doCall(Object event) {
					return event instanceof IProcessableElementTag;
				}

			}), model);

		// Locate the template to decorate
		FragmentExpression decorateTemplateExpression = new ExpressionProcessor(
			context).parseFragmentExpression(attributeValue);
		TemplateModel decorateTemplate = templateModelFinder.findTemplate(decorateTemplateExpression);
		TemplateData decorateTemplateData = decorateTemplate.getTemplateData();
		IModel decorateModel = ((decorateTemplate.cloneModel()));

		// Gather all fragment parts from this page to apply to the new document
		// after decoration has taken place
		Map<String, List<IModel>> pageFragments = new FragmentFinder(getDialectPrefix()).findFragments(
			model);

		// Choose the decorator to use based on template mode, then apply it
		XmlDocumentDecorator decorator =
			getTemplateMode().equals(TemplateMode.HTML) ? new HtmlDocumentDecorator(context,
				sortingStrategy, autoHeadMerging)
				: getTemplateMode().equals(TemplateMode.XML) ? new XmlDocumentDecorator(context) : null;
		if (!DefaultGroovyMethods.asBoolean(decorator)) {
			throw new IllegalArgumentException(
				"Layout dialect cannot be applied to the " + String.valueOf(getTemplateMode())
					+ " template mode, only HTML and XML template modes are currently supported");
		}

		IModel resultTemplate = decorator.decorate(decorateModel, contentTemplate);
		IModelExtensions.replaceModel(model, 0, resultTemplate);
		structureHandler.setTemplateData(decorateTemplateData);

		// Save layout fragments for use later by layout:fragment processors
		FragmentExtensions.setLocalFragmentCollection(structureHandler, context, pageFragments, true);

		// Scope variables in fragment definition to template.  Parameters *must* be
		// named as there is no mechanism for setting their name at the target
		// layout/template.
		if (decorateTemplateExpression.hasParameters()) {
			if (decorateTemplateExpression.hasSyntheticParameters()) {
				throw new IllegalArgumentException(
					"Fragment parameters must be named when used with layout:decorate/data-layout-decorate");
			}

			DefaultGroovyMethods.each(decorateTemplateExpression.getParameters(),
				new Closure(this, this) {
					public void doCall(Object parameter) {
						structureHandler.setLocalVariable(
							(String) ((Assignation) parameter).getLeft().execute(context),
							((Assignation) parameter).getRight().execute(context));
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

	private static final String PROCESSOR_NAME = "decorate";
	private static final int PROCESSOR_PRECEDENCE = 0;
	private final boolean autoHeadMerging;
	private final SortingStrategy sortingStrategy;
}
