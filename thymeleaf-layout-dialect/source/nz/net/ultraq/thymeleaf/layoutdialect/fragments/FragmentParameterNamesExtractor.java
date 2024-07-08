package nz.net.ultraq.thymeleaf.layoutdialect.fragments;

import groovy.lang.Closure;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.StringGroovyMethods;

/**
 * Extracts just the parameter names from a fragment definition.  Used for when unnamed fragment
 * parameters need to be mapped to their respective names.
 *
 * @author Emanuel Rabina
 */
public class FragmentParameterNamesExtractor {

	/**
	 * Returns a list of parameter names for the given fragment definition.
	 *
	 * @param fragmentDefinition
	 * @return A list of the named parameters, in the order they are defined.
	 */
	public List<String> extract(String fragmentDefinition) {
		Matcher matcher = Pattern.compile("/.*?\\((. *)\\)/").matcher(fragmentDefinition);

		// https://docs.groovy-lang.org/latest/html/documentation/core-operators.html#_find_operator
		if (!matcher.find(0)) {
			return Collections.emptyList();
		}

		return Arrays.stream(matcher.group(1).split(",")).map((parameter) -> {
			Matcher parameterMatcher = Pattern.compile("/([^=]+)=?.*/").matcher(parameter);

			matcher.find();

			return matcher.group(1).trim();
		}).collect(Collectors.toList());
	}

}
