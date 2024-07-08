package nz.net.ultraq.thymeleaf.layoutdialect.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import nz.net.ultraq.thymeleaf.layoutdialect.context.extensions.IContextExtensions;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.thymeleaf.context.AbstractEngineContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.TemplateModel;
import org.thymeleaf.standard.expression.FragmentExpression;

/**
 * A simple API for retrieving (immutable template) models using Thymeleaf's template manager.
 *
 * @author Emanuel Rabina
 */
public class TemplateModelFinder {

	/**
	 * Return a model for any arbitrary item in a template.
	 *
	 * @param templateName
	 * @param selector     A Thymeleaf DOM selector, which in turn is an AttoParser DOM selector.  See
	 *                     the Appendix in the Using Thymeleaf docs for the DOM selector syntax.
	 * @return Model for the selected template and selector.
	 */
	private TemplateModel find(String templateName, String selector) {

		return context.getConfiguration().getTemplateManager().parseStandalone(context, templateName,
			StringGroovyMethods.asBoolean(selector) ? DefaultGroovyMethods.asType(
				new ArrayList<>(Collections.singletonList(selector)), Set.class) : null,
			context.getTemplateMode(), true, true);
	}

	/**
	 * Return a model for any arbitrary item in a template.
	 *
	 * @param templateName
	 * @return Model for the selected template and selector.
	 */
	private TemplateModel find(String templateName) {
		return find(templateName, null);
	}

	/**
	 * Return the model specified by the given fragment expression.
	 *
	 * @param fragmentExpression
	 * @return Fragment matching the fragment specification.
	 */
	public TemplateModel findFragment(FragmentExpression fragmentExpression) {

		Object dialectPrefix = IContextExtensions.getPrefixForDialect(context, LayoutDialect.class);
		final String string = fragmentExpression.getTemplateName().execute(context).toString();
		String templateName = StringGroovyMethods.asBoolean(string) ? string : "this";
		if (templateName.equals("this")) {
			templateName = context.getTemplateData().getTemplate();
		}

		return findFragment(templateName,
			fragmentExpression.getFragmentSelector().execute(context).toString(),
			(String) dialectPrefix);
	}

	/**
	 * Return the model specified by the template and fragment name parameters.
	 *
	 * @param templateName
	 * @param fragmentName
	 * @param dialectPrefix
	 * @return Fragment matching the fragment specification.
	 */
	public TemplateModel findFragment(String templateName, String fragmentName,
		String dialectPrefix) {

		return find(templateName,
			fragmentName != null && dialectPrefix != null ? "//[" + dialectPrefix + ":fragment='" + fragmentName
				+ "' or " + dialectPrefix + ":fragment^='" + fragmentName + "(' or " + dialectPrefix
				+ ":fragment^='" + fragmentName + " (' or " + "data-" + dialectPrefix + "-fragment='"
				+ fragmentName + "' or " + "data-" + dialectPrefix + "-fragment^='" + fragmentName
				+ "(' or " + "data-" + dialectPrefix + "-fragment^='" + fragmentName + " ('" + "]"
				: null);
	}

	/**
	 * Return the model specified by the template and fragment name parameters.
	 *
	 * @param templateName
	 * @param fragmentName
	 * @return Fragment matching the fragment specification.
	 */
	public TemplateModel findFragment(String templateName, String fragmentName) {
		return findFragment(templateName, fragmentName, null);
	}

	/**
	 * Return the model specified by the template and fragment name parameters.
	 *
	 * @param templateName
	 * @return Fragment matching the fragment specification.
	 */
	public TemplateModel findFragment(String templateName) {
		return findFragment(templateName, null, null);
	}

	/**
	 * Return a model for the template specified by the given fragment expression.
	 *
	 * @param fragmentExpression
	 * @return Template model matching the fragment specification.
	 */
	public TemplateModel findTemplate(FragmentExpression fragmentExpression) {

		return find(fragmentExpression.getTemplateName().execute(context).toString());
	}

	/**
	 * Return a model for the template specified by the given template name.
	 *
	 * @param templateName
	 * @return Template model matching the fragment specification.
	 */
	public TemplateModel findTemplate(String templateName) {

		return find(templateName);
	}

	public final ITemplateContext getContext() {
		return context;
	}

	public TemplateModelFinder(AbstractEngineContext context) {
		this.context = context;
	}

	private final AbstractEngineContext context;
}
