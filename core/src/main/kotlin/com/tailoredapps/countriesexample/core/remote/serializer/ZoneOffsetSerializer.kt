package com.tailoredapps.countriesexample.core.remote.serializer

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import org.threeten.bp.DateTimeException
import org.threeten.bp.ZoneOffset

@Serializer(forClass = ZoneOffset::class)
object ZoneOffsetSerializer : KSerializer<ZoneOffset?> {
    override val descriptor: SerialDescriptor = StringDescriptor.withName(ZoneOffset::class.java.simpleName)

    override fun serialize(encoder: Encoder, obj: ZoneOffset?) {
        encoder.encodeString(obj.toString())
    }

    override fun deserialize(decoder: Decoder): ZoneOffset? {
        return try {
            if (decoder.decodeNotNullMark()) {
                ZoneOffset.of(decoder.decodeString().removePrefix("UTC"))
            } else {
                null
            }
        } catch (e: DateTimeException) {
            null
        }
    }
}