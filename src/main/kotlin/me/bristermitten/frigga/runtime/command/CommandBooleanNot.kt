package me.bristermitten.frigga.runtime.command

import me.bristermitten.frigga.runtime.FriggaContext
import me.bristermitten.frigga.runtime.Stack
import me.bristermitten.frigga.runtime.UPON_NAME
import me.bristermitten.frigga.runtime.type.TypeInstance

data class CommandBooleanNot(
    val inverting: Command
) : Command() {

    override fun eval(stack: Stack, context: FriggaContext) {
        inverting.eval(stack, context)

        val toInvert = stack.pull()

        val notFunction = if (toInvert.value is TypeInstance) {
            context.findTypeFunction(toInvert.type, toInvert.value, OPERATOR_NOT_NAME, emptyList())
        } else {
            context.findFunction(toInvert.type, OPERATOR_NOT_NAME, emptyList())
        }

        requireNotNull(notFunction) {
            "Type ${toInvert.type} does not define a function named 'not', cannot be inverted."
        }

        context.defineProperty(UPON_NAME, toInvert, true)

        notFunction.call(stack, context, emptyList())
    }
}
