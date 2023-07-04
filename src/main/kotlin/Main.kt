import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import kotlin.math.*

fun main() {
    val width = 640
    val height = 480

    glfwInit()
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)
    val windowHandle = glfwCreateWindow(width, height, "Flume", 0, 0)
    glfwMakeContextCurrent(windowHandle)
    glfwSwapInterval(1)
    glfwShowWindow(windowHandle)

    GL.createCapabilities()

    val context = DirectContext.makeGL()

    val fbId = GL11.glGetInteger(0x8CA6)
    val renderTarget = BackendRenderTarget.makeGL(width, height, 0, 8, fbId, FramebufferFormat.GR_GL_RGBA8)

    val surface = Surface.makeFromBackendRenderTarget(
        context,
        renderTarget,
        SurfaceOrigin.BOTTOM_LEFT,
        SurfaceColorFormat.RGBA_8888,
        ColorSpace.sRGB
    )!!

    val canvas = surface.canvas

    while (!glfwWindowShouldClose(windowHandle)) {
        canvas.clear(0xFFFFFFFF.toInt())

        val paintRed = Paint().apply { color = 0xFFFF0000.toInt() }
        val paintGreen = Paint().apply { color = 0xFF00FF00.toInt() }

        canvas.drawRect(Rect.makeXYWH(100f, 100f, 100f, 100f), paintRed)

        val paintOpacity = Paint().apply { alpha = 50 }
        canvas.saveLayer(Rect.makeXYWH(0f, 0f, width.toFloat(), height.toFloat()), paintOpacity)

        canvas.drawRect(Rect.makeXYWH(300f, 100f, 100f, 100f), paintGreen)

        canvas.restore()

        context.flush()
        glfwSwapBuffers(windowHandle)
        glfwPollEvents()
    }
}