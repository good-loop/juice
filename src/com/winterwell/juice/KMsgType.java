package com.winterwell.juice;

/**
 * 
 * Constants for type of the contect.
 * @author ivan
 *
 */
public enum KMsgType {
	POST,		// Blog post
	COMMENT,	// User comment
	PAGE,
	VIDEO,
	IMAGE,
	EVENT,
	MISC,
	
	/** Not really a msg type! */
	PERSON,
	LOCATION,
	COMPANY
}
