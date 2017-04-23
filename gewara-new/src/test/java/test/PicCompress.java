package test;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.FileOutputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;

import org.apache.commons.lang.StringUtils;

public class PicCompress {
	public static void main(String[] args) {
		System.out.println(Runtime.getRuntime().availableProcessors());
		printSize(90, 90, 50, 50);
	}
	public static void printSize(int srcHeight, int srcWidth, int height, int width){
		
		if(srcHeight < height && srcWidth < width){
			System.out.println("use Original");
		}
		int x=0, y=0;
		if(srcHeight > height && srcWidth < width){
			y = (srcHeight - height)/2;
		}else if(srcHeight < height && srcWidth > width){
			x = (srcWidth - width)/2;
		}else{//做缩放
			int maxWidth = width, maxHeight = height;
			if(srcHeight*1.0/srcWidth > height*1.0/width){
				maxHeight = srcHeight;
				y = (srcHeight*width/srcWidth - height)/2;
			}else{
				maxWidth = srcWidth;
				x = (srcWidth*height/srcHeight - width)/2;
			}
			System.out.print("x:" + x + ",y:" + y + ",maxWidth:" + maxWidth + ",maxHeight:" + maxHeight);
		}
	}

	/**
	 * @param srcFilePath
	 * @param descFilePath
	 * @param quality 这里指定压缩的程度，参数qality是取值0~1范围内
	 * @return
	 */
	public static boolean compressPic(String srcFilePath, String descFilePath, float quality) {
		File file = null;
		BufferedImage src = null;
		FileOutputStream out = null;
		ImageWriter imgWrier;
		ImageWriteParam imgWriteParams;

		// 指定写图片的方式为 jpg
		imgWrier = ImageIO.getImageWritersByFormatName("jpg").next();
		imgWriteParams = new JPEGImageWriteParam(null);
		// 要使用压缩，必须指定压缩方式为MODE_EXPLICIT
		imgWriteParams.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
		imgWriteParams.setCompressionQuality(quality);
		imgWriteParams.setProgressiveMode(JPEGImageWriteParam.MODE_DISABLED);
		ColorModel colorModel = ColorModel.getRGBdefault();
		// 指定压缩时使用的色彩模式
		imgWriteParams.setDestinationType(new javax.imageio.ImageTypeSpecifier(colorModel, colorModel.createCompatibleSampleModel(16, 16)));

		try {
			if (StringUtils.isBlank(srcFilePath)) {
				return false;
			} else {
				file = new File(srcFilePath);
				src = ImageIO.read(file);
				out = new FileOutputStream(descFilePath);

				imgWrier.reset();
				// 必须先指定 out值，才能调用write方法, ImageOutputStream可以通过任何 OutputStream构造
				imgWrier.setOutput(ImageIO.createImageOutputStream(out));
				// 调用write方法，就可以向输入流写图片
				imgWrier.write(null, new IIOImage(src, null, null), imgWriteParams);
				out.flush();
				out.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}