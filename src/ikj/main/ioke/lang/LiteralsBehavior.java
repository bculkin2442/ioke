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
public class LiteralsBehavior {
	public static void init(IokeObject obj) throws ControlFlow {
		final Runtime runtime = obj.runtime;
		obj.setKind("DefaultBehavior Literals");

		obj.registerMethod(runtime.newNativeMethod(
				"returns a new message with the name given as argument to this method.",
				new NativeMethod("message") {
					private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
							.builder().withRequiredPositional("name")
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
						Object o = args.get(0);

						String name = null;
						if (IokeObject.data(o) instanceof Text) {
							name = Text.getText(o);
						} else {
							name = Text.getText(Interpreter.send(
									context.runtime.asText, context, o));
						}

						Message m = new Message(context.runtime, name);
						IokeObject ret = context.runtime.createMessage(m);
						if (".".equals(name)) {
							Message.setIsTerminator(ret, true);
						}
						Message.copySourceLocation(message, ret);
						return ret;
					}
				}));

		obj.registerMethod(runtime.newNativeMethod(
				"Takes one evaluated argument and returns a new Pair of the receiver and the argument",
				new NativeMethod("=>") {
					private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
							.builder().withRequiredPositional("other")
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

						return context.runtime.newPair(on, args.get(0));
					}
				}));

		obj.registerMethod(runtime.newNativeMethod(
				"Takes one evaluated argument that is expected to be a Text, and returns the symbol corresponding to that text",
				new NativeMethod(":") {
					private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
							.builder().withRequiredPositional("symbolText")
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

						String sym = Text.getText(Interpreter.send(
								runtime.asText, context, args.get(0)));
						return context.runtime.getSymbol(sym);
					}
				}));

		obj.registerMethod(runtime.newNativeMethod(
				"creates a new Dict from the arguments provided. these arguments can be two different things - either a keyword argument, or a pair. if it's a keyword argument, the entry added to the dict for it will be a symbol with the name from the keyword, without the ending colon. if it's not a keyword, it is expected to be an evaluated pair, where the first part of the pair is the key, and the second part is the value.",
				new NativeMethod("dict") {
					private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
							.builder().withRest("pairs")
							.withKeywordRest("keywordPairs")
							.getArguments();

					@Override
					public DefaultArgumentsDefinition getArguments() {
						return ARGUMENTS;
					}

					@Override
					public Object activate(IokeObject method,
							IokeObject context, IokeObject message,
							Object on) throws ControlFlow {
						getArguments().checkArgumentCount(context, message,
								on);

						List<Object> arguments = message.getArguments();
						Map<Object, Object> moo = new HashMap<>(
								arguments.size());

						for (Object o : arguments) {
							Object key, value;
							if (Message.isKeyword(o)) {
								String str = Message.name(o);
								key = context.runtime.getSymbol(str
										.substring(0, str.length() - 1));
								if (Message.next(o) != null) {
									value = Interpreter
											.getEvaluatedArgument(
													Message.next(o),
													context);
								} else {
									value = context.runtime.nil;
								}
							} else {
								Object result = Interpreter
										.getEvaluatedArgument(o, context);
								if ((result instanceof IokeObject)
										&& (IokeObject.data(
												result) instanceof Pair)) {
									key = Pair.getFirst(result);
									value = Pair.getSecond(result);
								} else {
									key = result;
									value = context.runtime.nil;
								}
							}

							moo.put(key, value);
						}

						return context.runtime.newDict(moo);
					}
				}));

		obj.registerMethod(runtime.newNativeMethod(
				"creates a new Set from the result of evaluating all arguments provided.",
				new NativeMethod("set") {
					private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
							.builder().withRest("elements").getArguments();

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

						return context.runtime.newSet(args);
					}
				}));
	}
}
