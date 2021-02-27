import java.awt.image.BufferedImage
import javax.imageio.ImageIO

fun getImage(path: String): BufferedImage {
    return Thread.currentThread().contextClassLoader.getResource(path)
        ?.openStream().use(ImageIO::read)
}