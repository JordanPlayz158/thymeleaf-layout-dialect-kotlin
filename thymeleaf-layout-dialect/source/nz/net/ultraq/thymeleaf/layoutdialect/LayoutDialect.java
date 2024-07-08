package nz.net.ultraq.thymeleaf.layoutdialect;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import nz.net.ultraq.thymeleaf.layoutdialect.decorators.DecorateProcessor;
import nz.net.ultraq.thymeleaf.layoutdialect.decorators.SortingStrategy;
import nz.net.ultraq.thymeleaf.layoutdialect.decorators.TitlePatternProcessor;
import nz.net.ultraq.thymeleaf.layoutdialect.decorators.strategies.AppendingStrategy;
import nz.net.ultraq.thymeleaf.layoutdialect.fragments.CollectFragmentProcessor;
import nz.net.ultraq.thymeleaf.layoutdialect.fragments.FragmentProcessor;
import nz.net.ultraq.thymeleaf.layoutdialect.includes.IncludeProcessor;
import nz.net.ultraq.thymeleaf.layoutdialect.includes.InsertProcessor;
import nz.net.ultraq.thymeleaf.layoutdialect.includes.ReplaceProcessor;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.standard.processor.StandardXmlNsTagProcessor;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * A dialect for Thymeleaf that lets you build layouts and reusable templates in order to improve
 * code reuse
 *
 * @author Emanuel Rabina
 */
public class LayoutDialect extends AbstractProcessorDialect {

	/**
	 * Constructor, configure the layout dialect.
	 *
	 * @param sortingStrategy
	 * @param autoHeadMerging Experimental option, set to {@code false} to skip the automatic merging
	 *                        of an HTML {@code <head>} section.
	 */
	public LayoutDialect(SortingStrategy sortingStrategy, boolean autoHeadMerging) {

		super(DIALECT_NAME, DIALECT_PREFIX, DIALECT_PRECEDENCE);

		this.sortingStrategy = sortingStrategy;
		this.autoHeadMerging = autoHeadMerging;
	}

	/**
	 * Constructor, configure the layout dialect.
	 *
	 * @param sortingStrategy
	 */
	public LayoutDialect(SortingStrategy sortingStrategy) {
		this(sortingStrategy, true);
	}


	public LayoutDialect() {
		this(new AppendingStrategy(), true);
	}

	/**
	 * Returns the layout dialect's processors.
	 *
	 * @param dialectPrefix
	 * @return All of the processors for HTML and XML template modes.
	 */
	@Override
	public Set<IProcessor> getProcessors(String dialectPrefix) {

		return new HashSet<>(
			Arrays.asList(new StandardXmlNsTagProcessor(TemplateMode.HTML, dialectPrefix),
				new DecorateProcessor(TemplateMode.HTML, dialectPrefix, sortingStrategy,
					autoHeadMerging), new IncludeProcessor(TemplateMode.HTML, dialectPrefix),
				new InsertProcessor(TemplateMode.HTML, dialectPrefix),
				new ReplaceProcessor(TemplateMode.HTML, dialectPrefix),
				new FragmentProcessor(TemplateMode.HTML, dialectPrefix),
				new CollectFragmentProcessor(TemplateMode.HTML, dialectPrefix),
				new TitlePatternProcessor(TemplateMode.HTML, dialectPrefix),
				new StandardXmlNsTagProcessor(TemplateMode.XML, dialectPrefix),
				new DecorateProcessor(TemplateMode.XML, dialectPrefix, sortingStrategy,
					autoHeadMerging), new IncludeProcessor(TemplateMode.XML, dialectPrefix),
				new InsertProcessor(TemplateMode.XML, dialectPrefix),
				new ReplaceProcessor(TemplateMode.XML, dialectPrefix),
				new FragmentProcessor(TemplateMode.XML, dialectPrefix),
				new CollectFragmentProcessor(TemplateMode.XML, dialectPrefix)));
	}

	public static String getDIALECT_NAME() {
		return DIALECT_NAME;
	}

	public static String getDIALECT_PREFIX() {
		return DIALECT_PREFIX;
	}

	public static int getDIALECT_PRECEDENCE() {
		return DIALECT_PRECEDENCE;
	}

	private static final String DIALECT_NAME = "Layout";
	private static final String DIALECT_PREFIX = "layout";
	private static final int DIALECT_PRECEDENCE = 10;
	private final boolean autoHeadMerging;
	private final SortingStrategy sortingStrategy;
}
