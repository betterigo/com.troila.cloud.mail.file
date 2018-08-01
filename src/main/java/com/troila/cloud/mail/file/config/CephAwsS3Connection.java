package com.troila.cloud.mail.file.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.troila.cloud.mail.file.config.constant.StorageBuckets;
import com.troila.cloud.mail.file.config.settings.CephSettings;

@Configuration
@EnableConfigurationProperties(value=CephSettings.class)
public class CephAwsS3Connection {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CephAwsS3Connection.class);
	
	@Autowired
	private CephSettings cephSettings;
	
	@SuppressWarnings("deprecation")
	@Bean
	public AmazonS3 createS3Conn() {
		AWSCredentials credentials = new BasicAWSCredentials(cephSettings.getAccessKey(), cephSettings.getSecretKey());
    	ClientConfiguration clientConfig = new ClientConfiguration();
    	clientConfig.setProtocol(Protocol.HTTP);
    	AmazonS3 s3Conn = null;
    	if(cephSettings.getServer() == null) {
    		return new AmazonS3Client();
    	}
    	try {			
    		s3Conn = AmazonS3ClientBuilder.standard()
    				.withCredentials(new AWSStaticCredentialsProvider(credentials))
    				.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(cephSettings.getServer(),""))//本地ceph不需要region
    				.withClientConfiguration(clientConfig)
    				.build();
    		LOGGER.info("ceph 服务器连接实例已创建！");
    		LOGGER.info("初始化ceph存储...");
    		for(String bucket : StorageBuckets.buckets) {
    			if(!s3Conn.doesBucketExistV2(bucket)) {
    				s3Conn.createBucket(bucket);
    			}
    		}
		} catch (Exception e) {
			LOGGER.error("连接ceph服务器异常,信息:{}",e.getMessage());
		}
    	LOGGER.info("初始化ceph存储...完毕！");
    	return s3Conn;
	}
}
