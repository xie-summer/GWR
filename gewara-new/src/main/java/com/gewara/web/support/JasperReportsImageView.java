package com.gewara.web.support;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRGraphics2DExporter;
import net.sf.jasperreports.engine.export.JRGraphics2DExporterParameter;

import org.patchca.PatchcaUtils;
import org.springframework.ui.jasperreports.JasperReportsUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.view.jasperreports.AbstractJasperReportsSingleFormatView;

public class JasperReportsImageView extends AbstractJasperReportsSingleFormatView{
	public JasperReportsImageView(){
		setContentType("image/jpeg");
	}
	@Override
	protected JRExporter createExporter() {
		try {
			JRExporter exporter = new JRGraphics2DExporter();
			//exporter.setParameter(JRGraphics2DExporterParameter.ZOOM_RATIO, Float.valueOf(4));
			return exporter;
		} catch (JRException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected boolean useWriter() {
		return false;
	}
	/**
	 * Perform rendering for a single Jasper Reports exporter, that is,
	 * for a pre-defined output format.
	 */
	@Override
	protected void renderReport(JasperPrint jasperPrint, Map<String, Object> model, HttpServletResponse response)
			throws Exception {

		JRExporter exporter = createExporter();
		Map<JRExporterParameter, Object> mergedExporterParameters = getConvertedExporterParameters();
		if (!CollectionUtils.isEmpty(mergedExporterParameters)) {
			exporter.setParameters(mergedExporterParameters);
		}
		BufferedImage bufferedImage = new BufferedImage(jasperPrint.getPageWidth(), jasperPrint.getPageHeight(), BufferedImage.TYPE_INT_RGB);  
		Graphics2D g = (Graphics2D) bufferedImage.getGraphics();  
		//设置相应参数信息  
		exporter.setParameter(JRGraphics2DExporterParameter.GRAPHICS_2D, g); 

		if (useWriter()) {
			renderReportUsingWriter(exporter, jasperPrint, response);
		}
		else {
			// IE workaround: write into byte array first.
			JasperReportsUtils.render(exporter, jasperPrint, (OutputStream)null);
			ByteArrayOutputStream baos = createTemporaryOutputStream();
			PatchcaUtils.printImage(baos, bufferedImage);
			writeToResponse(response, baos);
		}
	}
}
