package com.winterwell.juice;

import java.io.Serializable;

import org.jsoup.nodes.Element;

import winterwell.utils.Key;
import winterwell.utils.containers.IntRange;

/**
 * Annotate a region of a document.
 * 
 * @author daniel
 */
public final class Anno<X> implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * ?? Does this serialize OK?
	 */
	Element src;
	
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
	}
	
	/**
	 * Note: This field is redundant
	 */
	final Key<X> name;
	
	final X value;

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
	
	
	
}
