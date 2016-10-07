/*
 * See LICENSE file in distribution for copyright and licensing
 * information.
 */
package ioke.lang;

import java.util.Collection;
import java.util.List;

import ioke.lang.exceptions.ControlFlow;

/**
 *
 * @author <a href="mailto:ola.bini@gmail.com">Ola Bini</a>
 */
public interface ArgumentsDefinition {
	void assignArgumentValues(final IokeObject locals,
			final IokeObject context, final IokeObject message,
			final Object on, final Call call) throws ControlFlow;

	void assignArgumentValues(final IokeObject locals,
			final IokeObject context, final IokeObject message,
			final Object on) throws ControlFlow;

	String getCode();

	String getCode(boolean lastComma);

	Collection<String> getKeywords();

	List<DefaultArgumentsDefinition.Argument> getArguments();

	boolean isEmpty();

	String getRestName();

	String getKrestName();
}
