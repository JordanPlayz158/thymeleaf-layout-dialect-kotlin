package nz.net.ultraq.thymeleaf.layoutdialect.models.extensions;

import java.util.Iterator;
import org.thymeleaf.model.IModel;

/**
 * An iterator that works with a model's immediate children, returning each one as a model of its
 * own.
 *
 * @author Emanuel Rabina
 */
public class ChildModelIterator implements Iterator<IModel> {

	public ChildModelIterator(IModel parent) {
		this.parent = parent;
	}

	@Override
	public boolean hasNext() {
		return currentIndex < (parent.size() - 1);
	}

	@Override
	public IModel next() {
		IModel subModel = IModelExtensions.getModel(parent, currentIndex);
		currentIndex += subModel.size();
		return subModel;
	}

	private final IModel parent;
	private int currentIndex = 1;
}
