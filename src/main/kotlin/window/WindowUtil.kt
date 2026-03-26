package windowUtil

val GRAY = rgbToColor(51, 51, 51, 255)
val BEIGE = rgbToColor(255, 248, 231, 255)
val WHITE = rgbToColor(255, 255, 255, 255)

typealias PixelVector = Pair<Pixel, Vector>
typealias PixelSpace = Array<PixelVector>

data class Pixel(
    val color: Int,
    val x: Int,
    val y: Int,
)

data class Vector(
    val x: Float,
    val y: Float,
    val z: Float,
) {
    public fun dist2(other: Vector): Float {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return dx * dx + dy * dy + dz * dz
    }

    fun toColor(): Int {
        fun denorm(f: Float) = ((f + 1f) / 2f * 255f).toInt().coerceIn(0, 255)
        return rgbToColor(denorm(x), denorm(y), denorm(z), 255)
    }
}

fun <A, B> cartesian(
    xs: Iterable<A>,
    ys: Iterable<B>,
) = sequence {
    for (x in xs) for (y in ys) yield(x to y)
}

fun rgbToColor(
    r: Int,
    g: Int,
    b: Int,
    a: Int,
) = (a shl 24) or (b shl 16) or (g shl 8) or r

fun colorToRGB(color: Int): Triple<Int, Int, Int> =
    Triple(
        (color) and 0xFF,
        (color shr 8) and 0xFF,
        (color shr 16) and 0xFF,
    )

fun List<Vector>.average() =
    Vector(
        x = map { it.x }.average().toFloat(),
        y = map { it.y }.average().toFloat(),
        z = map { it.z }.average().toFloat(),
    )
