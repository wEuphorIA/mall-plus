package com.jzo2o.mall.common.file.service.plugin.impl;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.DeleteObjectsRequest;
import com.aliyun.oss.model.ObjectMetadata;
import com.jzo2o.mall.common.enums.ResultCode;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.file.service.plugin.FilePlugin;
import com.jzo2o.mall.system.enums.OssEnum;
import com.jzo2o.mall.system.model.dto.OssSetting;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * 阿里oss 文件操作
 */

@Slf4j
public class AliFilePlugin implements FilePlugin {

    private OssSetting ossSetting;

    public AliFilePlugin(OssSetting ossSetting) {
        this.ossSetting = ossSetting;
    }

    @Override
    public OssEnum pluginName() {
        return OssEnum.ALI_OSS;
    }

    /**
     * 获取oss client
     *
     * @return
     */
    private OSS getOssClient() {
        return new OSSClientBuilder().build(
                ossSetting.getAliyunOSSEndPoint(),
                ossSetting.getAliyunOSSAccessKeyId(),
                ossSetting.getAliyunOSSAccessKeySecret());
    }


    /**
     * 获取配置前缀
     *
     * @return
     */
    private String getUrlPrefix() {
        return "https://" + ossSetting.getAliyunOSSBucketName() + "." + ossSetting.getAliyunOSSEndPoint() + "/";
    }

    @Override
    public String pathUpload(String filePath, String key) {
        OSS ossClient = getOssClient();
        try {
            ossClient.putObject(ossSetting.getAliyunOSSBucketName(), key, new File(filePath));
        } catch (OSSException oe) {
            log.error("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            log.error("Error Message: " + oe.getErrorMessage());
            log.error("Error Code:       " + oe.getErrorCode());
            log.error("Request ID:      " + oe.getRequestId());
            log.error("Host ID:           " + oe.getHostId());
            throw new ServiceException(ResultCode.OSS_EXCEPTION_ERROR);
        } catch (ClientException ce) {
            log.error("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            log.error("Error Message: " + ce.getMessage());
            throw new ServiceException(ResultCode.OSS_EXCEPTION_ERROR);
        } finally {
            /*
             * Do not forget to shut down the client finally to release all allocated resources.
             */
            ossClient.shutdown();
        }
        ossClient.shutdown();
        return getUrlPrefix() + key;
    }

    @Override
    public String inputStreamUpload(InputStream inputStream, String key) {
        OSS ossClient = getOssClient();
        try {
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentType("image/jpg");
            ossClient.putObject(ossSetting.getAliyunOSSBucketName(), key, inputStream, meta);
        } catch (OSSException oe) {
            log.error("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            log.error("Error Message: " + oe.getErrorMessage());
            log.error("Error Code:       " + oe.getErrorCode());
            log.error("Request ID:      " + oe.getRequestId());
            log.error("Host ID:           " + oe.getHostId());
            throw new ServiceException(ResultCode.OSS_EXCEPTION_ERROR);
        } catch (ClientException ce) {
            log.error("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            log.error("Error Message: " + ce.getMessage());
            throw new ServiceException(ResultCode.OSS_EXCEPTION_ERROR);
        } finally {
            /*
             * Do not forget to shut down the client finally to release all allocated resources.
             */
            ossClient.shutdown();
        }
        ossClient.shutdown();
        return getUrlPrefix() + key;
    }

    @Override
    public void deleteFile(List<String> key) {
        OSS ossClient = getOssClient();

        try {
            ossClient.deleteObjects(
                    new DeleteObjectsRequest(ossSetting.getAliyunOSSBucketName()).withKeys(key));
        } catch (OSSException oe) {
            log.error("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            log.error("Error Message: " + oe.getErrorMessage());
            log.error("Error Code:       " + oe.getErrorCode());
            log.error("Request ID:      " + oe.getRequestId());
            log.error("Host ID:           " + oe.getHostId());
            throw new ServiceException(ResultCode.OSS_DELETE_ERROR);
        } catch (ClientException ce) {
            log.error("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            log.error("Error Message: " + ce.getMessage());
            throw new ServiceException(ResultCode.OSS_DELETE_ERROR);
        } finally {
            /*
             * Do not forget to shut down the client finally to release all allocated resources.
             */
            ossClient.shutdown();
        }
    }
}
