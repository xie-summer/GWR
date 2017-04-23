package com.gewara.web.action.api2.resources;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.gewara.constant.ApiConstant;
import com.gewara.model.acl.GewaraUser;
import com.gewara.model.content.Picture;
import com.gewara.model.user.Member;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.GewaPicService;
import com.gewara.util.DateUtil;
import com.gewara.util.PictureUtil;
import com.gewara.util.StringUtil;
import com.gewara.web.action.api.ApiAuth;
import com.gewara.web.action.api.BaseApiController;
import com.gewara.web.filter.NewApiAuthenticationFilter;
import com.gewara.web.support.ErrorMultipartRequest;

/**
 * 图片API
 * @author taiqichao
 *
 */
@Controller
public class Api2PictureController extends BaseApiController{
	
	@Autowired
	@Qualifier("gewaPicService")
	private GewaPicService gewaPicService;
	


	/**
	 * 获取图片列表
	 * @param tag
	 * @param relatedid
	 * @param from
	 * @param maxnum
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/api2/picture/pictureList.xhtml")
	public String pictureList(
			String tag,
			Long relatedid,
			@RequestParam(defaultValue="0",required=false,value="from")int from,
			@RequestParam(defaultValue="20",required=false,value="maxnum") int maxnum, 
			ModelMap model){
		if(StringUtils.isBlank(tag)) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "tag不能为空！");
		if(relatedid == null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "relateid不能为空！");
		if(maxnum > 20) maxnum = 20;
		List<Picture> picList = pictureService.getPictureListByRelatedid(tag, relatedid, from, maxnum);
		int count=pictureService.getPictureCountByRelatedid(tag, relatedid);
		model.put("picList", picList);
		model.put("count", count);
		return getXmlView(model, "inner/activity/pictureList.vm");
	}


	/**
	 * 上传图片
	 * @param memberEncode
	 * @param picHex
	 * @param filetype
	 * @param tag
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/api2/picture/uploadPicture.xhtml")
	public String uploadPicture(String memberEncode,String picHex,String filetype, 
			String tag, ModelMap model, HttpServletRequest request){
		if(StringUtils.isBlank(memberEncode)) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "memberEncode不能为空！");
		Member member = memberService.getMemberByEncode(memberEncode);
		if(member == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "用户不存在！");
		int count = pictureService.getPictureCount(tag, member.getId(),member.getId(), DateUtil.getCurTruncTimestamp(), DateUtil.getLastTimeOfDay(DateUtil.getCurFullTimestamp()));
		if(count > 100)return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "上传图片超过限制！");
		if(StringUtils.isBlank(picHex)) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "picHex不能为空！");
		if(StringUtils.isBlank(filetype)) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "filetype不能为空！");
		if(StringUtils.isBlank(tag)) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "tag不能为空！");
		ErrorCode<String> returnCode = this.uploadPic(member.getId(), picHex, tag, filetype,"images/barmember/");
		if(!returnCode.isSuccess())return  getErrorXmlView(model,returnCode.getErrcode(),returnCode.getMsg());
		Picture picture = new Picture(tag,member.getId(),member.getId(),returnCode.getMsg());
		picture.setMemberType(GewaraUser.USER_TYPE_MEMBER);
		this.daoService.saveObject(picture);
		model.put("pic", picture);
		return getXmlView(model, "inner/activity/picture.vm");
	}
	
	/**
	 * 删除图片
	 * @param memberEncode
	 * @param pictureid
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/api2/picture/delPicture.xhtml")
	public String delPicture(String memberEncode,String pictureid,
			ModelMap model, HttpServletRequest request){
		if(StringUtils.isBlank(memberEncode)) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "memberEncode不能为空！");
		Member member = memberService.getMemberByEncode(memberEncode);
		if(member == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "用户不存在！");
		if(StringUtils.isBlank(pictureid)) return  getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "pictureid不能为空！");
		String [] idArr = StringUtils.split(pictureid,",");
		List<Long> idList = new ArrayList<Long>();
		for (String id : idArr) if(StringUtils.isNotBlank(id)) idList.add(Long.valueOf(id));
		List<Picture> picList = this.daoService.getObjectList(Picture.class,idList);
		for (Picture pic : picList) {
			if(!pic.hasMemberType(GewaraUser.USER_TYPE_MEMBER) || !member.getId().equals(pic.getMemberid())) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能删除其他用户上传的图片！");
		}
		try {
			daoService.removeObjectList(picList);
			for (Picture pic : picList) {
				gewaPicService.removePicture(pic.getPicturename());
			}
			return getXmlView(model, "api/mobile/result.vm");
		} catch (IOException e) {
			dbLogger.error(StringUtil.getExceptionTrace(e));
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "操作异常");
		}
	}
	
	@SuppressWarnings("unchecked")
	private ErrorCode<String> uploadPic(long memberid, String pic, String tag, String filetype, String path){
		if(pic ==null) return ErrorCode.getFailure("上传图片的十六进制流不能为空");
		try {
			if(StringUtils.isBlank(filetype)) return ErrorCode.getFailure(ApiConstant.CODE_PARAM_ERROR, "传递参数错误！");
			ByteArrayInputStream is = new ByteArrayInputStream(Hex.decodeHex(pic.toCharArray()));
			String filename = gewaPicService.saveToTempPic(is, filetype);
			if(!PictureUtil.isValidPicType(StringUtil.getFilenameExtension(filename))) {
				return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR,  "上传图片格式不合法！只支持jpg,png,gif,jpeg格式");
			}
			gewaPicService.saveTempFileToRemote(filename);
			String filepath =  gewaPicService.moveRemoteTempTo(memberid, tag, memberid, path, filename);
			return ErrorCode.getSuccess(filepath.replaceFirst("/",""));
		} catch (Exception e) {
			return ErrorCode.getFailure(ApiConstant.CODE_PARAM_ERROR,  "上传图片错误，请重试!");
		}
	}
	
	
	/**
	 * 通用单张图片上传入口
	 * @return
	 */
	@RequestMapping("/api2/picture/singleUpload.xhtml")
	public String commonUploadPicture(HttpServletRequest request,ModelMap model){
		
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		if(multipartRequest instanceof ErrorMultipartRequest){
			String msg = ((ErrorMultipartRequest)multipartRequest).getErrorMsg();
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, msg);
		}
		
