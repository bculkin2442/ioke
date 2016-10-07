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
 *
 * @author <a href="mailto:ola.bini@gmail.com">Ola Bini</a>
 */
public abstract class IokeData {
	public final int		type;

	public final static int	TYPE_NONE				= 0;
	public final static int	TYPE_DEFAULT_METHOD		= 1;
	public final static int	TYPE_DEFAULT_MACRO		= 2;
	public final static int	TYPE_DEFAULT_SYNTAX		= 3;
	public final static int	TYPE_LEXICAL_MACRO		= 4;
	public final static int	TYPE_ALIAS_METHOD		= 5;
	public final static int	TYPE_NATIVE_METHOD		= 6;
	public final static int	TYPE_JAVA_CONSTRUCTOR	= 7;
	public final static int	TYPE_JAVA_FIELD_GETTER	= 8;
	public final static int	TYPE_JAVA_FIELD_SETTER	= 9;
	public final static int	TYPE_JAVA_METHOD		= 10;
	public final static int	TYPE_METHOD_PROTOTYPE	= 11;
	public final static int	TYPE_LEXICAL_BLOCK		= 12;

	public IokeData() {
		this(TYPE_NONE);
	}

	public IokeData(final int type) {
		this.type = type;
	}

	public final static IokeData	None	= new IokeData() {
											};

	public final static IokeData	Nil		= new IokeData() {
												@Override
												public void init(
														IokeObject obj) {
													obj.setKind("nil");
													obj.body.flags |= IokeObject.NIL_F
															| IokeObject.FALSY_F;
												}

												@Override
												public void checkMimic(
														IokeObject obj,
														IokeObject m,
														IokeObject context)
														throws ControlFlow {
													final IokeObject condition = IokeObject
															.as(IokeObject
																	.getCellChain(
																			context.runtime.condition,
																			m,
																			context,
																			"Error",
																			"CantMimicOddball"),
																	context)
															.mimic(m,
																	context);
													condition.setCell(
															"message", m);
													condition.setCell(
															"context",
															context);
													condition.setCell(
															"receiver",
															obj);
													context.runtime
															.errorCondition(
																	condition);
												}

												@Override
												public String toString(
														IokeObject self) {
													return "nil";
												}
											};

	public final static IokeData	False	= new IokeData() {
												@Override
												public void init(
														IokeObject obj) {
													obj.setKind("false");
													obj.body.flags |= IokeObject.FALSY_F;
												}

												@Override
												public void checkMimic(
														IokeObject obj,
														IokeObject m,
														IokeObject context)
														throws ControlFlow {
													final IokeObject condition = IokeObject
															.as(IokeObject
																	.getCellChain(
																			context.runtime.condition,
																			m,
																			context,
																			"Error",
																			"CantMimicOddball"),
																	context)
															.mimic(m,
																	context);
													condition.setCell(
															"message", m);
													condition.setCell(
															"context",
															context);
													condition.setCell(
															"receiver",
															obj);
													context.runtime
															.errorCondition(
																	condition);
												}

												@Override
												public String toString(
														IokeObject self) {
													return "false";
												}
											};

	public final static IokeData	True	= new IokeData() {
												@Override
												public void init(
														IokeObject obj) {
													obj.setKind("true");
												}

												@Override
												public void checkMimic(
														IokeObject obj,
														IokeObject m,
														IokeObject context)
														throws ControlFlow {
													final IokeObject condition = IokeObject
															.as(IokeObject
																	.getCellChain(
																			context.runtime.condition,
																			m,
																			context,
																			"Error",
																			"CantMimicOddball"),
																	context)
															.mimic(m,
																	context);
													condition.setCell(
															"message", m);
													condition.setCell(
															"context",
															context);
													condition.setCell(
															"receiver",
															obj);
													context.runtime
															.errorCondition(
																	condition);
												}

												@Override
												public String toString(
														IokeObject self) {
													return "true";
												}
											};

	public void init(IokeObject obj) throws ControlFlow {
	}

	public void checkMimic(IokeObject obj, IokeObject m,
			IokeObject context) throws ControlFlow {
	}

	public boolean isMessage() {
		return false;
	}

	public boolean isSymbol() {
		return false;
	}

	public IokeObject negate(IokeObject obj) {
		return obj;
	}

	public final boolean isEqualTo(IokeObject self, Object other)
			throws ControlFlow {
		Object cell = IokeObject.findCell(self, "==");

		if (cell == self.runtime.nul) {
			boolean result = (other instanceof IokeObject)
					&& (self.body == IokeObject.as(other, self).body);
			return result;
		}
		boolean result = IokeObject.isTrue(Interpreter.send(
				self.runtime.eqMessage, self.runtime.ground, self,
				self.runtime.createMessage(
						Message.wrap(IokeObject.as(other, self)))));
		return result;
	}

