package nz.net.ultraq.thymeleaf.layoutdialect.models;

import groovy.lang.Closure;
import java.util.List;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.standard.expression.Assignation;
import org.thymeleaf.standard.expression.AssignationSequence;
import org.thymeleaf.standard.expression.AssignationUtils;

/**
 * Parser for variable declaration strings, which are the expressions that are found withing
 * {@code th:with} processors.  This is really a wrapper around Thymeleaf's {@link AssignationUtils}
 * class, which is a crazy house of code that splits the expression string into the parts needed by
 * this dialect.
 *
 * @author Emanuel Rabina
 */
public class VariableDeclarationParser {

	/**
	 * Parse a variable declaration string, returning as many variable declaration objects as there
	 * are variable declarations.
	 *
	 * @param declarationString
	 * @return List of variable declaration objects.
	 */
	public List<VariableDeclaration> parse(String declarationString) {

		AssignationSequence assignationSequence = AssignationUtils.parseAssignationSequence(context,
			declarationString, false);
		return DefaultGroovyMethods.collect(assignationSequence,
			new Closure<VariableDeclaration>(this, this) {
				public VariableDeclaration doCall(Object assignation) {
					return new VariableDeclaration((Assignation) assignation);
				}

			});
	}

	public final IExpressionContext getContext() {
		return context;
	}

	public VariableDeclarationParser(IExpressionContext context) {
		this.context = context;
	}

	private final IExpressionContext context;
}
