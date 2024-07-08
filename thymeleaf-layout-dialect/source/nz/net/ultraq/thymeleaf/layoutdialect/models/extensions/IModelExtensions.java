package nz.net.ultraq.thymeleaf.layoutdialect.models.extensions;

import groovy.lang.Closure;
import java.util.Iterator;
import java.util.List;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.thymeleaf.engine.TemplateModel;
import org.thymeleaf.model.ICloseElementTag;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IOpenElementTag;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.model.ITemplateEvent;
import org.thymeleaf.model.IText;

/**
 * Meta-programming extensions to the {@link IModel} class.
 *
 * @author Emanuel Rabina
 */
public class IModelExtensions {

	/**
	 * Set that a model evaluates to 'false' if it has no events.
	 *
	 * @param self
	 * @return {@code true} if this model has events.
	 */
	public static boolean asBoolean(IModel self) {
		return self.size() > 0;
	}

	/**
	 * If this model represents an element, then this method returns an iterator over any potential
	 * child items as models of their own.
	 *
	 * @param self
	 * @return New model iterator.
	 */
	public static Iterator<IModel> childModelIterator(IModel self) {
		return isElement(self) ? new ChildModelIterator(self) : null;
	}

	/**
	 * Iterate through each event in the model.
	 *
	 * @param self
	 * @param closure
	 */
	public static void each(IModel self, Closure closure) {
		DefaultGroovyMethods.each(iterator(self), closure);
	}

	/**
	 * Compare 2 models, returning {@code true} if all of the model's events are equal.
	 *
	 * @param self
	 * @param other
	 * @return {@code true} if this model is the same as the other one.
	 */
	@SuppressWarnings("EqualsOverloaded")
	public static boolean equals(IModel self, final Object other) {
		if (other instanceof IModel && self.size() == ((IModel) other).size()) {
			return everyWithIndex(self, new Closure<Boolean>(null, null) {
				public Boolean doCall(Object event, Object index) {
					return event.equals(((IModel) other).get((int) index));
				}

			});
		}

		return false;
	}

	/**
	 * Return {@code true} only if all the events in the model return {@code true} for the given
	 * closure.
	 *
	 * @param self
	 * @param closure
	 * @return {@code true} if every event satisfies the closure.
	 */
	public static boolean everyWithIndex(IModel self, Closure<Boolean> closure) {
		for (Integer i = 0; i < self.size(); i++) {
			if (!closure.call(self.get(i), i)) {
				return false;
			}

		}

		return true;
	}

	/**
	 * Returns the first event in the model that meets the criteria of the given closure.
	 *
	 * @param self
	 * @param closure
	 * @return The first event to match the closure criteria, or {@code null} if nothing matched.
	 */
	public static ITemplateEvent find(IModel self, Closure<Boolean> closure) {
		return ((ITemplateEvent) (DefaultGroovyMethods.find(iterator(self), closure)));
	}

	/**
	 * Find all events in the model that match the given closure.
	 *
	 * @param self
	 * @param closure
	 * @return A list of matched events.
	 */
	public static List<ITemplateEvent> findAll(IModel self, Closure<Boolean> closure) {
		return DefaultGroovyMethods.findAll(iterator(self), closure);
	}

	/**
	 * Returns the index of the first event in the model that meets the criteria of the given
	 * closure.
	 *
	 * @param self
	 * @param closure
	 * @return The index of the first event to match the closure criteria, or {@code -1} if nothing
	 * matched.
	 */
	public static int findIndexOf(IModel self, Closure<Boolean> closure) {
		return DefaultGroovyMethods.findIndexOf(iterator(self), closure);
	}

	/**
	 * A special variant of {@code findIndexOf} that uses models, as I seem to be using those a lot.
	 * <p>
	 * This doesn't use an equality check, but an object reference check, so if a submodel is ever
	 * located from a parent (eg: any of the {@code find} methods, you can use this method to find the
	 * location of that submodel within the event queue.
	 *
	 * @param self
	 * @param model
	 * @return Index of an extracted submodel within this model.
	 */
	public static int findIndexOfModel(IModel self, IModel model) {
		final ITemplateEvent modelEvent = first(model);
		return findIndexOf(self, new Closure<Boolean>(null, null) {
			public Boolean doCall(Object event) {
				return DefaultGroovyMethods.is(event, modelEvent);
			}

		});
	}

