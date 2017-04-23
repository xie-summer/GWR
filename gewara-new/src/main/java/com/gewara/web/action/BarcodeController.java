package com.gewara.web.action;

import java.io.OutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

@Controller
public class BarcodeController extends AnnotationController {	
	
	public static String BARCODE_MSG = "msg";
	
	@RequestMapping("/barcode.xhtml")
	public void showPicture(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String msg = request.getParameter(BARCODE_MSG);
		if(StringUtils.isBlank(msg)) return;
		try{
			response.setHeader("Cache-Control", "no-store");
			response.setHeader("Pragma", "no-cache");
			response.setDateHeader("Expires", 0);
			response.setContentType("image/jpeg");
			ServletOutputStream sos = response.getOutputStream();
			encode(msg, 180, 50, sos);
			sos.flush();
			sos.close();
		}catch (Exception e) {
			dbLogger.error("", e);
		}
	}

    public void encode(String msg, int width, int height, OutputStream stream) {     
        int codeWidth = 3 + (7 * 6) + 5 + (7 * 6) + 3;     
        codeWidth = Math.max(codeWidth, width);     
        try {     
        	MultiFormatWriter formatWriter = new MultiFormatWriter();
            BitMatrix bitMatrix = formatWriter.encode(msg, BarcodeFormat.CODE_128, codeWidth, height, null);     
            MatrixToImageWriter.writeToStream(bitMatrix, "png", stream);     
        } catch (Exception e) {     
           dbLogger.error("", e);    
        }     
    } 
}

