package nz.net.ultraq.thymeleaf.layoutdialect.models;

import org.thymeleaf.standard.expression.Assignation;

/**
 * Representation of a scoped variable declaration made through {@code th:with} attributes.  This is
 * really a wrapper around Thymeleaf's {@link Assignation} class, but simplified to just the left
 * and right hand components in string form.
 *
 * @author Emanuel Rabina
 */
public class VariableDeclaration {

	/**
	 * Constructor, create an instance from a Thymeleaf assignation.
	 *
	 * @param assignation
	 */
	public VariableDeclaration(Assignation assignation) {
		String[] nameAndValue = assignation.getStringRepresentation().split("=");
		this.name = nameAndValue[0];
		this.value = nameAndValue[1];
	}

	/**
	 * Reconstructs the variable for use with {@code th:with}.
	 *
	 * @return {name}=${value}
	 */
	@Override
	public String toString() {

		return getName() + "=" + getValue();
	}

	public final String getName() {
		return name;
	}

	public final String getValue() {
		return value;
	}

	private final String name;
	private final String value;
}
