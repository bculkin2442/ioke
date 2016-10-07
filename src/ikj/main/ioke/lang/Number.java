/*
 * See LICENSE file in distribution for copyright and licensing
 * information.
 */
package ioke.lang;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gnu.math.BigSquareRoot;
import gnu.math.BitOps;
import gnu.math.Complex;
import gnu.math.DFloNum;
import gnu.math.IntFraction;
import gnu.math.IntNum;
import gnu.math.RatNum;
import gnu.math.RealNum;
import ioke.lang.exceptions.ControlFlow;

/**
 *
 * @author <a href="mailto:ola.bini@gmail.com">Ola Bini</a>
 */
public class Number extends IokeData {
	private final RatNum	value;
	private final boolean	kind;

	public Number(RatNum value) {
		this.value = value;
		kind = false;
	}

	public static IntNum getFrom(long javaNumber) {
		return IntNum.make(javaNumber);
	}

	public static IntNum getFrom(String textRepresentation) {
		if (textRepresentation.startsWith("0x")
				|| textRepresentation.startsWith("0X")) {
			return IntNum.valueOf(textRepresentation.substring(2), 16);
		} else {
			return IntNum.valueOf(textRepresentation);
		}
	}

	private Number() {
		this.value = IntNum.make(0);
		kind = true;
	}

	public static Number integer(String val) {
		return new Number(getFrom(val));
	}

	public static Number integer(long val) {
		return new Number(getFrom(val));
	}

	public static Number integer(IntNum val) {
		return new Number(val);
	}

	public static Number ratio(IntFraction val) {
		return new Number(val);
	}

	public String asJavaString() {
		return value.toString();
	}

	public int asJavaInteger() {
		return value.intValue();
	}

	public long asJavaLong() {
		return value.longValue();
	}

	public RatNum getValue() {
		return value;
	}

	@Override
	public IokeObject negate(IokeObject obj) {
		return obj.runtime
				.newNumber((RatNum) RatNum.neg(Number.value(obj)));
	}

	@Override
	public String toString() {
		return asJavaString();
	}

	@Override
	public String toString(IokeObject obj) {
		return asJavaString();
	}

	public static String getInspect(Object on) {
		return ((Number) (IokeObject.data(on))).inspect(on);
	}

	public String inspect(Object obj) {
		return asJavaString();
	}

	@Override
	public IokeObject convertToNumber(IokeObject self, IokeObject m,
			IokeObject context) {
		return self;
	}

	@Override
	public IokeObject convertToRational(IokeObject self, IokeObject m,
			final IokeObject context, boolean signalCondition)
			throws ControlFlow {
		return self;
	}

	@Override
	public IokeObject convertToDecimal(IokeObject self, IokeObject m,
			final IokeObject context, boolean signalCondition)
			throws ControlFlow {
		return context.runtime.newDecimal(this);
	}

	public static RatNum value(Object number) {
		return ((Number) IokeObject.data(number)).value;
	}

	public static IntNum intValue(Object number) {
		return (IntNum) ((Number) IokeObject.data(number)).value;
	}

	public static int extractInt(Object number, IokeObject m,
			IokeObject context) throws ControlFlow {
		if (!(IokeObject.data(number) instanceof Number)) {
			number = IokeObject.convertToNumber(number, m, context);
		}

		return intValue(number).intValue();
	}

