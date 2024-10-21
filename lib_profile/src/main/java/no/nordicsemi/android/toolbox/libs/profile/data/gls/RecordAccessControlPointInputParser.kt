package no.nordicsemi.android.toolbox.libs.profile.data.gls

import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray.Companion.opCode
import no.nordicsemi.android.kotlin.ble.core.data.util.IntFormat
import no.nordicsemi.android.toolbox.libs.profile.data.common.MutableData
import androidx.annotation.IntRange

object RecordAccessControlPointInputParser {
    private const val OP_CODE_REPORT_STORED_RECORDS: Byte = 1
    private const val OP_CODE_DELETE_STORED_RECORDS: Byte = 2
    private const val OP_CODE_ABORT_OPERATION: Byte = 3
    private const val OP_CODE_REPORT_NUMBER_OF_RECORDS: Byte = 4
    private const val OP_CODE_NUMBER_OF_STORED_RECORDS_RESPONSE: Byte = 5
    private const val OP_CODE_RESPONSE_CODE: Byte = 6
    private const val OPERATOR_NULL: Byte = 0
    private const val OPERATOR_ALL_RECORDS: Byte = 1
    private const val OPERATOR_LESS_THEN_OR_EQUAL: Byte = 2
    private const val OPERATOR_GREATER_THEN_OR_EQUAL: Byte = 3
    private const val OPERATOR_WITHING_RANGE: Byte = 4
    private const val OPERATOR_FIRST_RECORD: Byte = 5
    private const val OPERATOR_LAST_RECORD: Byte = 6

    fun reportAllStoredRecords(): DataByteArray {
        return create(OP_CODE_REPORT_STORED_RECORDS, OPERATOR_ALL_RECORDS)
    }

    fun reportFirstStoredRecord(): DataByteArray {
        return create(OP_CODE_REPORT_STORED_RECORDS, OPERATOR_FIRST_RECORD)
    }

    fun reportLastStoredRecord(): DataByteArray {
        return create(OP_CODE_REPORT_STORED_RECORDS, OPERATOR_LAST_RECORD)
    }

    fun reportStoredRecordsLessThenOrEqualTo(
        filter: FilterType, formatType: IntFormat,
        parameter: Int
    ): DataByteArray {
        return create(OP_CODE_REPORT_STORED_RECORDS, OPERATOR_LESS_THEN_OR_EQUAL, filter, formatType, parameter)
    }

    fun reportStoredRecordsGreaterThenOrEqualTo(
        filter: FilterType, formatType: IntFormat,
        parameter: Int
    ): DataByteArray {
        return create(OP_CODE_REPORT_STORED_RECORDS, OPERATOR_GREATER_THEN_OR_EQUAL, filter, formatType, parameter)
    }

    fun reportStoredRecordsFromRange(
        filter: FilterType,
        formatType: IntFormat,
        start: Int, end: Int
    ): DataByteArray {
        return create(OP_CODE_REPORT_STORED_RECORDS, OPERATOR_WITHING_RANGE, filter, formatType, start, end)
    }

    fun reportStoredRecordsLessThenOrEqualTo(@IntRange(from = 0) sequenceNumber: Int): DataByteArray {
        return create(
            OP_CODE_REPORT_STORED_RECORDS,
            OPERATOR_LESS_THEN_OR_EQUAL,
            FilterType.SEQUENCE_NUMBER,
            IntFormat.FORMAT_UINT16_LE,
            sequenceNumber
        )
    }

    fun reportStoredRecordsGreaterThenOrEqualTo(@IntRange(from = 0) sequenceNumber: Int): DataByteArray {
        return create(
            OP_CODE_REPORT_STORED_RECORDS,
            OPERATOR_GREATER_THEN_OR_EQUAL,
            FilterType.SEQUENCE_NUMBER,
            IntFormat.FORMAT_UINT16_LE,
            sequenceNumber
        )
    }

    fun reportStoredRecordsFromRange(
        @IntRange(from = 0) startSequenceNumber: Int,
        @IntRange(from = 0) endSequenceNumber: Int
    ): DataByteArray {
        return create(
            OP_CODE_REPORT_STORED_RECORDS,
            OPERATOR_WITHING_RANGE,
            FilterType.SEQUENCE_NUMBER,
            IntFormat.FORMAT_UINT16_LE,
            startSequenceNumber,
            endSequenceNumber
        )
    }

    fun deleteAllStoredRecords(): DataByteArray {
        return create(OP_CODE_DELETE_STORED_RECORDS, OPERATOR_ALL_RECORDS)
    }

    fun deleteFirstStoredRecord(): DataByteArray {
        return create(OP_CODE_DELETE_STORED_RECORDS, OPERATOR_FIRST_RECORD)
    }

    fun deleteLastStoredRecord(): DataByteArray {
        return create(OP_CODE_DELETE_STORED_RECORDS, OPERATOR_LAST_RECORD)
    }

    fun deleteStoredRecordsLessThenOrEqualTo(
        filter: FilterType,
        formatType: IntFormat,
        parameter: Int
    ): DataByteArray {
        return create(OP_CODE_DELETE_STORED_RECORDS, OPERATOR_LESS_THEN_OR_EQUAL, filter, formatType, parameter)
    }

    fun deleteStoredRecordsGreaterThenOrEqualTo(
        filter: FilterType,
        formatType: IntFormat,
        parameter: Int
    ): DataByteArray {
        return create(OP_CODE_DELETE_STORED_RECORDS, OPERATOR_GREATER_THEN_OR_EQUAL, filter, formatType, parameter)
    }

