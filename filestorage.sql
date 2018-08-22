/*
Navicat MySQL Data Transfer

Source Server         : 172.27.108.93
Source Server Version : 50722
Source Host           : 172.27.108.93:3306
Source Database       : filestorage

Target Server Type    : MYSQL
Target Server Version : 50722
File Encoding         : 65001

Date: 2018-08-22 17:44:53
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for file_info
-- ----------------------------
DROP TABLE IF EXISTS `file_info`;
CREATE TABLE `file_info` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `file_name` varchar(255) NOT NULL COMMENT '文件名称',
  `md5` varchar(64) NOT NULL COMMENT '文件MD5值',
  `size` bigint(20) DEFAULT '0' COMMENT '文件大小',
  `gmt_create` datetime DEFAULT NULL,
  `status` varchar(32) DEFAULT '0',
  `gmt_modify` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=330 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for file_info_ext
-- ----------------------------
DROP TABLE IF EXISTS `file_info_ext`;
CREATE TABLE `file_info_ext` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `base_fid` int(10) unsigned NOT NULL,
  `original_file_name` text NOT NULL,
  `suffix` varchar(32) DEFAULT NULL,
  `file_type` varchar(32) DEFAULT NULL,
  `acl` varchar(32) DEFAULT NULL COMMENT 'null 为私有',
  `secret_key` varchar(32) DEFAULT NULL,
  `gmt_expired` datetime DEFAULT NULL COMMENT 'null 为永久',
  `gmt_create` datetime DEFAULT NULL,
  `gmt_modify` datetime DEFAULT NULL,
  `gmt_delete` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `base_fid` (`base_fid`) USING BTREE,
  KEY `gmt_expired` (`gmt_expired`) USING BTREE,
  CONSTRAINT `file_fk_1` FOREIGN KEY (`base_fid`) REFERENCES `file_info` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=685 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for file_other_info
-- ----------------------------
DROP TABLE IF EXISTS `file_other_info`;
CREATE TABLE `file_other_info` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `fid` int(10) unsigned DEFAULT NULL,
  `download_times` int(11) NOT NULL DEFAULT '0' COMMENT '下载次数',
  `share_times` int(11) NOT NULL DEFAULT '0' COMMENT '分享次数',
  `score` int(11) NOT NULL DEFAULT '0',
  `gmt_create` datetime DEFAULT NULL,
  `gmt_modify` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fid_fk` (`fid`),
  CONSTRAINT `fid_fk` FOREIGN KEY (`fid`) REFERENCES `file_info_ext` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=488 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for folder
-- ----------------------------
DROP TABLE IF EXISTS `folder`;
CREATE TABLE `folder` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `pid` int(11) DEFAULT '0' COMMENT '此文件夹的父文件夹id',
  `uid` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `type` varchar(255) DEFAULT NULL COMMENT '文件夹分类',
  `is_empty` tinyint(1) DEFAULT '0',
  `is_deleted` tinyint(1) DEFAULT '0',
  `gmt_create` datetime DEFAULT NULL,
  `gmt_modify` datetime DEFAULT NULL,
  `gmt_delete` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for folder_file
-- ----------------------------
DROP TABLE IF EXISTS `folder_file`;
CREATE TABLE `folder_file` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `folder_id` int(11) unsigned NOT NULL,
  `file_id` int(10) unsigned NOT NULL,
  `is_deleted` tinyint(1) DEFAULT '0',
  `gmt_create` datetime DEFAULT NULL,
  `gmt_modify` datetime DEFAULT NULL,
  `gmt_delete` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `file_id` (`file_id`),
  KEY `folder_fk` (`folder_id`),
  CONSTRAINT `file_fk` FOREIGN KEY (`file_id`) REFERENCES `file_info_ext` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `folder_fk` FOREIGN KEY (`folder_id`) REFERENCES `folder` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=548 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `nick` varchar(255) DEFAULT NULL,
  `user_code` varchar(255) DEFAULT NULL COMMENT '来自哪里的用户',
  `disable` tinyint(1) DEFAULT '0',
  `gmt_create` datetime DEFAULT NULL,
  `gmt_modify` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `user_code_index` (`user_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for user_settings
-- ----------------------------
DROP TABLE IF EXISTS `user_settings`;
CREATE TABLE `user_settings` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `uid` int(10) unsigned DEFAULT NULL,
  `volume` bigint(20) unsigned DEFAULT NULL,
  `used` bigint(20) unsigned DEFAULT NULL,
  `max_file_size` bigint(20) unsigned DEFAULT NULL,
  `download_speed_limit` bigint(20) unsigned DEFAULT NULL,
  `upload_speed_limit` bigint(20) unsigned DEFAULT NULL,
  `vip` tinyint(1) DEFAULT NULL,
  `gmt_create` datetime DEFAULT NULL,
  `gmt_modify` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8;

-- ----------------------------
-- View structure for v_file_detail_info
-- ----------------------------
DROP VIEW IF EXISTS `v_file_detail_info`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`%` SQL SECURITY DEFINER VIEW `v_file_detail_info` AS select `file_info`.`file_name` AS `file_name`,`file_info`.`md5` AS `md5`,`file_info`.`size` AS `size`,`file_info_ext`.`id` AS `id`,`file_info_ext`.`base_fid` AS `base_fid`,`file_info_ext`.`original_file_name` AS `original_file_name`,`file_info_ext`.`suffix` AS `suffix`,`file_info_ext`.`file_type` AS `file_type`,`file_info_ext`.`gmt_create` AS `gmt_create`,`file_info_ext`.`gmt_modify` AS `gmt_modify`,`file_info_ext`.`gmt_delete` AS `gmt_delete`,`file_info`.`status` AS `status`,`file_info_ext`.`acl` AS `acl`,`file_info_ext`.`gmt_expired` AS `gmt_expired`,`file_other_info`.`download_times` AS `download_times`,`file_other_info`.`share_times` AS `share_times`,`file_other_info`.`score` AS `score`,`file_info_ext`.`secret_key` AS `secret_key` from ((`file_info` join `file_info_ext`) join `file_other_info`) where ((`file_info`.`id` = `file_info_ext`.`base_fid`) and (`file_info_ext`.`id` = `file_other_info`.`fid`)) ;

-- ----------------------------
-- View structure for v_user_file
-- ----------------------------
DROP VIEW IF EXISTS `v_user_file`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`%` SQL SECURITY DEFINER VIEW `v_user_file` AS select `folder_file`.`id` AS `id`,`folder_file`.`folder_id` AS `folder_id`,`folder_file`.`file_id` AS `file_id`,`v_file_detail_info`.`file_name` AS `file_name`,`folder_file`.`is_deleted` AS `is_deleted`,`folder`.`uid` AS `uid`,`folder`.`name` AS `folder_name`,`folder`.`type` AS `folder_type`,`v_file_detail_info`.`md5` AS `md5`,`v_file_detail_info`.`size` AS `size`,`v_file_detail_info`.`original_file_name` AS `original_file_name`,`v_file_detail_info`.`suffix` AS `suffix`,`v_file_detail_info`.`file_type` AS `file_type`,`v_file_detail_info`.`gmt_create` AS `gmt_create`,`v_file_detail_info`.`gmt_modify` AS `gmt_modify`,`v_file_detail_info`.`gmt_delete` AS `gmt_delete`,`v_file_detail_info`.`status` AS `status`,`v_file_detail_info`.`acl` AS `acl`,`v_file_detail_info`.`gmt_expired` AS `gmt_expired`,`v_file_detail_info`.`download_times` AS `download_times`,`v_file_detail_info`.`share_times` AS `share_times`,`v_file_detail_info`.`score` AS `score`,`v_file_detail_info`.`secret_key` AS `secret_key` from ((`folder` join `folder_file` on((`folder_file`.`folder_id` = `folder`.`id`))) join `v_file_detail_info`) where (`folder_file`.`file_id` = `v_file_detail_info`.`id`) ;

-- ----------------------------
-- View structure for v_user_info
-- ----------------------------
DROP VIEW IF EXISTS `v_user_info`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`%` SQL SECURITY DEFINER VIEW `v_user_info` AS select `user`.`name` AS `name`,`user`.`password` AS `password`,`user`.`nick` AS `nick`,`user`.`user_code` AS `user_code`,`user`.`disable` AS `disable`,`user`.`gmt_create` AS `gmt_create`,`user`.`gmt_modify` AS `gmt_modify`,`user_settings`.`volume` AS `volume`,`user_settings`.`used` AS `used`,`user_settings`.`max_file_size` AS `max_file_size`,`user_settings`.`download_speed_limit` AS `download_speed_limit`,`user_settings`.`upload_speed_limit` AS `upload_speed_limit`,`user_settings`.`vip` AS `vip`,`user`.`id` AS `id` from (`user` join `user_settings`) where (`user`.`id` = `user_settings`.`uid`) ;