	@Override
	public void init(IokeObject obj) throws ControlFlow {
		final Runtime runtime = obj.runtime;
		final IokeObject number = obj;

		obj.setKind("Number");
		obj.mimics(IokeObject
				.as(runtime.mixins.getCell(null, null, "Comparing"), obj),
				runtime.nul, runtime.nul);

		IokeObject real = new IokeObject(runtime,
				"A real number can be either a rational number or a decimal number",
				new Number());
		real.singleMimicsWithoutCheck(number);
		real.setKind("Number Real");
		number.registerCell("Real", real);

		final IokeObject rational = new IokeObject(runtime,
				"A rational number is either an integer or a ratio",
				new Number());
		rational.singleMimicsWithoutCheck(real);
		rational.setKind("Number Rational");
		number.registerCell("Rational", rational);

		final IokeObject integer = new IokeObject(runtime,
				"An integral number", new Number());
		integer.singleMimicsWithoutCheck(rational);
		integer.setKind("Number Integer");
		number.registerCell("Integer", integer);
		runtime.integer = integer;

		final IokeObject ratio = new IokeObject(runtime,
				"A ratio of two integral numbers", new Number());
		ratio.singleMimicsWithoutCheck(rational);
		ratio.setKind("Number Ratio");
		number.registerCell("Ratio", ratio);
		runtime.ratio = ratio;

		IokeObject decimal = new IokeObject(runtime,
				"An exact, unlimited representation of a decimal number",
				new Decimal(BigDecimal.ZERO));
		decimal.singleMimicsWithoutCheck(real);
		decimal.init();
		number.registerCell("Decimal", decimal);

		final IokeObject infinity = new IokeObject(runtime,
				"A value representing infinity",
				new Number(RatNum.infinity(1)));
		infinity.singleMimicsWithoutCheck(ratio);
		infinity.setKind("Number Infinity");
		number.registerCell("Infinity", infinity);
		runtime.infinity = infinity;

		final IokeObject infinity2 = new IokeObject(runtime,
				"A value representing infinity",
				new Number(RatNum.infinity(1)));
		infinity2.singleMimicsWithoutCheck(ratio);
		infinity2.setKind("Number \u221E");
		number.registerCell("\u221E", infinity2);

		number.registerMethod(
				runtime.newNativeMethod("returns a hash for the number",
						new NativeMethod.WithNoArguments("hash") {
							@Override
							public Object activate(IokeObject method,
									IokeObject context, IokeObject message,
									Object on) throws ControlFlow {
								getArguments().getEvaluatedArguments(
										context, message, on,
										new ArrayList<Object>(),
										new HashMap<String, Object>());
								return context.runtime
										.newNumber(((Number) IokeObject
												.data(on)).value
														.hashCode());
							}
						}));

		rational.registerMethod(runtime.newNativeMethod(
				"returns the square root of the receiver. this should return the same result as calling ** with 0.5",
				new NativeMethod.WithNoArguments("sqrt") {
					@Override
					public Object activate(IokeObject method,
							IokeObject context, IokeObject message,
							Object on) throws ControlFlow {
						getArguments().getEvaluatedArguments(context,
								message, on, new ArrayList<Object>(),
								new HashMap<String, Object>());

						RatNum value = ((Number) IokeObject
								.data(on)).value;
						if (value instanceof IntFraction) {
							IntNum num = value.numerator();
							IntNum den = value.denominator();
							BigDecimal nums = new BigSquareRoot()
									.get(num.asBigDecimal());
							BigDecimal dens = new BigSquareRoot()
									.get(den.asBigDecimal());
							try {
								num = IntNum.valueOf(nums
										.toBigIntegerExact().toString());
								den = IntNum.valueOf(dens
										.toBigIntegerExact().toString());
								return context.runtime.newNumber(
										new IntFraction(num, den));
							} catch (ArithmeticException e) {
								// Ignore and fall through
							}
						}

						if (RatNum.compare(value, IntNum.zero()) < 1) {
							final IokeObject condition = IokeObject
									.as(IokeObject.getCellChain(
											context.runtime.condition,
											message, context, "Error",
											"Arithmetic"), context)
									.mimic(message, context);
							condition.setCell("message", message);
							condition.setCell("context", context);
							condition.setCell("receiver", on);

							context.runtime.errorCondition(condition);
						}

						return context.runtime
								.newDecimal(new BigSquareRoot()
										.get(value.asBigDecimal()));
					}
				}));

		number.registerMethod(runtime.newNativeMethod(
				"returns true if the left hand side number is equal to the right hand side number.",
				new TypeCheckingNativeMethod("==") {
					private final TypeCheckingArgumentsDefinition ARGUMENTS = TypeCheckingArgumentsDefinition
							.builder().receiverMustMimic(runtime.number)
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
						Number d = (Number) IokeObject.data(on);
						Object other = args.get(0);
						return ((other instanceof IokeObject)
								&& (IokeObject
										.data(other) instanceof Number)
								&& (((d.kind || ((Number) IokeObject
										.data(other)).kind)
												? on == other
												: d.value
														.equals(((Number) IokeObject
																.data(other)).value))))
																		? context.runtime._true
																		: context.runtime._false;
					}
				}));

