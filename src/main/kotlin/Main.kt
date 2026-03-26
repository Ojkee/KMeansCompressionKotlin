import window.Window

fun main() {
    Window(width = 1200, height = 1000, "imgs/Lenna.png", K = 10).use { window ->
        window.run()
    }
}
