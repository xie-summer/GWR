package com.gewara.command;

import java.io.Serializable;

import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

public class PdfReportUtil implements Serializable {

	private static final long serialVersionUID = 777999585643384930L;
	private static Font keyfont;// 设置字体大小
	private static Font textfont;// 设置字体大小
	public final static int MAXWIDTH = 580;
	public final static float ROWHEIGHT = 25;
	public final static int COLSPANS = 10;
	public final static int ROWWIDTH = 58;
	static {
		try {
			BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
			keyfont = new Font(bfChinese, 14, Font.BOLD);// 设置字体大小
			textfont = new Font(bfChinese, 12, Font.NORMAL);// 设置字体大小
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Font getKeyfont() {
		return keyfont;
	}

	public static Font getTextfont() {
		return textfont;
	}

	public static PdfPCell createCell(String value, Font font, int align) {
		PdfPCell cell = new PdfPCell();
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		cell.setHorizontalAlignment(align);
		cell.setMinimumHeight(ROWHEIGHT);
		cell.setPhrase(new Phrase(value, font));
		return cell;
	}
	
	public static PdfPCell createCell(Image image, int align, int colspan){
		PdfPCell cell = new PdfPCell();
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		cell.setHorizontalAlignment(align);
		cell.setImage(image);
		cell.setColspan(colspan);
		cell.setMinimumHeight(ROWHEIGHT);
		return cell;
	}
	
	public static PdfPCell createCell(String value, Font font) {
		PdfPCell cell = new PdfPCell();
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setMinimumHeight(ROWHEIGHT);
		cell.setPhrase(new Phrase(value, font));
		return cell;
	}

	public static PdfPCell createCell(String value, Font font, int align, int colspan) {
		PdfPCell cell = new PdfPCell();
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		cell.setHorizontalAlignment(align);
		cell.setMinimumHeight(ROWHEIGHT);
		cell.setColspan(colspan);
		cell.setPhrase(new Phrase(value, font));
		return cell;
	}
	public static PdfPCell createCell(String value, Font font, int align, int colspan, boolean boderFlag) {
		return createCell(value, font, align, colspan, 1, boderFlag);
	}
	
	public static PdfPCell createCell(String value, Font font, int align, int colspan, int rowspan, boolean boderFlag) {
		PdfPCell cell = new PdfPCell();
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		cell.setHorizontalAlignment(align);
		cell.setColspan(colspan);
		cell.setRowspan(rowspan);
		cell.setPhrase(new Phrase(value, font));
		cell.setMinimumHeight(ROWHEIGHT);
		cell.setPadding(3.0f);
		if (!boderFlag) {
			cell.setBorder(0);
			cell.setPaddingTop(15.0f);
			cell.setPaddingBottom(8.0f);
		}
		return cell;
	}

	public static PdfPTable createTable(int colNumber) {
		PdfPTable table = new PdfPTable(colNumber);
		try {
			table.setTotalWidth(MAXWIDTH);
			table.setLockedWidth(true);
			table.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.getDefaultCell().setBorder(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return table;
	}

}
