package com.librarysystem.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.librarysystem.model.Book;

import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class JsonDataStorage implements DataStorage {

    private static final Logger logger = LoggerFactory.getLogger(JsonDataStorage.class);
    
    private final String filePath;
    private final ObjectMapper objectMapper; // Jackson ObjectMapper nesnesi

    /**
     * Varsayılan constructor - Spring Bean için
     * application.properties'ten değeri okur veya varsayılan değer kullanır
     */
    public JsonDataStorage(@Value("${library.data.file:data/library_data.json}") String filePath) {
        this.filePath = filePath;
        this.objectMapper = new ObjectMapper();
        // JSON'un daha okunabilir olması için pretty printing özelliğini etkinleştir
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // Dosya yoksa oluşturmayı dene
        ensureFileExists();
        logger.info("JsonDataStorage başlatıldı. Dosya yolu: {}", this.filePath);
    }

    @Override
    public void saveBooks(List<Book> books) {
        try {
            File file = new File(filePath);
            // Gerekirse üst dizinleri oluştur
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            objectMapper.writeValue(file, books);
        } catch (IOException e) {
            logger.error("Kitapları dosyaya kaydederken hata oluştu ({}): {}", filePath, e.getMessage(), e);
            // Hata durumunda ne yapılacağına karar verilebilir (örn. loglama, kullanıcıya bildirme)
        }
    }

    @Override
    public List<Book> loadBooks() {
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) {
             logger.debug("Veri dosyası bulunamadı veya boş ({}). Yeni bir liste oluşturuluyor.", filePath);
            return new ArrayList<>(); // Dosya yoksa veya boşsa boş liste döndür
        }

        try {
            // JSON dosyasından Book listesine dönüştürme
            return objectMapper.readValue(file, new TypeReference<List<Book>>() {});
        } catch (IOException e) {
            logger.error("Kitapları dosyadan yüklerken hata oluştu ({}): {}", filePath, e.getMessage(), e);
            // Hata durumunda boş liste döndür veya hatayı yeniden fırlat
            return new ArrayList<>();
        }
    }

     /**
     * Veri dosyasının var olduğundan emin olur, yoksa oluşturur.
     */
    private void ensureFileExists() {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                File parentDir = file.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs(); // Üst dizinleri oluştur
                }
                if (file.createNewFile()) {
                    logger.info("Veri dosyası oluşturuldu: {}", filePath);
                    // Yeni oluşturulan dosyaya boş bir JSON dizisi yazabiliriz
                    saveBooks(new ArrayList<>());
                }
            } catch (IOException e) {
                logger.error("Veri dosyası oluşturulurken hata oluştu ({}): {}", filePath, e.getMessage(), e);
            }
        }
    }
}
