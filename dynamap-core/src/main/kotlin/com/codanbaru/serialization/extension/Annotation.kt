package com.codanbaru.serialization.extension

import com.codanbaru.serialization.annotation.SerialType
import com.codanbaru.serialization.dynamodb.DynamoType

internal val List<Annotation>.serialTypeAnnotation: SerialType?
    get() {
        return firstOrNull { it is SerialType } as SerialType?
    }
internal val List<Annotation>.dynamoTypeAnnotation: DynamoType?
    get() {
        return serialTypeAnnotation?.type
    }
