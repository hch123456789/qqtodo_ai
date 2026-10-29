package com.example.qqtodoAi.service;

import com.example.qqtodoAi.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class FileParseService {

    public String extractText(File file, String fileName) {
        try {
            if (isWordXml(file)) {
                return extractWordXml(file);
            }

            String ext = FileUtils.getExtension(fileName).toLowerCase();
            switch (ext) {
                case "pdf":
                    return extractPdf(file);
                case "docx":
                    return extractDocxWithFallback(file);
                case "doc":
                    return extractDoc(file);
                case "xlsx":
                case "xls":
                    return extractXlsx(file);
                default:
                    log.warn("[文件] 不支持的类型: {}", ext);
                    return null;
            }
        } catch (Exception e) {
            log.error("[文件] 解析失败: {}", fileName, e);
            return null;
        }
    }

    private String extractPdf(File file) throws Exception {
        try (PDDocument doc = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    private String extractDocxWithFallback(File file) throws Exception {
        String text = extractDocx(file);
        if (text == null || text.isBlank()) {
            log.warn("[文件] docx 提取为空，尝试按 doc 格式解析");
            return extractDoc(file);
        }
        return text;
    }

    private String extractDocx(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument doc = new XWPFDocument(fis)) {

            StringBuilder sb = new StringBuilder();

            for (XWPFParagraph p : doc.getParagraphs()) {
                String line = p.getText();
                if (line != null && !line.isBlank()) {
                    sb.append(line).append("\n");
                }
            }

            for (XWPFTable table : doc.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        sb.append(cell.getText()).append("\t");
                    }
                    sb.append("\n");
                }
                sb.append("\n");
            }

            return sb.toString();
        }
    }

    private String extractDoc(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file);
             HWPFDocument doc = new HWPFDocument(fis);
             WordExtractor extractor = new WordExtractor(doc)) {
            return extractor.getText();
        }
    }

    private String extractXlsx(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {
            StringBuilder sb = new StringBuilder();
            DataFormatter formatter = new DataFormatter();
            for (Sheet sheet : workbook) {
                sb.append("【").append(sheet.getSheetName()).append("】\n");
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        sb.append(formatter.formatCellValue(cell)).append("\t");
                    }
                    sb.append("\n");
                }
                sb.append("\n");
            }
            return sb.toString();
        }
    }

    private boolean isWordXml(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] head = new byte[5];
            int n = fis.read(head);
            if (n < 5) {
                return false;
            }
            return "<?xml".equals(new String(head, StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }

    private String extractWordXml(File file) throws Exception {
        String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);

        Pattern paraPattern = Pattern.compile("<w:p(?: [^>]*)?>.*?</w:p>", Pattern.DOTALL);
        Pattern textPattern = Pattern.compile("<w:t(?: [^>]*)?>([^<]*)</w:t>");

        StringBuilder result = new StringBuilder();
        Matcher paraMatcher = paraPattern.matcher(content);
        boolean hasPara = false;
        boolean first = true;

        while (paraMatcher.find()) {
            hasPara = true;
            if (!first) {
                result.append("\n");
            }
            Matcher tm = textPattern.matcher(paraMatcher.group());
            while (tm.find()) {
                result.append(tm.group(1));
            }
            first = false;
        }

        if (!hasPara) {
            Matcher tm = textPattern.matcher(content);
            while (tm.find()) {
                result.append(tm.group(1));
            }
        }

        return decodeEntities(result.toString());
    }

    private String decodeEntities(String s) {
        return s.replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&apos;", "'");
    }
}