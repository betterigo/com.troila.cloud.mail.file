package com.troila.cloud.mail.file.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

@Configuration
public class CephAwsS3Connection {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CephAwsS3Connection.class);
	
	@Value("${ceph.access_key}")
	private String accessKey;
	
	@Value("${ceph.secret_key}")
	private String secretKey;
	
	@Value("${ceph.server}")
	private String cephServer;
	
	@Bean
	public AmazonS3 createS3Conn() {
		AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
    	ClientConfiguration clientConfig = new ClientConfiguration();
    	clientConfig.setProtocol(Protocol.HTTP);	
    	AmazonS3 s3Conn = AmazonS3ClientBuilder.standard()
    			.withCredentials(new AWSStaticCredentialsProvider(credentials))
    			.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(cephServer,""))//本地ceph不需要region
    			.withClientConfiguration(clientConfig)
    			.build();
    	LOGGER.info("ceph 服务器连接实例已创建！");
    	LOGGER.info("初始化ceph存储...");
    	if(!s3Conn.doesBucketExistV2("mailcloud.test")) {
    		s3Conn.createBucket("mailcloud.test");
    	}
    	LOGGER.info("初始化ceph存储...完毕！");
    	return s3Conn;
	}
}
