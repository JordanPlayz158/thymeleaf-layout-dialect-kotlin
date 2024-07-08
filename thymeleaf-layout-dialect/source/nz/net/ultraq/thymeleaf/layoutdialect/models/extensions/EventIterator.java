package nz.net.ultraq.thymeleaf.layoutdialect.models.extensions;

import java.util.Iterator;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.ITemplateEvent;

/**
 * An iterator that treats a model as a queue of events.
 */
public class EventIterator implements Iterator<ITemplateEvent> {

	public EventIterator(IModel model) {
		this.model = model;
	}

	@Override
	public boolean hasNext() {
		return currentIndex < model.size();
	}

	@Override
	public ITemplateEvent next() {
		return model.get(currentIndex++);
	}

	private final IModel model;
	private int currentIndex = 0;
}
