/*
 * See LICENSE file in distribution for copyright and licensing
 * information.
 */
package ioke.lang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ioke.lang.exceptions.ControlFlow;

/**
 *
 * @author <a href="mailto:ola.bini@gmail.com">Ola Bini</a>
 */
public class Hook extends IokeData {
	private List<IokeObject> connected;

	public Hook(List<IokeObject> connected) {
		this.connected = connected;
	}

	private void rewire(IokeObject self) {
		for (IokeObject io : connected) {
			if (io.body.hooks == null) {
				io.body.hooks = new LinkedList<>();
			}
			if (!io.body.hooks.contains(self)) {
				io.body.hooks.add(self);
			}
		}
	}

	public static void fireCellAdded(IokeObject on, IokeObject message,
			IokeObject context, String name) throws ControlFlow {
		Collection<IokeObject> hooks = on.body.hooks;
		if (hooks != null) {
			IokeObject sym = context.runtime.getSymbol(name);
			IokeObject cellAddedMessage = context.runtime.cellAddedMessage;
			for (IokeObject h : hooks) {
				Interpreter.send(cellAddedMessage, context, h, on, sym);
			}
		}
	}

	public static void fireCellChanged(IokeObject on, IokeObject message,
			IokeObject context, String name, Object prevValue)
			throws ControlFlow {
		Collection<IokeObject> hooks = on.body.hooks;
		if (hooks != null) {
			IokeObject sym = context.runtime.getSymbol(name);
			IokeObject cellChangedMessage = context.runtime.cellChangedMessage;
			for (IokeObject h : hooks) {
				Interpreter.send(cellChangedMessage, context, h, on, sym,
						prevValue);
			}
		}
	}

	public static void fireCellRemoved(IokeObject on, IokeObject message,
			IokeObject context, String name, Object prevValue)
			throws ControlFlow {
		Collection<IokeObject> hooks = on.body.hooks;
		if (hooks != null) {
			IokeObject sym = context.runtime.getSymbol(name);
			IokeObject cellRemovedMessage = context.runtime.cellRemovedMessage;
			for (IokeObject h : hooks) {
				Interpreter.send(cellRemovedMessage, context, h, on, sym,
						prevValue);
			}
		}
	}

	public static void fireCellUndefined(IokeObject on, IokeObject message,
			IokeObject context, String name, Object prevValue)
			throws ControlFlow {
		Collection<IokeObject> hooks = on.body.hooks;
		if (hooks != null) {
			IokeObject sym = context.runtime.getSymbol(name);
			IokeObject cellUndefinedMessage = context.runtime.cellUndefinedMessage;
			for (IokeObject h : hooks) {
				Interpreter.send(cellUndefinedMessage, context, h, on, sym,
						prevValue);
			}
		}
	}

	public static void fireMimicAdded(IokeObject on, IokeObject message,
			IokeObject context, IokeObject newMimic) throws ControlFlow {
		Collection<IokeObject> hooks = on.body.hooks;
		if (hooks != null) {
			IokeObject mimicAddedMessage = context.runtime.mimicAddedMessage;
			for (IokeObject h : hooks) {
				Interpreter.send(mimicAddedMessage, context, h, on,
						newMimic);
			}
		}
	}

	public static void fireMimicRemoved(IokeObject on, IokeObject message,
			IokeObject context, Object removedMimic) throws ControlFlow {
		Collection<IokeObject> hooks = on.body.hooks;
		if (hooks != null) {
			IokeObject mimicRemovedMessage = context.runtime.mimicRemovedMessage;
			for (IokeObject h : hooks) {
				Interpreter.send(mimicRemovedMessage, context, h, on,
						removedMimic);
			}
		}
	}

	public static void fireMimicsChanged(IokeObject on, IokeObject message,
			IokeObject context, Object changedMimic) throws ControlFlow {
		Collection<IokeObject> hooks = on.body.hooks;
		if (hooks != null) {
			IokeObject mimicsChangedMessage = context.runtime.mimicsChangedMessage;
			for (IokeObject h : hooks) {
				Interpreter.send(mimicsChangedMessage, context, h, on,
						changedMimic);
			}
		}
	}

	public static void fireMimicked(IokeObject on, IokeObject message,
			IokeObject context, IokeObject mimickingObject)
			throws ControlFlow {
		Collection<IokeObject> hooks = on.body.hooks;
		if (hooks != null) {
			IokeObject mimickedMessage = context.runtime.mimickedMessage;
			for (IokeObject h : hooks) {
				Interpreter.send(mimickedMessage, context, h, on,
						mimickingObject);
			}
		}
	}

	public static void init(final Runtime runtime) throws ControlFlow {
		final IokeObject obj = new IokeObject(runtime,
				"A hook allow you to observe what happens to a specific object. All hooks have Hook in their mimic chain.",
				new Hook(new ArrayList<IokeObject>()));
		obj.setKind("Hook");
		obj.singleMimicsWithoutCheck(runtime.origin);
		runtime.iokeGround.registerCell("Hook", obj);

		obj.registerMethod(runtime.newNativeMethod(
				"Takes one or more arguments to hook into and returns a new Hook connected to them.",
				new NativeMethod("into") {
					private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
							.builder()
							.withRequiredPositional("firstConnected")
							.withRest("restConnected").getArguments();

					@Override
					public DefaultArgumentsDefinition getArguments() {
						return ARGUMENTS;
					}

					@Override
					public Object activate(IokeObject method,
							IokeObject context, IokeObject message,
							Object on) throws ControlFlow {
						List<Object> args = new ArrayList<>();
						getArguments().getEvaluatedArguments(context,
								message, on, args,
								new HashMap<String, Object>());
						IokeObject hook = obj.allocateCopy(context,
								message);
						hook.singleMimicsWithoutCheck(obj);

						List<IokeObject> objs = new ArrayList<>(
								args.size());
						for (Object o : args) {
							objs.add(IokeObject.as(o, context));
						}
						Hook h = new Hook(objs);
						hook.setData(h);
						h.rewire(hook);
						return hook;
					}
				}));

		obj.registerMethod(runtime.newNativeMethod(
				"returns the objects this hook is connected to",
				new TypeCheckingNativeMethod.WithNoArguments(
						"connectedObjects", obj) {
					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						Hook h = (Hook) IokeObject.data(on);
						List l = new ArrayList<Object>(h.connected);
						return method.runtime.newList(l);
					}
				}));

		obj.registerMethod(runtime.newNativeMethod(
				"Takes one argument and will add that to the list of connected objects",
				new NativeMethod("hook!") {
					private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
							.builder()
							.withRequiredPositional("objectToHookInto")
							.getArguments();

					@Override
					public DefaultArgumentsDefinition getArguments() {
						return ARGUMENTS;
					}

					@Override
					public Object activate(IokeObject method,
							IokeObject context, IokeObject message,
							Object on) throws ControlFlow {
						List<Object> args = new ArrayList<>();
						getArguments().getEvaluatedArguments(context,
								message, on, args,
								new HashMap<String, Object>());
						Hook h = (Hook) IokeObject.data(on);
						h.connected
								.add(IokeObject.as(args.get(0), context));
						h.rewire(IokeObject.as(on, context));
						return on;
					}
				}));
	}

	@Override
	public IokeData cloneData(IokeObject obj, IokeObject m,
			IokeObject context) {
		return new Hook(new ArrayList<>(connected));
	}
}// Hook
