/*
 * See LICENSE file in distribution for copyright and licensing
 * information.
 */
package ioke.lang;

import ioke.lang.exceptions.ControlFlow;

/**
 * @author <a href="mailto:ola.bini@gmail.com">Ola Bini</a>
 */
public class ErrorConditionRunnable implements RunnableWithControlFlow {
	private final Runtime		runtime;
	private final IokeObject	condition;

	public ErrorConditionRunnable(Runtime runtime, IokeObject condition) {
		this.runtime = runtime;
		this.condition = condition;
	}

	@Override
	public void run() throws ControlFlow {
		runtime.errorCondition(condition);
	}
}
