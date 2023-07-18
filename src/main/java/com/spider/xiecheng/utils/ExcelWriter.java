package com.spider.xiecheng.utils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelWriter {
    private Workbook workbook;
    private Sheet sheet;
    private String filename;
    List<Row> rowHandles = new ArrayList<>();
    public void open(String filename) {
        this.filename = filename;
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet();
    }
    public void write(int row, int col, String val) {
        if (rowHandles.size() < row + 1) {
            rowHandles.add(sheet.createRow(row));
        }
        Row r = rowHandles.get(row);
        Cell c = r.createCell(col);
        c.setCellValue(val);
    }
    public void save() {
        // 保存Excel文件
        try (FileOutputStream outputStream = new FileOutputStream(filename + ".xlsx")) {
            workbook.write(outputStream);
            System.out.println("Excel file created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        close();
    }
    private void close() {
        // 关闭工作簿
        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
