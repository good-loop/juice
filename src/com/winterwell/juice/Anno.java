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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Anno)) {
			return false;
		}
		
		Anno other = (Anno) obj;
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		
		return true;
	}
	
	
	
}
