package com.codanbaru.serialization

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import com.codanbaru.serialization.extension.subproperty
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule

@OptIn(ExperimentalSerializationApi::class)
internal class DynamoMapCompositeEncoder(
    override val property: String,
    override val configuration: DynamapConfiguration,
    override val serializersModule: SerializersModule,
    private val consumer: (Map<String, AttributeValue>) -> Unit,
) : DynamoCompositeEncoder(property, configuration, serializersModule) {
    private var `object`: MutableMap<String, AttributeValue> = mutableMapOf()

    // implementation borrowed from JsonEncoder - https://github.com/Kotlin/kotlinx.serialization/blob/master/formats/json/commonMain/src/kotlinx/serialization/json/internal/TreeJsonEncoder.kt#L234
    private lateinit var key: String
    private var isKey: Boolean = true

    override fun <T> encodeElement(
        descriptor: SerialDescriptor,
        index: Int,
        value: T,
        builder: (List<Annotation>, SerialDescriptor, String, T, (AttributeValue) -> Unit) -> Unit,
    ) {
        val elementAnnotations = annotationsAtIndex(descriptor, index)
        val elementDescriptor = descriptorAtIndex(descriptor, index)
        val elementName = propertyAtIndex(descriptor, index)

        builder(elementAnnotations, elementDescriptor, elementName, value) {
            if (descriptor.kind is StructureKind.MAP && configuration.indexMapsByKeys) {
                when (isKey) {
                    true -> when (it) {
                        is AttributeValue.S -> {
                            key = it.asS()
                            isKey = false
                        }
                        else -> error(
                            "dynamo maps must have string-able keys: property=${property.subproperty(
                                elementName,
                            )}, value=$value",
                        )
                    }
                    false -> {
                        `object`[key] = it // CHECK: Should we raise exception if encoder is already finished?
                        isKey = true
                    }
                }
            } else {
                `object`[elementName] = it // CHECK: Should we raise exception if encoder is already finished?
            }
        }
    }

    override fun encodeInlineElement(
        descriptor: SerialDescriptor,
        index: Int,
        builder: (List<Annotation>, SerialDescriptor, String, (AttributeValue) -> Unit) -> Encoder,
    ): Encoder {
        val elementAnnotations = annotationsAtIndex(descriptor, index)
        val elementDescriptor = descriptorAtIndex(descriptor, index)
        val elementName = propertyAtIndex(descriptor, index)

        return builder(elementAnnotations, elementDescriptor, elementName) {
            `object`[elementName] = it // CHECK: Should we raise exception if encoder is already finished?
        }
    }

    override fun finish() {
        // CHECK: Should we raise exception if encoder is already finished?

        consumer(`object`.toMap())
    }

    override fun shouldEncodeElementDefault(
        descriptor: SerialDescriptor,
        index: Int,
    ): Boolean = true

    private fun annotationsAtIndex(
        descriptor: SerialDescriptor,
        index: Int,
    ): List<Annotation> = descriptor.getElementAnnotations(index)

    private fun propertyAtIndex(
        descriptor: SerialDescriptor,
        index: Int,
    ): String = descriptor.getElementName(index)

    private fun descriptorAtIndex(
        descriptor: SerialDescriptor,
        index: Int,
    ): SerialDescriptor = descriptor.getElementDescriptor(index)
}
