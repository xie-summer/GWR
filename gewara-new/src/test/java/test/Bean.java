package test;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableUtils;

public class Bean implements Writable {
	private int a;
	private String b;
	private double c;
	private Date e;

	public int getA() {
		return a;
	}

	public void setA(int a) {
		this.a = a;
	}

	public String getB() {
		return b;
	}

	public void setB(String b) {
		this.b = b;
	}

	public double getC() {
		return c;
	}

	public void setC(double c) {
		this.c = c;
	}

	public Date getE() {
		return e;
	}

	public void setE(Date e) {
		this.e = e;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		WritableUtils.writeVInt(out, a);
		WritableUtils.writeString(out, b);
		WritableUtils.writeVLong(out, Double.doubleToLongBits(c));
		WritableUtils.writeVLong(out, e.getTime());
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		a = WritableUtils.readVInt(in);
		b = WritableUtils.readString(in);
		c = Double.longBitsToDouble(WritableUtils.readVLong(in));
		e = new Date(WritableUtils.readVLong(in));
	}
}