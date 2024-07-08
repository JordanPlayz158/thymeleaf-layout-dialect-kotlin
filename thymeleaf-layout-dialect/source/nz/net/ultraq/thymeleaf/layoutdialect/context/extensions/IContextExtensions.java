package nz.net.ultraq.thymeleaf.layoutdialect.context.extensions;

import org.thymeleaf.DialectConfiguration;
import org.thymeleaf.context.AbstractEngineContext;
import org.thymeleaf.context.IContext;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.dialect.IProcessorDialect;
import org.thymeleaf.standard.StandardDialect;

/**
 * Meta-programming extensions to the {@link IContext} class.
 *
 * @author Emanuel Rabina
 */
public class IContextExtensions {

	/**
	 * Enables use of the {@code value = context[key]} syntax over the context object, is a synonym
	 * for the {@code getVariable} method.
	 *
	 * @param self
	 * @param name Name of the variable on the context to retrieve.
	 * @return The variable value, or {@code null} if the variable isn't mapped to anything on the
	 * context.
	 */
	public static Object getAt(IContext self, String name) {
		return self.getVariable(name);
	}

	/**
	 * Returns the configured prefix for the given dialect.  If the dialect prefix has not been
	 * configured.
	 *
	 * @param self
	 * @param dialectClass
	 * @return The configured prefix for the dialect, or {@code null} if the dialect being queried
	 * hasn't been configured.
	 */

	// 		return self.getOrCreate(DIALECT_PREFIX_PREFIX + dialectClass.name) { ->
	//			def dialectConfiguration = self.configuration.dialectConfigurations.find { dialectConfig ->
	//				return dialectClass.isInstance(dialectConfig.dialect)
	//			}
	//			return dialectConfiguration?.prefixSpecified ?
	//					dialectConfiguration?.prefix :
	//					dialectConfiguration?.dialect?.prefix
	//		}

	// Was previously final Class<IProcessorDialect> dialectClass
	public static String getPrefixForDialect(final AbstractEngineContext self,
		final Class<? extends IProcessorDialect> dialectClass) {
		String prefix = (String) getAt(self, DIALECT_PREFIX_PREFIX + dialectClass.getName());

		if (prefix == null) {
			DialectConfiguration dialectConfiguration = null;

			for (DialectConfiguration dialectConfig : self.getConfiguration()
				.getDialectConfigurations()) {
				if (dialectClass.isInstance(dialectConfig.getDialect())) {
					dialectConfiguration = dialectConfig;
					break;
				}
			}

			if (dialectConfiguration == null) {
				return null;
			}

			if (dialectConfiguration.isPrefixSpecified()) {
				return dialectConfiguration.getPrefix();
			}

			return ((AbstractProcessorDialect) dialectConfiguration.getDialect()).getPrefix();
		}

		return prefix;
	}

	/**
	 * Enables use of the {@code context[key] = value} syntax over the context object, is a synonym
	 * for the {@code setVariable} method.
	 *
	 * @param self
	 * @param name  Name of the variable to map the value to.
	 * @param value The value to set.
	 */
	public static void putAt(AbstractEngineContext self, String name, Object value) {
		self.setVariable(name, value);
	}

	private static final String DIALECT_PREFIX_PREFIX = "DialectPrefix::";
}
