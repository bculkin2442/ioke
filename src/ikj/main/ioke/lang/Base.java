/*
 * See LICENSE file in distribution for copyright and licensing
 * information.
 */
package ioke.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ioke.lang.exceptions.ControlFlow;

/**
 *
 * @author <a href="mailto:ola.bini@gmail.com">Ola Bini</a>
 */
public class Base {
	public static Object cellNames(IokeObject context, IokeObject message,
			Object on, boolean includeMimics, Object cutoff)
			throws ControlFlow {
		if (includeMimics) {
			IdentityHashMap<Object, Object> visited = new IdentityHashMap<>();
			List<Object> names = new ArrayList<>();
			Set<Object> visitedNames = new HashSet<>();
			Set<String> undefined = new HashSet<>();
			Runtime runtime = context.runtime;
			List<Object> toVisit = new ArrayList<>();
			toVisit.add(on);

			while (!toVisit.isEmpty()) {
				IokeObject current = IokeObject.as(toVisit.remove(0),
						context);
				if (!visited.containsKey(current)) {
					visited.put(current, null);
					if (cutoff != current) {
						toVisit.addAll(current.getMimics());
					}

					Body.Cell c = current.body.firstAdded;
					while (c != null) {
						String s = c.name;
						if (!undefined.contains(s)) {
							if (c.value == runtime.nul) {
								undefined.add(s);
							} else {
								Object x = runtime.getSymbol(s);
								if (!visitedNames.contains(x)) {
									visitedNames.add(x);
									names.add(x);
								}
							}
						}

						c = c.orderedNext;
					}
				}
			}

			return runtime.newList(names);
		}
		List<Object> names = new ArrayList<>();
		Runtime runtime = context.runtime;

		Body.Cell c = IokeObject.as(on, context).body.firstAdded;
		while (c != null) {
			String s = c.name;
			if (c.value != runtime.nul) {
				names.add(runtime.getSymbol(s));
			}
			c = c.orderedNext;
		}

		return runtime.newList(names);
	}

	public static Object cells(IokeObject context, IokeObject message,
			Object on, boolean includeMimics) throws ControlFlow {
		Map<Object, Object> cells = new LinkedHashMap<>();
		Runtime runtime = context.runtime;

		if (includeMimics) {
			IdentityHashMap<Object, Object> visited = new IdentityHashMap<>();
			Set<String> undefined = new HashSet<>();

			List<Object> toVisit = new ArrayList<>();
			toVisit.add(on);

			while (!toVisit.isEmpty()) {
				IokeObject current = IokeObject.as(toVisit.remove(0),
						context);
				if (!visited.containsKey(current)) {
					visited.put(current, null);
					toVisit.addAll(current.getMimics());

					Body.Cell c = current.body.firstAdded;
					while (c != null) {
						String s = c.name;
						if (!undefined.contains(s)) {
							Object val = c.value;
							if (val == runtime.nul) {
								undefined.add(s);
							} else {
								Object x = runtime.getSymbol(s);
								if (!cells.containsKey(x)) {
									cells.put(x, val);
								}
							}
						}
						c = c.orderedNext;
					}
				}
			}
		} else {
			Body.Cell c = IokeObject.as(on, context).body.firstAdded;
			while (c != null) {
				String s = c.name;
				if (c.value != runtime.nul) {
					cells.put(runtime.getSymbol(s), c.value);
				}
				c = c.orderedNext;
			}
		}
		return runtime.newDict(cells);
	}

	public static Object assignCell(IokeObject context, IokeObject message,
			Object on, Object first, Object val) throws ControlFlow {
		String name = Text.getText(
				Interpreter.send(context.runtime.asText, context, first));
		if (val instanceof IokeObject) {
			if ((IokeObject.data(val) instanceof Named)
					&& ((Named) IokeObject.data(val)).getName() == null) {
				((Named) IokeObject.data(val)).setName(name);
			} else if (name.length() > 0
					&& Character.isUpperCase(name.charAt(0))
					&& !IokeObject.as(val, context).hasKind()) {
				if (on == context.runtime.ground) {
					IokeObject.as(val, context).setKind(name);
				} else {
					IokeObject.as(val, context).setKind(
							IokeObject.as(on, context).getKind(message,
									context) + " " + name);
				}
			}
		}

		return IokeObject.setCell(on, message, context, name, val);
	}

	public static Object documentation(IokeObject context,
			IokeObject message, Object on) throws ControlFlow {
		String docs = IokeObject.as(on, context).getDocumentation();
		if (null == docs) {
			return context.runtime.nil;
		}
		return context.runtime.newText(docs);
	}

	public static Object setDocumentation(IokeObject context,
			IokeObject message, Object on, Object arg) throws ControlFlow {
		if (arg == context.runtime.nil) {
			IokeObject.as(on, context).setDocumentation(null, message,
					context);
		} else {
			String s = Text.getText(arg);
			IokeObject.as(on, context).setDocumentation(s, message,
					context);
		}
		return arg;
	}

