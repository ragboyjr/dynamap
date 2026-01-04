package com.codanbaru.serialization.writer

import com.codanbaru.serialization.dynamodb.DynamoType

public sealed class PrimitiveWriterException(
    public open val value: Any,
    public open val type: DynamoType,
    override val message: String? = null,
    override val cause: Throwable? = null,
) : Throwable(message, cause) {
    public class Generic(
        override val value: Any,
        override val type: DynamoType,
        override val message: String?,
        override val cause: Throwable?,
    ) : PrimitiveWriterException(value, type, message, cause)

    public class UnsupportedType(
        override val value: Any,
        override val type: DynamoType,
        public val supportedTypes: List<DynamoType>,
    ) : PrimitiveWriterException(
            value = value,
            type = type,
            message = supportedTypes.joinToString { "'$it'" }.let {
                "Unable to write '$value' value as '$type' dynamo type. Supported types: $it."
            },
        )
}
