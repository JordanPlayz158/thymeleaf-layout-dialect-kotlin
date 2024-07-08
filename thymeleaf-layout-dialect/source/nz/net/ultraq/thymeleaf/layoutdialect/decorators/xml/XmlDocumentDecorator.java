package nz.net.ultraq.thymeleaf.layoutdialect.decorators.xml;

import groovy.lang.Closure;
import nz.net.ultraq.thymeleaf.layoutdialect.decorators.Decorator;
import nz.net.ultraq.thymeleaf.layoutdialect.models.AttributeMerger;
import nz.net.ultraq.thymeleaf.layoutdialect.models.extensions.IModelExtensions;
import org.thymeleaf.context.AbstractEngineContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.ICloseElementTag;
import org.thymeleaf.model.IComment;
import org.thymeleaf.model.IDocType;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IOpenElementTag;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.model.ITemplateEvent;

/**
 * A decorator made to work over an XML document.
 *
 * @author Emanuel Rabina
 */
public class XmlDocumentDecorator implements Decorator {
	private final ITemplateContext context;

	public XmlDocumentDecorator(ITemplateContext context) {
		this.context = context;
	}

	/**
	 * Decorates the target XML document with the source one.
	 *
	 * @param targetDocumentModel
	 * @param sourceDocumentModel
	 * @return Result of the decoration.
	 */
	@Override
	public IModel decorate(IModel targetDocumentModel, IModel sourceDocumentModel) {

		IModelFactory modelFactory = context.getModelFactory();

		// Find the root element of each document to work with
		Closure<IModel> rootModelFinder = new Closure<IModel>(this, this) {
			public IModel doCall(IModel documentModel) {
				return IModelExtensions.findModel(documentModel,
					new Closure<Boolean>(this, this) {
						public Boolean doCall(Object documentEvent) {
							return documentEvent instanceof IProcessableElementTag;
						}
					});
			}

		};

		// Decorate the target document with the source one
		IModel resultDocumentModel = new AttributeMerger((AbstractEngineContext) context).merge(
			rootModelFinder.call(targetDocumentModel), rootModelFinder.call(sourceDocumentModel));

		Closure<Boolean> documentContainsDocType = new Closure<Boolean>(this, this) {
			public Boolean doCall(IModel document) {
				for (int i = 0; i < document.size(); i++) {
					ITemplateEvent event = document.get(i);
					if (event instanceof IDocType) {
						return true;
					}

					if (event instanceof IOpenElementTag) {
						break;
					}

				}

				return false;
			}

		};

		// Copy certain items outside of the root element
		for (int i = 0; i < targetDocumentModel.size(); i++) {
			ITemplateEvent event = targetDocumentModel.get(i);

			// Only copy doctypes if the source document doesn't already have one
			if (event instanceof IDocType) {
				if (!documentContainsDocType.call(sourceDocumentModel)) {
					IModelExtensions.insertWithWhitespace(resultDocumentModel, 0, event, modelFactory);
				}

			} else if (event instanceof IComment) {
				IModelExtensions.insertWithWhitespace(resultDocumentModel, 0, event, modelFactory);
			} else if (event instanceof IOpenElementTag) {
				break;
			}

		}

		for (int i = targetDocumentModel.size() - 1; i >= 0; i--) {
			ITemplateEvent event = targetDocumentModel.get(i);
			if (event instanceof IComment) {
				IModelExtensions.insertWithWhitespace(resultDocumentModel, resultDocumentModel.size(),
					event, modelFactory);
			} else if (event instanceof ICloseElementTag) {
				break;
			}

		}

		return ((IModel) (resultDocumentModel));
	}

	public final ITemplateContext getContext() {
		return context;
	}
}
