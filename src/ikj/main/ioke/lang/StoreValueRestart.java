/*
 * See LICENSE file in distribution for copyright and licensing
 * information.
 */
package ioke.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ioke.lang.exceptions.ControlFlow;

/**
 * @author <a href="mailto:ola.bini@gmail.com">Ola Bini</a>
 */
public class StoreValueRestart extends Restart.ArgumentGivingRestart {
	private final String		name;
	private final Object[]		newCell;
	private final IokeObject	recv;

	public StoreValueRestart(String name, Object[] newCell,
			IokeObject recv) {
		super("useValue");
		this.name = name;
		this.newCell = newCell;
		this.recv = recv;
	}

	@Override
	public String report() {
		return "Store value for: " + name;
	}

	@Override
	public List<String> getArgumentNames() {
		return new ArrayList<>(Arrays.asList("newValue"));
	}

	@Override
	public IokeObject invoke(IokeObject context, List<Object> arguments)
			throws ControlFlow {
		newCell[0] = arguments.get(0);
		recv.setCell(name, newCell[0]);
		return context.runtime.nil;
	}
}
