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
public class Pair extends IokeData {
	private Object	first;
	private Object	second;

	public Pair(Object first, Object second) {
		this.first = first;
		this.second = second;
	}

	public static Object getFirst(Object pair) {
		return ((Pair) IokeObject.data(pair)).getFirst();
	}

	public static Object getSecond(Object pair) {
		return ((Pair) IokeObject.data(pair)).getSecond();
	}

	public Object getFirst() {
		return first;
	}

	public Object getSecond() {
		return second;
	}

	@Override
	public void init(IokeObject obj) throws ControlFlow {
		final Runtime runtime = obj.runtime;

		obj.setKind("Pair");
		obj.mimics(IokeObject.as(
				runtime.mixins.getCell(null, null, "Enumerable"), null),
				runtime.nul, runtime.nul);
		obj.mimics(IokeObject
				.as(runtime.mixins.getCell(null, null, "Comparing"), null),
				runtime.nul, runtime.nul);

		obj.registerMethod(
				runtime.newNativeMethod("returns a hash for the pair",
						new NativeMethod.WithNoArguments("hash") {
							@Override
							public Object activate(IokeObject method,
									IokeObject context, IokeObject message,
									Object on) throws ControlFlow {
								getArguments().getEvaluatedArguments(
										context, message, on,
										new ArrayList<>(),
										new HashMap<String, Object>());
								int one = ((Pair) IokeObject
										.data(on)).first.hashCode();
								int two = ((Pair) IokeObject
										.data(on)).second.hashCode();
								return context.runtime
										.newNumber(one + 13 * two);
							}
						}));

		obj.registerMethod(runtime.newNativeMethod(
				"returns true if the left hand side pair is equal to the right hand side pair.",
				new TypeCheckingNativeMethod("==") {
					private final TypeCheckingArgumentsDefinition ARGUMENTS = TypeCheckingArgumentsDefinition
							.builder().receiverMustMimic(runtime.pair)
							.withRequiredPositional("other")
							.getArguments();

					@Override
					public TypeCheckingArgumentsDefinition getArguments() {
						return ARGUMENTS;
					}

					@Override
					public Object activate(IokeObject self, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						getArguments().getEvaluatedArguments(context,
								message, on, args,
								new HashMap<String, Object>());
						Pair d = (Pair) IokeObject.data(on);
						Object other = args.get(0);
						return ((other instanceof IokeObject)
								&& (IokeObject.data(other) instanceof Pair)
								&& d.first.equals(((Pair) IokeObject
										.data(other)).first)
								&& d.second.equals(((Pair) IokeObject
										.data(other)).second))
												? context.runtime._true
												: context.runtime._false;
					}
				}));

		obj.registerMethod(
				runtime.newNativeMethod("Returns the first value",
						new TypeCheckingNativeMethod.WithNoArguments(
								"first", runtime.pair) {
							@Override
							public Object activate(IokeObject method,
									Object on, List<Object> args,
									Map<String, Object> keywords,
									IokeObject context, IokeObject message)
									throws ControlFlow {
								return ((Pair) IokeObject.data(on)).first;
							}
						}));

		obj.registerMethod(
				runtime.newNativeMethod("Returns the first value",
						new TypeCheckingNativeMethod.WithNoArguments("key",
								runtime.pair) {
							@Override
							public Object activate(IokeObject method,
									Object on, List<Object> args,
									Map<String, Object> keywords,
									IokeObject context, IokeObject message)
									throws ControlFlow {
								return ((Pair) IokeObject.data(on)).first;
							}
						}));

		obj.registerMethod(
				runtime.newNativeMethod("Returns the second value",
						new TypeCheckingNativeMethod.WithNoArguments(
								"second", runtime.pair) {
							@Override
							public Object activate(IokeObject method,
									Object on, List<Object> args,
									Map<String, Object> keywords,
									IokeObject context, IokeObject message)
									throws ControlFlow {
								return ((Pair) IokeObject.data(on)).second;
							}
						}));

		obj.registerMethod(
				runtime.newNativeMethod("Returns the second value",
						new TypeCheckingNativeMethod.WithNoArguments(
								"value", runtime.pair) {
							@Override
							public Object activate(IokeObject method,
									Object on, List<Object> args,
									Map<String, Object> keywords,
									IokeObject context, IokeObject message)
									throws ControlFlow {
								return ((Pair) IokeObject.data(on)).second;
							}
						}));

		obj.registerMethod(runtime.newNativeMethod(
				"Returns a text inspection of the object",
				new TypeCheckingNativeMethod.WithNoArguments("inspect",
						runtime.pair) {
					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						return method.runtime.newText(Pair.getInspect(on));
					}
				}));

		obj.registerMethod(runtime.newNativeMethod(
				"Returns a brief text inspection of the object",
				new TypeCheckingNativeMethod.WithNoArguments("notice",
						runtime.pair) {
					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						return method.runtime.newText(Pair.getNotice(on));
					}
				}));
	}

	@Override
	public IokeData cloneData(IokeObject obj, IokeObject m,
			IokeObject context) {
		return new Pair(first, second);
	}

	public static String getInspect(Object on) throws ControlFlow {
		return ((Pair) (IokeObject.data(on))).inspect(on);
	}

	public static String getNotice(Object on) throws ControlFlow {
		return ((Pair) (IokeObject.data(on))).notice(on);
	}

	@Override
	public String toString() {
		return "" + first + " => " + second;
	}

	@Override
	public String toString(IokeObject obj) {
		return "" + first + " => " + second;
	}

	public String inspect(Object obj) throws ControlFlow {
		StringBuilder sb = new StringBuilder();

		sb.append(IokeObject.inspect(first));
		sb.append(" => ");
		sb.append(IokeObject.inspect(second));

		return sb.toString();
	}

	public String notice(Object obj) throws ControlFlow {
		StringBuilder sb = new StringBuilder();

		sb.append(IokeObject.notice(first));
		sb.append(" => ");
		sb.append(IokeObject.notice(second));

		return sb.toString();
	}
}// Pair