	/**
	 * Returns the first instance of a model that meets the given closure criteria.
	 *
	 * @param self
	 * @param closure
	 * @return A model over the event that matches the closure criteria, or {@code null} if nothing
	 * matched.
	 */
	public static IModel findModel(IModel self, Closure<Boolean> closure) {
		return getModel(self, findIndexOf(self, closure));
	}

	/**
	 * Returns the first event on the model.
	 *
	 * @param self
	 * @return The model's first event.
	 */
	public static ITemplateEvent first(IModel self) {
		return self.get(0);
	}

	/**
	 * Returns the model at the given index.  If the event at the index is an opening element, then
	 * the returned model will consist of that element and all the way through to the matching closing
	 * element.
	 *
	 * @param self
	 * @param pos  A valid index within the current model.
	 * @return Model at the given position, or `null` if the position is outside of the event queue.
	 */
	public static IModel getModel(IModel self, int pos) {
		if (0 <= pos && pos < self.size()) {
			IModel clone = self.cloneModel();
			int removeBefore = self instanceof TemplateModel ? pos - 1 : pos;
			int removeAfter = clone.size() - (removeBefore + sizeOfModelAt(self, pos));
			while (removeBefore-- > 0) {
				removeFirst(clone);
			}

			while (removeAfter-- > 0) {
				removeLast(clone);
			}

			return ((IModel) (clone));
		}

		return null;
	}

	/**
	 * Inserts a model, creating whitespace events around it so that it appears in line with all the
	 * existing events.
	 * <p>
	 * This is currently only targeting uses in the layout dialect so doesn't work very well as a
	 * general-purpose whitespace generator.
	 *
	 * @param self
	 * @param pos          A valid index within the current model.
	 * @param model
	 * @param modelFactory
	 */
	public static void insertModelWithWhitespace(IModel self, int pos, IModel model,
		IModelFactory modelFactory) {

		if (0 <= pos && pos <= self.size()) {

			// Derive the amount of whitespace to apply by finding the first
			// whitespace event before the insertion point.  Defaults to a single tab.
			String whitespace = "\t";
			if (pos > 0) {
				for (int i = pos - 1; i >= 0; i--) {
					ITemplateEvent event = self.get(i);

					if (!ITemplateEventExtensions.isWhitespace(event)) {
						continue;
					}

					String text = ((IText) event).getText();

					if (!DefaultGroovyMethods.asBoolean(
						text.isEmpty())) {
						whitespace = text.replaceAll(StringGroovyMethods.bitwiseNegate("/[\r\n]/").pattern(), "");
						break;
					}

				}

			}

			// Insert an extra whitespace event for when adding to an immediately-closed
			// element, eg: <div></div>
			if (pos > 0 && self.get(pos - 1) instanceof IOpenElementTag && self.get(
				pos) instanceof ICloseElementTag) {
				self.insertModel(pos,
					modelFactory.createModel(modelFactory.createText(System.lineSeparator())));
			}

			self.insertModel(pos, model);
			self.insertModel(pos,
				modelFactory.createModel(modelFactory.createText(System.lineSeparator() + whitespace)));
		}

	}

	/**
	 * Inserts an event, creating a whitespace event before it so that it appears in line with all the
	 * existing events.
	 *
	 * @param self
	 * @param pos          A valid index within the current model.
	 * @param event
	 * @param modelFactory
	 */
	public static void insertWithWhitespace(IModel self, int pos, ITemplateEvent event,
		IModelFactory modelFactory) {

		if (0 <= pos && pos <= self.size()) {

			// TODO: Because I can't check the parent for whitespace hints, I should
			//       make this smarter and find whitespace within the model to copy.
			IModel whitespace = getModel(self, pos);
			if ((whitespace == null ? null : isWhitespace(whitespace))) {
				self.insert(pos, event);
				self.insertModel(pos, whitespace);
			} else {
				IText newLine = modelFactory.createText("\n");
				if (pos == 0) {
					self.insert(pos, newLine);
					self.insert(pos, event);
				} else if (pos == self.size()) {
					self.insert(pos, newLine);
					self.insert(pos, event);
					self.insert(pos, newLine);
				}

			}

		}

	}

	/**
	 * Returns whether or not this model represents a single HTML element.
	 *
	 * @param self
	 * @return {@code true} if the first event in this model is an opening tag and the last event is
	 * the matching closing tag.
	 */
	public static boolean isElement(IModel self) {
		return sizeOfModelAt(self, 0) == self.size();
	}

