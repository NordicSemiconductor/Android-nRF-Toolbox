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
public class GlucoseMeasurementContextParser {
	private static final int UNIT_kg = 0;
	private static final int UNIT_l = 1;

	public static String parse(final Data data) {
		final StringBuilder builder = new StringBuilder();

		int offset = 0;
		final int flags = data.getIntValue(Data.FORMAT_UINT8, offset);
		offset += 1;

		final boolean carbohydratePresent = (flags & 0x01) > 0;
		final boolean mealPresent = (flags & 0x02) > 0;
		final boolean testerHealthPresent = (flags & 0x04) > 0;
		final boolean exercisePresent = (flags & 0x08) > 0;
		final boolean medicationPresent = (flags & 0x10) > 0;
		final int medicationUnit = (flags & 0x20) > 0 ? UNIT_l : UNIT_kg;
		final boolean hbA1cPresent = (flags & 0x40) > 0;
		final boolean moreFlagsPresent = (flags & 0x80) > 0;

		final int sequenceNumber = data.getIntValue(Data.FORMAT_UINT16, offset);
		offset += 2;

		if (moreFlagsPresent) // not supported yet
			offset += 1;

		builder.append("Sequence number: ").append(sequenceNumber);

		if (carbohydratePresent) {
			final int carbohydrateId = data.getIntValue(Data.FORMAT_UINT8, offset);
			final float carbohydrateUnits = data.getFloatValue(Data.FORMAT_SFLOAT, offset + 1);
			builder.append("\nCarbohydrate: ").append(getCarbohydrate(carbohydrateId)).append(" (").append(carbohydrateUnits).append(carbohydrateUnits == UNIT_kg ? "kg" : "l").append(")");
			offset += 3;
		}

		if (mealPresent) {
			final int meal = data.getIntValue(Data.FORMAT_UINT8, offset);
			builder.append("\nMeal: ").append(getMeal(meal));
			offset += 1;
		}

		if (testerHealthPresent) {
			final int testerHealth = data.getIntValue(Data.FORMAT_UINT8, offset);
			final int tester = (testerHealth & 0xF0) >> 4;
			final int health = (testerHealth & 0x0F);
			builder.append("\nTester: ").append(getTester(tester));
			builder.append("\nHealth: ").append(getHealth(health));
			offset += 1;
		}

		if (exercisePresent) {
			final int exerciseDuration = data.getIntValue(Data.FORMAT_UINT16, offset);
			final int exerciseIntensity = data.getIntValue(Data.FORMAT_UINT8, offset + 2);
			builder.append("\nExercise duration: ").append(exerciseDuration).append("s (intensity ").append(exerciseIntensity).append("%)");
			offset += 3;
		}

		if (medicationPresent) {
			final int medicationId = data.getIntValue(Data.FORMAT_UINT8, offset);
			final float medicationQuantity = data.getFloatValue(Data.FORMAT_SFLOAT, offset + 1);
			builder.append("\nMedication: ").append(getMedicationId(medicationId)).append(" (").append(medicationQuantity).append(medicationUnit == UNIT_kg ? "kg" : "l");
			offset += 3;
		}

		if (hbA1cPresent) {
			final float HbA1c = data.getFloatValue(Data.FORMAT_SFLOAT, offset);
			builder.append("\nHbA1c: ").append(HbA1c).append("%");
		}
		return builder.toString();
	}

	private static String getCarbohydrate(final int id) {
		switch (id) {
		case 1:
			return "Breakfast";
		case 2:
			return "Lunch";
		case 3:
			return "Dinner";
		case 4:
			return "Snack";
		case 5:
			return "Drink";
		case 6:
			return "Supper";
		case 7:
			return "Brunch";
		default:
			return "Reserved for future use (" + id + ")";
		}
	}

	private static String getMeal(final int id) {
		switch (id) {
		case 1:
			return "Preprandial (before meal)";
		case 2:
			return "Postprandial (after meal)";
		case 3:
			return "Fasting";
		case 4:
			return "Casual (snacks, drinks, etc.)";
		case 5:
			return "Bedtime";
		default:
			return "Reserved for future use (" + id + ")";
		}
	}

	private static String getTester(final int id) {
		switch (id) {
		case 1:
			return "Self";
		case 2:
			return "Health Care Professional";
		case 3:
			return "Lab test";
		case 4:
			return "Casual (snacks, drinks, etc.)";
		case 15:
			return "Tester value not available";
		default:
			return "Reserved for future use (" + id + ")";
		}
	}

	private static String getHealth(final int id) {
		switch (id) {
		case 1:
			return "Minor health issues";
		case 2:
			return "Major health issues";
		case 3:
			return "During menses";
		case 4:
			return "Under stress";
		case 5:
			return "No health issues";
		case 15:
			return "Health value not available";
		default:
			return "Reserved for future use (" + id + ")";
		}
	}

	private static String getMedicationId(final int id) {
		switch (id) {
		case 1:
			return "Rapid acting insulin";
		case 2:
			return "Short acting insulin";
		case 3:
			return "Intermediate acting insulin";
		case 4:
			return "Long acting insulin";
		case 5:
			return "Pre-mixed insulin";
		default:
			return "Reserved for future use (" + id + ")";
		}
	}
}
