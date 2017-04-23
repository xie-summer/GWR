package com.gewara.web.action.api2mobile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.constant.ApiConstant;
import com.gewara.helper.PassbookUtil;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.SellSeat;
import com.gewara.model.user.Member;
import com.gewara.service.MessageService;
import com.gewara.untrans.GewaPicService;
import com.gewara.untrans.GewaPicService.StreamWriter;
import com.gewara.untrans.impl.StreamPicWriter;
import com.gewara.util.JsonUtils;
import com.gewara.web.action.api.BaseApiController;
import com.gewara.web.support.ResponseStreamWriter;

@Controller
public class Api2PassbookController extends BaseApiController implements InitializingBean {
	@Autowired@Qualifier("gewaPicService")
	private GewaPicService gewaPicService;
	@Autowired@Qualifier("messageService")
	private MessageService messageService;
	private Map<Long, byte[]> logoMap = new HashMap<Long, byte[]>();
	private Map<Long, String> fileHash = new HashMap<Long, String>();
	@RequestMapping("/testPass.xhtml")
	@ResponseBody
	public String testPass(String fileName) throws Exception{
		
		if(StringUtils.isBlank(fileName)) fileName = "F:\\test.zip";
		OutputStream os = new FileOutputStream(fileName);
		TicketOrder order = daoService.getObject(TicketOrder.class, 32922697L);
		byte[] content = getZipContent(order);
		os.write(content);
		os.close();
		return fileName;
	}
	@RequestMapping("/api2/mobile/getPassbook.xhtml")
	public String getPassbook(ModelMap model, String memberEncode, String tradeNo, HttpServletResponse response) throws Exception{
		Member member = memberService.getMemberByEncode(memberEncode);
		if(member==null)  return getErrorXmlView(model, ApiConstant.CODE_MEMBER_NOT_EXISTS, "用户不存在！");
		TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeNo, false);
		if(order.getMemberid().equals(member.getId())) getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能下载他人订单！");
		byte[] content = getZipContent(order);
		String contentType = "application/zip";
		StreamWriter rwp = new ResponseStreamWriter(response, tradeNo, contentType);
		rwp.write(new ByteArrayInputStream(content), System.currentTimeMillis());
		return null;
	}
	private byte[] getZipContent(TicketOrder order) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ZipOutputStream zos = PassbookUtil.getZipStream(bos);
		Map<String, String> manifest = new HashMap<String, String>(MANIFEST);		

		for(String file: FILEDATA.keySet()){
			manifest.put(file, PassbookUtil.rsa(FILEDATA.get(file)));
			PassbookUtil.addEntry(zos, file, FILEDATA.get(file));
		}
		byte[] logo = getMovieLogo(order.getMovieid());
		if(logo!=null){
			String hash = fileHash.get(order.getMovieid());
			manifest.put("thumbnail.png", hash);
			manifest.put("thumbnail@2x.png", hash);
			PassbookUtil.addEntry(zos, "thumbnail.png", logo);
			PassbookUtil.addEntry(zos, "thumbnail@2x.png", logo);
		}
		
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid(), true);
		Cinema cinema = daoService.getObject(Cinema.class, opi.getCinemaid());
		List<SellSeat> seatList = ticketOrderService.getOrderSeatList(order.getId());
		String password = messageService.getOrderPassword(order, seatList);
		String passJson = PassbookUtil.getPassJson(order, opi, cinema.getAddress(), password,cinema);
		byte[] passJsonBytes = passJson.getBytes("UTF-8");
		manifest.put("pass.json", PassbookUtil.rsa(passJsonBytes));
		
		byte[] manifestContent = JsonUtils.writeMapToJson(manifest).getBytes("UTF-8");
		PassbookUtil.addEntry(zos, "manifest.json", manifestContent);
		PassbookUtil.addEntry(zos, "pass.json", passJsonBytes);
		
		byte[] signature = PassbookUtil.sign(manifestContent);
		PassbookUtil.addEntry(zos, "signature", signature);
		zos.close();
		return bos.toByteArray();
	}
	private byte[] getMovieLogo(Long movieid) throws Exception {
		byte[] logo = logoMap.get(movieid);
		if(logo==null){
			Movie movie = daoService.getObject(Movie.class, movieid);
			String picname = movie.getLogo();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			StreamWriter writer = new StreamPicWriter(baos);
			try {
				if(StringUtils.isNotBlank(picname)){
					gewaPicService.getPicture(writer, picname, 120, 160, true);
				}
				logo = baos.toByteArray();
				logoMap.put(movieid, logo);
				fileHash.put(movieid, PassbookUtil.rsa(logo));
			} catch (IOException e) {
				return null;
			}
		}
		return logo;
	}
	private Map<String, byte[]> FILEDATA = new HashMap<String, byte[]>();
	private Map<String, String> MANIFEST = new HashMap<String, String>();
	@RequestMapping("/initPassbook.xhtml")
	@ResponseBody
	public String init() throws Exception {
		File filePath = new File(getClass().getClassLoader().getResource("com/gewara/passbook").getFile());
		File[] files = filePath.listFiles();
		for(File file:files){
			if(file.isDirectory()) continue;
			String name = file.getName();
			byte[] bytes = IOUtils.toByteArray(new FileInputStream(file));
			FILEDATA.put(name, bytes);
			MANIFEST.put(name, PassbookUtil.rsa(bytes));
		}
		return "success";
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		init();
	}

}
