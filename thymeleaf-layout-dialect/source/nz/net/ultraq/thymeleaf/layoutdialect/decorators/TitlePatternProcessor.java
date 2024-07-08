package nz.net.ultraq.thymeleaf.layoutdialect.decorators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nz.net.ultraq.thymeleaf.layoutdialect.context.extensions.IContextExtensions;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * Allows for greater control of the resulting {@code <title>} element by specifying a pattern with
 * some special tokens.  This can be used to extend the layout's title with the content's one,
 * instead of simply overriding it.
 *
 * @author Emanuel Rabina
 */
public class TitlePatternProcessor extends AbstractAttributeTagProcessor {

	/**
	 * Constructor, sets this processor to work on the 'title-pattern' attribute.
	 *
	 * @param templateMode
	 * @param dialectPrefix
	 */
	public TitlePatternProcessor(TemplateMode templateMode, String dialectPrefix) {

		super(templateMode, dialectPrefix, null, false, PROCESSOR_NAME, true, PROCESSOR_PRECEDENCE,
			true);
	}

	/**
	 * Process the {@code layout:title-pattern} directive, replaces the title text with the titles
	 * from the content and layout pages.
	 *
	 * @param context
	 * @param tag
	 * @param attributeName
	 * @param attributeValue
	 * @param structureHandler
	 */
	@Override
	@SuppressWarnings("AssignmentToStaticFieldFromInstanceMethod")
	protected void doProcess(ITemplateContext context, IProcessableElementTag tag,
		final AttributeName attributeName, String attributeValue,
		IElementTagStructureHandler structureHandler) {

		// Ensure this attribute is only on the <title> element
		if (!tag.getElementCompleteName().equals("title")) {
			throw new IllegalArgumentException(
				attributeName + " processor should only appear in a <title> element");
		}

		String titlePattern = attributeValue;
		IModelFactory modelFactory = context.getModelFactory();

		IModel contentTitle = (IModel) IContextExtensions.getAt(context, CONTENT_TITLE_KEY);
		IModel layoutTitle = (IModel) IContextExtensions.getAt(context, LAYOUT_TITLE_KEY);

		// Break the title pattern up into tokens to map to their respective models
		IModel titleModel = modelFactory.createModel();
		if (layoutTitle != null && contentTitle != null) {
			Matcher matcher = TOKEN_PATTERN.matcher(titlePattern);
			while (matcher.find()) {
				String text = titlePattern.substring(matcher.regionStart(), matcher.start());
				if (StringGroovyMethods.asBoolean(text)) {
					titleModel.add(modelFactory.createText(text));
				}

				String token = matcher.group(1);
				titleModel.addModel(token.equals(TOKEN_LAYOUT_TITLE) ? layoutTitle : contentTitle);
				matcher.region(matcher.regionStart() + text.length() + token.length(),
					titlePattern.length());
			}

			String remainingText = titlePattern.substring(matcher.regionStart());
			if (StringGroovyMethods.asBoolean(remainingText)) {
				titleModel.add(modelFactory.createText(remainingText));
			}

		} else if (DefaultGroovyMethods.asBoolean(contentTitle)) {
			titleModel.addModel(contentTitle);
		} else if (DefaultGroovyMethods.asBoolean(layoutTitle)) {
			titleModel.addModel(layoutTitle);
		}

		structureHandler.setBody(titleModel, true);
	}

	public static String getPROCESSOR_NAME() {
		return PROCESSOR_NAME;
	}

	public static int getPROCESSOR_PRECEDENCE() {
		return PROCESSOR_PRECEDENCE;
	}

	public static String getCONTENT_TITLE_KEY() {
		return CONTENT_TITLE_KEY;
	}

	public static String getLAYOUT_TITLE_KEY() {
		return LAYOUT_TITLE_KEY;
	}

	private static final String TOKEN_LAYOUT_TITLE = "$LAYOUT_TITLE";
	private static final Pattern TOKEN_PATTERN = StringGroovyMethods.bitwiseNegate(Pattern.compile("/(\\$(LAYOUT|CONTENT)_TITLE)/").pattern());
	private static final String PROCESSOR_NAME = "title-pattern";
	private static final int PROCESSOR_PRECEDENCE = 1;
	private static final String CONTENT_TITLE_KEY = "LayoutDialect::ContentTitle";
	private static final String LAYOUT_TITLE_KEY = "LayoutDialect::LayoutTitle";
}
