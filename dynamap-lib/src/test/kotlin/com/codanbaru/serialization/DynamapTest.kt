package com.codanbaru.serialization

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import com.codanbaru.serialization.format.decodeFromAttribute
import com.codanbaru.serialization.format.decodeFromItem
import com.codanbaru.serialization.format.encodeToAttribute
import com.codanbaru.serialization.format.encodeToItem
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

class DynamapTest {
    var dynamap = Dynamap {
        classDiscriminator = "_type"
    }

    @Test
    fun `only primitives`() {
        assertCodec(
            Fixtures.Primitives(
                string = "string",
                char = 'c',
                short = 0,
                int = 1,
                long = 2,
                float = 3.0F,
                double = 4.0,
            ),
            mapOf(
                "string" to AttributeValue.S("string"),
                "char" to AttributeValue.N("c"),
                "short" to AttributeValue.N("0"),
                "int" to AttributeValue.N("1"),
                "long" to AttributeValue.N("2"),
                "float" to AttributeValue.N("3.0"),
                "double" to AttributeValue.N("4.0"),
            )
        )
    }

    @Test
    fun `optionals with all values set`() {
        assertCodec(
            Fixtures.Optionals(
                a = 1,
                b = 2,
                c = 3
            ),
            mapOf(
                "a" to AttributeValue.N("1"),
                "b" to AttributeValue.N("2"),
                "c" to AttributeValue.N("3"),
            )
        )
    }

    @Test
    fun `optionals with no optionals set`() {
        assertAll(
            {
                assertCodec(
                    Fixtures.Optionals(
                        a = 0
                    ),
                    mapOf(
                        "a" to AttributeValue.N("0"),
                        "b" to AttributeValue.N("1"),
                        "c" to AttributeValue.N("2"),
                    )
                )
            },
            {
                assertDecode(
                    Fixtures.Optionals(a = 0),
                    mapOf("a" to AttributeValue.N("0")),
                )
            }
        )
    }

    @Test
    fun `optionals with middle optionals not set`() {
        assertAll(
            {
                assertCodec(
                    Fixtures.Optionals(
                        a = 0,
                        c = 3,
                    ),
                    mapOf(
                        "a" to AttributeValue.N("0"),
                        "b" to AttributeValue.N("1"),
                        "c" to AttributeValue.N("3"),
                    )
                )
            },
            {
                assertDecode(
                    Fixtures.Optionals(a = 0, c = 3),
                    mapOf(
                        "a" to AttributeValue.N("0"),
                        "c" to AttributeValue.N("3")
                    ),
                )
            }
        )
    }

    @Test
    fun `cannot encode or decode objects`() {
        assertThrows<DynamapSerializationException.InvalidKind> {
            dynamap.encodeToItem(Fixtures.SingletonObject)
        }
    }

    @Test
    fun `supports lists - non empty`() {
        assertCodec(
            Fixtures.ContainsList(listOf("a")),
            mapOf("list" to AttributeValue.L(listOf(AttributeValue.S("a"))))
        )
    }

    @Test
    fun `supports lists - empty`() {
        assertCodec(
            Fixtures.ContainsList(emptyList()),
            mapOf("list" to AttributeValue.L(emptyList()))
        )
    }

    @Test
    fun `supports maps - non empty - index by keys = false`() {
        dynamap = Dynamap {
            indexMapsByKeys = false
        }

        assertCodec(
            Fixtures.ContainsMap(mapOf("a" to 1, "b" to 2, "c" to 3)),
            mapOf("map" to AttributeValue.M(mapOf(
                "0" to AttributeValue.S("a"),
                "1" to AttributeValue.N("1"),
                "2" to AttributeValue.S("b"),
                "3" to AttributeValue.N("2"),
                "4" to AttributeValue.S("c"),
                "5" to AttributeValue.N("3"),
            )))
        )
    }

    @Test
    fun `can encode maps of enums`() {
        dynamap = Dynamap {
            indexMapsByKeys = true
        }

        val expectedValue = mapOf(
            Fixtures.TestEnum.TestA to 1
        )
        val expectedAttribute = AttributeValue.M(mapOf(
            "TestA" to AttributeValue.N("1")
        ))

        assertAll(
            { assertEquals(expectedAttribute, dynamap.encodeToAttribute(expectedValue)) },
            { assertEquals(expectedValue, dynamap.decodeFromAttribute(expectedAttribute)) },
            { assertEquals(expectedValue, dynamap.decodeFromAttribute(dynamap.encodeToAttribute(expectedValue))) },
        )
    }

    @Test
    fun `supports maps - non empty - index by keys = true`() {
        dynamap = Dynamap {
            indexMapsByKeys = true
        }

        assertCodec(
            Fixtures.ContainsMap(mapOf("a" to 1, "b" to 2, "c" to 3)),
            mapOf("map" to AttributeValue.M(mapOf(
                "a" to AttributeValue.N("1"),
                "b" to AttributeValue.N("2"),
                "c" to AttributeValue.N("3"),
            )))
        )
    }

    @Test
    fun `supports maps - empty`() {
        assertCodec(
            Fixtures.ContainsMap(emptyMap()),
            mapOf("map" to AttributeValue.M(emptyMap()))
        )
    }

    @Test
    fun `supports maps - nested types`() {
        dynamap = Dynamap { indexMapsByKeys = true }
        assertCodec(
            Fixtures.ContainsComplexMap(mapOf("a" to Fixtures.ContainsComplexMap.Nested(1))),
            mapOf("map" to AttributeValue.M(mapOf(
                "a" to AttributeValue.M(mapOf(
                    "a" to AttributeValue.N("1")
                ))
            )))
        )
    }

    @Test
    fun `polymorphic`() {
        assertAll(
            {
                assertCodec(
                    Fixtures.Polymorphic(type = Fixtures.Polymorphic.Type.A(1)),
                    mapOf(
                        "type" to AttributeValue.M(mapOf(
                            "_type" to AttributeValue.S("a"),
                            "a" to AttributeValue.N("1"),
                        ))
                    )
                )
            }
        )
    }

    inline fun <reified T>assertCodec(expectedObj: T, expectedItem: Map<String, AttributeValue>) {
        val encodedItem = dynamap.encodeToItem(expectedObj)
        assertAll(
            { assertEquals(expectedItem, encodedItem, "encoding to item") },
            { assertDecode(expectedObj, expectedItem) },
            { assertEquals(expectedObj, dynamap.decodeFromItem(encodedItem)) },
        )
    }

    inline fun <reified T>assertDecode(expectedObj: T, actualItem: Map<String, AttributeValue>) {
        assertEquals(expectedObj, dynamap.decodeFromItem(actualItem), "decoding from item")
    }
}