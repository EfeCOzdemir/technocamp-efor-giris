package com.technocamp.efor.report;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import com.technocamp.efor.performance.PerformanceResponse;

@Component
public class ExcelExporter {

    private static final String[] HEADERS =
            {"Temsilci Adı", "Sicil", "Tarih", "Başlangıç", "Bitiş", "Mola (saat)", "Mazeret", "Mazeret Saati"};
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    public byte[] export(List<PerformanceResponse> rows) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Performans Raporu");

            Font bold = workbook.createFont();
            bold.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(bold);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                header.createCell(i).setCellValue(HEADERS[i]);
                header.getCell(i).setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (PerformanceResponse p : rows) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(p.firstName() + " " + p.surname());
                row.createCell(1).setCellValue(p.agentId());
                row.createCell(2).setCellValue(p.dateInfo() == null ? "" : DATE.format(p.dateInfo()));
                row.createCell(3).setCellValue(p.beginTime() == null ? "" : TIME.format(p.beginTime()));
                row.createCell(4).setCellValue(p.endTime() == null ? "" : TIME.format(p.endTime()));
                row.createCell(5).setCellValue(p.timeout() == null ? 0 : p.timeout());
                row.createCell(6).setCellValue(p.excuse() == null ? "" : p.excuse());
                row.createCell(7).setCellValue(p.excuseHours() == null ? 0 : p.excuseHours());
            }

            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }
            sheet.createFreezePane(0, 1);
            workbook.write(out);
            return out.toByteArray();
        }
    }
}