	/**
	 * Returns whether or not this model represents collapsible whitespace.
	 *
	 * @param self
	 * @return {@code true} if this is a collapsible text model.
	 */
	public static boolean isWhitespace(IModel self) {
		return self.size() == 1 && ITemplateEventExtensions.isWhitespace(first(self));
	}

	/**
	 * Used to make this class iterable as an event queue.
	 *
	 * @param self
	 * @return A new iterator over the events of this model.
	 */
	public static Iterator<ITemplateEvent> iterator(IModel self) {
		return new EventIterator(self);
	}

	/**
	 * Returns the last event on the model.
	 *
	 * @param self
	 * @return The model's last event.
	 */
	@SuppressWarnings("UnnecessaryCallForLastElement")
	public static ITemplateEvent last(IModel self) {
		return self.get(self.size() - 1);
	}

	/**
	 * Remove a model identified by an event matched by the given closure.  Note that this closure can
	 * match any event in the model, including the top-level model itself.
	 *
	 * @param self
	 * @param closure
	 */
	public static void removeAllModels(IModel self, Closure<Boolean> closure) {
		while (true) {
			int modelIndex = findIndexOf(self, closure);
			if (modelIndex == -1) {
				return;

			}

			removeModel(self, modelIndex);
		}

	}

	/**
	 * If the model represents an element open to close tags, then this method removes all of the
	 * inner events.
	 *
	 * @param self
	 */
	public static void removeChildren(IModel self) {
		if (isElement(self)) {
			while (self.size() > 2) {
				self.remove(1);
			}

		}

	}

	/**
	 * Removes the first event on the model.
	 *
	 * @param self
	 */
	public static void removeFirst(IModel self) {
		self.remove(0);
	}

	/**
	 * Removes the last event on the model.
	 *
	 * @param self
	 */
	public static void removeLast(IModel self) {
		self.remove(self.size() - 1);
	}

	/**
	 * Removes a models-worth of events from the specified position.  What this means is that, if the
	 * event at the position is an opening element, then it, and everything up to and including its
	 * matching end element, is removed.
	 *
	 * @param self
	 * @param pos  A valid index within the current model.
	 */
	public static void removeModel(IModel self, int pos) {
		if (0 <= pos && pos < self.size()) {
			int modelSize = sizeOfModelAt(self, pos);
			while (modelSize > 0) {
				self.remove(pos);
				modelSize--;
			}

		}

	}

	/**
	 * Replaces the model at the specified index with the given model.
	 *
	 * @param self
	 * @param pos   A valid index within the current model.
	 * @param model
	 */
	public static void replaceModel(IModel self, int pos, IModel model) {
		if (0 <= pos && pos < self.size()) {
			removeModel(self, pos);
			self.insertModel(pos, model);
		}

	}

	/**
	 * If an opening element exists at the given position, this method will return the 'size' of that
	 * element (number of events from here to its matching closing tag).
	 *
	 * @param self
	 * @param index
	 * @return Size of an element from the given position, or 1 if the event at the position isn't an
	 * opening element.
	 */
	@SuppressWarnings("EmptyIfStatement")
	public static int sizeOfModelAt(IModel self, int index) {

		int eventIndex = index;
		ITemplateEvent event = self.get(eventIndex++);

		if (event instanceof IOpenElementTag) {
			Integer level = 0;
			while (true) {
				event = self.get(eventIndex++);
				if (event instanceof IOpenElementTag) {
					level++;
				} else if (event instanceof ICloseElementTag) {
					if (((ICloseElementTag) event).isUnmatched()) {
						// Do nothing.  Unmatched closing tags do not correspond to any
						// opening element, and so should not affect the model level.
					} else if (level == 0) {
						break;
					} else {
						level--;
					}

				}

			}

			return eventIndex - index;
		}

		return 1;
	}

	/**
	 * Removes whitespace events from the head and tail of the model's underlying event queue.
	 *
	 * @param self
	 */
	public static void trim(IModel self) {
		while (ITemplateEventExtensions.isWhitespace(first(self))) {
			removeFirst(self);
		}

		while (ITemplateEventExtensions.isWhitespace(last(self))) {
			removeLast(self);
		}

	}

	/**
	 *
	 */
	public static IModel cloneModel(IModel target, IModel source) {
		if (target != null) {
			return target.cloneModel();
		} else if (source != null) {
			return source.cloneModel();
		}

		return null;
	}

}
