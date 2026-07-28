package com.blindspot.app.util

import com.blindspot.app.data.model.RoutePoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies [PolylineCodec] against the canonical example from Google's Encoded Polyline
 * specification, plus a round-trip through [PolylineCodec.encode] and [PolylineCodec.decode].
 */
class PolylineCodecTest {

    @Test
    fun decode_matchesGoogleReferenceExample() {
        // From the Encoded Polyline Algorithm Format reference.
        val decoded = PolylineCodec.decode("_p~iF~ps|U_ulLnnqC_mqNvxq`@")

        val expected = listOf(
            RoutePoint(38.5, -120.2),
            RoutePoint(40.7, -120.95),
            RoutePoint(43.252, -126.453),
        )
        assertEquals(expected.size, decoded.size)
        expected.zip(decoded).forEach { (want, got) ->
            assertEquals(want.latitude, got.latitude, 1e-5)
            assertEquals(want.longitude, got.longitude, 1e-5)
        }
    }

    @Test
    fun encode_matchesGoogleReferenceExample() {
        val points = listOf(
            RoutePoint(38.5, -120.2),
            RoutePoint(40.7, -120.95),
            RoutePoint(43.252, -126.453),
        )
        assertEquals("_p~iF~ps|U_ulLnnqC_mqNvxq`@", PolylineCodec.encode(points))
    }

    @Test
    fun encodeThenDecode_roundTripsWithinPrecision() {
        val points = listOf(
            RoutePoint(40.71234, -73.98765),
            RoutePoint(40.71890, -73.98012),
            RoutePoint(40.72567, -73.97456),
        )

        val decoded = PolylineCodec.decode(PolylineCodec.encode(points))

        assertEquals(points.size, decoded.size)
        points.zip(decoded).forEach { (want, got) ->
            assertEquals(want.latitude, got.latitude, 1e-5)
            assertEquals(want.longitude, got.longitude, 1e-5)
        }
    }

    @Test
    fun decode_emptyStringReturnsEmptyList() {
        assertTrue(PolylineCodec.decode("").isEmpty())
    }
}
