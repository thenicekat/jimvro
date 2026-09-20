package com.divyateja.jimvro.data

import java.io.InputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal fun parseInBodyCsv(input: InputStream): List<MeasurementEntity> =
    input.bufferedReader().use { reader ->
        val header = reader.readLine()?.removePrefix("\uFEFF")?.split(',') ?: error("InBody CSV is empty")
        fun column(name: String) = header.indexOf(name).takeIf { it >= 0 } ?: error("This is not an InBody CSV: missing $name")
        val date = column("Date")
        val weight = column("Weight(kg)")
        val bodyFat = column("Percent Body Fat(%)")
        val muscle = column("Skeletal Muscle Mass(kg)")
        val fatMass = column("Body Fat Mass(kg)")
        val bmr = column("Basal Metabolic Rate(kcal)")
        val score = column("InBody Score")
        val visceralFat = column("Visceral Fat Level(Level)")
        fun String?.number() = this?.takeUnless { it == "-" || it.isBlank() }?.toDoubleOrNull()
        reader.lineSequence().mapNotNull { line ->
            val fields = line.split(',')
            val timestamp = fields.getOrNull(date)?.trim().orEmpty()
            val measuredOn = timestamp.take(8).takeIf { it.length == 8 }?.let {
                LocalDate.parse(it, DateTimeFormatter.BASIC_ISO_DATE).toString()
            } ?: return@mapNotNull null
            MeasurementEntity(
                measuredOn = measuredOn,
                weightKg = fields.getOrNull(weight).number(),
                bodyFatPct = fields.getOrNull(bodyFat).number(),
                skeletalMuscleKg = fields.getOrNull(muscle).number(),
                bodyFatMassKg = fields.getOrNull(fatMass).number(),
                basalMetabolicRateKcal = fields.getOrNull(bmr).number(),
                inBodyScore = fields.getOrNull(score).number(),
                visceralFatLevel = fields.getOrNull(visceralFat).number(),
                sourceId = "inbody:$timestamp",
            )
        }.toList()
    }
