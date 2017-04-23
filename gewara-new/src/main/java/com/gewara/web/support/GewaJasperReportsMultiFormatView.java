package com.gewara.web.support;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.servlet.view.jasperreports.AbstractJasperReportsView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsCsvView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsHtmlView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsMultiFormatView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsPdfView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsXlsView;

public class GewaJasperReportsMultiFormatView extends JasperReportsMultiFormatView{
	public GewaJasperReportsMultiFormatView(){
		Map<String, Class<? extends AbstractJasperReportsView>> formatMappings = new HashMap<String, Class<? extends AbstractJasperReportsView>>(5);
		formatMappings.put("csv", JasperReportsCsvView.class);
		formatMappings.put("html", JasperReportsHtmlView.class);
		formatMappings.put("pdf", JasperReportsPdfView.class);
		formatMappings.put("xls", JasperReportsXlsView.class);
		formatMappings.put("image", JasperReportsImageView.class);
		setFormatMappings(formatMappings);
	}
}
