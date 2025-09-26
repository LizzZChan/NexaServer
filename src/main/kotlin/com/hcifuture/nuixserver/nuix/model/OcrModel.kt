import net.sourceforge.tess4j.ITesseract
import net.sourceforge.tess4j.Tesseract
import net.sourceforge.tess4j.TesseractException
import java.io.File


class OcrModel(private val language: String = "zh", private val datapath: String? = null) {

  private val tesseract: ITesseract = Tesseract()

  init {
    tesseract.setLanguage(language)
    datapath?.let {
      tesseract.setDatapath(it)
    }
  }

  fun performOcr(imageFile: File?): String {
    return try {
      tesseract.doOCR(imageFile)
    } catch (e: TesseractException) {
      e.printStackTrace()
      "Error during OCR processing: ${e.message}"
    }
  }
}
