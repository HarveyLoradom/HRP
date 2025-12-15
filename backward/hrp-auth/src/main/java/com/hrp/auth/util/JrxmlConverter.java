package com.hrp.auth.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Iterator;

/**
 * JSON模板转JRXML转换器
 * 将前端设计的JSON格式模板转换为JasperReports的JRXML格式
 */
@Component
public class JrxmlConverter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 将JSON模板转换为JRXML格式
     * 
     * @param templateJson JSON格式的模板（Fabric.js格式）
     * @param pageSize 页面大小（A4, A3等）
     * @param orientation 方向（portrait, landscape）
     * @param marginTop 上边距
     * @param marginBottom 下边距
     * @param marginLeft 左边距
     * @param marginRight 右边距
     * @return JRXML格式的字符串
     */
    public String convertJsonToJrxml(String templateJson, String pageSize, String orientation,
                                     Integer marginTop, Integer marginBottom, 
                                     Integer marginLeft, Integer marginRight) {
        if (templateJson == null || templateJson.isEmpty()) {
            return generateEmptyJrxml(pageSize, orientation, marginTop, marginBottom, marginLeft, marginRight);
        }

        try {
            JsonNode rootNode = objectMapper.readTree(templateJson);
            
            // 获取页面尺寸
            int pageWidth = getPageWidth(pageSize, orientation);
            int pageHeight = getPageHeight(pageSize, orientation);
            
            StringBuilder jrxml = new StringBuilder();
            jrxml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            jrxml.append("<jasperReport xmlns=\"http://jasperreports.sourceforge.net/jasperreports\"\n");
            jrxml.append("              xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
            jrxml.append("              xsi:schemaLocation=\"http://jasperreports.sourceforge.net/jasperreports http://jasperreports.sourceforge.net/xsd/jasperreport.xsd\"\n");
            jrxml.append("              name=\"report\"\n");
            jrxml.append("              pageWidth=\"").append(pageWidth).append("\"\n");
            jrxml.append("              pageHeight=\"").append(pageHeight).append("\"\n");
            jrxml.append("              columnWidth=\"").append(pageWidth - (marginLeft != null ? marginLeft : 20) - (marginRight != null ? marginRight : 20)).append("\"\n");
            jrxml.append("              leftMargin=\"").append(marginLeft != null ? marginLeft : 20).append("\"\n");
            jrxml.append("              rightMargin=\"").append(marginRight != null ? marginRight : 20).append("\"\n");
            jrxml.append("              topMargin=\"").append(marginTop != null ? marginTop : 20).append("\"\n");
            jrxml.append("              bottomMargin=\"").append(marginBottom != null ? marginBottom : 20).append("\"\n");
            jrxml.append("              uuid=\"report-uuid\">\n");
            
            // 添加参数定义
            jrxml.append("  <parameter name=\"REPORT_DATA\" class=\"java.util.Collection\"/>\n");
            
            // 添加字段定义（从JSON中提取）
            jrxml.append("  <field name=\"data\" class=\"java.util.Map\"/>\n");
            
            // Title区域
            jrxml.append("  <title>\n");
            jrxml.append("    <band height=\"50\" splitType=\"Stretch\"/>\n");
            jrxml.append("  </title>\n");
            
            // PageHeader区域
            jrxml.append("  <pageHeader>\n");
            jrxml.append("    <band height=\"50\" splitType=\"Stretch\"/>\n");
            jrxml.append("  </pageHeader>\n");
            
            // Detail区域 - 解析JSON中的对象
            jrxml.append("  <detail>\n");
            jrxml.append("    <band height=\"100\" splitType=\"Stretch\">\n");
            
            // 处理Fabric.js格式的JSON
            JsonNode objects = null;
            if (rootNode.has("objects") && rootNode.get("objects").isArray()) {
                objects = rootNode.get("objects");
            } else if (rootNode.isArray()) {
                // 如果根节点就是数组
                objects = rootNode;
            }
            
            if (objects != null && objects.isArray()) {
                int yPos = 0;
                
                for (JsonNode obj : objects) {
                    String type = obj.has("type") ? obj.get("type").asText() : "";
                    double left = obj.has("left") ? obj.get("left").asDouble() : 0;
                    double top = obj.has("top") ? obj.get("top").asDouble() : 0;
                    double width = obj.has("width") ? obj.get("width").asDouble() : 100;
                    double height = obj.has("height") ? obj.get("height").asDouble() : 20;
                    
                    // 处理缩放
                    if (obj.has("scaleX")) {
                        width *= obj.get("scaleX").asDouble();
                    }
                    if (obj.has("scaleY")) {
                        height *= obj.get("scaleY").asDouble();
                    }
                    
                    if ("text".equals(type) || "i-text".equals(type) || "textbox".equals(type)) {
                        // 文本字段
                        String text = obj.has("text") ? obj.get("text").asText() : "";
                        String fieldKey = obj.has("fieldKey") ? obj.get("fieldKey").asText() : null;
                        
                        // 如果没有fieldKey，尝试从text中提取 {fieldKey}
                        if ((fieldKey == null || fieldKey.isEmpty()) && text.contains("{") && text.contains("}")) {
                            int start = text.indexOf("{");
                            int end = text.indexOf("}", start);
                            if (end > start) {
                                fieldKey = text.substring(start + 1, end);
                            }
                        }
                        
                        // 解析文本中的字段引用 {fieldKey}
                        if (fieldKey != null && !fieldKey.isEmpty()) {
                            // 使用字段引用
                            jrxml.append("      <textField isStretchWithOverflow=\"true\">\n");
                            jrxml.append("        <reportElement x=\"").append((int)left).append("\" y=\"").append((int)top).append("\" width=\"").append((int)width).append("\" height=\"").append((int)height).append("\" uuid=\"text-").append(yPos).append("\"/>\n");
                            jrxml.append("        <textElement>\n");
                            int fontSize = obj.has("fontSize") ? obj.get("fontSize").asInt() : 12;
                            jrxml.append("          <font size=\"").append(fontSize).append("\"");
                            if (obj.has("fontWeight") && "bold".equals(obj.get("fontWeight").asText())) {
                                jrxml.append(" isBold=\"true\"");
                            }
                            jrxml.append("/>\n");
                            String textAlign = obj.has("textAlign") ? obj.get("textAlign").asText() : "left";
                            if ("center".equals(textAlign)) {
                                jrxml.append("          <paragraph alignment=\"Center\"/>\n");
                            } else if ("right".equals(textAlign)) {
                                jrxml.append("          <paragraph alignment=\"Right\"/>\n");
                            }
                            jrxml.append("        </textElement>\n");
                            jrxml.append("        <textFieldExpression><![CDATA[$F{data}.get(\"").append(escapeXml(fieldKey)).append("\") != null ? $F{data}.get(\"").append(escapeXml(fieldKey)).append("\").toString() : \"\"]]></textFieldExpression>\n");
                            jrxml.append("      </textField>\n");
                        } else if (text.contains("{") && text.contains("}")) {
                            // 包含字段引用的文本
                            String processedText = processFieldReferences(text);
                            jrxml.append("      <textField isStretchWithOverflow=\"true\">\n");
                            jrxml.append("        <reportElement x=\"").append((int)left).append("\" y=\"").append((int)top).append("\" width=\"").append((int)width).append("\" height=\"").append((int)height).append("\" uuid=\"text-").append(yPos).append("\"/>\n");
                            jrxml.append("        <textElement>\n");
                            int fontSize = obj.has("fontSize") ? obj.get("fontSize").asInt() : 12;
                            jrxml.append("          <font size=\"").append(fontSize).append("\"/>\n");
                            jrxml.append("        </textElement>\n");
                            jrxml.append("        <textFieldExpression><![CDATA[").append(processedText).append("]]></textFieldExpression>\n");
                            jrxml.append("      </textField>\n");
                        } else if (text != null && !text.isEmpty()) {
                            // 静态文本
                            jrxml.append("      <staticText>\n");
                            jrxml.append("        <reportElement x=\"").append((int)left).append("\" y=\"").append((int)top).append("\" width=\"").append((int)width).append("\" height=\"").append((int)height).append("\" uuid=\"static-").append(yPos).append("\"/>\n");
                            jrxml.append("        <textElement>\n");
                            int fontSize = obj.has("fontSize") ? obj.get("fontSize").asInt() : 12;
                            jrxml.append("          <font size=\"").append(fontSize).append("\"/>\n");
                            jrxml.append("        </textElement>\n");
                            jrxml.append("        <text><![CDATA[").append(escapeXml(text)).append("]]></text>\n");
                            jrxml.append("      </staticText>\n");
                        }
                    } else if ("rect".equals(type)) {
                        // 矩形
                        jrxml.append("      <rectangle>\n");
                        jrxml.append("        <reportElement x=\"").append((int)left).append("\" y=\"").append((int)top).append("\" width=\"").append((int)width).append("\" height=\"").append((int)height).append("\" uuid=\"rect-").append(yPos).append("\"/>\n");
                        jrxml.append("      </rectangle>\n");
                    } else if ("image".equals(type)) {
                        // 图片
                        jrxml.append("      <image>\n");
                        jrxml.append("        <reportElement x=\"").append((int)left).append("\" y=\"").append((int)top).append("\" width=\"").append((int)width).append("\" height=\"").append((int)height).append("\" uuid=\"img-").append(yPos).append("\"/>\n");
                        jrxml.append("        <imageExpression><![CDATA[]]></imageExpression>\n");
                        jrxml.append("      </image>\n");
                    }
                    
                    yPos++;
                }
            }
            
            jrxml.append("    </band>\n");
            jrxml.append("  </detail>\n");
            
            // PageFooter区域
            jrxml.append("  <pageFooter>\n");
            jrxml.append("    <band height=\"50\" splitType=\"Stretch\"/>\n");
            jrxml.append("  </pageFooter>\n");
            
            jrxml.append("</jasperReport>");
            
            return jrxml.toString();
        } catch (Exception e) {
            throw new RuntimeException("转换JSON到JRXML失败：" + e.getMessage(), e);
        }
    }

    /**
     * 处理字段引用，将 {fieldKey} 转换为 $F{data}.get("fieldKey")
     */
    private String processFieldReferences(String text) {
        StringBuilder result = new StringBuilder();
        int start = 0;
        int pos = 0;
        
        while ((pos = text.indexOf("{", start)) != -1) {
            result.append("\"").append(text.substring(start, pos)).append("\" + ");
            int end = text.indexOf("}", pos);
            if (end != -1) {
                String fieldKey = text.substring(pos + 1, end);
                result.append("($F{data}.get(\"").append(escapeXml(fieldKey)).append("\") != null ? $F{data}.get(\"").append(escapeXml(fieldKey)).append("\").toString() : \"\")");
                start = end + 1;
            } else {
                result.append(text.substring(pos));
                start = text.length();
            }
        }
        
        if (start < text.length()) {
            result.append("\"").append(text.substring(start)).append("\"");
        }
        
        return result.toString();
    }

    /**
     * 转义XML特殊字符
     */
    private String escapeXml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }

    /**
     * 生成空的JRXML模板
     */
    private String generateEmptyJrxml(String pageSize, String orientation,
                                     Integer marginTop, Integer marginBottom,
                                     Integer marginLeft, Integer marginRight) {
        int pageWidth = getPageWidth(pageSize, orientation);
        int pageHeight = getPageHeight(pageSize, orientation);
        
        StringBuilder jrxml = new StringBuilder();
        jrxml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        jrxml.append("<jasperReport xmlns=\"http://jasperreports.sourceforge.net/jasperreports\"\n");
        jrxml.append("              xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        jrxml.append("              xsi:schemaLocation=\"http://jasperreports.sourceforge.net/jasperreports http://jasperreports.sourceforge.net/xsd/jasperreport.xsd\"\n");
        jrxml.append("              name=\"report\"\n");
        jrxml.append("              pageWidth=\"").append(pageWidth).append("\"\n");
        jrxml.append("              pageHeight=\"").append(pageHeight).append("\"\n");
        jrxml.append("              columnWidth=\"").append(pageWidth - (marginLeft != null ? marginLeft : 20) - (marginRight != null ? marginRight : 20)).append("\"\n");
        jrxml.append("              leftMargin=\"").append(marginLeft != null ? marginLeft : 20).append("\"\n");
        jrxml.append("              rightMargin=\"").append(marginRight != null ? marginRight : 20).append("\"\n");
        jrxml.append("              topMargin=\"").append(marginTop != null ? marginTop : 20).append("\"\n");
        jrxml.append("              bottomMargin=\"").append(marginBottom != null ? marginBottom : 20).append("\"\n");
        jrxml.append("              uuid=\"report-uuid\">\n");
        jrxml.append("  <parameter name=\"REPORT_DATA\" class=\"java.util.Collection\"/>\n");
        jrxml.append("  <field name=\"data\" class=\"java.util.Map\"/>\n");
        jrxml.append("  <title>\n");
        jrxml.append("    <band height=\"50\" splitType=\"Stretch\"/>\n");
        jrxml.append("  </title>\n");
        jrxml.append("  <detail>\n");
        jrxml.append("    <band height=\"100\" splitType=\"Stretch\"/>\n");
        jrxml.append("  </detail>\n");
        jrxml.append("</jasperReport>");
        
        return jrxml.toString();
    }

    /**
     * 获取页面宽度（像素）
     */
    private int getPageWidth(String pageSize, String orientation) {
        int width = 595; // A4 默认宽度
        if ("A4".equals(pageSize)) {
            width = "landscape".equals(orientation) ? 842 : 595;
        } else if ("A3".equals(pageSize)) {
            width = "landscape".equals(orientation) ? 1191 : 842;
        } else if ("Letter".equals(pageSize)) {
            width = "landscape".equals(orientation) ? 792 : 612;
        }
        return width;
    }

    /**
     * 获取页面高度（像素）
     */
    private int getPageHeight(String pageSize, String orientation) {
        int height = 842; // A4 默认高度
        if ("A4".equals(pageSize)) {
            height = "landscape".equals(orientation) ? 595 : 842;
        } else if ("A3".equals(pageSize)) {
            height = "landscape".equals(orientation) ? 842 : 1191;
        } else if ("Letter".equals(pageSize)) {
            height = "landscape".equals(orientation) ? 612 : 792;
        }
        return height;
    }
}

