package nz.net.ultraq.thymeleaf.layoutdialect.decorators.html;

import groovy.lang.Closure;
import java.util.LinkedHashMap;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import nz.net.ultraq.thymeleaf.layoutdialect.context.extensions.IContextExtensions;
import nz.net.ultraq.thymeleaf.layoutdialect.decorators.Decorator;
import nz.net.ultraq.thymeleaf.layoutdialect.decorators.TitlePatternProcessor;
import nz.net.ultraq.thymeleaf.layoutdialect.models.ElementMerger;
import nz.net.ultraq.thymeleaf.layoutdialect.models.ModelBuilder;
import nz.net.ultraq.thymeleaf.layoutdialect.models.extensions.ChildModelIterator;
import nz.net.ultraq.thymeleaf.layoutdialect.models.extensions.IModelExtensions;
import nz.net.ultraq.thymeleaf.layoutdialect.models.extensions.IProcessableElementTagExtensions;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.thymeleaf.context.AbstractEngineContext;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.dialect.IProcessorDialect;
import org.thymeleaf.model.IAttribute;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.standard.processor.StandardTextTagProcessor;
import org.thymeleaf.standard.processor.StandardUtextTagProcessor;

/**
 * Decorator for the {@code <title>} part of the template to handle the special processing required
 * for the {@code layout:title-pattern} processor.
 *
 * @author Emanuel Rabina
 */
public class HtmlTitleDecorator implements Decorator {

	private final AbstractEngineContext context;

	public HtmlTitleDecorator(ITemplateContext context) {
		this.context = (AbstractEngineContext) context;
	}


	/**
	 * Special decorator for the {@code <title>} part, accumulates the important processing parts for
	 * the {@code layout:title-pattern} processor.
	 *
	 * @param targetTitleModel
	 * @param sourceTitleModel
	 * @return A new {@code <title>} model that is the result of decorating the {@code <title>}s.
	 */
	@Override
	@SuppressWarnings("SpaceAroundOperator")
	public IModel decorate(IModel targetTitleModel, IModel sourceTitleModel) {

		final ModelBuilder modelBuilder = new ModelBuilder(context);

		final String layoutDialectPrefix = IContextExtensions.getPrefixForDialect(context, LayoutDialect.class);
		final String standardDialectPrefix = IContextExtensions.getPrefixForDialect(context, StandardDialect.class);

		// Get the title pattern to use
		Closure<IAttribute> titlePatternProcessorRetriever = new Closure<IAttribute>(this, this) {
			public IAttribute doCall(IModel titleModel) {
				if (titleModel == null) {
					return null;
				}

				IProcessableElementTag tag = (IProcessableElementTag) IModelExtensions.first(titleModel);

				if (tag == null) {
					return null;
				}


				return tag.getAttribute(layoutDialectPrefix, TitlePatternProcessor.getPROCESSOR_NAME());
			}
		};
		final IAttribute retriever = titlePatternProcessorRetriever.call(sourceTitleModel);
		final IAttribute retriever1 = titlePatternProcessorRetriever.call(targetTitleModel);
		final IAttribute titlePatternProcessor = DefaultGroovyMethods.asBoolean(retriever) ? retriever
			: DefaultGroovyMethods.asBoolean(retriever1) ? retriever1 : null;

		Object resultTitle;

		// Set the title pattern to use on a new model, as well as the important
		// title result parts that we want to use on the pattern.
		if (DefaultGroovyMethods.asBoolean(titlePatternProcessor)) {
			Closure<IModel> extractTitle = new Closure<IModel>(this, this) {
				public IModel doCall(IModel titleModel, String contextKey) {
					// This title part already exists from a previous run, so do nothing
					if (DefaultGroovyMethods.asBoolean(
						IContextExtensions.getAt(getContext(), contextKey))) {
						return null;
					}

					if (IModelExtensions.asBoolean(titleModel)) {
						final IProcessableElementTag titleTag = (IProcessableElementTag) IModelExtensions.first(titleModel);

						// Escapable title from a th:text attribute on the title tag
						if (titleTag.hasAttribute(standardDialectPrefix, StandardTextTagProcessor.ATTR_NAME)) {
							return putAt0(getContext(), contextKey,
								modelBuilder.build(new Closure(HtmlTitleDecorator.this, HtmlTitleDecorator.this) {
									public Object doCall(Object it) {
										LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>(1);
										map.put("th:text", titleTag.getAttributeValue(standardDialectPrefix, StandardTextTagProcessor.ATTR_NAME));
										return invokeMethod("th:block", new Object[]{map});
									}

									public Object doCall() {
										return doCall(null);
									}

								}));
						} else if (titleTag.hasAttribute(standardDialectPrefix, StandardUtextTagProcessor.ATTR_NAME)) {
							return putAt0(getContext(), (contextKey),
								modelBuilder.build(new Closure(HtmlTitleDecorator.this, HtmlTitleDecorator.this) {
									public Object doCall(Object it) {
										LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>(1);
										map.put("th:utext", titleTag.getAttributeValue(standardDialectPrefix,
												StandardUtextTagProcessor.ATTR_NAME));
										return invokeMethod("th:block", new Object[]{map});
									}

									public Object doCall() {
										return doCall(null);
									}

								}));
						} else {
							final IModel titleChildrenModel = getContext().getModelFactory().createModel();

							IModelExtensions.childModelIterator(titleModel).forEachRemaining(titleChildrenModel::addModel);
							DefaultGroovyMethods.putAt(getContext(), contextKey, titleChildrenModel);
							return titleChildrenModel;
						}

					}

					return null;
				}

			};
			extractTitle.call(sourceTitleModel, TitlePatternProcessor.getCONTENT_TITLE_KEY());
			extractTitle.call(targetTitleModel, TitlePatternProcessor.getLAYOUT_TITLE_KEY());

			resultTitle = modelBuilder.build(new Closure(this, this) {
				public Object doCall(Object it) {
					LinkedHashMap map = new LinkedHashMap(1);
					map.put(titlePatternProcessor.getAttributeCompleteName(), titlePatternProcessor.getValue());
					return invokeMethod("title", new Object[]{map});
				}

				public Object doCall() {
					return doCall(null);
				}

			});
		} else {
			resultTitle = new ElementMerger(context).merge(targetTitleModel, sourceTitleModel);
		}

		return ((IModel) (resultTitle));
	}

	public final ITemplateContext getContext() {
		return context;
	}

	private static <Value> Value putAt0(Object propOwner, String property, Value newValue) {
		DefaultGroovyMethods.putAt(propOwner, property, newValue);
		return newValue;
	}
}
