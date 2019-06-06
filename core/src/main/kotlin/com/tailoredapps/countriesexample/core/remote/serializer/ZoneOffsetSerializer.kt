package com.tailoredapps.countriesexample.core.remote.serializer

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.withName
import org.threeten.bp.DateTimeException
import org.threeten.bp.ZoneOffset

@Serializer(forClass = ZoneOffset::class)
object ZoneOffsetSerializer : KSerializer<ZoneOffset> {
    override val descriptor: SerialDescriptor = StringDescriptor.withName(ZoneOffset::class.java.simpleName)

    override fun serialize(encoder: Encoder, obj: ZoneOffset) {
        encoder.encodeString(obj.toString())
    }

    override fun deserialize(decoder: Decoder): ZoneOffset {
        return try {
            ZoneOffset.of(decoder.decodeString().removePrefix("UTC"))
        } catch (e: DateTimeException) {
            ZoneOffset.UTC
        }
    }
}