package br.com.devlovers.cache;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.encoding.Encoding;
import org.springframework.core.io.ClassPathResource;

public class FontCache {

    private static final Map<String, PDFont> CACHE = new HashMap<>();

    public static PDFont getFont(PDDocument document, String fontPath) throws IOException {
        if (!CACHE.containsKey(fontPath)) {
            ClassPathResource fontResource = new ClassPathResource(fontPath);
            PDFont font = PDTrueTypeFont.load(document, fontResource.getInputStream(), Encoding.getInstance(COSName.WIN_ANSI_ENCODING));
            CACHE.put(fontPath, font);
        }
        return CACHE.get(fontPath);
    }
}
