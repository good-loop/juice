package com.winterwell.juice;

import java.io.Serializable;

import winterwell.utils.Key;
import winterwell.utils.containers.IntRange;

/**
 * Annotate a region of a document.
 * 
 * @author daniel
 */
public final class Anno<X> 
extends IntRange implements Serializable {
	private static final long serialVersionUID = 1L;

	transient AJuicer juicer;
	
	public Anno(int start, int end, Key type, X value) {
		super(start, end);
		this.type = type.getName();
		this.value = value;
	}
	
	final String type;
	final X value;
	
	public CharSequence getText(CharSequence base) {
		return base.subSequence(low, high);
	}

	@Override
	public String toString() {
		return "Anno [type=" + type + ", value=" + value + ", high=" + high
				+ ", low=" + low + "]";
	}
	
	
}
