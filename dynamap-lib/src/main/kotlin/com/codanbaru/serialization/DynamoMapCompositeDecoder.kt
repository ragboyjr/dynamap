package com.codanbaru.serialization

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import com.codanbaru.serialization.extension.subproperty
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.SerializersModule

@OptIn(ExperimentalSerializationApi::class)
internal class DynamoMapCompositeDecoder(
    private val `object`: Map<String, AttributeValue>,

    override val property: String,

    override val configuration: DynamapConfiguration,
    override val serializersModule: SerializersModule
): DynamoCompositeDecoder(property, configuration, serializersModule) {
    private val keys by lazy { `object`.keys.toList() }

    override fun <T> decodeElement(descriptor: SerialDescriptor, index: Int, builder: (List<Annotation>, SerialDescriptor, String, AttributeValue) -> T): T {
        val elementAnnotations = annotationsAtIndex(descriptor, index)
        val elementDescriptor = descriptorAtIndex(descriptor, index)
        val elementName = propertyAtIndex(descriptor, index)
        val element = elementAtIndex(descriptor, index)

        return builder(elementAnnotations, elementDescriptor, elementName, element)
    }

    private var currentIndex = 0
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        when (descriptor.kind) {
            is StructureKind.CLASS -> if (currentIndex >= descriptor.elementsCount) return CompositeDecoder.DECODE_DONE
            is StructureKind.MAP -> if (currentIndexIsInvalidForMapKind()) return CompositeDecoder.DECODE_DONE
            else -> Unit
        }

        val currentIndex = this.currentIndex
        this.currentIndex += 1

        // Check if the element we try to decode is present,
        // is element is not present, try to decode next element index.
        val element = try { elementAtIndex(descriptor, currentIndex) } catch (throwable: Throwable) { null }
        if (element == null) {
            return decodeElementIndex(descriptor)
        } else {
            return currentIndex
        }
    }

    /**
     * For maps, kotlinx serialization expects to call decodeElement 2 times for every *entry* in the final map which is
     * why we need to double the map size when indexing by keys.
     */
    private fun currentIndexIsInvalidForMapKind() = if (configuration.indexMapsByKeys) {
        currentIndex >= `object`.size * 2
    } else {
        currentIndex >= `object`.size
    }

    private fun annotationsAtIndex(descriptor: SerialDescriptor, index: Int): List<Annotation> =
        descriptor.getElementAnnotations(index)

    private fun propertyAtIndex(descriptor: SerialDescriptor, index: Int): String =
        descriptor.getElementName(index)

    private fun descriptorAtIndex(descriptor: SerialDescriptor, index: Int): SerialDescriptor =
        descriptor.getElementDescriptor(index)

    private fun elementAtIndex(descriptor: SerialDescriptor, index: Int): AttributeValue {
        val propertyName = propertyAtIndex(descriptor, index)

        if (descriptor.kind is StructureKind.MAP && configuration.indexMapsByKeys) {
            val key = keys[index / 2]
            return when (index % 2 == 0) {
                true -> AttributeValue.S(key)
                false -> `object`.getValue(key)
            }
        }

        var element = `object`[propertyName]
        if (element == null && configuration.evaluateUndefinedAttributesAsNullAttribute) {
            if (!descriptor.isElementOptional(index)) {
                element = AttributeValue.Null(true)
            }
        }

        return element ?: throw DynamapSerializationException.UnexpectedUndefined(property.subproperty(propertyName))
    }
}
