package nz.net.ultraq.thymeleaf.layoutdialect.decorators.html;

import groovy.lang.Closure;
import nz.net.ultraq.thymeleaf.layoutdialect.decorators.SortingStrategy;
import nz.net.ultraq.thymeleaf.layoutdialect.decorators.xml.XmlDocumentDecorator;
import nz.net.ultraq.thymeleaf.layoutdialect.models.extensions.IModelExtensions;
import nz.net.ultraq.thymeleaf.layoutdialect.models.extensions.ITemplateEventExtensions;
import org.thymeleaf.context.AbstractEngineContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.ICloseElementTag;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IOpenElementTag;
import org.thymeleaf.model.ITemplateEvent;

/**
 * A decorator made to work over an HTML document.  Decoration for a document involves 2
 * sub-decorators: a special one for the {@code <head>} element, and a standard one for the
 * {@code <body>} element.
 *
 * @author Emanuel Rabina
 */
public class HtmlDocumentDecorator extends XmlDocumentDecorator {

	/**
	 * Constructor, builds a decorator with the given configuration.
	 *
	 * @param context
	 * @param sortingStrategy
	 * @param autoHeadMerging
	 */
	public HtmlDocumentDecorator(ITemplateContext context, SortingStrategy sortingStrategy,
		boolean autoHeadMerging) {

		super(context);
		this.sortingStrategy = sortingStrategy;
		this.autoHeadMerging = autoHeadMerging;
	}

	/**
	 * Decorate an entire HTML page.
	 *
	 * @param targetDocumentModel
	 * @param sourceDocumentModel
	 * @return Result of the decoration.
	 */
	@Override
	public IModel decorate(IModel targetDocumentModel, IModel sourceDocumentModel) {

		IModelFactory modelFactory = getContext().getModelFactory();
		IModel resultDocumentModel = targetDocumentModel.cloneModel();

		// Head decoration
		Closure headModelFinder = new Closure(this, this) {
			public Object doCall(ITemplateEvent event) {
				return ITemplateEventExtensions.isOpeningElementOf(event, "head");
			}

		};
		if (autoHeadMerging) {
			IModel targetHeadModel = IModelExtensions.findModel(resultDocumentModel, headModelFinder);
			IModel resultHeadModel = new HtmlHeadDecorator(getContext(), sortingStrategy).decorate(
				targetHeadModel, IModelExtensions.findModel(sourceDocumentModel, headModelFinder));
			if (IModelExtensions.asBoolean(resultHeadModel)) {
				if (IModelExtensions.asBoolean(targetHeadModel)) {
					IModelExtensions.replaceModel(resultDocumentModel,
						IModelExtensions.findIndexOfModel(resultDocumentModel, targetHeadModel),
						resultHeadModel);
				} else {
					IModelExtensions.insertModelWithWhitespace(resultDocumentModel,
						IModelExtensions.findIndexOf(resultDocumentModel, new Closure<Boolean>(this, this) {
							public Boolean doCall(Object event) {
								return (event instanceof IOpenElementTag
									&& ((IOpenElementTag) event).getElementCompleteName().equals("body")) || (
									event instanceof ICloseElementTag
										&& ((ICloseElementTag) event).getElementCompleteName().equals("html"));
							}

						}) - 1, resultHeadModel, modelFactory);
				}

			}

		} else {
			// TODO: If autoHeadMerging is false, this really shouldn't be needed as
			//       the basis for `resultDocumentModel` should be the source model.
			//       This 'hack' is OK for an experimental option, but the fact that
			//       it exists means I should rethink how the result model is made.
			IModelExtensions.replaceModel(resultDocumentModel,
				IModelExtensions.findIndexOf(resultDocumentModel, headModelFinder),
				IModelExtensions.findModel(sourceDocumentModel, headModelFinder));
		}

		// Body decoration
		Closure<Boolean> bodyModelFinder = new Closure<Boolean>(this, this) {
			public Boolean doCall(Object event) {
				return event instanceof IOpenElementTag
					&& ((IOpenElementTag) event).getElementCompleteName().equals("body");
			}

		};
		IModel targetBodyModel = IModelExtensions.findModel(resultDocumentModel, bodyModelFinder);
		IModel resultBodyModel = new HtmlBodyDecorator((AbstractEngineContext) getContext()).decorate(targetBodyModel,
			IModelExtensions.findModel(sourceDocumentModel, bodyModelFinder));
		if (IModelExtensions.asBoolean(resultBodyModel)) {
			if (IModelExtensions.asBoolean(targetBodyModel)) {
				IModelExtensions.replaceModel(resultDocumentModel,
					IModelExtensions.findIndexOfModel(resultDocumentModel, targetBodyModel), resultBodyModel);
			} else {
				IModelExtensions.insertModelWithWhitespace(resultDocumentModel,
					IModelExtensions.findIndexOf(resultDocumentModel, new Closure<Boolean>(this, this) {
						public Boolean doCall(Object event) {
							return ITemplateEventExtensions.isClosingElementOf((ITemplateEvent) event, "html");
						}

					}) - 1, resultBodyModel, modelFactory);
			}

		}

		return super.decorate(resultDocumentModel, sourceDocumentModel);
	}

	public final SortingStrategy getSortingStrategy() {
		return sortingStrategy;
	}

	public final boolean getAutoHeadMerging() {
		return autoHeadMerging;
	}

	public final boolean isAutoHeadMerging() {
		return autoHeadMerging;
	}

	private final SortingStrategy sortingStrategy;
	private final boolean autoHeadMerging;
}