	public static void init(final IokeObject base) throws ControlFlow {
		base.setKind("Base");
		base.registerMethod(base.runtime.newNativeMethod(
				"returns the documentation text of the object called on. anything can have a documentation text - this text will initially be nil.",
				new NativeMethod.WithNoArguments("documentation") {
					@Override
					public Object activate(IokeObject method,
							IokeObject context, IokeObject message,
							Object on) throws ControlFlow {
						getArguments().getEvaluatedArguments(context,
								message, on, new ArrayList<>(),
								new HashMap<String, Object>());
						return documentation(context, message, on);
					}
				}));

		base.registerMethod(base.runtime.newNativeMethod(
				"returns a boolean indicating of this object should be activated or not.",
				new NativeMethod.WithNoArguments("activatable") {
					@Override
					public Object activate(IokeObject method,
							IokeObject context, IokeObject message,
							Object on) throws ControlFlow {
						getArguments().getEvaluatedArguments(context,
								message, on, new ArrayList<>(),
								new HashMap<String, Object>());
						return IokeObject.as(on, context).isActivatable()
								? context.runtime._true
								: context.runtime._false;
					}
				}));

		base.registerMethod(base.runtime.newNativeMethod(
				"sets the activatable flag for a specific object. this will not impact objects that mimic this object..",
				new TypeCheckingNativeMethod("activatable=") {
					private final TypeCheckingArgumentsDefinition ARGUMENTS = TypeCheckingArgumentsDefinition
							.builder()
							.withRequiredPositional("activatableFlag")
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
						IokeObject.as(on, context).setActivatable(
								IokeObject.isTrue(args.get(0)));
						return args.get(0);
					}
				}));

		base.registerMethod(
				base.runtime.newNativeMethod("returns this object",
						new NativeMethod.WithNoArguments("identity") {
							@Override
							public Object activate(IokeObject method,
									IokeObject context, IokeObject message,
									Object on) throws ControlFlow {
								getArguments().getEvaluatedArguments(
										context, message, on,
										new ArrayList<>(),
										new HashMap<String, Object>());
								return on;
							}
						}));