	public final int hashCode(IokeObject self) throws ControlFlow {
		Object cell = IokeObject.findCell(self, "hash");

		if (cell == self.runtime.nul) {
			return System.identityHashCode(self.body);
		}
		return Number.extractInt(
				Interpreter.send(self.runtime.hashMessage,
						self.runtime.ground, self),
				self.runtime.hashMessage, self.runtime.ground);
	}

	public IokeData cloneData(IokeObject obj, IokeObject m,
			IokeObject context) {
		return this;
	}

	public Object convertTo(IokeObject self, String kind,
			boolean signalCondition, String conversionMethod,
			IokeObject message, final IokeObject context)
			throws ControlFlow {
		if (IokeObject.isKind(self, kind, context)) {
			return self;
		}
		if (signalCondition) {
			final IokeObject condition = IokeObject
					.as(IokeObject.getCellChain(context.runtime.condition,
							message, context, "Error", "Type",
							"IncorrectType"), context)
					.mimic(message, context);
			condition.setCell("message", message);
			condition.setCell("context", context);
			condition.setCell("receiver", self);
			condition.setCell("expectedType",
					context.runtime.getSymbol(kind));

			final Object[] newCell = new Object[] { self };

			context.runtime.withRestartReturningArguments(
					new RunnableWithControlFlow() {
						@Override
						public void run() throws ControlFlow {
							context.runtime.errorCondition(condition);
						}
					}, context,
					new Restart.ArgumentGivingRestart("useValue") {
						@Override
						public List<String> getArgumentNames() {
							return new ArrayList<>(
									Arrays.asList("newValue"));
						}

						@Override
						public IokeObject invoke(IokeObject context,
								List<Object> arguments)
								throws ControlFlow {
							newCell[0] = arguments.get(0);
							return context.runtime.nil;
						}
					});

			return IokeObject.convertTo(newCell[0], kind, signalCondition,
					conversionMethod, message, context);
		}
		return null;
	}

	public Object convertTo(IokeObject self, Object mimic,
			boolean signalCondition, String conversionMethod,
			IokeObject message, final IokeObject context)
			throws ControlFlow {
		if (IokeObject.isMimic(self, IokeObject.as(mimic, context),
				context)) {
			return self;
		}
		if (signalCondition) {
			final IokeObject condition = IokeObject
					.as(IokeObject.getCellChain(context.runtime.condition,
							message, context, "Error", "Type",
							"IncorrectType"), context)
					.mimic(message, context);
			condition.setCell("message", message);
			condition.setCell("context", context);
			condition.setCell("receiver", self);
			condition.setCell("expectedType", mimic);

			final Object[] newCell = new Object[] { self };

			context.runtime.withRestartReturningArguments(
					new RunnableWithControlFlow() {
						@Override
						public void run() throws ControlFlow {
							context.runtime.errorCondition(condition);
						}
					}, context,
					new Restart.ArgumentGivingRestart("useValue") {
						@Override
						public List<String> getArgumentNames() {
							return new ArrayList<>(
									Arrays.asList("newValue"));
						}

						@Override
						public IokeObject invoke(IokeObject context,
								List<Object> arguments)
								throws ControlFlow {
							newCell[0] = arguments.get(0);
							return context.runtime.nil;
						}
					});

			return IokeObject.convertTo(mimic, newCell[0], signalCondition,
					conversionMethod, message, context);
		}
		return null;
	}

	public IokeObject convertToRational(IokeObject self, IokeObject m,
			final IokeObject context, boolean signalCondition)
			throws ControlFlow {
		if (signalCondition) {
			final IokeObject condition = IokeObject.as(
					IokeObject.getCellChain(context.runtime.condition, m,
							context, "Error", "Type", "IncorrectType"),
					context).mimic(m, context);
			condition.setCell("message", m);
			condition.setCell("context", context);
			condition.setCell("receiver", self);
			condition.setCell("expectedType",
					context.runtime.getSymbol("Rational"));

			final Object[] newCell = new Object[] { self };

			context.runtime.withRestartReturningArguments(
					new RunnableWithControlFlow() {
						@Override
						public void run() throws ControlFlow {
							context.runtime.errorCondition(condition);
						}
					}, context,
					new Restart.ArgumentGivingRestart("useValue") {
						@Override
						public List<String> getArgumentNames() {
							return new ArrayList<>(
									Arrays.asList("newValue"));
						}

						@Override
						public IokeObject invoke(IokeObject context,
								List<Object> arguments)
								throws ControlFlow {
							newCell[0] = arguments.get(0);
							return context.runtime.nil;
						}
					});

			return IokeObject.convertToRational(newCell[0], m, context,
					signalCondition);
		}
		return null;
	}

