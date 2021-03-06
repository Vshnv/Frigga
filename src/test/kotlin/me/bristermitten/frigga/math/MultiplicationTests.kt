package me.bristermitten.frigga.math

import io.kotest.matchers.shouldBe
import me.bristermitten.frigga.FriggaTest
import me.bristermitten.frigga.RANDOM_TEST_COUNT
import me.bristermitten.frigga.runtime.data.decValue
import me.bristermitten.frigga.runtime.data.intValue
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test

class MultiplicationTests : FriggaTest() {

    @Test
    fun `Assert Simple Integer Multiplication Functioning Correctly`() {
        val code = """
            x = 3
            x * 2
        """.trimIndent()
        val result = runtime.execute(code)

        handleExceptions(result)
        result.leftoverStack.first() shouldBe intValue(3 * 2)
    }

    @Test
    fun `Assert More Complex Integer Multiplication Functioning Correctly`() {
        val code = """
            x = 3
            x * x * 5 * x * -1
        """.trimIndent()
        val result = runtime.execute(code)

        handleExceptions(result)
        result.leftoverStack.first() shouldBe intValue(3 * 3 * (5 * 3 * -1))
    }

    @Test
    fun `Assert Simple Decimal Multiplication Functioning Correctly`() {
        val code = """
            x = 3.5
            x * 2.9
        """.trimIndent()
        val result = runtime.execute(code)

        handleExceptions(result)
        result.leftoverStack.first() shouldBe decValue(3.5 * 2.9)
    }

    @Test
    fun `Assert More Complex Decimal Multiplication Functioning Correctly`() {
        val code = """
            x = 3.52
            x * x * 5.1486 * x * -1.41875
        """.trimIndent()
        val result = runtime.execute(code)

        handleExceptions(result)
        result.leftoverStack.first() shouldBe decValue(3.52 * 3.52 * 5.1486 * 3.52 * -1.41875)
    }

    @RepeatedTest(RANDOM_TEST_COUNT)
    fun `Assert Random Decimal Multiplication Functioning Correctly`() {
        val start = Math.random()
        val param1 = Math.random()
        val param2 = Math.random()

        val code = """
            x = $start
            x * $param1 * $param2
        """.trimIndent()
        val result = runtime.execute(code)

        handleExceptions(result)
        result.leftoverStack.first() shouldBe decValue(start * param1 * param2)
    }

}
