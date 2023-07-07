package common

import org.jetbrains.skia.*

class LayerTree {
    var rootLayer: Layer? = null

    fun preroll() {
        assert(rootLayer != null)

        rootLayer!!.preroll()
    }

    fun paint(context: PaintContext) {
        rootLayer?.paint(context)
    }
}


abstract class Layer {
    var paintBounds = Rect.Companion.makeWH(0f, 0f)

    abstract fun paint(context: PaintContext)
    abstract fun preroll()
}

open class ContainerLayer : Layer() {
    val children: MutableList<Layer> = mutableListOf()

    override fun preroll() {
        paintBounds = prerollChildren()
    }

    protected fun prerollChildren(): Rect {
        var bounds = kEmptyRect
        for (child in children) {
            child.preroll()
            bounds = bounds.join(child.paintBounds)
        }
        return bounds
    }

    override fun paint(context: PaintContext) {
        for (child in children) {
            child.paint(context)
        }
    }
}

class PictureLayer() : Layer() {
    var picture: Picture? = null

    override fun preroll() {
        paintBounds = picture!!.cullRect
    }

    override fun paint(context: PaintContext) {
        picture?.playback(context.canvas)
    }
}

class TransformLayer(var transform: Matrix33 = Matrix33.IDENTITY) : ContainerLayer() {
    companion object {
        fun withOffset(
            transform: Matrix33 = Matrix33.IDENTITY,
            offset: Offset = Offset.zero,
        ): TransformLayer {
            val move = Matrix33.makeTranslate(offset.dx.toFloat(), offset.dy.toFloat())
            return TransformLayer(move)
        }
    }

    override fun preroll() {
        val childPaintBounds = prerollChildren()
        paintBounds = transform.mapRect(childPaintBounds)
    }

    override fun paint(context: PaintContext) {
        context.canvas.save()
        context.canvas.concat(transform)

        super.paint(context)

        context.canvas.restore()
    }
}

class OpacityLayer(var alpha: Int? = null) : ContainerLayer() {
    override fun paint(context: PaintContext) {
        val paint = Paint()
        if (alpha != null) {
            paint.alpha = alpha!!
        }
        context.canvas.saveLayer(paintBounds.roundOut(), paint)
        super.paint(context)
        context.canvas.restore()
    }
}

class ClipPathLayer(var clipPath: Path, var clipBehavior: Clip = Clip.AntiAlias) : ContainerLayer() {
    override fun preroll() {
        val clipPathBounds = clipPath.bounds
        val childPaintBounds = prerollChildren()
        if (childPaintBounds.intersect(clipPathBounds) != null) {
            paintBounds = childPaintBounds
        }
    }

    override fun paint(context: PaintContext) {
        context.canvas.save()
        context.canvas.clipPath(clipPath, clipBehavior != Clip.HardEdge)

        super.paint(context)
        context.canvas.restore()
    }
}

class ClipRectLayer(var clipRect: Rect, var clipBehavior: Clip = Clip.AntiAlias) : ContainerLayer() {
    override fun preroll() {
        val childPaintBounds = prerollChildren()
        if (childPaintBounds.intersect(clipRect) != null) {
            paintBounds = childPaintBounds
        }
    }

    override fun paint(context: PaintContext) {
        context.canvas.save()
        context.canvas.clipRect(clipRect, clipBehavior != Clip.HardEdge)

        super.paint(context)
        context.canvas.restore()
    }
}

class ClipRRectLayer(var clipRRect: RRect, var clipBehavior: Clip = Clip.AntiAlias) : ContainerLayer() {
    override fun preroll() {
        val clipPaintBounds = prerollChildren()
        if (clipPaintBounds.intersect(clipRRect) != null) {
            paintBounds = clipPaintBounds
        }
    }

    override fun paint(context: PaintContext) {
        context.canvas.save()
        context.canvas.clipRRect(clipRRect, clipBehavior != Clip.HardEdge)

        super.paint(context)
        context.canvas.restore()
    }
}

data class PaintContext(
    val canvas: Canvas,
    val context: DirectContext,
)

enum class Clip {
    None, HardEdge, AntiAlias
}
