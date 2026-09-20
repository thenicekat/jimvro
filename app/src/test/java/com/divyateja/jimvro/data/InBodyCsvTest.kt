package com.divyateja.jimvro.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.ByteArrayInputStream

class InBodyCsvTest {
    @Test fun importsInBodyMetricsAndSkipsMissingValues() {
        val csv = "\uFEFFDate,Weight(kg),Skeletal Muscle Mass(kg),Body Fat Mass(kg),Percent Body Fat(%),Basal Metabolic Rate(kcal),InBody Score,Visceral Fat Level(Level)\n20260920100057,73.3,32.9,16.0,21.8,1608,73.0,6.0\n20260912083132,73.1,-,16.0,21.9,1604,73.0,6.0"

        val readings = parseInBodyCsv(ByteArrayInputStream(csv.encodeToByteArray()))

        assertEquals(2, readings.size)
        assertEquals("2026-09-20", readings[0].measuredOn)
        assertEquals(32.9, readings[0].skeletalMuscleKg!!, 0.001)
        assertEquals("inbody:20260912083132", readings[1].sourceId)
        assertNull(readings[1].skeletalMuscleKg)
    }
}
