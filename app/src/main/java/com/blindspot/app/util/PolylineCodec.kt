package com.blindspot.app.util

import com.blindspot.app.data.model.RoutePoint
import kotlin.math.round

/**
 * Encoder/decoder for the Google "Encoded Polyline Algorithm Format", the compact string
 * representation Geoapify (and most routing backends) use to ship a route geometry.
 *
 * The backend returns the route as an encoded polyline string; the client decodes it into a
 * list of coordinates to render on the map. [encode] exists mainly so the mock routing layer
 * can produce a realistic payload that exercises the same [decode] path used in production.
 *
 * Kept framework-free and pure so it is trivially unit-testable.
 */
object PolylineCodec {

    /** Default coordinate precision. Geoapify encodes at 5 decimal places. */
    const val DEFAULT_PRECISION = 5

    /**
     * Decodes an [encoded] polyline string into ordered [RoutePoint]s.
     *
     * [precision] must match the value the geometry was encoded with (5 for Geoapify). A blank
     * string decodes to an empty list.
     */
    fun decode(encoded: String, precision: Int = DEFAULT_PRECISION): List<RoutePoint> {
        val factor = Math.pow(10.0, precision.toDouble())
        val points = ArrayList<RoutePoint>()
        var index = 0
        var lat = 0
        var lng = 0

        while (index < encoded.length) {
            lat += decodeDelta(encoded, index).also { index = it.nextIndex }.value
            lng += decodeDelta(encoded, index).also { index = it.nextIndex }.value
            points.add(RoutePoint(latitude = lat / factor, longitude = lng / factor))
        }
        return points
    }

    /**
     * Encodes [points] into a polyline string at the given [precision]. Inverse of [decode].
     */
    fun encode(points: List<RoutePoint>, precision: Int = DEFAULT_PRECISION): String {
        val factor = Math.pow(10.0, precision.toDouble())
        val builder = StringBuilder()
        var prevLat = 0
        var prevLng = 0

        for (point in points) {
            val lat = round(point.latitude * factor).toInt()
            val lng = round(point.longitude * factor).toInt()
            encodeDelta(lat - prevLat, builder)
            encodeDelta(lng - prevLng, builder)
            prevLat = lat
            prevLng = lng
        }
        return builder.toString()
    }

    private data class Delta(val value: Int, val nextIndex: Int)

    private fun decodeDelta(encoded: String, start: Int): Delta {
        var index = start
        var shift = 0
        var result = 0
        var byte: Int
        do {
            byte = encoded[index++].code - 63
            result = result or ((byte and 0x1f) shl shift)
            shift += 5
        } while (byte >= 0x20)
        val delta = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        return Delta(value = delta, nextIndex = index)
    }

    private fun encodeDelta(delta: Int, builder: StringBuilder) {
        var value = if (delta < 0) (delta shl 1).inv() else delta shl 1
        while (value >= 0x20) {
            builder.append(((0x20 or (value and 0x1f)) + 63).toChar())
            value = value shr 5
        }
        builder.append((value + 63).toChar())
    }
}
