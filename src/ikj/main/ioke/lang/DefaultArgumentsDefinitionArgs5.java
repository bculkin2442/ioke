/*
 * See LICENSE file in distribution for copyright and licensing
 * information.
 */
package ioke.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import ioke.lang.exceptions.ControlFlow;

/**
 *
 * @author <a href="mailto:ola.bini@gmail.com">Ola Bini</a>
 */
public class DefaultArgumentsDefinitionArgs5
		implements ArgumentsDefinition {
	private final String	name0;
	private final String	name1;
	private final String	name2;
	private final String	name3;
	private final String	name4;

	public DefaultArgumentsDefinitionArgs5(String name0, String name1,
			String name2, String name3, String name4) {
		this.name0 = name0;
		this.name1 = name1;
		this.name2 = name2;
		this.name3 = name3;
		this.name4 = name4;
	}

	@Override
	public void assignArgumentValues(final IokeObject locals,
			final IokeObject context, final IokeObject message,
			final Object on, final Call call) throws ControlFlow {
		final Runtime runtime = context.runtime;
		if (call.cachedPositional == null) {
			call.cachedArgCount = 5;
			call.cachedPositional = DefaultArgumentsDefinitionArgs1
					.assign(context, message, on, 5);
			locals.setCell(name0, call.cachedPositional.get(0));
			locals.setCell(name1, call.cachedPositional.get(1));
			locals.setCell(name2, call.cachedPositional.get(2));
			locals.setCell(name3, call.cachedPositional.get(3));
			locals.setCell(name4, call.cachedPositional.get(4));
		} else {
			locals.setCell(name0, call.cachedPositional.get(0));
			locals.setCell(name1, call.cachedPositional.get(1));
			locals.setCell(name2, call.cachedPositional.get(2));
			locals.setCell(name3, call.cachedPositional.get(3));
			locals.setCell(name4, call.cachedPositional.get(4));
		}
	}

	@Override
	public void assignArgumentValues(final IokeObject locals,
			final IokeObject context, final IokeObject message,
			final Object on) throws ControlFlow {
		List<Object> result = DefaultArgumentsDefinitionArgs1
				.assign(context, message, on, 5);
		locals.setCell(name0, result.get(0));
		locals.setCell(name1, result.get(1));
		locals.setCell(name2, result.get(2));
		locals.setCell(name3, result.get(3));
		locals.setCell(name4, result.get(4));
	}

	@Override
	public String getCode() {
		return getCode(true);
	}

	@Override
	public String getCode(boolean lastComma) {
		if (lastComma) {
			return name0 + ", " + name1 + ", " + name2 + ", " + name3
					+ ", " + name4 + ", ";
		}
		return name0 + ", " + name1 + ", " + name2 + ", " + name3 + ", "
				+ name4;
	}

	@Override
	public Collection<String> getKeywords() {
		return new ArrayList<>();
	}

	@Override
	public List<DefaultArgumentsDefinition.Argument> getArguments() {
		return Arrays.asList(
				new DefaultArgumentsDefinition.Argument(name0),
				new DefaultArgumentsDefinition.Argument(name1),
				new DefaultArgumentsDefinition.Argument(name2),
				new DefaultArgumentsDefinition.Argument(name3),
				new DefaultArgumentsDefinition.Argument(name4));
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public String getRestName() {
		return null;
	}

	@Override
	public String getKrestName() {
		return null;
	}
}