		base.registerMethod(base.runtime.newNativeMethod(
				"sets the documentation string for a specific object.",
				new TypeCheckingNativeMethod("documentation=") {
					private final TypeCheckingArgumentsDefinition ARGUMENTS = TypeCheckingArgumentsDefinition
							.builder().withRequiredPositional("text")
							.whichMustMimic(base.runtime.text).orBeNil()
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
						return setDocumentation(context, message, on,
								args.get(0));
					}
				}));

		base.registerMethod(base.runtime.newNativeMethod(
				"will return a new derivation of the receiving object. Might throw exceptions if the object is an oddball object.",
				new NativeMethod.WithNoArguments("mimic") {
					@Override
					public Object activate(IokeObject method,
							IokeObject context, IokeObject message,
							Object on) throws ControlFlow {
						getArguments().checkArgumentCount(context, message,
								on);
						return IokeObject.as(on, context).mimic(message,
								context);
					}
				}));

		base.registerMethod(base.runtime.newNativeMethod(
				"expects two or more arguments, the first arguments unevaluated, the last evaluated. assigns the result of evaluating the last argument in the context of the caller, and assigns this result to the name/s provided by the first arguments. the first arguments remains unevaluated. the result of the assignment is the value assigned to the name. if the last argument is a method-like object and it's name is not set, that name will be set to the name of the cell.",
				new NativeMethod("=") {
					private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
							.builder()
							.withRequiredPositionalUnevaluated("place")
							.withRestUnevaluated(
									"morePlacesForDestructuring")
							.withRequiredPositional("value")
							.getArguments();

					@Override
					public DefaultArgumentsDefinition getArguments() {
						return ARGUMENTS;
					}

					private Object recursiveDestructuring(
							List<Object> places, int numPlaces,
							IokeObject message, IokeObject context,
							Object on, Object toTuple) throws ControlFlow {
						Object tupledValue = Interpreter.send(
								context.runtime.asTuple, context, toTuple);
						Object[] values = Tuple.getElements(tupledValue);
						int numValues = values.length;

						int min = Math.min(numValues, numPlaces);

						boolean hadEndingUnderscore = false;

						for (int i = 0; i < min; i++) {
							IokeObject m1 = IokeObject.as(places.get(i),
									context);
							String name = m1.getName();
							if (name.equals("_")) {
								if (i == numPlaces - 1) {
									hadEndingUnderscore = true;
								}
							} else {
								if (m1.getArguments().size() == 0) {
									Object value = values[i];

									IokeObject.assign(on, name, value,
											context, message);

									if (value instanceof IokeObject) {
										if ((IokeObject
												.data(value) instanceof Named)
												&& ((Named) IokeObject
														.data(value))
																.getName() == null) {
											((Named) IokeObject
													.data(value))
															.setName(name);
										} else if (name.length() > 0
												&& Character.isUpperCase(
														name.charAt(0))
												&& !IokeObject
														.as(value, context)
														.hasKind()) {
											if (on == context.runtime.ground) {
												IokeObject
														.as(value, context)
														.setKind(name);
											} else {
												IokeObject
														.as(value, context)
														.setKind(IokeObject
																.as(on, context)
																.getKind(
																		message,
																		context)
																+ " "
																+ name);
											}
										}
									}
								} else if (name.equals("")) {
									List<Object> newArgs = m1
											.getArguments();
									recursiveDestructuring(newArgs,
											newArgs.size(), message,
											context, on, values[i]);
								} else {
									String newName = name + "=";
									List<Object> arguments = new ArrayList<>(
											m1.getArguments());
									arguments.add(context.runtime
											.createMessage(Message
													.wrap(IokeObject.as(
															values[i], context))));
									IokeObject msg = context.runtime
											.newMessageFrom(message,
													newName, arguments);
									Interpreter.send(msg, context, on);
								}
							}
						}

						if (numPlaces > min || (numValues > min
								&& !hadEndingUnderscore)) {
							IokeObject condition = IokeObject
									.as(IokeObject.getCellChain(
											context.runtime.condition,
											message, context, "Error",
											"DestructuringMismatch"),
											context)
									.mimic(message, context);
							condition.setCell("message", message);
							condition.setCell("context", context);
							condition.setCell("receiver", on);

							context.runtime.errorCondition(condition);
						}

						return tupledValue;
					}

					@Override
					public Object activate(IokeObject method,
							IokeObject context, IokeObject message,
							Object on) throws ControlFlow {
						getArguments().checkArgumentCount(context, message,
								on);

						List<Object> args = IokeObject.as(message, context)
								.getArguments();
						if (args.size() == 2) {
							IokeObject m1 = IokeObject.as(args.get(0),
									context);

							String name = m1.getName();
							if (m1.getArguments().size() == 0) {
								Object value = Interpreter
										.getEvaluatedArgument(args.get(1),
												context);

								IokeObject.assign(on, name, value, context,
										message);

								if (value instanceof IokeObject) {
									if ((IokeObject
											.data(value) instanceof Named)
											&& ((Named) IokeObject.data(
													value)).getName() == null) {
										((Named) IokeObject.data(value))
												.setName(name);
									} else if (name.length() > 0
											&& Character.isUpperCase(
													name.charAt(0))
											&& !IokeObject
													.as(value, context)
													.hasKind()) {
										if (on == context.runtime.ground) {
											IokeObject.as(value, context)
													.setKind(name);
										} else {
											IokeObject.as(value, context)
													.setKind(IokeObject
															.as(on, context)
															.getKind(
																	message,
																	context)
															+ " " + name);
										}
									}
								}

								return value;
							}
							String newName = name + "=";
							List<Object> arguments = new ArrayList<>(
									m1.getArguments());
							arguments.add(args.get(1));
							IokeObject msg = context.runtime
									.newMessageFrom(message, newName,
											arguments);
							return Interpreter.send(msg, context, on);
						}
						int lastIndex = args.size() - 1;
						int numPlaces = lastIndex;

						return recursiveDestructuring(args, numPlaces,
								message, context, on,
								Interpreter.getEvaluatedArgument(
										args.get(lastIndex), context));
					}
				}));

		base.registerMethod(base.runtime.newNativeMethod(
				"expects one evaluated text or symbol argument and returns the cell that matches that name, without activating even if it's activatable.",
				new NativeMethod("cell") {
					private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
							.builder().withRequiredPositional("cellName")
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

						String name = Text.getText(
								Interpreter.send(context.runtime.asText,
										context, args.get(0)));
						return IokeObject.getCell(on, message, context,
								name);
					}
				}));

		base.registerMethod(base.runtime.newNativeMethod(
				"expects one evaluated text or symbol argument and returns a boolean indicating whether such a cell is reachable from this point.",
				new NativeMethod("cell?") {
					private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
							.builder().withRequiredPositional("cellName")
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

						String name = Text.getText(
								Interpreter.send(context.runtime.asText,
										context, args.get(0)));
						return IokeObject.findCell(on, context,
								name) != context.runtime.nul
										? context.runtime._true
										: context.runtime._false;
					}
				}));

		base.registerMethod(base.runtime.newNativeMethod(
				"expects one evaluated text or symbol argument and returns a boolean indicating whether this cell is owned by the receiver or not. the assumption is that the cell should exist. if it doesn't exist, a NoSuchCell condition will be signalled.",
				new NativeMethod("cellOwner?") {
					private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
							.builder().withRequiredPositional("cellName")
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

						String name = Text.getText(
								Interpreter.send(context.runtime.asText,
										context, args.get(0)));
						return (IokeObject.findPlace(on, message, context,
								name) == on) ? context.runtime._true
										: context.runtime._false;
					}
				}));

		base.registerMethod(base.runtime.newNativeMethod(
				"expects one evaluated text or symbol argument and returns the closest object that defines such a cell. if it doesn't exist, a NoSuchCell condition will be signalled.",
				new NativeMethod("cellOwner") {
					private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
							.builder().withRequiredPositional("cellName")
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

						String name = Text.getText(
								Interpreter.send(context.runtime.asText,
										context, args.get(0)));
						Object result = IokeObject.findPlace(on, message,
								context, name);
						if (result == context.runtime.nul) {
							return context.runtime.nil;
						}
						return result;
					}
				}));

		base.registerMethod(base.runtime.newNativeMethod(
				"expects one evaluated text or symbol argument and removes that cell from the current receiver. if the current receiver has no such object, signals a condition. note that if another cell with that name is available in the mimic chain, it will still be accessible after calling this method. the method returns the receiver.",
				new NativeMethod("removeCell!") {
					private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
							.builder().withRequiredPositional("cellName")
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

						String name = Text.getText(
								Interpreter.send(context.runtime.asText,
										context, args.get(0)));
						IokeObject.removeCell(on, message, context, name);
						return on;
					}
				}));

		base.registerMethod(base.runtime.newNativeMethod(
				"expects one evaluated text or symbol argument and makes that cell undefined in the current receiver. what that means is that from now on it will look like this cell doesn't exist in the receiver or any of its mimics. the cell will not show up if you call cellNames on the receiver or any of the receivers mimics. the undefined status can be removed by doing removeCell! on the correct cell name. a cell name that doesn't exist can still be undefined. the method returns the receiver.",
				new NativeMethod("undefineCell!") {
					private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
							.builder().withRequiredPositional("cellName")
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

						String name = Text.getText(
								Interpreter.send(context.runtime.asText,
										context, args.get(0)));
						IokeObject.undefineCell(on, message, context,
								name);
						return on;
					}
				}));

		base.registerMethod(base.runtime.newNativeMethod(
				"takes one optional evaluated boolean argument, which defaults to false. if false, this method returns a list of the cell names of the receiver. if true, it returns the cell names of this object and all it's mimics recursively.",
				new NativeMethod("cellNames") {
					private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
							.builder()
							.withOptionalPositional("includeMimics",
									"false")
							.withOptionalPositional("cutoff", "nil")
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

						return cellNames(context, message, on,
								args.size() > 0
										&& IokeObject.isTrue(args.get(0)),
								(args.size() > 1) ? args.get(1) : null);
					}
				}));

		base.registerMethod(base.runtime.newNativeMethod(
				"takes one optional evaluated boolean argument, which defaults to false. if false, this method returns a dict of the cell names and values of the receiver. if true, it returns the cell names and values of this object and all it's mimics recursively.",
				new NativeMethod("cells") {
					private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
							.builder()
							.withOptionalPositional("includeMimics",
									"false")
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
						return cells(context, message, on, args.size() > 0
								&& IokeObject.isTrue(args.get(0)));
					}
				}));

		base.registerMethod(base.runtime.newNativeMethod(
				"expects one evaluated text or symbol argument that names the cell to set, sets this cell to the result of evaluating the second argument, and returns the value set.",
				new NativeMethod("cell=") {
					private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
							.builder().withRequiredPositional("cellName")
							.withRequiredPositional("value")
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
						return assignCell(context, message, on,
								args.get(0), args.get(1));

					}
				}));

		base.registerMethod(base.runtime.newNativeMethod(
				"returns true if the left hand side is equal to the right hand side. exactly what this means depend on the object. the default behavior of Ioke objects is to only be equal if they are the same instance.",
				new NativeMethod("==") {
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
						Object other = args.get(0);
						return (IokeObject.as(on,
								context).body == IokeObject.as(other,
										context).body)
												? context.runtime._true
												: context.runtime._false;
					}
				}));

		base.registerMethod(base.runtime.newNativeMethod(
				"returns a hash for the object",
				new NativeMethod.WithNoArguments("hash") {
					@Override
					public Object activate(IokeObject method,
							IokeObject context, IokeObject message,
							Object on) throws ControlFlow {
						getArguments().getEvaluatedArguments(context,
								message, on, new ArrayList<>(),
								new HashMap<String, Object>());

						return context.runtime
								.newNumber(System.identityHashCode(
										IokeObject.as(on, context).body));
					}
				}));
	}
}// Base
