package nz.net.ultraq.thymeleaf.layoutdialect.models;

import groovy.lang.Closure;
import groovy.util.BuilderSupport;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import nz.net.ultraq.thymeleaf.layoutdialect.PojoLoggerFactory;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.slf4j.Logger;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.ElementDefinition;
import org.thymeleaf.engine.ElementDefinitions;
import org.thymeleaf.engine.HTMLElementDefinition;
import org.thymeleaf.engine.HTMLElementType;
import org.thymeleaf.model.AttributeValueQuotes;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * Create Thymeleaf 3 models using the Groovy builder syntax.
 *
 * @author Emanuel Rabina
 */
public class ModelBuilder extends BuilderSupport {

	/**
	 * Constructor, create a new model builder.
	 *
	 * @param context
	 */
	public ModelBuilder(ITemplateContext context) {

		this(context.getModelFactory(), context.getConfiguration().getElementDefinitions(),
			context.getTemplateMode());
	}

	/**
	 * Constructor, create a new model builder.
	 *
	 * @param modelFactory
	 * @param elementDefinitions
	 * @param templateMode
	 */
	public ModelBuilder(IModelFactory modelFactory, ElementDefinitions elementDefinitions,
		TemplateMode templateMode) {

		this.modelFactory = modelFactory;
		this.elementDefinitions = elementDefinitions;
		this.templateMode = templateMode;
	}

	/**
	 * Appends an existing model to the model being built.
	 *
	 * @param model
	 */
	public void add(IModel model) {
		IModel current = ((IModel) getCurrent());

		current.insertModel(current.size(), model);
	}

	/**
	 * Captures the top `build` call so that it doesn't end up as a node in the final model.
	 *
	 * @param definition
	 * @return The model built using the closure definition.
	 */
	public IModel build(Closure<IModel> definition) {

		setClosureDelegate(definition, null);
		return definition.call();
	}

	/**
	 * Create a model for the given element.
	 *
	 * @param name Element name.
	 * @return New model representing an element with the given name.
	 */
	@Override
	protected Object createNode(Object name) {

		return createNode(name, null, null);
	}

	/**
	 * Create a model for the given element and inner text content.
	 *
	 * @param name  Element name.
	 * @param value Text content.
	 * @return New model representing an element with the given name and content.
	 */
	@Override
	protected Object createNode(Object name, Object value) {

		return createNode(name, null, value);
	}

	/**
	 * Create a model for the given element and attributes.
	 *
	 * @param name       Element name.
	 * @param attributes Element attributes.
	 * @return New model representing an element with the given name and attributes.
	 */
	@Override
	protected Object createNode(Object name, Map attributes) {

		return createNode(name, attributes, null);
	}

	/**
	 * Create a model for the given element, attributes, and inner text content.
	 *
	 * @param name       Element name.
	 * @param attributes Element attributes.
	 * @param value      Text content.
	 * @return New model representing an element with the given name, attributes, and content.
	 */
	@Override
	@SuppressWarnings("AssignmentToStaticFieldFromInstanceMethod")
	protected IModel createNode(Object name, final Map attributes, Object value) {

		// Normalize values for Java implementations as the model factory doesn't
		// know what to do with Groovy versions of things
		String elementName = name.toString();
		String elementText = value.toString();
		if (DefaultGroovyMethods.asBoolean(attributes)) {
			DefaultGroovyMethods.each(attributes.entrySet(), new Closure<String>(this, this) {
				public String doCall(Object entry) {
					return putAt0(attributes, (((Entry) entry).getKey()),
						attributes.get((((Entry) entry).getKey())).toString());
				}

			});
		}

		IModel model = modelFactory.createModel();
		HTMLElementDefinition elementDefinition = (HTMLElementDefinition) elementDefinitions.forName(templateMode, elementName);

		// HTML void element
		if (elementDefinition.getType().equals(HTMLElementType.VOID)) {
			if (attributes != null && attributes.get("standalone") != null) {
				attributes.remove("standalone");
				model.add(modelFactory.createStandaloneElementTag(elementName, attributes,
					AttributeValueQuotes.DOUBLE, false, true));
			} else if (attributes != null && attributes.get("void") != null) {
				attributes.remove("void");
				model.add(modelFactory.createStandaloneElementTag(elementName, attributes,
					AttributeValueQuotes.DOUBLE, false, false));
			} else {
				if (!encounteredVoidTags.contains(elementName)) {
					logger.warn("Instructed to write a closing tag {} for an HTML void element.  "
							+ "This might cause processing errors further down the track.  "
							+ "To avoid this, either self close the opening element, remove the closing tag, or process this template using the XML processing mode.  "
							+ "See https://html.spec.whatwg.org/multipage/syntax.html#void-elements for more information on HTML void elements.",
						elementName);
					DefaultGroovyMethods.leftShift(encounteredVoidTags, elementName);
				}

				model.add(modelFactory.createStandaloneElementTag(elementName, attributes,
					AttributeValueQuotes.DOUBLE, false, false));
				model.add(modelFactory.createCloseElementTag(elementName, false, true));
			}

		} else if (attributes != null && attributes.get("standalone") != null) {
			attributes.remove("standalone");
			model.add(modelFactory.createStandaloneElementTag(elementName, attributes,
				AttributeValueQuotes.DOUBLE, false, true));
		} else {
			model.add(
				modelFactory.createOpenElementTag(elementName, attributes, AttributeValueQuotes.DOUBLE,
					false));
			if (StringGroovyMethods.asBoolean(elementText)) {
				model.add(modelFactory.createText(elementText));
			}

			model.add(modelFactory.createCloseElementTag(elementName));
		}

		return ((IModel) (model));
	}

	/**
	 * Link a parent and child node.  A child node is appended to a parent by being the last sub-model
	 * before the parent close tag.
	 *
	 * @param parent
	 * @param child
	 */
	@Override
	protected void nodeCompleted(Object parent, Object child) {
		IModel parentModel = (IModel) parent;

		if (DefaultGroovyMethods.asBoolean(parent)) {
			parentModel.insertModel(parentModel.size(), (IModel) child);
		}
	}

	/**
	 * Does nothing.  Because models only copy events when added to one another, we can't just add
	 * child events at this point - we need to wait until that child has had it's children added, and
	 * so on.  So the parent/child link is made in the {@link ModelBuilder#nodeCompleted} method
	 * instead.
	 */
	@Override
	protected void setParent(Object parent, Object child) {
	}

	private static final Logger logger = new PojoLoggerFactory().getLogger(ModelBuilder.class);
	@SuppressWarnings("FieldName")
	private static final HashSet<String> encounteredVoidTags = new HashSet<>();
	private final ElementDefinitions elementDefinitions;
	private final IModelFactory modelFactory;
	private final TemplateMode templateMode;

	private static <K, V, Value extends V> Value putAt0(Map<K, V> propOwner, K key, Value value) {
		propOwner.put(key, value);
		return value;
	}
}
