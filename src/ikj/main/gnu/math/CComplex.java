// Copyright (c) 1997 Per M.A. Bothner.
// This is free software; for terms and warranty disclaimer see ./COPYING.

package gnu.math;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * General Cartesian Complex number. Use this instead of DComplex if you
 * want exact complex numbers.
 * 
 * @author Per Bothner
 */

public class CComplex extends Complex implements Externalizable {
	RealNum	real;
	RealNum	imag;

	public CComplex() {
	}

	public CComplex(RealNum real, RealNum imag) {
		this.real = real;
		this.imag = imag;
	}

	@Override
	public RealNum re() {
		return real;
	}

	@Override
	public RealNum im() {
		return imag;
	}

	/**
	 * @serialData Write the real and imaginary parts, as Objects.
	 */
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(real);
		out.writeObject(imag);
	}

	@Override
	public void readExternal(ObjectInput in)
			throws IOException, ClassNotFoundException {
		real = (RealNum) in.readObject();
		imag = (RealNum) in.readObject();
	}
}
