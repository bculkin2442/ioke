// Copyright (c) 1997 Per M.A. Bothner.
// This is free software; for terms and warranty disclaimer see ./COPYING.

package gnu.math;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Implementation of exact rational numbers a ratio of two IntNums.
 * 
 * @author Per Bothner
 */

public class IntFraction extends RatNum implements Externalizable {
	IntNum	num;
	IntNum	den;

	IntFraction() {
	}

	public IntFraction(IntNum num, IntNum den) {
		this.num = num;
		this.den = den;
	}

	@Override
	public final IntNum numerator() {
		return num;
	}

	@Override
	public final IntNum denominator() {
		return den;
	}

	@Override
	public final boolean isNegative() {
		return num.isNegative();
	}

	@Override
	public final int sign() {
		return num.sign();
	}

	@Override
	public final int compare(Object obj) {
		if (obj instanceof RatNum) {
			return RatNum.compare(this, (RatNum) obj);
		}
		return ((RealNum) obj).compareReversed(this);
	}

	@Override
	public int compareReversed(Numeric x) {
		return RatNum.compare((RatNum) x, this);
	}

	@Override
	public Numeric add(Object y, int k) {
		if (y instanceof RatNum) {
			return RatNum.add(this, (RatNum) y, k);
		}
		if (!(y instanceof Numeric)) {
			throw new IllegalArgumentException();
		}
		return ((Numeric) y).addReversed(this, k);
	}

	@Override
	public Numeric addReversed(Numeric x, int k) {
		if (!(x instanceof RatNum)) {
			throw new IllegalArgumentException();
		}
		return RatNum.add((RatNum) x, this, k);
	}

	@Override
	public Numeric mul(Object y) {
		if (y instanceof RatNum) {
			return RatNum.times(this, (RatNum) y);
		}
		if (!(y instanceof Numeric)) {
			throw new IllegalArgumentException();
		}
		return ((Numeric) y).mulReversed(this);
	}

	@Override
	public Numeric mulReversed(Numeric x) {
		if (!(x instanceof RatNum)) {
			throw new IllegalArgumentException();
		}
		return RatNum.times((RatNum) x, this);
	}

	@Override
	public Numeric div(Object y) {
		if (y instanceof RatNum) {
			return RatNum.divide(this, (RatNum) y);
		}
		if (!(y instanceof Numeric)) {
			throw new IllegalArgumentException();
		}
		return ((Numeric) y).divReversed(this);
	}

	@Override
	public Numeric divReversed(Numeric x) {
		if (!(x instanceof RatNum)) {
			throw new IllegalArgumentException();
		}
		return RatNum.divide((RatNum) x, this);
	}

	public static IntFraction neg(IntFraction x) {
		// If x is normalized, we do not need to call RatNum.make to
		// normalize.
		return new IntFraction(IntNum.neg(x.numerator()), x.denominator());
	}

	@Override
	public Numeric neg() {
		return IntFraction.neg(this);
	}

	@Override
	public long longValue() {
		return toExactInt(ROUND).longValue();
	}

	@Override
	public double doubleValue() {
		boolean neg = num.isNegative();
		if (den.isZero()) {
			return (neg ? Double.NEGATIVE_INFINITY
					: num.isZero() ? Double.NaN
							: Double.POSITIVE_INFINITY);
		}
		IntNum n = num;
		if (neg) {
			n = IntNum.neg(n);
		}
		int num_len = n.intLength();
		int den_len = den.intLength();
		int exp = 0;
		if (num_len < den_len + 54) {
			exp = den_len + 54 - num_len;
			n = IntNum.shift(n, exp);
			exp = -exp;
		}

		// Divide n (which is shifted num) by den, using truncating
		// division,
		// and return quot and remainder.
		IntNum quot = new IntNum();
		IntNum remainder = new IntNum();
		IntNum.divide(n, den, quot, remainder, TRUNCATE);
		quot = quot.canonicalize();
		remainder = remainder.canonicalize();

		return quot.roundToDouble(exp, neg, !remainder.isZero());
	}

	@Override
	public String toString(int radix) {
		return num.toString(radix) + '/' + den.toString(radix);
	}

	/**
	 * @serialData Write the (canonicalized) numerator and denominator
	 *             IntNums.
	 */
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(num);
		out.writeObject(den);
	}

	/**
	 * @serialData Read the numerator and denominator as IntNums. Assumes
	 *             they have no common factors.
	 */
	@Override
	public void readExternal(ObjectInput in)
			throws IOException, ClassNotFoundException {
		num = (IntNum) in.readObject();
		den = (IntNum) in.readObject();
	}
}