		if(multipartRequest.getFileMap().entrySet().size()>1){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不支持多图上传.");
		}
		
		//获取FileData
		MultipartFile multipartFile=null;
		for(Map.Entry<String, MultipartFile> entry:multipartRequest.getFileMap().entrySet()){
			if("FileData".equals(entry.getKey())){
				multipartFile=entry.getValue();
				break;
			}
		}
		if(null==multipartFile){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "FileData不能为空");
		}
		
		//文件格式校验
		String extName=StringUtil.getFilenameExtension(multipartFile.getOriginalFilename());
		if(!PictureUtil.isValidPicType(extName)) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "图片格式错误，只支持jpg、jpeg、gif、png");
		}
		
		String filePath=null;
		try {
			ApiAuth auth=NewApiAuthenticationFilter.getApiAuth();
			String fileName = gewaPicService.saveToTempPic(multipartFile.getInputStream(), extName);
			gewaPicService.saveTempFileToRemote(fileName);
		    String dstDir="images/ms/"+auth.getApiUser().getId()+"/" + DateUtil.format(new Date(), "yyyyMM") + "/";
			filePath =  gewaPicService.moveRemoteTempTo(auth.getApiUser().getId(), "partner_ms", null, dstDir, fileName);
		} catch (Exception e) {
			dbLogger.error(StringUtil.getExceptionTrace(e));
		}
		if(null==filePath){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "文件上传失败");
		}
		model.put("filePath", filePath);
		return getXmlView(model, "api2/resources/singleUploadResult.vm");
	}
	
	
}
