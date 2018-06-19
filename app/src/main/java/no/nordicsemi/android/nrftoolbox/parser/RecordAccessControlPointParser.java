/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package no.nordicsemi.android.nrftoolbox.parser;

import no.nordicsemi.android.ble.data.Data;

@SuppressWarnings("ConstantConditions")
public class RecordAccessControlPointParser {
	private final static int OP_CODE_REPORT_STORED_RECORDS = 1;
	private final static int OP_CODE_DELETE_STORED_RECORDS = 2;
	private final static int OP_CODE_ABORT_OPERATION = 3;
	private final static int OP_CODE_REPORT_NUMBER_OF_RECORDS = 4;
	private final static int OP_CODE_NUMBER_OF_STORED_RECORDS_RESPONSE = 5;
	private final static int OP_CODE_RESPONSE_CODE = 6;

	private final static int OPERATOR_NULL = 0;
	private final static int OPERATOR_ALL_RECORDS = 1;
	private final static int OPERATOR_LESS_THEN_OR_EQUAL = 2;
	private final static int OPERATOR_GREATER_THEN_OR_EQUAL = 3;
	private final static int OPERATOR_WITHING_RANGE = 4;
	private final static int OPERATOR_FIRST_RECORD = 5;
	private final static int OPERATOR_LAST_RECORD = 6;

	private final static int RESPONSE_SUCCESS = 1;
	private final static int RESPONSE_OP_CODE_NOT_SUPPORTED = 2;
	private final static int RESPONSE_INVALID_OPERATOR = 3;
	private final static int RESPONSE_OPERATOR_NOT_SUPPORTED = 4;
	private final static int RESPONSE_INVALID_OPERAND = 5;
	private final static int RESPONSE_NO_RECORDS_FOUND = 6;
	private final static int RESPONSE_ABORT_UNSUCCESSFUL = 7;
	private final static int RESPONSE_PROCEDURE_NOT_COMPLETED = 8;
	private final static int RESPONSE_OPERAND_NOT_SUPPORTED = 9;

	public static String parse(final Data data) {
		final StringBuilder builder = new StringBuilder();
		final int opCode = data.getIntValue(Data.FORMAT_UINT8, 0);
		final int operator = data.getIntValue(Data.FORMAT_UINT8, 1);

		switch (opCode) {
		case OP_CODE_REPORT_STORED_RECORDS:
		case OP_CODE_DELETE_STORED_RECORDS:
		case OP_CODE_ABORT_OPERATION:
		case OP_CODE_REPORT_NUMBER_OF_RECORDS:
			builder.append(getOpCode(opCode)).append("\n");
			break;
		case OP_CODE_NUMBER_OF_STORED_RECORDS_RESPONSE: {
			builder.append(getOpCode(opCode)).append(": ");
			final int value = data.getIntValue(Data.FORMAT_UINT16, 2);
			builder.append(value).append("\n");
			break;
		}
		case OP_CODE_RESPONSE_CODE: {
			builder.append(getOpCode(opCode)).append(" for ");
			final int targetOpCode = data.getIntValue(Data.FORMAT_UINT8, 2);
			builder.append(getOpCode(targetOpCode)).append(": ");
			final int status = data.getIntValue(Data.FORMAT_UINT8, 3);
			builder.append(getStatus(status)).append("\n");
			break;
		}
		}

		switch (operator) {
		case OPERATOR_ALL_RECORDS:
		case OPERATOR_FIRST_RECORD:
		case OPERATOR_LAST_RECORD:
			builder.append("Operator: ").append(getOperator(operator)).append("\n");
			break;
		case OPERATOR_GREATER_THEN_OR_EQUAL:
		case OPERATOR_LESS_THEN_OR_EQUAL: {
			final int filter = data.getIntValue(Data.FORMAT_UINT8, 2);
			final int value = data.getIntValue(Data.FORMAT_UINT16, 3);
			builder.append("Operator: ").append(getOperator(operator)).append(" ").append(value).append(" (filter: ").append(filter).append(")\n");
			break;
		}
		case OPERATOR_WITHING_RANGE: {
			final int filter = data.getIntValue(Data.FORMAT_UINT8, 2);
			final int value1 = data.getIntValue(Data.FORMAT_UINT16, 3);
			final int value2 = data.getIntValue(Data.FORMAT_UINT16, 5);
			builder.append("Operator: ").append(getOperator(operator)).append(" ").append(value1).append("-").append(value2).append(" (filter: ").append(filter).append(")\n");
			break;
		}
		}
		if (builder.length() > 0)
			builder.setLength(builder.length() - 1);

		return builder.toString();
	}

	private static String getOpCode(final int opCode) {
		switch (opCode) {
		case OP_CODE_REPORT_STORED_RECORDS:
			return "Report stored records";
		case OP_CODE_DELETE_STORED_RECORDS:
			return "Delete stored records";
		case OP_CODE_ABORT_OPERATION:
			return "Abort operation";
		case OP_CODE_REPORT_NUMBER_OF_RECORDS:
			return "Report number of stored records";
		case OP_CODE_NUMBER_OF_STORED_RECORDS_RESPONSE:
			return "Number of stored records response";
		case OP_CODE_RESPONSE_CODE:
			return "Response Code";
		default:
			return "Reserved for future use";
		}
	}

	private static String getOperator(final int operator) {
		switch (operator) {
		case OPERATOR_NULL:
			return "Null";
		case OPERATOR_ALL_RECORDS:
			return "All records";
		case OPERATOR_LESS_THEN_OR_EQUAL:
			return "Less than or equal to";
		case OPERATOR_GREATER_THEN_OR_EQUAL:
			return "Greater than or equal to";
		case OPERATOR_WITHING_RANGE:
			return "Within range of";
		case OPERATOR_FIRST_RECORD:
			return "First record(i.e. oldest record)";
		case OPERATOR_LAST_RECORD:
			return "Last record (i.e. most recent record)";
		default:
			return "Reserved for future use";
		}
	}

	private static String getStatus(final int status) {
		switch (status) {
		case RESPONSE_SUCCESS:
			return "Success";
		case RESPONSE_OP_CODE_NOT_SUPPORTED:
			return "Operation not supported";
		case RESPONSE_INVALID_OPERATOR:
			return "Invalid operator";
		case RESPONSE_OPERATOR_NOT_SUPPORTED:
			return "Operator not supported";
		case RESPONSE_INVALID_OPERAND:
			return "Invalid operand";
		case RESPONSE_NO_RECORDS_FOUND:
			return "No records found";
		case RESPONSE_ABORT_UNSUCCESSFUL:
			return "Abort unsuccessful";
		case RESPONSE_PROCEDURE_NOT_COMPLETED:
			return "Procedure not completed";
		case RESPONSE_OPERAND_NOT_SUPPORTED:
			return "Operand not supported";
		default:
			return "Reserved for future use";
		}
	}
}
