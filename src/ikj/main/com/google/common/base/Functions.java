/*
 * Copyright (C) 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.base;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.Map;

/**
 * Useful functions.
 *
 * <p>
 * All methods returns serializable functions as long as they're given
 * serializable parameters.
 *
 * @author Mike Bostock
 * @author Vlad Patryshev
 * @author Jared Levy
 */
public final class Functions {
	private Functions() {
	}

	/**
	 * Returns a function that calls {@code toString()} on its argument.
	 * The function does not accept nulls; it will throw a
	 * {@link NullPointerException} when applied to {@code null}.
	 */
	public static Function<Object, String> toStringFunction() {
		return ToStringFunction.INSTANCE;
	}

	// enum singleton pattern
	private enum ToStringFunction implements Function<Object, String> {
		INSTANCE;

		@Override
		public String apply(Object o) {
			return o.toString();
		}

		@Override
		public String toString() {
			return "toString";
		}
	}

	/**
	 * Returns the identity function.
	 */
	@SuppressWarnings("unchecked")
	public static <E> Function<E, E> identity() {
		return (Function<E, E>) IdentityFunction.INSTANCE;
	}

	// enum singleton pattern
	private enum IdentityFunction implements Function<Object, Object> {
		INSTANCE;

		@Override
		public Object apply(Object o) {
			return o;
		}

		@Override
		public String toString() {
			return "identity";
		}
	}

	/**
	 * Returns a function which performs a map lookup.
	 *
	 * <p>
	 * The difference between a map and a function is that a map is defined
	 * on a set of keys, while a function is defined on all inputs of the
	 * correct type. The function created by this method returns
	 * {@code null} for all inputs that do not belong to the map's key set.
	 *
	 * @param map
	 *            source map that determines the function behavior
	 * @return function that returns {@code map.get(a)} for each {@code a}
	 */
	public static <K, V> Function<K, V> forMap(Map<K, V> map) {
		return new FunctionForMapNoDefault<>(map);
	}

	private static class FunctionForMapNoDefault<K, V>
			implements Function<K, V>, Serializable {
		private final Map<K, V> map;

		public FunctionForMapNoDefault(Map<K, V> map) {
			this.map = checkNotNull(map);
		}

		@Override
		public V apply(K a) {
			return map.get(a);
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof FunctionForMapNoDefault) {
				FunctionForMapNoDefault<?, ?> that = (FunctionForMapNoDefault<?, ?>) o;
				return map.equals(that.map);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return map.hashCode();
		}

		@Override
		public String toString() {
			return "forMap(" + map + ")";
		}

		private static final long serialVersionUID = 0;
	}

	/**
	 * Returns a function which performs a map lookup with a default value.
	 * The function created by this method returns {@code defaultValue} for
	 * all inputs that do not belong to the map's key set.
	 *
	 * @param map
	 *            source map that determines the function behavior
	 * @param defaultValue
	 *            the value to return for inputs that aren't map keys
	 * @return function that returns {@code map.get(a)} when {@code a} is a
	 *         key, or {@code defaultValue} otherwise
	 */
	public static <K, V> Function<K, V> forMap(Map<K, ? extends V> map,
			V defaultValue) {
		return new ForMapWithDefault<>(map, defaultValue);
	}

	private static class ForMapWithDefault<K, V>
			implements Function<K, V>, Serializable {
		private final Map<K, ? extends V>	map;
		private final V						defaultValue;

		public ForMapWithDefault(Map<K, ? extends V> map, V defaultValue) {
			this.map = checkNotNull(map);
			this.defaultValue = defaultValue;
		}

		@Override
		public V apply(K a) {
			return map.containsKey(a) ? map.get(a) : defaultValue;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof ForMapWithDefault) {
				ForMapWithDefault<?, ?> that = (ForMapWithDefault<?, ?>) o;
				return map.equals(that.map)
						&& Objects.equal(defaultValue, that.defaultValue);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return map.hashCode() + defaultValue.hashCode();
		}

		@Override
		public String toString() {
			return "forMap(" + map + ", defaultValue=" + defaultValue
					+ ")";
		}

		private static final long serialVersionUID = 0;
	}

	/**
	 * Returns the composition of two functions. For {@code f: A->B} and
	 * {@code g: B->C}, composition is defined as the function h such that
	 * {@code h(a) == g(f(a))} for each {@code a}.
	 *
	 * @see <a href="//en.wikipedia.org/wiki/Function_composition">
	 *      function composition</a>
	 *
	 * @param g
	 *            the second function to apply
	 * @param f
	 *            the first function to apply
	 * @return the composition of {@code f} and {@code g}
	 */
	public static <A, B, C> Function<A, C> compose(Function<B, C> g,
			Function<A, ? extends B> f) {
		return new FunctionComposition<>(g, f);
	}

	private static class FunctionComposition<A, B, C>
			implements Function<A, C>, Serializable {
		private final Function<B, C>			g;
		private final Function<A, ? extends B>	f;

		public FunctionComposition(Function<B, C> g,
				Function<A, ? extends B> f) {
			this.g = checkNotNull(g);
			this.f = checkNotNull(f);
		}

		@Override
		public C apply(A a) {
			return g.apply(f.apply(a));
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof FunctionComposition) {
				FunctionComposition<?, ?, ?> that = (FunctionComposition<?, ?, ?>) obj;
				return f.equals(that.f) && g.equals(that.g);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return f.hashCode() ^ g.hashCode();
		}

		@Override
		public String toString() {
			return g.toString() + "(" + f.toString() + ")";
		}

		private static final long serialVersionUID = 0;
	}

	/**
	 * Creates a function that returns the same boolean output as the given
	 * predicate for all inputs.
	 */
	public static <T> Function<T, Boolean> forPredicate(
			Predicate<T> predicate) {
		return new PredicateFunction<>(predicate);
	}

	/** @see Functions#forPredicate */
	private static class PredicateFunction<T>
			implements Function<T, Boolean>, Serializable {
		private final Predicate<T> predicate;

		private PredicateFunction(Predicate<T> predicate) {
			this.predicate = checkNotNull(predicate);
		}

		@Override
		public Boolean apply(T t) {
			return predicate.apply(t);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof PredicateFunction) {
				PredicateFunction<?> that = (PredicateFunction<?>) obj;
				return predicate.equals(that.predicate);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return predicate.hashCode();
		}

		@Override
		public String toString() {
			return "forPredicate(" + predicate + ")";
		}

		private static final long serialVersionUID = 0;
	}

	/**
	 * Creates a function that returns {@code value} for any input.
	 *
	 * @param value
	 *            the constant value for the function to return
	 * @return a function that always returns {@code value}
	 */
	public static <E> Function<Object, E> constant(E value) {
		return new ConstantFunction<>(value);
	}

	private static class ConstantFunction<E>
			implements Function<Object, E>, Serializable {
		private final E value;

		public ConstantFunction(E value) {
			this.value = value;
		}

		@Override
		public E apply(Object from) {
			return value;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ConstantFunction) {
				ConstantFunction<?> that = (ConstantFunction<?>) obj;
				return Objects.equal(value, that.value);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return (value == null) ? 0 : value.hashCode();
		}

		@Override
		public String toString() {
			return "constant(" + value + ")";
		}

		private static final long serialVersionUID = 0;
	}
}
