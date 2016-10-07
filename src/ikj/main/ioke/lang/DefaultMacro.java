/*
 * See LICENSE file in distribution for copyright and licensing
 * information.
 */
package ioke.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ioke.lang.exceptions.ControlFlow;

/**
 *
 * @author <a href="mailto:ola.bini@gmail.com">Ola Bini</a>
 */
public class DefaultMacro extends IokeData
		implements Named, Inspectable, AssociatedCode {
	String				name;
	private IokeObject	code;

	public DefaultMacro(String name) {
		super(IokeData.TYPE_DEFAULT_MACRO);
		this.name = name;
	}

	public DefaultMacro(IokeObject context, IokeObject code) {
		this((String) null);

		this.code = code;
	}

	@Override
	public IokeObject getCode() {
		return code;
	}

	public String getCodeString() {
		return "macro(" + Message.code(code) + ")";

	}

	@Override
	public String getFormattedCode(Object self) throws ControlFlow {
		return "macro(\n  "
				+ Message.formattedCode(code, 2, (IokeObject) self) + ")";
	}

	@Override
	public void init(IokeObject macro) throws ControlFlow {
		macro.setKind("DefaultMacro");
		macro.setActivatable(true);

		macro.registerMethod(macro.runtime.newNativeMethod(
				"returns the name of the macro",
				new TypeCheckingNativeMethod.WithNoArguments("name",
						macro) {
					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						return context.runtime.newText(
								((DefaultMacro) IokeObject.data(on)).name);
					}
				}));

		macro.registerMethod(macro.runtime.newNativeMethod(
				"activates this macro with the arguments given to call",
				new NativeMethod("call") {
					private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
							.builder().withRestUnevaluated("arguments")
							.getArguments();

					@Override
					public DefaultArgumentsDefinition getArguments() {
						return ARGUMENTS;
					}

					@Override
					public Object activate(IokeObject self,
							IokeObject context, IokeObject message,
							Object on) throws ControlFlow {
						return Interpreter.activate(
								IokeObject.as(on, context), context,
								message, context.getRealContext());
					}
				}));

		macro.registerMethod(macro.runtime.newNativeMethod(
				"returns the message chain for this macro",
				new TypeCheckingNativeMethod.WithNoArguments("message",
						macro) {
					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						return ((AssociatedCode) IokeObject.data(on))
								.getCode();
					}
				}));

		macro.registerMethod(macro.runtime.newNativeMethod(
				"returns the code for the argument definition",
				new TypeCheckingNativeMethod.WithNoArguments(
						"argumentsCode", macro) {
					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						return context.runtime.newText(
								((AssociatedCode) IokeObject.data(on))
										.getArgumentsCode());
					}
				}));

		macro.registerMethod(macro.runtime.newNativeMethod(
				"Returns a text inspection of the object",
				new TypeCheckingNativeMethod.WithNoArguments("inspect",
						macro) {
					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						return context.runtime
								.newText(DefaultMacro.getInspect(on));
					}
				}));

		macro.registerMethod(macro.runtime.newNativeMethod(
				"Returns a brief text inspection of the object",
				new TypeCheckingNativeMethod.WithNoArguments("notice",
						macro) {
					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						return context.runtime
								.newText(DefaultMacro.getNotice(on));
					}
				}));

		macro.registerMethod(macro.runtime.newNativeMethod(
				"returns the full code of this macro, as a Text",
				new TypeCheckingNativeMethod.WithNoArguments("code",
						macro) {
					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						IokeData data = IokeObject.data(on);

						if (data instanceof DefaultMacro) {
							return context.runtime.newText(
									((DefaultMacro) data).getCodeString());
						}
						return context.runtime.newText(
								((AliasMethod) data).getCodeString());
					}
				}));

		macro.registerMethod(macro.runtime.newNativeMethod(
				"returns idiomatically formatted code for this macro",
				new TypeCheckingNativeMethod.WithNoArguments(
						"formattedCode", macro) {
					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						return context.runtime.newText(
								((AssociatedCode) IokeObject.data(on))
										.getFormattedCode(method));
					}
				}));
	}

	@Override
	public String getArgumentsCode() {
		return "...";
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public static String getInspect(Object on) {
		return ((Inspectable) (IokeObject.data(on))).inspect(on);
	}

	public static String getNotice(Object on) {
		return ((Inspectable) (IokeObject.data(on))).notice(on);
	}

	@Override
	public String inspect(Object self) {
		if (name == null) {
			return "macro(" + Message.code(code) + ")";
		}
		return name + ":macro(" + Message.code(code) + ")";
	}

	@Override
	public String notice(Object self) {
		if (name == null) {
			return "macro(...)";
		}
		return name + ":macro(...)";
	}

	public static Object activateWithCallAndDataFixed(
			final IokeObject self, IokeObject context, IokeObject message,
			Object on, Object call, Map<String, Object> data)
			throws ControlFlow {
		DefaultMacro dm = (DefaultMacro) self.data;
		if (dm.code == null) {
			IokeObject condition = IokeObject
					.as(IokeObject.getCellChain(context.runtime.condition,
							message, context, "Error", "Invocation",
							"NotActivatable"), context)
					.mimic(message, context);
			condition.setCell("message", message);
			condition.setCell("context", context);
			condition.setCell("receiver", on);
			condition.setCell("method", self);
			condition.setCell("report", context.runtime.newText(
					"You tried to activate a method without any code - did you by any chance activate the DefaultMacro kind by referring to it without wrapping it inside a call to cell?"));
			context.runtime.errorCondition(condition);
			return null;
		}

		IokeObject c = context.runtime.locals.mimic(message, context);
		c.setCell("self", on);
		c.setCell("@", on);
		c.registerMethod(c.runtime.newNativeMethod(
				"will return the currently executing macro receiver",
				new NativeMethod.WithNoArguments("@@") {
					@Override
					public Object activate(IokeObject method,
							IokeObject context, IokeObject message,
							Object on) throws ControlFlow {
						getArguments().getEvaluatedArguments(context,
								message, on, new ArrayList<>(),
								new HashMap<String, Object>());
						return self;
					}
				}));
		c.setCell("currentMessage", message);
		c.setCell("surroundingContext", context);
		c.setCell("call", call);
		for (Map.Entry<String, Object> d : data.entrySet()) {
			String s = d.getKey();
			c.setCell(s.substring(0, s.length() - 1), d.getValue());
		}

		try {
			return context.runtime.interpreter.evaluate(dm.code, c, on, c);
		} catch (ControlFlow.Return e) {
			if (e.context == c) {
				return e.getValue();
			}
			throw e;
		}
	}

	public static Object activateFixed(final IokeObject self,
			IokeObject context, IokeObject message, Object on)
			throws ControlFlow {
		DefaultMacro dm = (DefaultMacro) self.data;
		if (dm.code == null) {
			IokeObject condition = IokeObject
					.as(IokeObject.getCellChain(context.runtime.condition,
							message, context, "Error", "Invocation",
							"NotActivatable"), context)
					.mimic(message, context);
			condition.setCell("message", message);
			condition.setCell("context", context);
			condition.setCell("receiver", on);
			condition.setCell("method", self);
			condition.setCell("report", context.runtime.newText(
					"You tried to activate a method without any code - did you by any chance activate the DefaultMacro kind by referring to it without wrapping it inside a call to cell?"));
			context.runtime.errorCondition(condition);
			return null;
		}

		IokeObject c = context.runtime.locals.mimic(message, context);
		c.setCell("self", on);
		c.setCell("@", on);
		c.registerMethod(c.runtime.newNativeMethod(
				"will return the currently executing macro receiver",
				new NativeMethod.WithNoArguments("@@") {
					@Override
					public Object activate(IokeObject method,
							IokeObject context, IokeObject message,
							Object on) throws ControlFlow {
						getArguments().getEvaluatedArguments(context,
								message, on, new ArrayList<>(),
								new HashMap<String, Object>());
						return self;
					}
				}));
		c.setCell("currentMessage", message);
		c.setCell("surroundingContext", context);
		c.setCell("call", context.runtime.newCallFrom(c, message, context,
				IokeObject.as(on, context)));

		try {
			return context.runtime.interpreter.evaluate(dm.code, c, on, c);
		} catch (ControlFlow.Return e) {
			if (e.context == c) {
				return e.getValue();
			}
			throw e;
		}
	}

	public static Object activateWithDataFixed(final IokeObject self,
			IokeObject context, IokeObject message, Object on,
			Map<String, Object> data) throws ControlFlow {
		DefaultMacro dm = (DefaultMacro) self.data;
		if (dm.code == null) {
			IokeObject condition = IokeObject
					.as(IokeObject.getCellChain(context.runtime.condition,
							message, context, "Error", "Invocation",
							"NotActivatable"), context)
					.mimic(message, context);
			condition.setCell("message", message);
			condition.setCell("context", context);
			condition.setCell("receiver", on);
			condition.setCell("method", self);
			condition.setCell("report", context.runtime.newText(
					"You tried to activate a method without any code - did you by any chance activate the DefaultMacro kind by referring to it without wrapping it inside a call to cell?"));
			context.runtime.errorCondition(condition);
			return null;
		}

		IokeObject c = context.runtime.locals.mimic(message, context);
		c.setCell("self", on);
		c.setCell("@", on);
		c.registerMethod(c.runtime.newNativeMethod(
				"will return the currently executing macro receiver",
				new NativeMethod.WithNoArguments("@@") {
					@Override
					public Object activate(IokeObject method,
							IokeObject context, IokeObject message,
							Object on) throws ControlFlow {
						getArguments().getEvaluatedArguments(context,
								message, on, new ArrayList<>(),
								new HashMap<String, Object>());
						return self;
					}
				}));
		c.setCell("currentMessage", message);
		c.setCell("surroundingContext", context);
		c.setCell("call", context.runtime.newCallFrom(c, message, context,
				IokeObject.as(on, context)));
		for (Map.Entry<String, Object> d : data.entrySet()) {
			String s = d.getKey();
			c.setCell(s.substring(0, s.length() - 1), d.getValue());
		}

		try {
			return context.runtime.interpreter.evaluate(dm.code, c, on, c);
		} catch (ControlFlow.Return e) {
			if (e.context == c) {
				return e.getValue();
			}
			throw e;
		}
	}
}// DefaultMacro
