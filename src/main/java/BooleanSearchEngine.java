import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class BooleanSearchEngine implements SearchEngine {
    private final Map<String, List<PageEntry>> indexedFiles = new HashMap<>();

    public BooleanSearchEngine(File pdfsDir) throws IOException {
        File[] pdfFiles = pdfsDir.listFiles();

        if (pdfFiles != null) {
            for (File pdfFile : pdfFiles) {
                if (pdfFile.getName().contains(".pdf")) { // Рассматриваем только пдф файлы
                    var doc = new PdfDocument(new PdfReader(pdfFile.getPath()));

                    for (int p = 1; p <= doc.getNumberOfPages(); p++) {
                        PdfPage currentPage = doc.getPage(p);
                        String text = PdfTextExtractor.getTextFromPage(currentPage);
                        String[] words = text.split("\\P{IsAlphabetic}+");
                        Map<String, Integer> freqs = new HashMap<>();
                        for (var word : words) {
                            if (word.isEmpty()) {
                                continue;
                            }
                            freqs.put(word.toLowerCase(), freqs.getOrDefault(word.toLowerCase(), 0) + 1);
                        }
                        for (Map.Entry<String, Integer> entry : freqs.entrySet()) {
                            PageEntry pEntry = new PageEntry(pdfFile.getName(), p, entry.getValue());

                            if (!indexedFiles.containsKey(entry.getKey())) {
                                indexedFiles.put(entry.getKey(), new ArrayList<>());
                            }
                            indexedFiles.get(entry.getKey()).add(pEntry);
                        }
                    }

                    doc.close();
                }
            }
        } else {
            throw new IOException("Директории " + pdfsDir.getName() + " не существует");
        }
    }

    @Override
    public List<PageEntry> search(String word) {
        return indexedFiles.containsKey(word.toLowerCase())
                ? indexedFiles.get(word.toLowerCase()).stream()
                .sorted().collect(Collectors.toList())
                : new ArrayList<>();
    }
}