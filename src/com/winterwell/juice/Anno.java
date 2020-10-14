package com.winterwell.juice;

import java.io.Serializable;

import org.jsoup.nodes.Element;

import com.winterwell.utils.Key;
import com.winterwell.utils.log.Log;

/**
 * Annotate a region of a document.
 * 
 * @author daniel
 */
public final class Anno<X> implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * ?? Does this serialize OK?
	 * Can be null
	 */
	Element src;
	
	/**
	 * For debug
	 */
	transient Class juicer;
	
	/**
	 * 
	 * @param type
	 * @param value
	 * @param src Annotations should have info on where they came from
	 * -- to allow juicers to work together. But this isn't always possible.
	 * E.g. this can be null if the source is multiple tags.
	 */
	public Anno(Key<X> type, X value, Element src) {
		this.name = type;
		this.value = value;
		this.src = src;
		// HACK debugging -- web XIds must have an @domain part, or be a url
		if (type==AJuicer.AUTHOR_XID) {
			String v = value.toString();
			if ( ! v.contains("@") && ! v.contains("://")) {
				Log.e("Juicer.Anno.fail", "Bogus oxid: "+v);
			}
		}
	}
	
	// Dan: toString methods are nice for debugging
	@Override
	public String toString() {
		return name+": "+value;
	}
	
	/**
	 * Note: This field is redundant
	 */
	final Key<X> name;
	
	final X value;
	
	public X getValue() {
		return value;
	}
	
	public Key<X> getName() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
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

	public Anno<X> setJuicer(AJuicer j) {
		juicer = j.getClass();
		return this;
	}
	
	
	
}
