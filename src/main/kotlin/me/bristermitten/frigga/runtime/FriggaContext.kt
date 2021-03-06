package me.bristermitten.frigga.runtime

import OutputType
import me.bristermitten.frigga.runtime.data.Property
import me.bristermitten.frigga.runtime.data.Value
import me.bristermitten.frigga.runtime.data.function.Function
import me.bristermitten.frigga.runtime.type.CallerType
import me.bristermitten.frigga.runtime.type.StackType
import me.bristermitten.frigga.runtime.type.Type
import me.bristermitten.frigga.runtime.type.TypeInstance

class FriggaContext(val name: String) {
    val stack = Stack()


    private var globalScope = loadGlobalScope()

    private val scope = ArrayDeque<FriggaScope>().apply {
        add(globalScope)
    }

    private val using = mutableSetOf<FriggaContext>()

    private fun loadGlobalScope(): FriggaScope {
        val globalScope = FriggaScope("global")
        globalScope.properties[STACK_NAME] = Property(
            STACK_NAME,
            emptySet(),
            Value(
                StackType,
                stack
            )
        )
        globalScope.properties[CALLER_NAME] = Property(
            CALLER_NAME,
            emptySet(),
            Value(
                CallerType,
                Unit
            )
        )
        globalScope.properties[STDOUT_NAME] = Property(
            STDOUT_NAME,
            emptySet(),
            Value(
                OutputType,
                Unit
            )
        )

        return globalScope
    }

    internal fun findProperty(name: String): Property? {
        for (scope in scope) {
            val property = scope.properties[name]
            if (property != null) {
                return property
            }
        }
        return using.asSequence().mapNotNull { it.findProperty(name) }.firstOrNull()
    }

    internal fun findPropertyScope(name: String): Pair<FriggaScope, Property>? {
        for (scope in scope) {
            val property = scope.properties[name]
            if (property != null) {
                return scope to property
            }
        }
        return using.asSequence().mapNotNull { it.findPropertyScope(name) }.firstOrNull()
    }

    internal fun findType(name: String): Type? {
        for (scope in scope) {
            val type = scope.types[name]
            if (type != null) {
                return type
            }
        }
        return using.asSequence().mapNotNull { it.findType(name) }.firstOrNull()
    }

    fun findTypeFunction(type: Type, value: TypeInstance, name: String, parameterTypes: List<Type>): Function? {
        val function = type.getFunction(name, parameterTypes) ?: return null
        return value.properties[function]?.value as Function? ?: using.asSequence().mapNotNull {
            it.findTypeFunction(
                type, value, name, parameterTypes
            )
        }.firstOrNull()
    }

    internal fun findFunction(type: Type? = null, name: String, parameterTypes: List<Type>): Function? {

        if (type != null) {
            val value = type.getFunction(name, parameterTypes)?.value
            return value as Function?
        }

        for (scope in scope) {
            val function = scope.functions[name]
            if (function != null) {
                return function
            }
        }

        //Constructors
        val constructorType = findType(name)

        return if (constructorType != null) {
            findFunction(constructorType, CONSTRUCTOR, parameterTypes)
        } else {
            using.asSequence().mapNotNull {
                it.findFunction(type, name, parameterTypes)
            }.firstOrNull()
        }
    }

    internal fun defineProperty(property: Property, force: Boolean = false) {
        if (property.name in reservedNames && !force) {
            throw IllegalArgumentException("Cannot define property with reserved name ${property.name}")
        }
        scope[0].properties[property.name] = property
        val value = property.value.value
        if (value is Function) {
            scope[0].functions[property.name] = value
        }
    }

    internal fun defineProperty(name: String, value: Value, forceReservedName: Boolean = false) {
        return defineProperty(
            Property(name, emptySet(), value),
            forceReservedName
        )
    }

    fun enterScope(name: String) {
        scope.addFirst(FriggaScope(name))
    }

    fun enterFunctionScope(name: String) {
        scope.addFirst(FriggaScope(name, true))
    }

    fun exitScope(): FriggaScope {
        return scope.removeFirst()
    }

    val deepestScope
        get() = scope.first()

    fun reset() {
        stack.clear()

        scope.forEach {
            it.functions.clear()
            it.properties.clear()
            it.types.clear()
        }

        scope.clear()
        globalScope = loadGlobalScope()
        scope += globalScope
    }

    fun defineType(type: Type) {
        scope[0].types[type.name] = type
    }

    fun use(namespace: FriggaContext) {
        using += namespace
    }

}
