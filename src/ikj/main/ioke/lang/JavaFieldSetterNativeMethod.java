/*
 * See LICENSE file in distribution for copyright and licensing
 * information.
 */
package ioke.lang;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import ioke.lang.exceptions.ControlFlow;

/**
 *
 * @author <a href="mailto:ola.bini@gmail.com">Ola Bini</a>
 */
public class JavaFieldSetterNativeMethod extends Method
		implements NativeImplementedMethod {
	private Class					declaringClass;
	private Field					field;
	private JavaArgumentsDefinition	arguments;

	public JavaFieldSetterNativeMethod(Field field) {
		super(field.getName() + "=", IokeData.TYPE_JAVA_FIELD_SETTER);
		this.field = field;
		this.declaringClass = field.getDeclaringClass();
		this.arguments = JavaArgumentsDefinition.createFrom(field);
	}

	public String getArgumentsCode() {
		return "...";
	}

	public static Object activateFixed(IokeObject self, IokeObject context,
			IokeObject message, Object on) throws ControlFlow {
		JavaFieldSetterNativeMethod nm = (JavaFieldSetterNativeMethod) self.data;
		List<Object> args = new LinkedList<>();
		nm.arguments.getJavaArguments(context, message, on, args);
		return nm.activate(self, on, args.get(0), context, message);
	}

	public Object activate(IokeObject self, Object on, Object arg,
			IokeObject context, IokeObject message) throws ControlFlow {
		try {
			if ((on instanceof IokeObject)
					&& (IokeObject.data(on) instanceof JavaWrapper)) {
				Object obj = ((JavaWrapper) IokeObject.data(on))
						.getObject();
				if (!(declaringClass.isInstance(obj))) {
					obj = obj.getClass();
				}

				field.set(obj, arg);

				Object result = arg;
				if (result == null) {
					return context.runtime.nil;
				} else if (result instanceof Boolean) {
					return ((Boolean) result).booleanValue()
							? context.runtime._true
							: context.runtime._false;
				}
				return result;
			}
			Object obj = on;
			if (!(declaringClass.isInstance(obj))) {
				obj = obj.getClass();
			}

			field.set(obj, arg);

			Object result = arg;
			if (result == null) {
				return context.runtime.nil;
			} else if (result instanceof Boolean) {
				return ((Boolean) result).booleanValue()
						? context.runtime._true : context.runtime._false;
			}
			return result;
		} catch (Exception e) {
			context.runtime.reportNativeException(e, message, context);
			return context.runtime.nil;
		}
	}

	@Override
	public String inspect(Object self) {
		return "method(" + declaringClass.getName() + "_" + field.getName()
				+ "=)";
	}
}// JavaFieldSetterNativeMethod