		rational.registerMethod(runtime.newNativeMethod(
				"compares this number against the argument, returning -1, 0 or 1 based on which one is larger. if the argument is a decimal, the receiver will be converted into a form suitable for comparing against a decimal, and then compared - it's not specified whether this will actually call Decimal#<=> or not. if the argument is neither a Rational nor a Decimal, it tries to call asRational, and if that doesn't work it returns nil.",
				new TypeCheckingNativeMethod("<=>") {
					private final TypeCheckingArgumentsDefinition ARGUMENTS = TypeCheckingArgumentsDefinition
							.builder().receiverMustMimic(rational)
							.withRequiredPositional("other")
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
						Object arg = args.get(0);

						IokeData data = IokeObject.data(arg);

						if (data instanceof Decimal) {
							return context.runtime.newNumber(Number
									.value(on).asBigDecimal()
									.compareTo(Decimal.value(arg)));
						} else {
							if (!(data instanceof Number)) {
								arg = IokeObject.convertToRational(arg,
										message, context, false);
								if (!(IokeObject
										.data(arg) instanceof Number)) {
									// Can't compare, so bail out
									return context.runtime.nil;
								}
							}

							if (on == rational || arg == rational
									|| on == integer || arg == integer
									|| on == ratio || arg == ratio) {
								if (arg == on) {
									return context.runtime.newNumber(0);
								}
								return context.runtime.nil;
							}

							return context.runtime.newNumber(
									IntNum.compare(Number.value(on),
											Number.value(arg)));
						}
					}
				}));

		number.registerMethod(runtime.newNativeMethod(
				"compares this against the argument. should be overridden - in this case only used to check for equivalent number kinds",
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
						List<Object> args = new ArrayList<Object>();
						getArguments().getEvaluatedArguments(context,
								message, on, args,
								new HashMap<String, Object>());

						Object arg = args.get(0);
						if (on == arg) {
							return context.runtime._true;
						} else {
							return context.runtime._false;
						}
					}
				}));

		rational.registerMethod(runtime.newNativeMethod(
				"compares this number against the argument, true if this number is the same, otherwise false",
				new TypeCheckingNativeMethod("==") {
					private final TypeCheckingArgumentsDefinition ARGUMENTS = TypeCheckingArgumentsDefinition
							.builder().receiverMustMimic(number)
							.withRequiredPositional("other")
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
						Object arg = args.get(0);
						if (on == rational || arg == rational
								|| on == integer || arg == integer
								|| on == ratio || arg == ratio
								|| on == infinity || arg == infinity
								|| on == infinity2 || arg == infinity2) {
							if (arg == on) {
								return context.runtime._true;
							}
							return context.runtime._false;
						}
						if (IokeObject.data(arg) instanceof Decimal) {
							return (Number.value(on).asBigDecimal()
									.compareTo(Decimal.value(arg)) == 0)
											? context.runtime._true
											: context.runtime._false;
						} else if (IokeObject
								.data(arg) instanceof Number) {
							return IntNum.compare(Number.value(on),
									Number.value(arg)) == 0
											? context.runtime._true
											: context.runtime._false;
						} else {
							return context.runtime._false;
						}
					}
				}));

		rational.registerMethod(runtime.newNativeMethod(
				"returns the difference between this number and the argument. if the argument is a decimal, the receiver will be converted into a form suitable for subtracting against a decimal, and then subtracted. if the argument is neither a Rational nor a Decimal, it tries to call asRational, and if that fails it signals a condition.",
				new TypeCheckingNativeMethod("-") {
					private final TypeCheckingArgumentsDefinition ARGUMENTS = TypeCheckingArgumentsDefinition
							.builder().receiverMustMimic(number)
							.withRequiredPositional("subtrahend")
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
						Object arg = args.get(0);

						IokeData data = IokeObject.data(arg);

						if (data instanceof Decimal) {
							return Interpreter.send(
									context.runtime.minusMessage, context,
									context.runtime.newDecimal(
											((Number) IokeObject
													.data(on))),
									arg);
						} else {
							if (!(data instanceof Number)) {
								arg = IokeObject.convertToRational(arg,
										message, context, true);
							}

							return context.runtime
									.newNumber((RatNum) Number.value(on)
											.sub(Number.value(arg)));
						}
					}
				}));

		rational.registerMethod(runtime.newNativeMethod(
				"returns the addition of this number and the argument. if the argument is a decimal, the receiver will be converted into a form suitable for addition against a decimal, and then added. if the argument is neither a Rational nor a Decimal, it tries to call asRational, and if that fails it signals a condition.",
				new TypeCheckingNativeMethod("+") {
					private final TypeCheckingArgumentsDefinition ARGUMENTS = TypeCheckingArgumentsDefinition
							.builder().receiverMustMimic(number)
							.withRequiredPositional("addend")
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
						Object arg = args.get(0);

						IokeData data = IokeObject.data(arg);

						if (data instanceof Decimal) {
							return Interpreter.send(
									context.runtime.plusMessage, context,
									context.runtime.newDecimal(
											((Number) IokeObject
													.data(on))),
									arg);
						} else {
							if (!(data instanceof Number)) {
								arg = IokeObject.convertToRational(arg,
										message, context, true);
							}

							return context.runtime
									.newNumber(RatNum.add(Number.value(on),
											Number.value(arg), 1));
						}
					}
				}));

		rational.registerMethod(runtime.newNativeMethod(
				"returns the product of this number and the argument. if the argument is a decimal, the receiver will be converted into a form suitable for multiplying against a decimal, and then multiplied. if the argument is neither a Rational nor a Decimal, it tries to call asRational, and if that fails it signals a condition.",
				new TypeCheckingNativeMethod("*") {
					private final TypeCheckingArgumentsDefinition ARGUMENTS = TypeCheckingArgumentsDefinition
							.builder().receiverMustMimic(number)
							.withRequiredPositional("multiplier")
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
						Object arg = args.get(0);

						IokeData data = IokeObject.data(arg);

						if (data instanceof Decimal) {
							return Interpreter.send(
									context.runtime.multMessage, context,
									context.runtime.newDecimal(
											((Number) IokeObject
													.data(on))),
									arg);
						} else {
							if (!(data instanceof Number)) {
								arg = IokeObject.convertToRational(arg,
										message, context, true);
							}

							return context.runtime.newNumber(RatNum.times(
									Number.value(on), Number.value(arg)));
						}
					}
				}));

		rational.registerMethod(runtime.newNativeMethod(
				"returns the quotient of this number and the argument. if the division is not exact, it will return a Ratio.",
				new TypeCheckingNativeMethod("/") {
					private final TypeCheckingArgumentsDefinition ARGUMENTS = TypeCheckingArgumentsDefinition
							.builder().receiverMustMimic(number)
							.withRequiredPositional("dividend")
							.getArguments();

					@Override
					public TypeCheckingArgumentsDefinition getArguments() {
						return ARGUMENTS;
					}

					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							final IokeObject context, IokeObject message)
							throws ControlFlow {
						Object arg = args.get(0);

						IokeData data = IokeObject.data(arg);

						if (data instanceof Decimal) {
							return Interpreter.send(
									context.runtime.divMessage, context,
									context.runtime.newDecimal(
											((Number) IokeObject
													.data(on))),
									arg);
						} else {
							if (!(data instanceof Number)) {
								arg = IokeObject.convertToRational(arg,
										message, context, true);
							}

							while (Number.value(arg).isZero()) {
								final IokeObject condition = IokeObject
										.as(IokeObject.getCellChain(
												context.runtime.condition,
												message, context, "Error",
												"Arithmetic",
												"DivisionByZero"), context)
										.mimic(message, context);
								condition.setCell("message", message);
								condition.setCell("context", context);
								condition.setCell("receiver", on);

								final Object[] newCell = new Object[] {
										arg };

								context.runtime
										.withRestartReturningArguments(
												new RunnableWithControlFlow() {
													public void run()
															throws ControlFlow {
														context.runtime
																.errorCondition(
																		condition);
													}
												}, context,
												new Restart.ArgumentGivingRestart(
														"useValue") {
													public List<String> getArgumentNames() {
														return new ArrayList<String>(
																Arrays.asList(
																		"newValue"));
													}

													public IokeObject invoke(
															IokeObject c2,
															List<Object> arguments)
															throws ControlFlow {
														newCell[0] = arguments
																.get(0);
														return c2.runtime.nil;
													}
												});

								arg = newCell[0];
							}

							return context.runtime.newNumber(RatNum.divide(
									Number.value(on), Number.value(arg)));
						}
					}
				}));

		integer.registerMethod(runtime.newNativeMethod(
				"returns the modulo of this number and the argument",
				new TypeCheckingNativeMethod("%") {
					private final TypeCheckingArgumentsDefinition ARGUMENTS = TypeCheckingArgumentsDefinition
							.builder().receiverMustMimic(integer)
							.withRequiredPositional("dividend")
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
						Object arg = args.get(0);

						IokeData data = IokeObject.data(arg);

						if (!(data instanceof Number)) {
							arg = IokeObject.convertToRational(arg,
									message, context, true);
						}

						return context.runtime.newNumber(
								IntNum.modulo(Number.intValue(on),
										Number.intValue(arg)));
					}
				}));

		integer.registerMethod(runtime.newNativeMethod(
				"returns how many times the first number can be divided by the second one",
				new TypeCheckingNativeMethod("div") {
					private final TypeCheckingArgumentsDefinition ARGUMENTS = TypeCheckingArgumentsDefinition
							.builder().receiverMustMimic(integer)
							.withRequiredPositional("dividend")
							.getArguments();

					@Override
					public TypeCheckingArgumentsDefinition getArguments() {
						return ARGUMENTS;
					}

					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							final IokeObject context, IokeObject message)
							throws ControlFlow {
						Object arg = args.get(0);

						while (Number.value(arg).isZero()) {
							final IokeObject condition = IokeObject
									.as(IokeObject.getCellChain(
											context.runtime.condition,
											message, context, "Error",
											"Arithmetic",
											"DivisionByZero"), context)
									.mimic(message, context);
							condition.setCell("message", message);
							condition.setCell("context", context);
							condition.setCell("receiver", on);

							final Object[] newCell = new Object[] { arg };

							context.runtime.withRestartReturningArguments(
									new RunnableWithControlFlow() {
										public void run()
												throws ControlFlow {
											context.runtime.errorCondition(
													condition);
										}
									}, context,
									new Restart.ArgumentGivingRestart(
											"useValue") {
										public List<String> getArgumentNames() {
											return new ArrayList<String>(
													Arrays.asList(
															"newValue"));
										}

										public IokeObject invoke(
												IokeObject c2,
												List<Object> arguments)
												throws ControlFlow {
											newCell[0] = arguments.get(0);
											return c2.runtime.nil;
										}
									});

							arg = newCell[0];
						}

						IokeData data = IokeObject.data(arg);
						return context.runtime.newNumber(IntNum.quotient(
								Number.intValue(on), Number.intValue(arg),
								IntNum.TRUNCATE));
					}
				}));

		integer.registerMethod(runtime.newNativeMethod(
				"returns a tuple of how many times the first number can be divided by the second one, and the remainder",
				new TypeCheckingNativeMethod("divmod") {
					private final TypeCheckingArgumentsDefinition ARGUMENTS = TypeCheckingArgumentsDefinition
							.builder().receiverMustMimic(integer)
							.withRequiredPositional("dividend")
							.getArguments();

					@Override
					public TypeCheckingArgumentsDefinition getArguments() {
						return ARGUMENTS;
					}

					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							final IokeObject context, IokeObject message)
							throws ControlFlow {
						Object arg = args.get(0);

						while (Number.value(arg).isZero()) {
							final IokeObject condition = IokeObject
									.as(IokeObject.getCellChain(
											context.runtime.condition,
											message, context, "Error",
											"Arithmetic",
											"DivisionByZero"), context)
									.mimic(message, context);
							condition.setCell("message", message);
							condition.setCell("context", context);
							condition.setCell("receiver", on);

							final Object[] newCell = new Object[] { arg };

							context.runtime.withRestartReturningArguments(
									new RunnableWithControlFlow() {
										public void run()
												throws ControlFlow {
											context.runtime.errorCondition(
													condition);
										}
									}, context,
									new Restart.ArgumentGivingRestart(
											"useValue") {
										public List<String> getArgumentNames() {
											return new ArrayList<String>(
													Arrays.asList(
															"newValue"));
										}

										public IokeObject invoke(
												IokeObject c2,
												List<Object> arguments)
												throws ControlFlow {
											newCell[0] = arguments.get(0);
											return c2.runtime.nil;
										}
									});

							arg = newCell[0];
						}

						IokeData data = IokeObject.data(arg);

						IntNum q = new IntNum();
						IntNum r = new IntNum();
						IntNum.divide(Number.intValue(on),
								Number.intValue(arg), q, r,
								IntNum.TRUNCATE);

						return context.runtime.newTuple(
								context.runtime
										.newNumber(q.canonicalize()),
								context.runtime
										.newNumber(r.canonicalize()));
					}
				}));

		rational.registerMethod(runtime.newNativeMethod(
				"returns this number to the power of the argument",
				new TypeCheckingNativeMethod("**") {
					private final TypeCheckingArgumentsDefinition ARGUMENTS = TypeCheckingArgumentsDefinition
							.builder().receiverMustMimic(rational)
							.withRequiredPositional("exponent")
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
						Object arg = args.get(0);

						IokeData data = IokeObject.data(arg);

						if (!(data instanceof Number)) {
							if (data instanceof Decimal) {
								return context.runtime.newDecimal(
										((RealNum) (Complex.power(
												Number.value(on),
												new DFloNum(Decimal
														.value(arg).toString()))))
																.asBigDecimal());
							} else {
								arg = IokeObject.convertToRational(arg,
										message, context, true);
							}
						}

						return context.runtime.newNumber((RatNum) Number
								.value(on).power(Number.intValue(arg)));
					}
				}));

		integer.registerMethod(runtime.newNativeMethod(
				"returns this number bitwise and the argument",
				new TypeCheckingNativeMethod("&") {
					private final TypeCheckingArgumentsDefinition ARGUMENTS = TypeCheckingArgumentsDefinition
							.builder().receiverMustMimic(integer)
							.withRequiredPositional("other")
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
						Object arg = args.get(0);

						IokeData data = IokeObject.data(arg);

						if (!(data instanceof Number)) {
							arg = IokeObject.convertToRational(arg,
									message, context, true);
						}

						return context.runtime
								.newNumber(BitOps.and(Number.intValue(on),
										Number.intValue(arg)));
					}
				}));

		integer.registerMethod(runtime.newNativeMethod(
				"returns this number bitwise or the argument",
				new TypeCheckingNativeMethod("|") {
					private final TypeCheckingArgumentsDefinition ARGUMENTS = TypeCheckingArgumentsDefinition
							.builder().receiverMustMimic(integer)
							.withRequiredPositional("other")
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
						Object arg = args.get(0);

						IokeData data = IokeObject.data(arg);

						if (!(data instanceof Number)) {
							arg = IokeObject.convertToRational(arg,
									message, context, true);
						}

						return context.runtime
								.newNumber(BitOps.ior(Number.intValue(on),
										Number.intValue(arg)));
					}
				}));

		integer.registerMethod(runtime.newNativeMethod(
				"returns this number bitwise xor the argument",
				new TypeCheckingNativeMethod("^") {
					private final TypeCheckingArgumentsDefinition ARGUMENTS = TypeCheckingArgumentsDefinition
							.builder().receiverMustMimic(integer)
							.withRequiredPositional("other")
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
						Object arg = args.get(0);

						IokeData data = IokeObject.data(arg);

						if (!(data instanceof Number)) {
							arg = IokeObject.convertToRational(arg,
									message, context, true);
						}

						return context.runtime
								.newNumber(BitOps.xor(Number.intValue(on),
										Number.intValue(arg)));
					}
				}));

		integer.registerMethod(runtime.newNativeMethod(
				"returns this number left shifted by the argument",
				new TypeCheckingNativeMethod("<<") {
					private final TypeCheckingArgumentsDefinition ARGUMENTS = TypeCheckingArgumentsDefinition
							.builder().receiverMustMimic(integer)
							.withRequiredPositional("other")
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
						Object arg = args.get(0);

						IokeData data = IokeObject.data(arg);

						if (!(data instanceof Number)) {
							arg = IokeObject.convertToRational(arg,
									message, context, true);
						}

						return context.runtime.newNumber(
								IntNum.shift(Number.intValue(on),
										Number.intValue(arg).intValue()));
					}
				}));

		integer.registerMethod(runtime.newNativeMethod(
				"returns this number right shifted by the argument",
				new TypeCheckingNativeMethod(">>") {
					private final TypeCheckingArgumentsDefinition ARGUMENTS = TypeCheckingArgumentsDefinition
							.builder().receiverMustMimic(integer)
							.withRequiredPositional("other")
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
						Object arg = args.get(0);

						IokeData data = IokeObject.data(arg);

						if (!(data instanceof Number)) {
							arg = IokeObject.convertToRational(arg,
									message, context, true);
						}

						return context.runtime.newNumber(
								IntNum.shift(Number.intValue(on),
										-Number.intValue(arg).intValue()));
					}
				}));

		rational.registerMethod(runtime.newNativeMethod(
				"Returns a text representation of the object",
				new NativeMethod.WithNoArguments("asText") {
					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						return runtime.newText(on.toString());
					}
				}));

		rational.registerMethod(obj.runtime.newNativeMethod(
				"Returns a text inspection of the object",
				new TypeCheckingNativeMethod.WithNoArguments("inspect",
						number) {
					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						return method.runtime
								.newText(Number.getInspect(on));
					}
				}));

		rational.registerMethod(obj.runtime.newNativeMethod(
				"Returns a brief text inspection of the object",
				new TypeCheckingNativeMethod.WithNoArguments("notice",
						number) {
					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						return method.runtime
								.newText(Number.getInspect(on));
					}
				}));

		integer.registerMethod(runtime.newNativeMethod(
				"Returns the successor of this number",
				new TypeCheckingNativeMethod.WithNoArguments("succ",
						integer) {
					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						return runtime.newNumber(IntNum
								.add(Number.intValue(on), IntNum.one()));
					}
				}));

		integer.registerMethod(runtime.newNativeMethod(
				"Returns the predecessor of this number",
				new TypeCheckingNativeMethod.WithNoArguments("pred",
						integer) {
					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						return runtime.newNumber(IntNum
								.sub(Number.intValue(on), IntNum.one()));
					}
				}));

		infinity.registerMethod(obj.runtime.newNativeMethod(
				"Returns a text inspection of the object",
				new TypeCheckingNativeMethod.WithNoArguments("inspect",
						infinity) {
					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						return method.runtime.newText("Infinity");
					}
				}));

		infinity.registerMethod(obj.runtime.newNativeMethod(
				"Returns a brief text inspection of the object",
				new TypeCheckingNativeMethod.WithNoArguments("notice",
						infinity) {
					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						return method.runtime.newText("Infinity");
					}
				}));

		infinity2.registerMethod(obj.runtime.newNativeMethod(
				"Returns a text inspection of the object",
				new TypeCheckingNativeMethod.WithNoArguments("inspect",
						infinity2) {
					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						return method.runtime.newText("\u221E");
					}
				}));

		infinity2.registerMethod(obj.runtime.newNativeMethod(
				"Returns a brief text inspection of the object",
				new TypeCheckingNativeMethod.WithNoArguments("notice",
						infinity2) {
					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						return method.runtime.newText("\u221E");
					}
				}));

		integer.registerMethod(runtime.newNativeMethod(
				"Expects one or two arguments. If one argument is given, executes it as many times as the value of the receiving number. If two arguments are given, the first will be an unevaluated name that will receive the current loop value on each repitition. the iteration length is limited to the positive maximum of a Java int",
				new NativeMethod("times") {
					private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
							.builder()
							.withRequiredPositionalUnevaluated(
									"argumentNameOrCode")
							.withOptionalPositionalUnevaluated("code")
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

						int num = Number.value(context.runtime.integer
								.convertToThis(on, message, context))
								.intValue();
						switch (message.getArgumentCount()) {
							case 0:
								return runtime.nil;
							case 1: {
								Object result = runtime.nil;
								while (num > 0) {
									result = Interpreter
											.getEvaluatedArgument(message,
													0, context);
									num--;
								}
								return result;
							}
							default:
								int ix = 0;
								String name = ((IokeObject) Message
										.getArg1(message)).getName();
								Object result = runtime.nil;
								while (ix < num) {
									context.setCell(name, runtime
											.newNumber(IntNum.make(ix)));
									result = Interpreter
											.getEvaluatedArgument(message,
													1, context);
									ix++;
								}
								return result;
						}
					}
				}));

		integer.registerMethod(runtime.newNativeMethod(
				"Returns a Text that represents the character with the same character code of this number",
				new TypeCheckingNativeMethod.WithNoArguments("char",
						integer) {
					@Override
					public Object activate(IokeObject method, Object on,
							List<Object> args,
							Map<String, Object> keywords,
							IokeObject context, IokeObject message)
							throws ControlFlow {
						return runtime.newText(String.valueOf(
								(char) Number.intValue(on).intValue()));
					}
				}));
	}
}// Number
