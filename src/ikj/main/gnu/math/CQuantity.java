// Copyright (c) 1997 Per M.A. Bothner.
// This is free software; for terms and warranty disclaimer see ./COPYING.

package gnu.math;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/** General Cartesian Complex quantity. */

public class CQuantity extends Quantity implements Externalizable {
	Complex	num;
	Unit	unt;

	public CQuantity(Complex num, Unit unit) {
		this.num = num;
		this.unt = unit;
	}

	public CQuantity(RealNum real, RealNum imag, Unit unit) {
		this.num = new CComplex(real, imag);
		this.unt = unit;
	}

	@Override
	public Complex number() {
		return num;
	}

	@Override
	public Unit unit() {
		return unt;
	}

	@Override
	public boolean isExact() {
		return num.isExact();
	}

	@Override
	public boolean isZero() {
		return num.isZero();
	}

	/**
	 * @serialData Write the complex value (using writeObject) followed by
	 *             the Unit (also using writeUnit).
	 */

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(num);
		out.writeObject(unt);
	}

	@Override
	public void readExternal(ObjectInput in)
			throws IOException, ClassNotFoundException {
		num = (Complex) in.readObject();
		unt = (Unit) in.readObject();
	}
}
