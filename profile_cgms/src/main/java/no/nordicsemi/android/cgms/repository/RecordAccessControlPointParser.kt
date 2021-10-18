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
package no.nordicsemi.android.cgms.repository

import no.nordicsemi.android.ble.data.Data

object RecordAccessControlPointParser {

    private const val OP_CODE_REPORT_STORED_RECORDS = 1
    private const val OP_CODE_DELETE_STORED_RECORDS = 2
    private const val OP_CODE_ABORT_OPERATION = 3
    private const val OP_CODE_REPORT_NUMBER_OF_RECORDS = 4
    private const val OP_CODE_NUMBER_OF_STORED_RECORDS_RESPONSE = 5
    private const val OP_CODE_RESPONSE_CODE = 6
    private const val OPERATOR_NULL = 0
    private const val OPERATOR_ALL_RECORDS = 1
    private const val OPERATOR_LESS_THEN_OR_EQUAL = 2
    private const val OPERATOR_GREATER_THEN_OR_EQUAL = 3
    private const val OPERATOR_WITHING_RANGE = 4
    private const val OPERATOR_FIRST_RECORD = 5
    private const val OPERATOR_LAST_RECORD = 6
    private const val RESPONSE_SUCCESS = 1
    private const val RESPONSE_OP_CODE_NOT_SUPPORTED = 2
    private const val RESPONSE_INVALID_OPERATOR = 3
    private const val RESPONSE_OPERATOR_NOT_SUPPORTED = 4
    private const val RESPONSE_INVALID_OPERAND = 5
    private const val RESPONSE_NO_RECORDS_FOUND = 6
    private const val RESPONSE_ABORT_UNSUCCESSFUL = 7
    private const val RESPONSE_PROCEDURE_NOT_COMPLETED = 8
    private const val RESPONSE_OPERAND_NOT_SUPPORTED = 9

    fun parse(data: Data): String {
        val builder = StringBuilder()
        val opCode = data.getIntValue(Data.FORMAT_UINT8, 0)!!
        val operator = data.getIntValue(Data.FORMAT_UINT8, 1)!!
        when (opCode) {
            OP_CODE_REPORT_STORED_RECORDS, OP_CODE_DELETE_STORED_RECORDS, OP_CODE_ABORT_OPERATION, OP_CODE_REPORT_NUMBER_OF_RECORDS -> builder.append(
                getOpCode(opCode)
            ).append("\n")
            OP_CODE_NUMBER_OF_STORED_RECORDS_RESPONSE -> {
                builder.append(getOpCode(opCode)).append(": ")
                val value = data.getIntValue(Data.FORMAT_UINT16, 2)!!
                builder.append(value).append("\n")
            }
            OP_CODE_RESPONSE_CODE -> {
                builder.append(getOpCode(opCode)).append(" for ")
                val targetOpCode = data.getIntValue(Data.FORMAT_UINT8, 2)!!
                builder.append(getOpCode(targetOpCode)).append(": ")
                val status = data.getIntValue(Data.FORMAT_UINT8, 3)!!
                builder.append(getStatus(status)).append("\n")
            }
        }
        when (operator) {
            OPERATOR_ALL_RECORDS, OPERATOR_FIRST_RECORD, OPERATOR_LAST_RECORD -> builder.append("Operator: ")
                .append(
                    getOperator(operator)
                ).append("\n")
            OPERATOR_GREATER_THEN_OR_EQUAL, OPERATOR_LESS_THEN_OR_EQUAL -> {
                val filter = data.getIntValue(Data.FORMAT_UINT8, 2)!!
                val value = data.getIntValue(Data.FORMAT_UINT16, 3)!!
                builder.append("Operator: ").append(getOperator(operator)).append(" ").append(value)
                    .append(" (filter: ").append(filter).append(")\n")
            }
            OPERATOR_WITHING_RANGE -> {
                val filter = data.getIntValue(Data.FORMAT_UINT8, 2)!!
                val value1 = data.getIntValue(Data.FORMAT_UINT16, 3)!!
                val value2 = data.getIntValue(Data.FORMAT_UINT16, 5)!!
                builder.append("Operator: ").append(getOperator(operator)).append(" ")
                    .append(value1).append("-").append(value2).append(" (filter: ").append(filter)
                    .append(")\n")
            }
        }
        if (builder.length > 0) builder.setLength(builder.length - 1)
        return builder.toString()
    }

    private fun getOpCode(opCode: Int): String {
        return when (opCode) {
            OP_CODE_REPORT_STORED_RECORDS -> "Report stored records"
            OP_CODE_DELETE_STORED_RECORDS -> "Delete stored records"
            OP_CODE_ABORT_OPERATION -> "Abort operation"
            OP_CODE_REPORT_NUMBER_OF_RECORDS -> "Report number of stored records"
            OP_CODE_NUMBER_OF_STORED_RECORDS_RESPONSE -> "Number of stored records response"
            OP_CODE_RESPONSE_CODE -> "Response Code"
            else -> "Reserved for future use"
        }
    }

    private fun getOperator(operator: Int): String {
        return when (operator) {
            OPERATOR_NULL -> "Null"
            OPERATOR_ALL_RECORDS -> "All records"
            OPERATOR_LESS_THEN_OR_EQUAL -> "Less than or equal to"
            OPERATOR_GREATER_THEN_OR_EQUAL -> "Greater than or equal to"
            OPERATOR_WITHING_RANGE -> "Within range of"
            OPERATOR_FIRST_RECORD -> "First record(i.e. oldest record)"
            OPERATOR_LAST_RECORD -> "Last record (i.e. most recent record)"
            else -> "Reserved for future use"
        }
    }

    private fun getStatus(status: Int): String {
        return when (status) {
            RESPONSE_SUCCESS -> "Success"
            RESPONSE_OP_CODE_NOT_SUPPORTED -> "Operation not supported"
            RESPONSE_INVALID_OPERATOR -> "Invalid operator"
            RESPONSE_OPERATOR_NOT_SUPPORTED -> "Operator not supported"
            RESPONSE_INVALID_OPERAND -> "Invalid operand"
            RESPONSE_NO_RECORDS_FOUND -> "No records found"
            RESPONSE_ABORT_UNSUCCESSFUL -> "Abort unsuccessful"
            RESPONSE_PROCEDURE_NOT_COMPLETED -> "Procedure not completed"
            RESPONSE_OPERAND_NOT_SUPPORTED -> "Operand not supported"
            else -> "Reserved for future use"
        }
    }
}
