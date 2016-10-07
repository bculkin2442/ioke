/*
 * See LICENSE file in distribution for copyright and licensing
 * information.
 */
package ioke.lang;

import java.util.ArrayList;
import java.util.HashMap;

import ioke.lang.exceptions.ControlFlow;

/**
 *
 * @author <a href="mailto:ola.bini@gmail.com">Ola Bini</a>
 */
public class Method extends IokeData implements Named, Inspectable {
	String name;

	public Method(String name, int type) {
		super(type);
		this.name = name;
	}

	public Method(IokeObject context, int type) {
		this((String) null, type);
	}

	@Override
	public void init(IokeObject method) throws ControlFlow {
		method.setKind("Method");
		method.setActivatable(true);

		method.registerMethod(method.runtime.newNativeMethod(
				"returns the name of the method",
				new NativeMethod.WithNoArguments("name") {
					@Override
					public Object activate(IokeObject self,
							IokeObject context, IokeObject message,
							Object on) throws ControlFlow {
						getArguments().getEvaluatedArguments(context,
								message, on, new ArrayList<>(),
								new HashMap<String, Object>());

						return context.runtime.newText(
								((Method) IokeObject.data(on)).name);
					}
				}));
		method.registerMethod(method.runtime.newNativeMethod(
				"Returns a text inspection of the object",
				new NativeMethod.WithNoArguments("inspect") {
					@Override
					public Object activate(IokeObject self,
							IokeObject context, IokeObject message,
							Object on) throws ControlFlow {
						getArguments().getEvaluatedArguments(context,
								message, on, new ArrayList<>(),
								new HashMap<String, Object>());

						return context.runtime
								.newText(Method.getInspect(on));
					}
				}));
		method.registerMethod(method.runtime.newNativeMethod(
				"Returns a brief text inspection of the object",
				new NativeMethod.WithNoArguments("notice") {
					@Override
					public Object activate(IokeObject self,
							IokeObject context, IokeObject message,
							Object on) throws ControlFlow {
						getArguments().getEvaluatedArguments(context,
								message, on, new ArrayList<>(),
								new HashMap<String, Object>());

						return context.runtime
								.newText(Method.getNotice(on));
					}
				}));
		method.registerMethod(method.runtime.newNativeMethod(
				"activates this method with the arguments given to call",
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

		method.registerMethod(method.runtime.newNativeMethod(
				"returns the full code of this method, as a Text",
				new NativeMethod.WithNoArguments("code") {
					@Override
					public Object activate(IokeObject self,
							IokeObject dynamicContext, IokeObject message,
							Object on) throws ControlFlow {
						getArguments().getEvaluatedArguments(
								dynamicContext, message, on,
								new ArrayList<>(),
								new HashMap<String, Object>());

						IokeData data = IokeObject.data(on);
						if (data instanceof Method) {
							return dynamicContext.runtime.newText(
									((Method) data).getCodeString());
						} else {
							return dynamicContext.runtime.newText(
									((AliasMethod) data).getCodeString());
						}
					}
				}));
	}

	@Override
	public String getName() {
		return name;
	}

	public String getCodeString() {
		return "method(nil)";
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public static Object activateFixed(IokeObject self, IokeObject context,
			IokeObject message, Object on) throws ControlFlow {
		IokeObject condition = IokeObject.as(
				IokeObject.getCellChain(context.runtime.condition, message,
						context, "Error", "Invocation", "NotActivatable"),
				context).mimic(message, context);
		condition.setCell("message", message);
		condition.setCell("context", context);
		condition.setCell("receiver", on);
		condition.setCell("method", self);
		condition.setCell("reportMsg",
				context.runtime.newText("You tried to activate a method ("
						+ ((Method) self.data).name
						+ ") without any code - did you by any chance activate the Method kind by referring to it without wrapping it inside a call to cell?"));
		context.runtime.errorCondition(condition);

		return self.runtime.nil;
	}

	public static String getInspect(Object on) {
		return ((Inspectable) (IokeObject.data(on))).inspect(on);
	}

	public static String getNotice(Object on) {
		return ((Inspectable) (IokeObject.data(on))).notice(on);
	}

	@Override
	public String inspect(Object self) {
		return getCodeString();
	}

	@Override
	public String notice(Object self) {
		if (name == null) {
			return "method(...)";
		} else {
			return name + ":method(...)";
		}
	}
}// Method
