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
public class Call extends IokeData {
	private IokeObject	ctx;
	private IokeObject	message;
	private IokeObject	surroundingContext;
	private IokeObject	on;
	List<Object>		cachedPositional;
	Map<String, Object>	cachedKeywords;
	int					cachedArgCount;

	public Call() {
	}

	public Call(IokeObject ctx, IokeObject message,
			IokeObject surroundingContext, IokeObject on) {
		this.ctx = ctx;
		this.message = message;
		this.surroundingContext = surroundingContext;
		this.on = on;
	}

	@Override
	public void init(IokeObject obj) throws ControlFlow {
		final Runtime runtime = obj.runtime;

		obj.setKind("Call");

		obj.registerMethod(runtime.newNativeMethod(
				"returns a list of all the unevaluated arguments",
				new TypeCheckingNativeMethod.WithNoArguments("arguments",
						runtime.call) {
					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						return context.runtime.newList(
								((Call) IokeObject.data(on)).message
										.getArguments());
					}
				}));

		obj.registerMethod(runtime.newNativeMethod(
				"returns the ground of the place this call originated",
				new TypeCheckingNativeMethod.WithNoArguments("ground",
						runtime.call) {
					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						getArguments().getEvaluatedArguments(context,
								message, on, new ArrayList<Object>(),
								new HashMap<String, Object>());

						return ((Call) IokeObject
								.data(on)).surroundingContext;
					}
				}));

		obj.registerMethod(
				runtime.newNativeMethod("returns the receiver of the call",
						new TypeCheckingNativeMethod.WithNoArguments(
								"receiver", runtime.call) {
							@Override
							public Object activate(IokeObject method,
									Object on, List<Object> args,
									Map<String, Object> keywords,
									IokeObject context, IokeObject message)
									throws ControlFlow {
								getArguments().getEvaluatedArguments(
										context, message, on,
										new ArrayList<Object>(),
										new HashMap<String, Object>());

								return ((Call) IokeObject.data(on)).on;
							}
						}));

		obj.registerMethod(runtime.newNativeMethod(
				"returns the currently executing context",
				new TypeCheckingNativeMethod.WithNoArguments(
						"currentContext", runtime.call) {
					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						getArguments().getEvaluatedArguments(context,
								message, on, new ArrayList<Object>(),
								new HashMap<String, Object>());

						return ((Call) IokeObject.data(on)).ctx;
					}
				}));

		obj.registerMethod(runtime.newNativeMethod(
				"returns the message that started this call",
				new TypeCheckingNativeMethod.WithNoArguments("message",
						runtime.call) {
					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						getArguments().getEvaluatedArguments(context,
								message, on, new ArrayList<Object>(),
								new HashMap<String, Object>());

						return ((Call) IokeObject.data(on)).message;
					}
				}));

		obj.registerMethod(runtime.newNativeMethod(
				"returns a list of the result of evaluating all the arguments to this call",
				new TypeCheckingNativeMethod.WithNoArguments(
						"evaluatedArguments", runtime.call) {
					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						getArguments().getEvaluatedArguments(context,
								message, on, new ArrayList<Object>(),
								new HashMap<String, Object>());

						IokeObject msg = ((Call) IokeObject
								.data(on)).message;
						return context.runtime.newList(
								Interpreter.getEvaluatedArguments(msg,
										((Call) IokeObject.data(
												on)).surroundingContext));
					}
				}));

		obj.registerMethod(runtime.newNativeMethod(
				"takes one evaluated text or symbol argument and resends the current message to that method/macro on the current receiver.",
				new TypeCheckingNativeMethod("resendToMethod") {
					private final TypeCheckingArgumentsDefinition ARGUMENTS = TypeCheckingArgumentsDefinition
							.builder().receiverMustMimic(runtime.call)
							.withRequiredPositional("cellName")
							.getArguments();

					@Override
					public TypeCheckingArgumentsDefinition getArguments() {
						return ARGUMENTS;
					}

					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						Call c = (Call) IokeObject.data(on);
						String name = Text.getText(Interpreter.send(
								runtime.asText, context, args.get(0)));
						IokeObject m = Message.copy(c.message);
						Message.setName(m, name);
						return Interpreter.send(m, c.surroundingContext,
								c.on);
					}
				}));

		obj.registerMethod(runtime.newNativeMethod(
				"takes one evaluated object and resends the current message with that object as the new receiver",
				new TypeCheckingNativeMethod("resendToReceiver") {
					private final TypeCheckingArgumentsDefinition ARGUMENTS = TypeCheckingArgumentsDefinition
							.builder().receiverMustMimic(runtime.call)
							.withRequiredPositional("newReceiver")
							.getArguments();

					@Override
					public TypeCheckingArgumentsDefinition getArguments() {
						return ARGUMENTS;
					}

					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						Call c = (Call) IokeObject.data(on);
						Object recv = args.get(0);
						return Interpreter.send(c.message,
								c.surroundingContext, recv);
					}
				}));

		obj.registerMethod(runtime.newNativeMethod(
				"uhm. this is a strange one. really.",
				new TypeCheckingNativeMethod("resendToValue") {
					private final TypeCheckingArgumentsDefinition ARGUMENTS = TypeCheckingArgumentsDefinition
							.builder().receiverMustMimic(runtime.call)
							.withRequiredPositional("value")
							.withOptionalPositional("newSelf", "nil")
							.getArguments();

					@Override
					public TypeCheckingArgumentsDefinition getArguments() {
						return ARGUMENTS;
					}

					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						Call c = (Call) IokeObject.data(on);
						Object self = c.on;
						if (args.size() > 1) {
							self = args.get(1);
						}

						return Interpreter.getOrActivate(args.get(0),
								c.surroundingContext, c.message, self);
					}
				}));

		obj.registerMethod(
				runtime.newNativeMethod("uhm. this one isn't too bad.",
						new TypeCheckingNativeMethod("activateValue") {
							private final TypeCheckingArgumentsDefinition ARGUMENTS = TypeCheckingArgumentsDefinition
									.builder()
									.receiverMustMimic(runtime.call)
									.withRequiredPositional("value")
									.withOptionalPositional("newSelf",
											"nil")
									.withKeywordRest("valuesToAdd")
									.getArguments();

							@Override
							public TypeCheckingArgumentsDefinition getArguments() {
								return ARGUMENTS;
							}

							@Override
							public Object activate(IokeObject method,
									Object on, List<Object> args,
									Map<String, Object> keys,
									IokeObject context, IokeObject message)
									throws ControlFlow {
								Call c = (Call) IokeObject.data(on);
								Object self = c.on;
								if (args.size() > 1) {
									self = args.get(1);
								}

								return Interpreter.activateWithData(
										IokeObject.as(args.get(0),
												context),
										c.surroundingContext, c.message,
										self, keys);
							}
						}));

		obj.registerMethod(runtime.newNativeMethod(
				"I really ought to write documentation for these methods, but I don't know how to describe what they do.",
				new TypeCheckingNativeMethod(
						"activateValueWithCachedArguments") {
					private final TypeCheckingArgumentsDefinition ARGUMENTS = TypeCheckingArgumentsDefinition
							.builder().receiverMustMimic(runtime.call)
							.withRequiredPositional("value")
							.withOptionalPositional("newSelf", "nil")
							.withKeywordRest("valuesToAdd").getArguments();

					@Override
					public TypeCheckingArgumentsDefinition getArguments() {
						return ARGUMENTS;
					}

					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args, Map<String, Object> keys,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						Call c = (Call) IokeObject.data(on);
						Object self = c.on;
						if (args.size() > 1) {
							self = args.get(1);
						}

						return Interpreter.activateWithCallAndData(
								IokeObject.as(args.get(0), context),
								c.surroundingContext, c.message, self, on,
								keys);
					}
				}));

	}

	public IokeData cloneData(IokeObject obj, IokeObject m,
			IokeObject context) {
		return new Call(this.ctx, this.message, this.surroundingContext,
				this.on);
	}
}// Call
