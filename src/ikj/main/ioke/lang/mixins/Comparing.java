/*
 * See LICENSE file in distribution for copyright and licensing
 * information.
 */
package ioke.lang.mixins;

import ioke.lang.IokeObject;
import ioke.lang.Runtime;

/**
 *
 * @author <a href="mailto:ola.bini@gmail.com">Ola Bini</a>
 */
public class Comparing {
	public static void init(IokeObject comparing) {
		Runtime runtime = comparing.runtime;
		comparing.setKind("Mixins Comparing");
	}
}// Comparing