	public IokeObject convertToDecimal(IokeObject self, IokeObject m,
			final IokeObject context, boolean signalCondition)
			throws ControlFlow {
		if (signalCondition) {
			final IokeObject condition = IokeObject.as(
					IokeObject.getCellChain(context.runtime.condition, m,
							context, "Error", "Type", "IncorrectType"),
					context).mimic(m, context);
			condition.setCell("message", m);
			condition.setCell("context", context);
			condition.setCell("receiver", self);
			condition.setCell("expectedType",
					context.runtime.getSymbol("Decimal"));

			final Object[] newCell = new Object[] { self };

			context.runtime.withRestartReturningArguments(
					new RunnableWithControlFlow() {
						@Override
						public void run() throws ControlFlow {
							context.runtime.errorCondition(condition);
						}
					}, context,
					new Restart.ArgumentGivingRestart("useValue") {
						@Override
						public List<String> getArgumentNames() {
							return new ArrayList<>(
									Arrays.asList("newValue"));
						}

						@Override
						public IokeObject invoke(IokeObject context,
								List<Object> arguments)
								throws ControlFlow {
							newCell[0] = arguments.get(0);
							return context.runtime.nil;
						}
					});

			return IokeObject.convertToDecimal(newCell[0], m, context,
					signalCondition);
		}
		return null;
	}

	public IokeObject convertToNumber(IokeObject self, IokeObject m,
			final IokeObject context) throws ControlFlow {
		final IokeObject condition = IokeObject.as(
				IokeObject.getCellChain(context.runtime.condition, m,
						context, "Error", "Type", "IncorrectType"),
				context).mimic(m, context);
		condition.setCell("message", m);
		condition.setCell("context", context);
		condition.setCell("receiver", self);
		condition.setCell("expectedType",
				context.runtime.getSymbol("Number"));

		final Object[] newCell = new Object[] { self };

		context.runtime.withRestartReturningArguments(
				new RunnableWithControlFlow() {
					@Override
					public void run() throws ControlFlow {
						context.runtime.errorCondition(condition);
					}
				}, context, new Restart.ArgumentGivingRestart("useValue") {
					@Override
					public List<String> getArgumentNames() {
						return new ArrayList<>(Arrays.asList("newValue"));
					}

					@Override
					public IokeObject invoke(IokeObject context,
							List<Object> arguments) throws ControlFlow {
						newCell[0] = arguments.get(0);
						return context.runtime.nil;
					}
				});

		return IokeObject.convertToNumber(newCell[0], m, context);
	}

	public IokeObject tryConvertToText(IokeObject self, IokeObject m,
			final IokeObject context) throws ControlFlow {
		return null;
	}

	public IokeObject convertToText(IokeObject self, IokeObject m,
			final IokeObject context, boolean signalCondition)
			throws ControlFlow {
		if (signalCondition) {
			final IokeObject condition = IokeObject.as(
					IokeObject.getCellChain(context.runtime.condition, m,
							context, "Error", "Type", "IncorrectType"),
					context).mimic(m, context);
			condition.setCell("message", m);
			condition.setCell("context", context);
			condition.setCell("receiver", self);
			condition.setCell("expectedType",
					context.runtime.getSymbol("Text"));

			final Object[] newCell = new Object[] { self };

			context.runtime.withRestartReturningArguments(
					new RunnableWithControlFlow() {
						@Override
						public void run() throws ControlFlow {
							context.runtime.errorCondition(condition);
						}
					}, context,
					new Restart.ArgumentGivingRestart("useValue") {
						@Override
						public List<String> getArgumentNames() {
							return new ArrayList<>(
									Arrays.asList("newValue"));
						}

						@Override
						public IokeObject invoke(IokeObject context,
								List<Object> arguments)
								throws ControlFlow {
							newCell[0] = arguments.get(0);
							return context.runtime.nil;
						}
					});

			return IokeObject.convertToText(newCell[0], m, context,
					signalCondition);
		}
		return null;
	}

