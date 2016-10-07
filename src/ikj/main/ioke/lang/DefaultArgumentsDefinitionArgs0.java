/*
 * See LICENSE file in distribution for copyright and licensing
 * information.
 */
package ioke.lang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ioke.lang.exceptions.ControlFlow;

/**
 *
 * @author <a href="mailto:ola.bini@gmail.com">Ola Bini</a>
 */
public class DefaultArgumentsDefinitionArgs0
		implements ArgumentsDefinition {
	@Override
	public void assignArgumentValues(final IokeObject locals,
			final IokeObject context, final IokeObject message,
			final Object on, final Call call) throws ControlFlow {
		if (call.cachedPositional == null) {
			call.cachedArgCount = 0;
			call.cachedPositional = DefaultArgumentsDefinitionArgs1
					.assign(context, message, on, 0);
		}
	}

	@Override
	public void assignArgumentValues(final IokeObject locals,
			final IokeObject context, final IokeObject message,
			final Object on) throws ControlFlow {
		DefaultArgumentsDefinitionArgs1.assign(context, message, on, 0);
	}

	@Override
	public String getCode() {
		return getCode(true);
	}

	@Override
	public String getCode(boolean lastComma) {
		return "";
	}

	@Override
	public Collection<String> getKeywords() {
		return new ArrayList<>();
	}

	@Override
	public List<DefaultArgumentsDefinition.Argument> getArguments() {
		return new ArrayList<>();
	}

	@Override
	public boolean isEmpty() {
		return true;
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
