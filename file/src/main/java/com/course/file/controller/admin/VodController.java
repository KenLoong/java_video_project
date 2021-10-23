package com.course.file.controller.admin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.OSSClient;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.vod.model.v20170321.CreateUploadVideoResponse;
import com.aliyuncs.vod.model.v20170321.GetMezzanineInfoResponse;
import com.aliyuncs.vod.model.v20170321.GetVideoPlayAuthResponse;
import com.course.server.dto.FileDto;
import com.course.server.dto.ResponseDto;
import com.course.server.enums.FileUseEnum;
import com.course.server.service.FileService;
import com.course.server.util.Base64ToMultipartFile;
import com.course.server.util.VodUtil;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
@RequestMapping("/admin")
public class VodController {

    private static final Logger LOG = LoggerFactory.getLogger(FileController.class);

    @Value("${vod.accessKeyId}")
    private String accessKeyId;

    @Value("${vod.accessKeySecret}")
    private String accessKeySecret;

    public static final String BUSINESS_NAME = "VOD上传";

    @Resource
    private FileService fileService;

    @PostMapping("/vod")
    public ResponseDto fileUpload(@RequestBody FileDto fileDto) throws Exception {
        LOG.info("上传文件开始");
        String use = fileDto.getUse();
        String key = fileDto.getKey();
        String suffix = fileDto.getSuffix();
        String shardBase64 = fileDto.getShard();
        MultipartFile shard = Base64ToMultipartFile.base64ToMultipart(shardBase64);

        // 保存文件到本地
        FileUseEnum useEnum = FileUseEnum.getByCode(use);

        //存储路径
        String dir = useEnum.name().toLowerCase();

        String path = new StringBuffer(dir)
                .append("/")
                .append(key)
                .append(".")
                .append(suffix)
                .toString(); //


        String vod = "";
        String fileUrl = "";
        try {
            // 初始化VOD客户端并获取上传地址和凭证
            DefaultAcsClient vodClient = VodUtil.initVodClient(accessKeyId, accessKeySecret);
            CreateUploadVideoResponse createUploadVideoResponse = VodUtil.createUploadVideo(vodClient, path);
            // 执行成功会返回VideoId、UploadAddress和UploadAuth
            vod = createUploadVideoResponse.getVideoId();
            JSONObject uploadAuth = JSONObject.parseObject(
                    Base64.decodeBase64(createUploadVideoResponse.getUploadAuth()), JSONObject.class);
            JSONObject uploadAddress = JSONObject.parseObject(
                    Base64.decodeBase64(createUploadVideoResponse.getUploadAddress()), JSONObject.class);
            // 使用UploadAuth和UploadAddress初始化OSS客户端
            OSSClient ossClient = VodUtil.initOssClient(uploadAuth, uploadAddress);
            // 上传文件，注意是同步上传会阻塞等待，耗时与文件大小和网络上行带宽有关
            VodUtil.uploadLocalFile(ossClient, uploadAddress, shard.getInputStream());
            LOG.info("上传视频成功, vod : " + vod);
            GetMezzanineInfoResponse response = VodUtil.getMezzanineInfo(vodClient, vod);
            System.out.println("获取视频信息, response : " + JSON.toJSONString(response));
            fileUrl = response.getMezzanine().getFileURL();

            // 关闭OSSClient。
            ossClient.shutdown();
        } catch (Exception e) {
            LOG.info("上传视频失败, ErrorMessage : " + e.getLocalizedMessage(), e);
        }


        LOG.info("保存文件记录开始");
        fileDto.setPath(path);
        fileDto.setVod(vod);
        fileService.save(fileDto);

        ResponseDto responseDto = new ResponseDto();
        fileDto.setPath(fileUrl); //返回前端的地址需要是可播放的地址
        responseDto.setContent(fileDto);


        return responseDto;
    }

    @RequestMapping(value = "/get-auth/{vod}", method = RequestMethod.GET)
    public ResponseDto getAuth(@PathVariable String vod) throws ClientException {
        LOG.info("获取播放授权开始: ");
        ResponseDto responseDto = new ResponseDto();
        DefaultAcsClient client = VodUtil.initVodClient(accessKeyId, accessKeySecret);
        GetVideoPlayAuthResponse response = new GetVideoPlayAuthResponse();
        try {
            response = VodUtil.getVideoPlayAuth(client, vod);
            LOG.info("授权码 = {}", response.getPlayAuth());
            responseDto.setContent(response.getPlayAuth());
            //VideoMeta信息
            LOG.info("VideoMeta = {}", JSON.toJSONString(response.getVideoMeta()));
        } catch (Exception e) {
            System.out.print("ErrorMessage = " + e.getLocalizedMessage());
        }
        LOG.info("获取播放授权结束");
        return responseDto;
    }
}
