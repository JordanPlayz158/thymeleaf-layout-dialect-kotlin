package nz.net.ultraq.thymeleaf.layoutdialect.fragments;

import groovy.lang.Closure;
import groovy.lang.Reference;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import nz.net.ultraq.thymeleaf.layoutdialect.PojoLoggerFactory;
import nz.net.ultraq.thymeleaf.layoutdialect.fragments.extensions.FragmentExtensions;
import nz.net.ultraq.thymeleaf.layoutdialect.models.ElementMerger;
import nz.net.ultraq.thymeleaf.layoutdialect.models.extensions.IModelExtensions;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.slf4j.Logger;
import org.thymeleaf.context.AbstractEngineContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.model.IText;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * Processor produced from FragmentProcessor in order to separate include and define logic to avoid
 * ambiguity.
 *
 * @author Emanuel Rabina
 * @author George Vinokhodov
 */
@Deprecated
public class CollectFragmentProcessor extends AbstractAttributeTagProcessor {

	/**
	 * Constructor, sets this processor to work on the 'collect' attribute.
	 *
	 * @param templateMode
	 * @param dialectPrefix
	 */
	public CollectFragmentProcessor(TemplateMode templateMode, String dialectPrefix) {

		super(templateMode, dialectPrefix, null, false, PROCESSOR_COLLECT, true, PROCESSOR_PRECEDENCE,
			true);
	}

	/**
	 * Inserts the content of <code>:define</code> fragments into the encountered collect
	 * placeholder.
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

		if (!deprecationWarned) {
			logger.warn(
				"The layout:collect/data-layout-collect processor is deprecated and will be removed in the next major version of the layout dialect.");
			deprecationWarned = true;
		}

		// Emit a warning if found in the <head> section
		if (getTemplateMode().equals(TemplateMode.HTML) && DefaultGroovyMethods.any(
			context.getElementStack(), new Closure<Boolean>(this, this) {
				public Boolean doCall(Object element) {
					return ((IProcessableElementTag) element).getElementCompleteName().equals("head");
				}

			})) {
			if (!warned) {
				logger.warn(
					"You don't need to put the layout:fragment/data-layout-fragment attribute into the <head> section - "
						+ "the decoration process will automatically copy the <head> section of your content templates into your layout page.");
				warned = true;
			}

		}

		// All :define fragments we collected, :collect fragments included to
		// determine where to stop.  Fragments after :collect are preserved for the
		// next :collect event.
		List<IModel> fragments = FragmentExtensions.getFragmentCollection(context).get(attributeValue);

		// Replace the tag body with the fragment
		if (DefaultGroovyMethods.asBoolean(fragments)) {
			final IModelFactory modelFactory = context.getModelFactory();
			ElementMerger merger = new ElementMerger((AbstractEngineContext) context);
			final Reference<IModel> replacementModel = new Reference<IModel>(
				modelFactory.createModel(tag));
			Boolean first = true;
			while (!fragments.isEmpty()) {
				IModel fragment = fragments.remove(0);
				if (DefaultGroovyMethods.asBoolean(((IProcessableElementTag) fragment.get(0)).getAttributeValue(getDialectPrefix(), CollectFragmentProcessor.getPROCESSOR_COLLECT()))) {
					break;
				}

				if (first) {
					replacementModel.set(merger.merge(replacementModel.get(), fragment));
					first = false;
				} else {
					final Reference<Boolean> firstEvent = new Reference<Boolean>(true);
					IModelExtensions.each(fragment, new Closure(this, this) {
						public void doCall(IProcessableElementTag event)
							throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
							if (firstEvent.get()) {
								firstEvent.set(false);

								replacementModel.get().add((IText) Class.forName("org.thymeleaf.engine.Text")
									.getDeclaredConstructor(CharSequence.class)
									.newInstance("\n"));
								replacementModel.get().add(
									modelFactory.removeAttribute(event, getDialectPrefix(),
										getPROCESSOR_DEFINE()));
							} else {
								replacementModel.get().add(event);
							}

						}

					});
				}

			}

			// Remove the layout:collect attribute - Thymeleaf won't do it for us
			// when using StructureHandler.replaceWith(...)
			replacementModel.get().replace(0,
				modelFactory.removeAttribute((IProcessableElementTag) IModelExtensions.first(replacementModel.get()),
					getDialectPrefix(), PROCESSOR_COLLECT));

			structureHandler.replaceWith(replacementModel.get(), true);
		}

	}

	public static String getPROCESSOR_DEFINE() {
		return PROCESSOR_DEFINE;
	}

	public static String getPROCESSOR_COLLECT() {
		return PROCESSOR_COLLECT;
	}

	public static int getPROCESSOR_PRECEDENCE() {
		return PROCESSOR_PRECEDENCE;
	}

	private static final Logger logger = new PojoLoggerFactory().getLogger(
		CollectFragmentProcessor.class);
	private static boolean warned = false;
	private static boolean deprecationWarned = false;
	private static final String PROCESSOR_DEFINE = "define";
	private static final String PROCESSOR_COLLECT = "collect";
	private static final int PROCESSOR_PRECEDENCE = 1;
}
