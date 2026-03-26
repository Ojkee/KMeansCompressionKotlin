package window

import raylibffi.Image
import raylibffi.Keys
import raylibffi.Raylib
import raylibffi.Texture
import windowUtil.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class Window(
    val width: Int,
    val height: Int,
    val fileName: String,
    val K: Int,
) : AutoCloseable {
    private val rl = Raylib.INSTANCE
    private var lena: Texture.ByValue

    private val imgWidth = 500
    private val imgHeight = 500
    private val imgOffsetX = width - imgWidth
    private var pixelSpace: PixelSpace

    private val plotWidth = imgWidth / 2
    private val plotHeight = imgHeight / 2
    private var angle = 0f
    private var angleDelta = 0.02f

    private var means: Array<Vector>
    private var meanRadius = 2f
    private var lenaMeans: Texture.ByValue

    init {
        require(K > 0) { "K must by positive, got $K" }
        require(width > 0 && height > 0) { "Screen size must be positive, got ($width x $height)" }

        rl.InitWindow(width, height, "KMeansCompression")
        lena = rl.LoadTexture(fileName)
        pixelSpace = pixelSpaceOfImage(rl.LoadImage(fileName))
        means = Array(K) { generateRandomMean() }
        lenaMeans = arrayToTexture(pixelColorsFromMeans())
    }

    fun run() {
        while (!rl.WindowShouldClose()) {
            event()
            update()
            draw()
        }
    }

    private fun event() {
        if (rl.IsKeyPressed(Keys.SPACE)) {
            updateMeans()
            rl.UnloadTexture(lenaMeans)
            lenaMeans = arrayToTexture(pixelColorsFromMeans())
        }
    }

    private fun update() {
        angle -= angleDelta
    }

    private fun draw() {
        rl.BeginDrawing()
        rl.ClearBackground(GRAY)
        rl.DrawTexture(lena, imgOffsetX, 0, WHITE)
        rl.DrawTexture(lenaMeans, imgOffsetX, imgHeight, WHITE)
        drawImagePlot()
        drawVectors(means)
        rl.EndDrawing()
    }

    private fun drawImagePlot() =
        pixelSpace
            .map { (p, v) -> (p to rotateXZ(v)) }
            .sortedByDescending { (_, v) -> v.z }
            .map(::normPixelVectorToPixel)
            .map(::translateCenter)
            .forEach { rl.DrawPixel(it.x, it.y, it.color) }

    private fun drawVectors(vs: Array<Vector>) =
        vs
            .asSequence()
            .map(::rotateXZ)
            .map(::vectorToPixel)
            .map(::translateCenter)
            .forEach {
                rl.DrawCircle(it.x, it.y, meanRadius + 2f, GRAY)
                rl.DrawCircle(it.x, it.y, meanRadius, it.color)
            }

    private fun pixelSpaceOfImage(image: Image.ByValue): PixelSpace {
        fun imgPosToPixel(coords: Pair<Int, Int>): Pixel {
            val (x, y) = coords
            val color = rl.GetImageColor(image, x, y)
            return Pixel(color, x, y)
        }

        return cartesian(0 until imgHeight, 0 until imgWidth)
            .map(::imgPosToPixel)
            .map(::pixelWithNormVector)
            .toList()
            .toTypedArray()
    }

    private fun pixelWithNormVector(pixel: Pixel): Pair<Pixel, Vector> {
        fun normChannel(c: Int): Float = c.toFloat() * 2f / 255f - 1f
        val (r, g, b) = colorToRGB(pixel.color)
        return (pixel to Vector(normChannel(r), normChannel(g), normChannel(b)))
    }

    fun rotateXZ(v: Vector): Vector {
        val cosAngle = cos(angle)
        val sinAngle = sin(angle)
        val newX = v.x * cosAngle - v.z * sinAngle
        val newZ = v.x * sinAngle + v.z * cosAngle
        return v.copy(x = newX, z = newZ)
    }

    private fun normPixelVectorToPixel(pv: PixelVector): Pixel {
        val (p, v) = pv
        return p.copy(
            x = (v.x * plotWidth / 2).toInt(),
            y = (v.y * plotHeight / 2).toInt(),
        )
    }

    private fun translateCenter(pixel: Pixel): Pixel =
        pixel.copy(
            x = pixel.x + imgOffsetX / 2,
            y = pixel.y + height / 2,
        )

    private fun generateRandomMean(): Vector {
        fun normFloat() = Random.nextFloat() * 2 - 1
        return Vector(normFloat(), normFloat(), normFloat())
    }

    private fun vectorToPixel(v: Vector): Pixel =
        Pixel(
            v.toColor(),
            x = (v.x * plotWidth / 2).toInt(),
            y = (v.y * plotHeight / 2).toInt(),
        )

    private fun updateMeans() {
        val clusters = pixelSpace.groupBy { (_, v) -> closestMeanIdx(v) }
        means =
            Array(K) { i ->
                clusters[i]?.map { (_, v) -> v }?.average() ?: generateRandomMean()
            }
    }

    private fun closestMeanIdx(v: Vector): Int = means.indices.minBy { i -> means[i].dist2(v) }

    private fun pixelColorsFromMeans(): Array<Pixel> =
        pixelSpace
            .map { (p, v) ->
                val meanColor = means[closestMeanIdx(v)].toColor()
                p.copy(color = meanColor)
            }.toTypedArray()

    fun arrayToTexture(pixels: Array<Pixel>): Texture.ByValue {
        val img = rl.GenImageColor(imgWidth, imgHeight, 0)
        val bytes = img.data!!.getByteArray(0, imgWidth * imgHeight * 4)

        for (pixel in pixels) {
            val i = (pixel.y * imgWidth + pixel.x) * 4
            bytes[i + 0] = (pixel.color and 0xFF).toByte()
            bytes[i + 1] = (pixel.color shr 8 and 0xFF).toByte()
            bytes[i + 2] = (pixel.color shr 16 and 0xFF).toByte()
            bytes[i + 3] = 255.toByte()
        }

        img.data!!.write(0, bytes, 0, bytes.size)
        val texture = rl.LoadTextureFromImage(img)
        rl.UnloadImage(img)
        return texture
    }

    override fun close() {
        rl.UnloadTexture(lena)
        rl.CloseWindow()
    }
}
