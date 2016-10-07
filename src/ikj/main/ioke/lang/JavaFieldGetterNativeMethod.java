/*
 * See LICENSE file in distribution for copyright and licensing
 * information.
 */
package ioke.lang;

import java.lang.reflect.Field;

import ioke.lang.exceptions.ControlFlow;

/**
 *
 * @author <a href="mailto:ola.bini@gmail.com">Ola Bini</a>
 */
public class JavaFieldGetterNativeMethod extends Method
		implements NativeImplementedMethod {
	private Class	declaringClass;
	private Field	field;

	public JavaFieldGetterNativeMethod(Field field) {
		super(field.getName(), IokeData.TYPE_JAVA_FIELD_GETTER);
		this.field = field;
		this.declaringClass = field.getDeclaringClass();
	}

	public String getArgumentsCode() {
		return "...";
	}

	public static Object activateFixed(IokeObject self, IokeObject context,
			IokeObject message, Object on) throws ControlFlow {
		JavaFieldGetterNativeMethod nm = (JavaFieldGetterNativeMethod) self.data;
		try {
			if ((on instanceof IokeObject)
					&& (IokeObject.data(on) instanceof JavaWrapper)) {
				Object obj = ((JavaWrapper) IokeObject.data(on))
						.getObject();
				if (!(nm.declaringClass.isInstance(obj))) {
					obj = obj.getClass();
				}

				Object result = nm.field.get(obj);
				if (result == null) {
					return context.runtime.nil;
				} else if (result instanceof Boolean) {
					return ((Boolean) result).booleanValue()
							? context.runtime._true
							: context.runtime._false;
				}
				return result;
			} else {
				Object obj = on;
				if (!(nm.declaringClass.isInstance(obj))) {
					obj = obj.getClass();
				}

				Object result = nm.field.get(obj);
				if (result == null) {
					return context.runtime.nil;
				} else if (result instanceof Boolean) {
					return ((Boolean) result).booleanValue()
							? context.runtime._true
							: context.runtime._false;
				}
				return result;
			}
		} catch (Exception e) {
			context.runtime.reportNativeException(e, message, context);
			return context.runtime.nil;
		}
	}

	@Override
	public String inspect(Object self) {
		return "method(" + declaringClass.getName() + "_" + field.getName()
				+ ")";
	}
}// JavaFieldGetterNativeMethod
