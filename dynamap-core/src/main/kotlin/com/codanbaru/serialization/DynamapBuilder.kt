package com.codanbaru.serialization

import kotlinx.serialization.modules.SerializersModule

public class DynamapBuilder internal constructor(
    dynamap: Dynamap,
) {
    public var serializersModule: SerializersModule = dynamap.serializersModule

    public var classDiscriminator: String = dynamap.configuration.classDiscriminator

    public var evaluateUndefinedAttributesAsNullAttribute: Boolean =
        dynamap.configuration.evaluateUndefinedAttributesAsNullAttribute

    public var booleanLiteral: DynamapConfiguration.BooleanLiteral = dynamap.configuration.booleanLiteral

    public var indexMapsByKeys: Boolean = dynamap.configuration.indexMapsByKeys

    fun build(): DynamapConfiguration = DynamapConfiguration(
        classDiscriminator = this@DynamapBuilder.classDiscriminator,
        evaluateUndefinedAttributesAsNullAttribute = this@DynamapBuilder.evaluateUndefinedAttributesAsNullAttribute,
        booleanLiteral = this@DynamapBuilder.booleanLiteral,
        indexMapsByKeys = this@DynamapBuilder.indexMapsByKeys,
    )
}
