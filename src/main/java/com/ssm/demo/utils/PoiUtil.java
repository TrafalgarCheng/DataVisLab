package com.ssm.demo.utils;

import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * author:13
 * date:2018-07
 */
public class PoiUtil {

    //excel默认宽度；
    private static int width = 512 * 14;
    //默认字体
    private static String excelfont = "微软雅黑";

    /**
     * @param excelName 导出的EXCEL名字
     * @param headers   导出的表格的表头
     * @param fileds    导出的数据 map.get(key) 对应的 key
     * @param formators 导出数据的样式
     * @param widths    表格的列宽度  默认为 512 * 14
     * @param data      数据集  List<Map>
     * @param response
     * @throws IOException
     */
    public static void exportFile(String excelName, String[] headers, String[] fileds, int[] formators, int[] widths, List<Map<String, Object>> data, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (widths == null) {
            widths = new int[fileds.length];
            for (int i = 0; i < fileds.length; i++) {
                widths[i] = width;
            }
        }
        if (formators == null) {
            formators = new int[fileds.length];
            for (int i = 0; i < fileds.length; i++) {
                formators[i] = 1;
            }
        }
        //设置文件名
        String fileName = "导出数据";
        if (!StringUtils.isEmpty(excelName)) {
            fileName = excelName;
        }
        //创建工作薄
        XSSFWorkbook wb = new XSSFWorkbook();
        //创建sheet
        XSSFSheet sheet = wb.createSheet("sheet1");
        //创建表头，没有则跳过此步骤
        int headerrow = 0;
        if (headers != null) {
            XSSFRow row = sheet.createRow(headerrow);
            //表头样式
            XSSFCellStyle style = wb.createCellStyle();
            XSSFFont font = wb.createFont();
            font.setBold(true);
            font.setFontName(excelfont);
            font.setFontHeightInPoints((short) 11);
            style.setFont(font);
            style.setAlignment(HorizontalAlignment.CENTER);
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
            style.setBorderTop(BorderStyle.THIN);
            for (int i = 0; i < headers.length; i++) {
                sheet.setColumnWidth((short) i, (short) widths[i]);
                XSSFCell cell = row.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(style);
            }
            headerrow++;
        }
        //表格主体
        if (data != null) {
            List styleList = new ArrayList();
            //列数
            for (int i = 0; i < fileds.length; i++) {
                XSSFCellStyle style = wb.createCellStyle();
                XSSFFont font = wb.createFont();
                font.setFontName(excelfont);
                font.setFontHeightInPoints((short) 10);
                style.setFont(font);
                style.setBorderBottom(BorderStyle.THIN);
                style.setBorderLeft(BorderStyle.THIN);
                style.setBorderRight(BorderStyle.THIN);
                style.setBorderTop(BorderStyle.THIN);
                if (formators[i] == 1) {
                    style.setAlignment(HorizontalAlignment.LEFT);
                } else if (formators[i] == 2) {
                    style.setAlignment(HorizontalAlignment.CENTER);
                } else if (formators[i] == 3) {
                    style.setAlignment(HorizontalAlignment.RIGHT);
                } else if (formators[i] == 4) {
                    //int类型
                    style.setAlignment(HorizontalAlignment.RIGHT);
                    style.setDataFormat(HSSFDataFormat.getBuiltinFormat("0"));
                } else if (formators[i] == 5) {
                    //float类型
                    style.setAlignment(HorizontalAlignment.RIGHT);
                    style.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0.00"));
                } else if (formators[i] == 6) {
                    //百分比类型
                    style.setAlignment(HorizontalAlignment.RIGHT);
                    style.setDataFormat(HSSFDataFormat.getBuiltinFormat("0.00%"));
                }
                styleList.add(style);
            }
            for (int i = 0; i < data.size(); i++) {  //行数
                XSSFRow row = sheet.createRow(headerrow);
                Map map = data.get(i);
                for (int j = 0; j < fileds.length; j++) {  //列数
                    XSSFCell cell = row.createCell(j);
                    Object o = map.get(fileds[j]);
                    if (o == null || "".equals(o)) {
                        cell.setCellValue("");
                    } else if (formators[j] == 4) {
                        //int
                        cell.setCellValue((Long.valueOf((map.get(fileds[j])) + "")).longValue());
                    } else if (formators[j] == 5 || formators[j] == 6) {
                        //float
                        cell.setCellValue((Double.valueOf((map.get(fileds[j])) + "")).doubleValue());
                    } else {
                        cell.setCellValue(map.get(fileds[j]) + "");
                    }
                    cell.setCellStyle((XSSFCellStyle) styleList.get(j));
                }
                headerrow++;
            }
        }
        //文件名+excel格式"xlsx"
        fileName = fileName + ".xlsx";
        String filename = "";
        try {
            filename = encodeChineseDownloadFileName(request, fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.setHeader("Content-disposition", filename);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-disposition", "attachment;filename=" + filename);
        response.setHeader("Pragma", "No-cache");
        OutputStream ouputStream = response.getOutputStream();
        wb.write(ouputStream);
        ouputStream.flush();
        ouputStream.close();
    }

    /**
     * 对文件流输出下载的中文文件名进行编码以屏蔽各种浏览器版本的差异性
     *
     * @throws UnsupportedEncodingException
     */
    public static String encodeChineseDownloadFileName(
            HttpServletRequest request, String pFileName) throws Exception {
        String filename = null;
        String agent = request.getHeader("USER-AGENT");
        if (null != agent) {
            if (-1 != agent.indexOf("Firefox")) {//Firefox
                filename = "=?UTF-8?B?" + (new String(org.apache.commons.codec.binary.Base64.encodeBase64(pFileName.getBytes("UTF-8")))) + "?=";
            } else if (-1 != agent.indexOf("Chrome")) {//Chrome
                filename = new String(pFileName.getBytes(), "ISO8859-1");
            } else {//IE7+
                filename = java.net.URLEncoder.encode(pFileName, "UTF-8");
                filename = filename.replace("+", "%20");
            }
        } else {
            filename = pFileName;
        }
        return filename;
    }

    /**
     * 获取sheet对象
     *
     * @param file
     * @return
     */
    public static XSSFSheet getXSSFSheet(File file) {
        InputStream is = null;
        XSSFWorkbook xssfWorkbook = null;
        try {
            is = new FileInputStream(file);
            xssfWorkbook = new XSSFWorkbook(is);
        } catch (IOException e) {
            return null;
        }
        XSSFSheet xssfSheet = xssfWorkbook.getSheetAt(0);
        return xssfSheet;
    }

    /**
     * 将单元格数据转换为String
     *
     * @param cell
     * @return
     */
    public static String getValue(XSSFCell cell) {
        String cellValue = "";
        if (null != cell) {
            //判断数据类型，防止报错
            switch (cell.getCellType()) {
                case NUMERIC: // 数字
                    DecimalFormat df = new DecimalFormat("0");
                    cellValue = df.format(cell.getNumericCellValue());
                    break;

                case STRING: // 字符串
                    cellValue = cell.getStringCellValue();
                    break;

                case BOOLEAN: // Boolean
                    cellValue = cell.getBooleanCellValue() + "";
                    break;

                case FORMULA: // 公式
                    cellValue = cell.getCellFormula() + "";
                    break;

                case _NONE: // 空值
                    cellValue = "";
                    break;

                case ERROR: // 故障
                    cellValue = "非法字符";
                    break;

                default:
                    cellValue = "未知类型";
                    break;
            }
        }
        return cellValue;
    }
}
