/*
 * See LICENSE file in distribution for copyright and licensing
 * information.
 */
package ioke.lang;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ioke.lang.exceptions.ControlFlow;
import ioke.lang.java.JavaIntegration;

/**
 *
 * @author <a href="mailto:ola.bini@gmail.com">Ola Bini</a>
 */
public class JavaGround {
	public static void init(IokeObject obj) throws ControlFlow {
		final Runtime runtime = obj.runtime;
		obj.setKind("JavaGround");
		obj.mimicsWithoutCheck(IokeObject.as(
				runtime.defaultBehavior.body.get("BaseBehavior"), null));
		obj.setCell("mimics?", ((IokeObject) runtime.defaultBehavior.body
				.get("Reflection")).body.get("mimics?"));

		obj.registerCell("JavaArray", runtime.javaArray);

		obj.registerMethod(runtime.newNativeMethod(
				"takes an internal name for a Java type and returns that object.",
				new TypeCheckingNativeMethod("primitiveJavaClass!") {
					private final TypeCheckingArgumentsDefinition ARGUMENTS = TypeCheckingArgumentsDefinition
							.builder().withRequiredPositional("name")
							.whichMustMimic(runtime.text).getArguments();

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
						Object arg = args.get(0);
						String name = Text.getText(arg);
						Class<?> c = null;
						try {
							c = context.runtime.classRegistry
									.getClassLoader().loadClass(name);
						} catch (Exception e) {
							runtime.reportNativeException(e, message,
									context);
						}
						return runtime.registry.wrap(c);
					}
				}));

		obj.registerMethod(runtime.newNativeMethod(
				"takes a Java class and a list of dimensions and creates a new instance of the array with those dimensions",
				new TypeCheckingNativeMethod("createPrimitiveJavaArray!") {
					private final TypeCheckingArgumentsDefinition ARGUMENTS = TypeCheckingArgumentsDefinition
							.builder()
							.withRequiredPositional("componentType")
							.withRest("dimensions").getArguments();

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
						Object ctype = args.get(0);
						Class<?> type = (ctype instanceof IokeObject)
								? (Class<?>) JavaWrapper.getObject(ctype)
								: ((Class<?>) ctype);
						int[] dimensions = new int[args.size() - 1];
						for (int i = 0; i < dimensions.length; i++) {
							dimensions[i] = Number
									.intValue(args.get(i + 1)).intValue();
						}
						return Array.newInstance(type, dimensions);
					}
				}));

		obj.registerMethod(runtime.newNativeMethod(
				"expects to be invoked on a Java String, either wrapped or unwrapped.",
				new NativeMethod.WithNoArguments(
						"primitiveMagic: String->Text") {
					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						if (on instanceof String) {
							return runtime.newText((String) on);
						}
						return runtime.newText(
								(String) JavaWrapper.getObject(on));
					}
				}));

		obj.registerMethod(runtime.newNativeMethod(
				"expects to be invoked on a Java Integer, either wrapped or unwrapped.",
				new NativeMethod.WithNoArguments(
						"primitiveMagic: Integer->Rational") {
					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						if (on instanceof Integer) {
							return runtime
									.newNumber(((Integer) on).longValue());
						}
						return runtime.newNumber(
								((Integer) JavaWrapper.getObject(on))
										.longValue());
					}
				}));

		obj.registerMethod(runtime.newNativeMethod(
				"expects to be invoked on a Java Byte, either wrapped or unwrapped.",
				new NativeMethod.WithNoArguments(
						"primitiveMagic: Byte->Rational") {
					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						if (on instanceof Byte) {
							return runtime
									.newNumber(((Byte) on).longValue());
						}
						return runtime.newNumber(
								((Byte) JavaWrapper.getObject(on))
										.longValue());
					}
				}));

		obj.registerMethod(runtime.newNativeMethod(
				"expects to be invoked on a Java Short, either wrapped or unwrapped.",
				new NativeMethod.WithNoArguments(
						"primitiveMagic: Short->Rational") {
					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						if (on instanceof Short) {
							return runtime
									.newNumber(((Short) on).longValue());
						}
						return runtime.newNumber(
								((Short) JavaWrapper.getObject(on))
										.longValue());
					}
				}));

		obj.registerMethod(runtime.newNativeMethod(
				"expects to be invoked on a Java Long, either wrapped or unwrapped.",
				new NativeMethod.WithNoArguments(
						"primitiveMagic: Long->Rational") {
					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						if (on instanceof Long) {
							return runtime
									.newNumber(((Long) on).longValue());
						}
						return runtime.newNumber(
								((Long) JavaWrapper.getObject(on))
										.longValue());
					}
				}));

		obj.registerMethod(runtime.newNativeMethod(
				"expects to be invoked on a Java Character, either wrapped or unwrapped.",
				new NativeMethod.WithNoArguments(
						"primitiveMagic: Character->Rational") {
					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						if (on instanceof Character) {
							return runtime.newNumber(
									((Character) on).charValue());
						}
						return runtime.newNumber(
								((Character) JavaWrapper.getObject(on))
										.charValue());
					}
				}));

		obj.registerMethod(runtime.newNativeMethod(
				"expects to be invoked on a Java Float, either wrapped or unwrapped.",
				new NativeMethod.WithNoArguments(
						"primitiveMagic: Float->Decimal") {
					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						if (on instanceof Float) {
							return runtime.newDecimal(BigDecimal
									.valueOf(((Float) on).doubleValue()));
						}
						return runtime.newDecimal(BigDecimal.valueOf(
								((Float) JavaWrapper.getObject(on))
										.doubleValue()));
					}
				}));

		obj.registerMethod(runtime.newNativeMethod(
				"expects to be invoked on a Java Double, either wrapped or unwrapped.",
				new NativeMethod.WithNoArguments(
						"primitiveMagic: Double->Decimal") {
					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						if (on instanceof Double) {
							return runtime.newDecimal(BigDecimal
									.valueOf(((Double) on).doubleValue()));
						}
						return runtime.newDecimal(BigDecimal.valueOf(
								((Double) JavaWrapper.getObject(on))
										.doubleValue()));
					}
				}));

		obj.registerMethod(runtime.newNativeMethod(
				"expects to be invoked with one or more Java types and will return either a new or a cached implementation of a combination of these types.",
				new NativeMethod("integrate") {
					private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
							.builder().withRequiredPositional("firstType")
							.withRest("restTypes").getArguments();

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

						Class[] types = new Class[args.size()];
						int i = 0;
						for (Object a : args) {
							if (a instanceof IokeObject) {
								types[i++] = (Class) JavaWrapper
										.getObject(a);
							} else {
								types[i++] = (Class) a;
							}
						}

						Class newType = JavaIntegration.getOrCreate(types,
								context.runtime.classRegistry);
						// System.err.println(newType);
						return IokeRegistry.integratedWrap(newType,
								context);
					}
				}));
	}
}
