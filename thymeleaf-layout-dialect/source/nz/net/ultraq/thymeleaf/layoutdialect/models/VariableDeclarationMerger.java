package nz.net.ultraq.thymeleaf.layoutdialect.models;

import groovy.lang.Closure;
import java.util.ArrayList;
import java.util.List;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.thymeleaf.context.IExpressionContext;

/**
 * Merges variable declarations in a {@code th:with} attribute processor, taking the declarations in
 * the target and combining them with the declarations in the source, overriding any same-named
 * declarations in the target.
 *
 * @author Emanuel Rabina
 */
public class VariableDeclarationMerger {

	/**
	 * Merge {@code th:with} attributes so that names from the source value overwrite the same names
	 * in the target value.
	 */
	public String merge(String target, String source) {

		if (!StringGroovyMethods.asBoolean(target)) {
			return source;
		}

		if (!StringGroovyMethods.asBoolean(source)) {
			return target;
		}

		VariableDeclarationParser declarationParser = new VariableDeclarationParser(context);
		List<VariableDeclaration> targetDeclarations = declarationParser.parse(target);
		final List<VariableDeclaration> sourceDeclarations = declarationParser.parse(source);

		final ArrayList<VariableDeclaration> newDeclarations = new ArrayList<>();
		DefaultGroovyMethods.each(targetDeclarations,
			new Closure<List<VariableDeclaration>>(this, this) {
				public List<VariableDeclaration> doCall(final Object targetDeclaration) {
					VariableDeclaration override = DefaultGroovyMethods.find(sourceDeclarations,
						new Closure<Boolean>(VariableDeclarationMerger.this,
							VariableDeclarationMerger.this) {
							public Boolean doCall(Object sourceDeclaration) {
								return ((VariableDeclaration) sourceDeclaration).getName()
									.equals(((VariableDeclaration) targetDeclaration).getName());
							}

						});
					if (DefaultGroovyMethods.asBoolean(override)) {
						sourceDeclarations.remove(override);
						return DefaultGroovyMethods.leftShift(newDeclarations, override);
					} else {
						return DefaultGroovyMethods.leftShift(newDeclarations,
							(VariableDeclaration) targetDeclaration);
					}

				}

			});
		DefaultGroovyMethods.each(sourceDeclarations,
			new Closure<List<VariableDeclaration>>(this, this) {
				public List<VariableDeclaration> doCall(Object targetAttributeDeclaration) {
					return DefaultGroovyMethods.leftShift(newDeclarations,
						(VariableDeclaration) targetAttributeDeclaration);
				}

			});

		return DefaultGroovyMethods.join(newDeclarations, ",");
	}

	public final IExpressionContext getContext() {
		return context;
	}

	public VariableDeclarationMerger(IExpressionContext context) {
		this.context = context;
	}

	private final IExpressionContext context;
}
