package com.codanbaru.serialization

public data class DynamapConfiguration(
    public val classDiscriminator: String = "__dynamap_serialization_type",

    public val evaluateUndefinedAttributesAsNullAttribute: Boolean = true,

    public val booleanLiteral: BooleanLiteral = BooleanLiteral("TRUE", "FALSE", false),

    /**
     * Map<String, *> types will be serialized to a structure like: mapOf("a" to 1, "b" to 2)
     *  false => { "0": "a", "1": 1, "2": "b", "3": 2)
     *  true => { "a": 1, "b": 2 }
     */
    public val indexMapsByKeys: Boolean = false,
) {

    public data class BooleanLiteral(
        val yes: String,
        val no: String,
        val caseSensitive: Boolean
    )
}