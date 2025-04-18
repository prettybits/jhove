/**********************************************************************
 * Jhove - JSTOR/Harvard Object Validation Environment
 * Copyright 2003 by JSTOR and the President and Fellows of Harvard College
 **********************************************************************/

package edu.harvard.hul.ois.jhove;

import edu.harvard.hul.ois.jhove.messages.JhoveMessage;

/**
 * This class encapsulates an error message from a Module, representing
 * a problem in the content being analyzed.
 */
public final class ErrorMessage extends Message {

	/******************************************************************
	 * CLASS CONSTRUCTOR.
	 ******************************************************************/

	/**
	 * Creates an ErrorMessage with an identifier.
	 * 
	 * @param message
	 *            The message text and its identifier.
	 */
	public ErrorMessage(JhoveMessage message) {
        this(message, NULL);
	}

	/**
	 * Creates an ErrorMessage with an identifier.
	 * 
	 * @param message
	 *            The message text and its identifier.
	 * @param offset
	 *            The offset in the file at which the problem
	 *            was detected.
	 */
	public ErrorMessage(JhoveMessage message, long offset) {
		this(message, message.getSubMessage(), offset);
	}

	/**
	 * Creates an ErrorMessage with an identifier.
	 * 
	 * @param message
	 *            The message text and its identifier.
	 * @param offset
	 *            The offset in the file at which the problem
	 *            was detected.
	 */
	public ErrorMessage(JhoveMessage message, int objNumber) {
		this(message, message.getSubMessage(), objNumber);
	}

	/**
	 * Creates an ErrorMessage with an identifier.
	 * 
	 * @param message
	 *            The message text and its identifier.
	 * @param offset
	 *            The offset in the file at which the problem
	 *            was detected.
	 */
	public ErrorMessage(JhoveMessage message, long offset, int objNumber) {
		this(message, message.getSubMessage(), offset, objNumber);
	}

	/**
	 * Creates an ErrorMessage with an identifier.
	 * 
	 * @param message
	 *            The message text and its identifier.
	 * @param subMessage
	 *            Human-readable additional information.
	 */
	public ErrorMessage(JhoveMessage message, String subMessage) {
        this(message, subMessage, NULL);
	}

	/**
	 * Creates an ErrorMessage with an identifier.
	 * 
	 * @param message
	 *            The message text and its identifier.
	 * @param subMessage
	 *            Human-readable additional information.
	 * @param offset
	 *            The offset in the file at which the problem
	 *            was detected.
	 */
	public ErrorMessage(JhoveMessage message, String subMessage,
			long offset) {
		this(message, subMessage, offset, NULLOBJ);
	}

	/**
	 * Creates an ErrorMessage with an identifier.
	 * 
	 * @param message
	 *            The message text and its identifier.
	 * @param subMessage
	 *            Human-readable additional information.
	 * @param offset
	 *            The offset in the file at which the problem
	 *            was detected.
	 */
	public ErrorMessage(JhoveMessage message, String subMessage,
			long offset, int objNumber) {
		super(message, subMessage, offset, objNumber, "Error");
	}
}