	public IokeObject convertToSymbol(IokeObject self, IokeObject m,
			final IokeObject context, final boolean signalCondition)
			throws ControlFlow {
		if (signalCondition) {
			final IokeObject condition = IokeObject.as(
					IokeObject.getCellChain(context.runtime.condition, m,
							context, "Error", "Type", "IncorrectType"),
					context).mimic(m, context);
			condition.setCell("message", m);
			condition.setCell("context", context);
			condition.setCell("receiver", self);
			condition.setCell("expectedType",
					context.runtime.getSymbol("Symbol"));

			final Object[] newCell = new Object[] { self };

			context.runtime.withRestartReturningArguments(
					new RunnableWithControlFlow() {
						@Override
						public void run() throws ControlFlow {
							context.runtime.errorCondition(condition);
						}
					}, context,
					new Restart.ArgumentGivingRestart("useValue") {
						@Override
						public List<String> getArgumentNames() {
							return new ArrayList<>(
									Arrays.asList("newValue"));
						}

						@Override
						public IokeObject invoke(IokeObject context,
								List<Object> arguments)
								throws ControlFlow {
							newCell[0] = arguments.get(0);
							return context.runtime.nil;
						}
					});

			return IokeObject.convertToSymbol(newCell[0], m, context,
					signalCondition);
		}
		return null;
	}

	public IokeObject convertToRegexp(IokeObject self, IokeObject m,
			final IokeObject context) throws ControlFlow {
		final IokeObject condition = IokeObject.as(
				IokeObject.getCellChain(context.runtime.condition, m,
						context, "Error", "Type", "IncorrectType"),
				context).mimic(m, context);
		condition.setCell("message", m);
		condition.setCell("context", context);
		condition.setCell("receiver", self);
		condition.setCell("expectedType",
				context.runtime.getSymbol("Regexp"));

		final Object[] newCell = new Object[] { self };

		context.runtime.withRestartReturningArguments(
				new RunnableWithControlFlow() {
					@Override
					public void run() throws ControlFlow {
						context.runtime.errorCondition(condition);
					}
				}, context, new Restart.ArgumentGivingRestart("useValue") {
					@Override
					public List<String> getArgumentNames() {
						return new ArrayList<>(Arrays.asList("newValue"));
					}

					@Override
					public IokeObject invoke(IokeObject context,
							List<Object> arguments) throws ControlFlow {
						newCell[0] = arguments.get(0);
						return context.runtime.nil;
					}
				});

		return IokeObject.convertToRegexp(newCell[0], m, context);
	}

	private static void report(Object self, IokeObject context,
			IokeObject message, String name) throws ControlFlow {
		IokeObject condition = IokeObject.as(
				IokeObject.getCellChain(context.runtime.condition, message,
						context, "Error", "Invocation", "NotActivatable"),
				context).mimic(message, context);
		condition.setCell("message", message);
		condition.setCell("context", context);
		condition.setCell("receiver", self);
		condition.setCell("methodName", context.runtime.getSymbol(name));
		context.runtime.errorCondition(condition);
	}

	public static Object activateFixed(IokeObject self, IokeObject context,
			IokeObject message, Object on) throws ControlFlow {
		Object cell = IokeObject.findCell(self, "activate");
		if (cell == context.runtime.nul) {
			report(self, context, message, "activate");
			return context.runtime.nil;
		}
		IokeObject newMessage = Message.deepCopy(message);
		newMessage.getArguments().clear();
		newMessage.getArguments()
				.add(context.runtime.createMessage(Message.wrap(context)));
		newMessage.getArguments()
				.add(context.runtime.createMessage(Message.wrap(message)));
		newMessage.getArguments().add(context.runtime
				.createMessage(Message.wrap(IokeObject.as(on, context))));
		return Interpreter.getOrActivate(cell, context, newMessage, self);
	}

	public List<Object> getArguments(IokeObject self) throws ControlFlow {
		report(self, self, self, "getArguments");
		return null;
	}

	public int getArgumentCount(IokeObject self) throws ControlFlow {
		report(self, self, self, "getArgumentCount");
		return -1;
	}

	public String getName(IokeObject self) throws ControlFlow {
		report(self, self, self, "getName");
		return null;
	}

	public String getFile(IokeObject self) throws ControlFlow {
		report(self, self, self, "getFile");
		return null;
	}

	public int getLine(IokeObject self) throws ControlFlow {
		report(self, self, self, "getLine");
		return -1;
	}

	public int getPosition(IokeObject self) throws ControlFlow {
		report(self, self, self, "getPosition");
		return -1;
	}

	public String toString(IokeObject self) {
		try {
			int h = hashCode(self);
			String hash = Integer.toHexString(h).toUpperCase();
			if (self == self.runtime.nul) {
				return "#<nul:" + hash + ">";
			}

			Object obj = Interpreter.send(self.runtime.kindMessage,
					self.runtime.ground, self);
			String kind = ((Text) IokeObject.data(obj)).getText();
			return "#<" + kind + ":" + hash + ">";
		} catch (ControlFlow e) {
		}
		return "an error happened somewhere";
	}

	public String getConvertMethod() {
		return null;
	}
}// IokeData
