package raylibffi

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure

interface Raylib : Library {
    companion object {
        val INSTANCE: Raylib = Native.load("raylib", Raylib::class.java)
    }

    // Init
    fun InitWindow(
        width: Int,
        height: Int,
        title: String,
    )

    fun WindowShouldClose(): Boolean

    fun CloseWindow()

    // Draw
    fun BeginDrawing()

    fun EndDrawing()

    fun DrawTexture(
        texture: Texture.ByValue,
        posX: Int,
        posY: Int,
        tint: Int,
    )

    fun ClearBackground(color: Int)

    fun DrawPixel(
        x: Int,
        y: Int,
        color: Int,
    )

    fun DrawCircle(
        centerX: Int,
        centerY: Int,
        radius: Float,
        color: Int,
    )

    // Image and Texture
    fun LoadTexture(file_name: String): Texture.ByValue

    fun UnloadTexture(texture: Texture.ByValue)

    fun LoadImage(file_name: String): Image.ByValue

    fun UnloadImage(image: Image.ByValue)

    fun LoadTextureFromImage(img: Image.ByValue): Texture.ByValue

    fun GetImageColor(
        image: Image.ByValue,
        x: Int,
        y: Int,
    ): Int

    fun GenImageColor(
        width: Int,
        height: Int,
        color: Int,
    ): Image.ByValue

    // Input
    fun IsKeyPressed(key: Int): Boolean

    fun IsKeyDown(key: Int): Boolean

    fun GetKeyPressed(): Int
}

open class Image : Structure() {
    class ByValue :
        Image(),
        Structure.ByValue

    @JvmField var data: Pointer? = null

    @JvmField var width: Int = 0

    @JvmField var height: Int = 0

    @JvmField var mipmaps: Int = 0

    @JvmField var format: Int = 0

    override fun getFieldOrder() = listOf("data", "width", "height", "mipmaps", "format")
}

open class Texture : Structure() {
    class ByValue :
        Texture(),
        Structure.ByValue

    @JvmField var id: Int = 0

    @JvmField var width: Int = 0

    @JvmField var height: Int = 0

    @JvmField var mipmaps: Int = 0

    @JvmField var format: Int = 0

    override fun getFieldOrder() = listOf("id", "width", "height", "mipmaps", "format")
}

object Keys {
    const val SPACE = 32
    const val ARROW_LEFT = 263
    const val ARROW_RIGHT = 262
}
