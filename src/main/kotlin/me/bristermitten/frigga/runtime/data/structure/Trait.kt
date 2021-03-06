package me.bristermitten.frigga.runtime.data.structure

import me.bristermitten.frigga.runtime.data.PropertyDeclaration

class Trait(
    name: String,
    override val parents: List<Structure>,
    val elements: List<PropertyDeclaration>
) : Structure(name) {
    init {

    }
}
