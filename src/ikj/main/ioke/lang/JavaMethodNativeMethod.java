/*
 * See LICENSE file in distribution for copyright and licensing
 * information.
 */
package ioke.lang;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import ioke.lang.exceptions.ControlFlow;

/**
 *
 * @author <a href="mailto:ola.bini@gmail.com">Ola Bini</a>
 */
public class JavaMethodNativeMethod extends ioke.lang.Method
		implements NativeImplementedMethod {
	private Class					declaringClass;
	private Method[]				methods;
	private JavaArgumentsDefinition	arguments;

	public JavaMethodNativeMethod(Method[] methods) {
		super(methods[0].getName(), IokeData.TYPE_JAVA_METHOD);
		this.methods = methods;
		this.declaringClass = methods[0].getDeclaringClass();
		this.arguments = JavaArgumentsDefinition.createFrom(methods);
	}

	public String getArgumentsCode() {
		return "...";
	}

	public static Object activateFixed(IokeObject self, IokeObject context,
			IokeObject message, Object on) throws ControlFlow {
		JavaMethodNativeMethod nm = (JavaMethodNativeMethod) self.data;
		List<Object> args = new LinkedList<>();
		Method method = (Method) nm.arguments.getJavaArguments(context,
				message, on, args);
		return nm.activate(self, on, args, method, context, message);
	}

	public final Object activate(IokeObject self, Object on,
			List<Object> args, Method method, IokeObject context,
			IokeObject message) throws ControlFlow {
		try {
			if ((on instanceof IokeObject)
					&& (IokeObject.data(on) instanceof JavaWrapper)) {
				// System.err.println("Invoking " + method.getName() + " on
				// " + ((JavaWrapper)IokeObject.data(on)).getObject() + "["
				// +
				// ((JavaWrapper)IokeObject.data(on)).getObject().getClass().getName()
				// + "]");
				// System.err.println(" method: " + method);
				// System.err.println(" class : " + declaringClass);
				Object obj = ((JavaWrapper) IokeObject.data(on))
						.getObject();
				if (!(declaringClass.isInstance(obj))) {
					obj = obj.getClass();
				}

				Object result = method.invoke(obj, args.toArray());
				if (result == null) {
					return context.runtime.nil;
				} else if (result instanceof Boolean) {
					return ((Boolean) result).booleanValue()
							? context.runtime._true
							: context.runtime._false;
				}
				return result;
			}
			// System.err.println("Invoking " + method.getName() + " on
			// " + on + "[" + on.getClass().getName() + "]");
			// System.err.println(" method: " + method);
			// System.err.println(" class : " + declaringClass);
			Object obj = on;
			if (!(declaringClass.isInstance(obj))) {
				obj = obj.getClass();
			}
			Object result = method.invoke(obj, args.toArray());
			if (result == null) {
				return context.runtime.nil;
			} else if (result instanceof Boolean) {
				return ((Boolean) result).booleanValue()
						? context.runtime._true : context.runtime._false;
			}
			return result;
		} catch (Exception e) {
			if ((Exception) e.getCause() != null) {
				context.runtime.reportNativeException(
						(Exception) e.getCause(), message, context);
			} else {
				context.runtime.reportNativeException(e, message, context);
			}

			return context.runtime.nil;
		}
	}

	@Override
	public String inspect(Object self) {
		return "method(" + methods[0].getDeclaringClass().getName() + "_"
				+ methods[0].getName() + ")";
	}
}
