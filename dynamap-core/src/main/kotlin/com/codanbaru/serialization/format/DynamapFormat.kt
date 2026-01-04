package com.codanbaru.serialization.format

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.SerializationStrategy

public interface DynamapFormat : SerialFormat {
    public fun <T> encodeToAttribute(
        serializer: SerializationStrategy<T>,
        value: T,
    ): AttributeValue

    public fun <T> decodeFromAttribute(
        deserializer: DeserializationStrategy<T>,
        value: AttributeValue,
    ): T
}