    fun deleteStoredRecordsFromRange(
        filter: FilterType,
        formatType: IntFormat,
        start: Int, end: Int
    ): DataByteArray {
        return create(OP_CODE_DELETE_STORED_RECORDS, OPERATOR_WITHING_RANGE, filter, formatType, start, end)
    }

    fun deleteStoredRecordsLessThenOrEqualTo(@IntRange(from = 0) sequenceNumber: Int): DataByteArray {
        return create(
            OP_CODE_DELETE_STORED_RECORDS,
            OPERATOR_LESS_THEN_OR_EQUAL,
            FilterType.SEQUENCE_NUMBER,
            IntFormat.FORMAT_UINT16_LE,
            sequenceNumber
        )
    }

    fun deleteStoredRecordsGreaterThenOrEqualTo(@IntRange(from = 0) sequenceNumber: Int): DataByteArray {
        return create(
            OP_CODE_DELETE_STORED_RECORDS,
            OPERATOR_GREATER_THEN_OR_EQUAL,
            FilterType.SEQUENCE_NUMBER,
            IntFormat.FORMAT_UINT16_LE,
            sequenceNumber
        )
    }

    fun deleteStoredRecordsFromRange(
        @IntRange(from = 0) startSequenceNumber: Int,
        @IntRange(from = 0) endSequenceNumber: Int
    ): DataByteArray {
        return create(
            OP_CODE_DELETE_STORED_RECORDS, OPERATOR_WITHING_RANGE,
            FilterType.SEQUENCE_NUMBER, IntFormat.FORMAT_UINT16_LE,
            startSequenceNumber, endSequenceNumber
        )
    }

    fun reportNumberOfAllStoredRecords(): DataByteArray {
        return create(OP_CODE_REPORT_NUMBER_OF_RECORDS, OPERATOR_ALL_RECORDS)
    }

    fun reportNumberOfStoredRecordsLessThenOrEqualTo(
        filter: FilterType,
        formatType: IntFormat,
        parameter: Int
    ): DataByteArray {
        return create(OP_CODE_REPORT_NUMBER_OF_RECORDS, OPERATOR_LESS_THEN_OR_EQUAL, filter, formatType, parameter)
    }

    fun reportNumberOfStoredRecordsGreaterThenOrEqualTo(
        filter: FilterType,
        formatType: IntFormat,
        parameter: Int
    ): DataByteArray {
        return create(OP_CODE_REPORT_NUMBER_OF_RECORDS, OPERATOR_GREATER_THEN_OR_EQUAL, filter, formatType, parameter)
    }

    fun reportNumberOfStoredRecordsFromRange(
        filter: FilterType,
        formatType: IntFormat,
        start: Int, end: Int
    ): DataByteArray {
        return create(OP_CODE_REPORT_NUMBER_OF_RECORDS, OPERATOR_WITHING_RANGE, filter, formatType, start, end)
    }

    fun reportNumberOfStoredRecordsLessThenOrEqualTo(@IntRange(from = 0) sequenceNumber: Int): DataByteArray {
        return create(
            OP_CODE_REPORT_NUMBER_OF_RECORDS,
            OPERATOR_LESS_THEN_OR_EQUAL,
            FilterType.SEQUENCE_NUMBER,
            IntFormat.FORMAT_UINT16_LE,
            sequenceNumber
        )
    }

    fun reportNumberOfStoredRecordsGreaterThenOrEqualTo(@IntRange(from = 0) sequenceNumber: Int): DataByteArray {
        return create(
            OP_CODE_REPORT_NUMBER_OF_RECORDS,
            OPERATOR_GREATER_THEN_OR_EQUAL,
            FilterType.SEQUENCE_NUMBER,
            IntFormat.FORMAT_UINT16_LE,
            sequenceNumber
        )
    }

    fun reportNumberOfStoredRecordsFromRange(
        @IntRange(from = 0) startSequenceNumber: Int,
        @IntRange(from = 0) endSequenceNumber: Int
    ): DataByteArray {
        return create(
            OP_CODE_REPORT_NUMBER_OF_RECORDS,
            OPERATOR_WITHING_RANGE,
            FilterType.SEQUENCE_NUMBER,
            IntFormat.FORMAT_UINT16_LE,
            startSequenceNumber,
            endSequenceNumber
        )
    }

    fun abortOperation(): DataByteArray {
        return create(OP_CODE_ABORT_OPERATION, OPERATOR_NULL)
    }

    private fun create(opCode: Byte, operator: Byte): DataByteArray {
        return opCode(opCode, operator)
    }

    private fun create(
        opCode: Byte, operator: Byte,
        filter: FilterType,
        formatType: IntFormat,
        vararg parameters: Int
    ): DataByteArray {
        val parameterLen = formatType.value and 0x0F
        val data = MutableData(ByteArray(2 + 1 + parameters.size * parameterLen))
        data.setByte(opCode.toInt(), 0)
        data.setByte(operator.toInt(), 1)
        if (parameters.size > 0) {
            data.setByte(filter.type.toInt(), 2)
            data.setValue(parameters[0], formatType, 3)
        }
        if (parameters.size == 2) {
            data.setValue(parameters[1], formatType, 3 + parameterLen)
        }
        return data.toByteData()
    }

    enum class FilterType(type: Int) {
        TIME_OFFSET(0x01),

        /** Alias of [.TIME_OFFSET]  */
        SEQUENCE_NUMBER(0x01), USER_FACING_TIME(0x02);

        val type: Byte

        init {
            this.type = type.toByte()
        }
    }
}