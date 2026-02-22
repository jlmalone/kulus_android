package org.kulus.android.data.api.v3

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class GlucoseLevelTest {

    @Test
    fun `classify returns PURPLE for hypoglycemia below 3_9 mmol`() {
        assertThat(GlucoseLevel.classify(2.0)).isEqualTo(GlucoseLevel.PURPLE)
        assertThat(GlucoseLevel.classify(3.5)).isEqualTo(GlucoseLevel.PURPLE)
        assertThat(GlucoseLevel.classify(3.89)).isEqualTo(GlucoseLevel.PURPLE)
    }

    @Test
    fun `classify returns GREEN for normal range 3_9 to 7_8 mmol`() {
        assertThat(GlucoseLevel.classify(3.9)).isEqualTo(GlucoseLevel.GREEN)
        assertThat(GlucoseLevel.classify(5.5)).isEqualTo(GlucoseLevel.GREEN)
        assertThat(GlucoseLevel.classify(7.8)).isEqualTo(GlucoseLevel.GREEN)
    }

    @Test
    fun `classify returns ORANGE for elevated range 7_8 to 11_1 mmol`() {
        assertThat(GlucoseLevel.classify(7.81)).isEqualTo(GlucoseLevel.ORANGE)
        assertThat(GlucoseLevel.classify(9.0)).isEqualTo(GlucoseLevel.ORANGE)
        assertThat(GlucoseLevel.classify(11.1)).isEqualTo(GlucoseLevel.ORANGE)
    }

    @Test
    fun `classify returns RED for high above 11_1 mmol`() {
        assertThat(GlucoseLevel.classify(11.2)).isEqualTo(GlucoseLevel.RED)
        assertThat(GlucoseLevel.classify(15.0)).isEqualTo(GlucoseLevel.RED)
        assertThat(GlucoseLevel.classify(25.0)).isEqualTo(GlucoseLevel.RED)
    }

    @Test
    fun `fromString parses case insensitively`() {
        assertThat(GlucoseLevel.fromString("green")).isEqualTo(GlucoseLevel.GREEN)
        assertThat(GlucoseLevel.fromString("GREEN")).isEqualTo(GlucoseLevel.GREEN)
        assertThat(GlucoseLevel.fromString("Orange")).isEqualTo(GlucoseLevel.ORANGE)
        assertThat(GlucoseLevel.fromString("RED")).isEqualTo(GlucoseLevel.RED)
        assertThat(GlucoseLevel.fromString("purple")).isEqualTo(GlucoseLevel.PURPLE)
    }

    @Test
    fun `fromString returns null for unknown level`() {
        assertThat(GlucoseLevel.fromString("unknown")).isNull()
        assertThat(GlucoseLevel.fromString("")).isNull()
        assertThat(GlucoseLevel.fromString("blue")).isNull()
    }
}
